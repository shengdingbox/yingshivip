package cn.dabaotv.movie.main;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import cn.dabaotv.movie.main.my.MyView;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.video.R;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;


/**
 * Created by suime on 2018/11/23.
 */

public class JHView extends LinearLayout implements QConfig{
    public View mView;
    public WebView webView;

    private MyView mMyView;
    private MyView.userdata mUserData;
    public JHView(Context context) {
        super(context);
        ininView();

    }
    public void ininView(){
        mView = View.inflate(getContext(), R.layout.main_home_jh,this);
        webView = mView.findViewById(R.id.webview);
        mMyView = new MyView(getContext());
        initWebviewSetting();
        mView.findViewById(R.id.tv_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.canGoBack()){
                    webView.goBack();
                }
            }
        });
        loadWeb();
    }

    public void loadWeb(){
        webView.loadUrl("http://feifei.dabaotv.cn/faxian");
    }

    public void initWebviewSetting(){
        webView.getSettings().setJavaScriptEnabled(true);// 支持js
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                webView.loadUrl(s);
                return true;
            }
        });//防止加载网页时调起系统浏览器
    }

}
