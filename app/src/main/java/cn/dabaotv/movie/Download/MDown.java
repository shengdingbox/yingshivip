package cn.dabaotv.movie.Download;

import android.app.Activity;

import cn.m.cn.文件;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by H19 on 2018/6/3 0003.
 */

public class MDown {
    private Activity mActivity;
    private String Down地址;
    private int Down类型; // type - 1单文件，m3u8
    private long Down长度;
    private long Down进度;
    private DownTask mDownTask;
    public String 目录;
    public String 文件名;

    public MDown down(Activity activity, String url, int type){
        this.Down地址 = url;
        this.Down类型 = type;
        this.mActivity = activity;
        return this;
    }
    public void setDir(String dir,String file){
        目录 = dir;
        文件.创建目录(mActivity,目录);
        文件名 = file;


    }
    public void start(){
        if (this.Down类型 == 1){
            mDownTask = new DownTask(mListener);
            mDownTask.execute(this.Down地址);
        }else if (this.Down类型 == 2){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //处理M3U8();
                }
            }).start();
        }
    }

    private M3U8Down mM3u8;
    private void 处理M3U8(){
        Request request = new Request.Builder().url(Down地址).get().build();
        String t_code = "";
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            Response response = okHttpClient.newCall(request).execute();
            t_code = response.body().string();
            if (t_code.length() > 5){
                mM3u8 = new M3U8Down().New(mActivity,t_code);
                mM3u8.setListener(mListener);
                mM3u8.setDir(目录);
                mM3u8.setFileName(文件名);
                mM3u8.setDownUrl(Down地址);
                mM3u8.开始下载();
            }else {
                下载失败("获取下载参数失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isPaused;
    public void 开始(){

    }
    public void 继续(){

    }
    public void 暂停(){
        if (isPaused == false){
            if (Down类型 == 2 && mM3u8 != null){
                mM3u8.暂停();
            }else if (mDownTask != null){
                mDownTask.pauseDownload();
            }
        }
        isPaused = true;
    }
    public void 停止(){

    }
    public void 删除(){

    }

    private void 下载失败(String t){

    }
    private void 进度改变(){

    }

    private DownListener 回调;
    private DownListener mListener = new DownListener() {
        @Override
        public void 进度改变(int progress) {
            回调.进度改变(progress);
        }

        @Override
        public void 下载成功() {
            回调.下载成功();
        }

        @Override
        public void 下载失败(String t) {
            回调.下载失败(t);
        }

        @Override
        public void 下载暂停() {
            回调.下载暂停();
        }

        @Override
        public void 下载取消() {
            回调.下载取消();
        }
    };
    public void setListener(DownListener listener){
        回调 = listener;
    }
    public interface DownListener {
        void 进度改变(int progress);//`通知当前进度

        void 下载成功();

        void 下载失败(String t);

        void 下载暂停();

        void 下载取消();
    }


}
