package cn.dabaotv.movie.Function;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import cn.dabaotv.movie.DB.DBjk;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.m.cn.本地;

/**
 * Created by H19 on 2018/6/5 0005.
 */

public class Jiexi2 {
    private Activity mActivity;
    private Jiexi2 mThis;
    private List<DBjk> 接口列表 = new ArrayList<>();
    private int 重试次数;
    private int 当前接口;
    private int mState;
    private String mUrl;
    public String 视频地址;
    public String 视频类型;

    public Jiexi2 New(Activity activity){
        mActivity = activity;
        mThis = this;
        加载接口列表();
        return this;
    }
    private void 加载接口列表(){
        接口列表 = DataSupport.findAll(DBjk.class);
        if (接口列表.size() < 1){
            接口列表.add(new DBjk("线路2","http://jx.itaoju.top/?url=",2));
            接口列表.add(new DBjk("线路3","http://www.itono.cn/ty/mdparse/index.php?id=",2));
        }
    }
    public void 切换线路(){
        当前接口++;
        start(mUrl);
    }
    public boolean is主线程 = false;
    public void start(String t){
        mUrl = t;
        if (当前接口 >= 接口列表.size()) 当前接口 = 0;
        final String url = 接口列表.get(当前接口).getUrl() + mUrl;
        mState = STATE_ING; // 解析中
        if (is主线程 == true){
            int chaoshi = 本地.get设置(mActivity,"player_parse_waiting_time",8);
            new Handler().postDelayed(计时,chaoshi * 1000);
        }
        if (mListener != null) mListener.状态(mThis,2,接口列表.get(当前接口).getName() + "开始解析");
        if (接口列表.get(当前接口).getType() == 1){
            盘古(url);
        }else {
            if (mActivity == null){解析失败("获取视频地址错误");return ;}
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final ParseWebUrlHelper parseWebUrlHelper = ParseWebUrlHelper.getInstance().init(mActivity, url);
                    parseWebUrlHelper.setOnParseListener(new ParseWebUrlHelper.OnParseWebUrlListener() {
                        @Override
                        public void onFindUrl(String url,String type,Map<String, String> headers) {
                            解析成功(url,type);
                        }
                        @Override
                        public void onError(String errorMsg) {
                            解析失败(errorMsg);
                        }
                    });
                    parseWebUrlHelper.startParse();
                }
            });
        }

    }
    // 盘古
    private PanGu mPangu;
    private class PanGu{
        public int code;
        public String msg;
        public String url;
        public String type;
        public String t;
        public String ip;
        public String version;
    }
    private void 盘古(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(url);
                try{
                    Gson gson = new Gson();
                    PanGu data = gson.fromJson(code,PanGu.class);
                    mPangu = data;
                    if (mPangu.code == 200){
                        解析成功(mPangu.url,mPangu.type);
                    }else {
                        if (mPangu.msg == null) mPangu.msg = " ";
                        解析失败(mPangu.msg);
                    }
                }catch (Exception e){
                    Log.i("mdown-play",e.toString());
                }
            }
        }).start();
    }
    private void 解析失败(String msg){
        mState = STATE_ERROR;
        if (重试次数 < 接口列表.size()){
            mListener.状态(mThis,2,msg);
            当前接口++;
            重试次数++;
            start(mUrl);
            if (is主线程 == true){
                new Handler().removeCallbacks(计时);
            }
        }else {
            mListener.状态(mThis,-1,msg);
        }
    }
    private void 解析成功(String url,String type){
        if (mState != STATE_ING) return; // 不等于加载中 就不在出结果
        mState = STATE_FINISH;
        if (is主线程 == true){
            new Handler().removeCallbacks(计时);
        }
        视频地址 = url;
        视频类型 = type;
        mListener.状态(mThis,0,url);
    }


    Runnable 计时 = new Runnable() {
        @Override
        public void run() {
            if (mState == STATE_ING){
                解析失败("解析超时");
            }
        }
        public void d(){}
    };

    public void 销毁(){
        mState = STATE_STOP;

    }

    private onListener mListener;
    public void setListener(onListener listener){
        mListener = listener;
    }
    public interface onListener{
        void 状态(Jiexi2 t, int errId, String msg);
    }

    private int STATE_ING = 1;
    private int STATE_FINISH = 2;
    private int STATE_STOP = 3;
    private int STATE_ERROR = 4;
}
