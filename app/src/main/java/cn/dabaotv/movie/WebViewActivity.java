package cn.dabaotv.movie;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import cn.dabaotv.movie.utils.BaseActivity;
import cn.dabaotv.video.R;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class WebViewActivity extends BaseActivity {
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webview = findViewById(R.id.webview);
        webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);// 支持js
        webview.setWebViewClient(new WebViewClient());//防止加载网页时调起系统浏览器

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if (!TextUtils.isEmpty("url")){
            webview.loadUrl(url);
        }else{
            ToastUtils.showShort("url不能为空！！！");
            finish();
        }
    }

    @Override
    protected boolean setFitsSystemWindows() {
        return true;
    }
}
