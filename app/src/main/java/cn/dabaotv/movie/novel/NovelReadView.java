package cn.dabaotv.movie.novel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.video.R;

import org.jsoup.Jsoup;

import okhttp3.Cache;

/**
 * Created by H19 on 2018/4/30 0030.
 */

public class NovelReadView extends LinearLayout {
    public View mView;
    public TextView mName;
    public TextView mContent;
    public NovelData mData;

    public int top;
    public int height;
    public int listIndex;
    private String novelUrl;

    public NovelReadView(Context context){
        super(context);
        loadview();
    }
    public void loadview(){
        this.setOrientation(LinearLayout.VERTICAL);
        mView = View.inflate(getContext(), R.layout.view_novel_readview,null);
        mName = (TextView) mView.findViewById(R.id.read_con_name);
        mContent = (TextView)mView.findViewById(R.id.read_con_content);
    }
    public void loadData(final String name,final String url){
        mName.setText(name);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(new Cache(getContext().getCacheDir(), 10240*1024),url);
                Message msg = new Message();
                org.jsoup.nodes.Element element = Jsoup.parse(code).body();
                String content = element.select("div#content").html();
                content = content.replaceAll("<br>\n<br>","\n");
                content = content.replaceAll("<br>","\n");
                content = content.replaceAll("&nbsp;"," ");
                mData = new NovelData(name,content);
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    mName.setText(mData.name);
                    mContent.setText(mData.content+mData.content);
                    setVisibility(VISIBLE);
                    addView(mView);
                    if (onHeightChange != null) onHeightChange.onHeight(getHeight());

            }
        }
        public void llll(){}
    };

    private OnHeightChange onHeightChange;
    public void setOnHeightChange(OnHeightChange onHeightChange){
        this.onHeightChange = onHeightChange;
    }
    public interface OnHeightChange{
        void onHeight(int height);
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public int getListIndex() {
        return listIndex;
    }
    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public void setViewPosition(int top,int height){

    }
}
