package cn.dabaotv.movie.Conl;

import android.app.Dialog;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import cn.dabaotv.movie.Function.Bion;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.utils.JsonUtils;
import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.movie.view.list.ItemList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cn.dabaotv.video.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.m.cn.styles.StyleStatusBar;
import cn.m.cn.信息框;
import cn.m.cn.文本;
import cn.m.cn.资源文件;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;


public class ListActivity extends SwipeBackActivity implements QConfig {

    private Bion mBion;
    private IListView mDataView; //  显示数据的列表框
    private int cutIndex = 1; // 当前分页位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datalist);
        StyleStatusBar.setWhiteBar(this);

        mBion = Q.getBion(getIntent());
//        System.out.println("mBion.type:"+mBion.type);
        ininHead();
        ininView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void ininHead(){
        findViewById(R.id.HeadReturn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView)findViewById(R.id.HeadTitle)).setText(mBion.name);
    }
    private void ininView(){

        // --------------- inin  筛选 tab ----------------------
        if (mBion.type != null && mBion.type.equals("dszblist")){
            ininView2();
            ininPlayerView();
        }else if(mBion.type != null && mBion.type.equals("$$spa$$")){
            initView3();
            //ininPlayerView();
        }
        else {
            ininView1();
            //ininPlayerView();
        }

        // ------------  inin 数据列表 --------------------

    }

    // 普通视频列表
    private void ininView1(){
        mDataView = (IListView) findViewById(R.id.ilist);
        mDataView.setIsLoadMore(true);
        mDataView.spanCount = 3;
        mDataView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                ininData(1);
                System.out.println("onRefresh调用");
                ininData(cutIndex + 1);
            }
        });
        mDataView.setLayout(R.layout.list_video,1);
        mDataView.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                Q.goPlayer(ListActivity.this,itemList.id);
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {
                Q.log("index",cutIndex + 1);
                ininData(cutIndex + 1);
            }
        });
        mDataView.setListNestedScrollingEnabled(false);

        if (!mBion.type.equals("$$spa$$")){
            ininSereenView();
            // 加载数据
            ininScreenView(getIntent().getStringExtra("screen"));
            mDataView.mAdapter.addHeaderView(mScreenView);
        }else {

        }
    }

    // 电视直播列表
    private void ininView2(){
        mDataView = (IListView) findViewById(R.id.ilist);
        mDataView.setIsLoadMore(false);
        mDataView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ininData2();
            }
        });
        mDataView.setLayout(R.layout.main_list_tv_item);
        mDataView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ininData2();
            }
        });
        mDataView.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                play2(itemList);
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {
            }
        });
        mDataView.setListNestedScrollingEnabled(false);
        ininData2();
    }

    private void initView3(){
        mDataView = (IListView) findViewById(R.id.ilist);
        mDataView.setIsLoadMore(true);
        mDataView.spanCount = 3;
        mDataView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData3();
            }
        });
        mDataView.setLayout(R.layout.list_video,1);
        mDataView.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                Q.goPlayer(ListActivity.this, JsonUtils.toJson(itemList),0);
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {
                Q.log("index",cutIndex + 1);
                initData3();
            }
        });
        mDataView.setListNestedScrollingEnabled(false);
        initData3();
    }
    private void initData3(){
        mDataView.clear();

        io.reactivex.Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String url = API + "?api=ct&id=" + mBion.id; //不知道
                String code = Ghttp.getHttp(url);
                emitter.onNext(code);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String code) throws Exception {
                        try{
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<ItemList>>() {}.getType();
                            List<ItemList> data = gson.fromJson(code,type);
                            if (data != null){
                                mDataView.addList(data);
                                mDataView.loadMoreEnd(false);
                            }else {
                                cutIndex = -1; // 没有数据  估计出错了吧
                                mDataView.loadMoreEnd(false);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
    }


    private Dialog loginDia;
    private void play2(final ItemList item){
        loginDia = 信息框.等待框(this,"加载中");

        io.reactivex.Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                String url = "http://wx168.ml0421.com/app" + 文本.get文本右边(item.url,item.url.length() - 1);
                final String code = Ghttp.getHttp(url);
                emitter.onNext(code);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String code) throws Exception {
                        try{
                            Document doc = Jsoup.parse(code);
                            final String sss = doc.select("video").attr("src");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    String con = 资源文件.getAssetsString(ListActivity.this,"playvideohtml");
                                    con = con.replaceAll("xxxtitlexxx",item.name);
                                    con = con.replaceAll("xxxurlxxx",sss);
                                    webView.loadData(con,"text/html; charset=UTF-8",null);
                                    Q.log("start",sss);
                                    if(loginDia != null){
                                        loginDia.dismiss();
                                        loginDia.cancel();
                                        loginDia = null;
                                    }
                                }

                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

       /* new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://wx168.ml0421.com/app" + 文本.get文本右边(item.url,item.url.length() - 1);
                String code = Ghttp.getHttp(url);
                try{
                    Document doc = Jsoup.parse(code);
                    final String sss = doc.select("video").attr("src");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String con = 资源文件.getAssetsString(ListActivity.this,"playvideohtml");
                            con = con.replaceAll("xxxtitlexxx",item.name);
                            con = con.replaceAll("xxxurlxxx",sss);
                            webView.loadData(con,"text/html; charset=UTF-8",null);
                            Q.log("start",sss);
                            if(loginDia != null){
                                loginDia.dismiss();
                                loginDia.cancel();
                                loginDia = null;
                            }
                        }

                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    private WebView webView;
    private void ininPlayerView(){
        webView = new WebView(this);
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true); // 允许加载JS
        webSetting.setDomStorageEnabled(true); // DOM对象

        webSetting.setLoadWithOverviewMode(true);
        webSetting.setUseWideViewPort(true); // 关键点
        webSetting.setAllowFileAccess(true); // 允许访问文件
        webSetting.setCacheMode(android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK); // 缓存

        webSetting.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSetting.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSetting.setDisplayZoomControls(false); //隐藏原生的缩放控件
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);// 默认先适应屏幕
        webSetting.setSupportMultipleWindows(true);// 走回调
        getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ArrayList<View> outView = new ArrayList<View>();
                getWindow().getDecorView().findViewsWithText(outView,"缓存",View.FIND_VIEWS_WITH_TEXT);
                int size = outView.size();
                if(outView!=null && outView.size()>0){
                    outView.get(0).setVisibility(View.GONE);
                    //((TextView)outView.get(0)).setText("Test");
                }
            }
        });
        webView.getSettings().setDefaultTextEncodingName("UTF-8");//设置默认为utf-8
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(com.tencent.smtt.sdk.WebView webView, String s) {
                webView.loadUrl("javascript:myFunction()");
                super.onPageFinished(webView, s);
            }
        });

        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        webView.getView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        Bundle data = new Bundle();
        data.putBoolean("standardFullScreen", false);// true表示标准全屏，false表示X5全屏；不设置默认false，
        data.putBoolean("supportLiteWnd", false);// false：关闭小窗；true：开启小窗；不设置默认true，
        data.putInt("DefaultVideoScreen", 2);// 1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
        webView.getX5WebViewExtension().invokeMiscMethod("setVideoParams", data);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });


    private View mScreenView;
    private List<TabLayout> mScreenTabList = new ArrayList<>();
    private Integer[] mTabSelectIndexList = new Integer[]{0,0,0};

    // ------------ 筛选 -------------------
    private void ininSereenView(){
        mScreenView = View.inflate(this,R.layout.conl_list_screen,null);
        mScreenTabList.add((TabLayout)mScreenView.findViewById(R.id.tabA));
        mScreenTabList.add((TabLayout)mScreenView.findViewById(R.id.tabB));
        mScreenTabList.add((TabLayout)mScreenView.findViewById(R.id.tabC));

        for (int i = 0; i < mScreenTabList.size(); i++) {
            final int cutI = i;
            mScreenTabList.get(i).addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    // 选中
                    if (mTabSelectIndexList[cutI] != tab.getPosition()){
                        System.out.println("ininSereenView调用");
                        mTabSelectIndexList[cutI] = tab.getPosition();
                        ininData(1);
                    }
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    // 未选中
                }
                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    // 复选

                }
            });
        }
    }
    private List<ItemScreen> 筛选条件;
    private class ItemScreen{
        public String name;
        public String[] data;
        public ItemScreen(){}
    }
    public void ininScreenView(String json){
        Type type = new TypeToken<List<ItemScreen>>() {}.getType();
        筛选条件 = new Gson().fromJson(json,type);
        String selectType = "";
        selectType = mBion.type;
        if (selectType == null || selectType.length() < 1) selectType = "全部";
        for (int i = 0;i < mScreenTabList.size();i++){
            if (i >= 筛选条件.size()){
                break;
            }
            for (int i2 = 0;i2 < 筛选条件.get(i).data.length;i2++){
                String name = 筛选条件.get(i).data[i2];
                mScreenTabList.get(i).addTab(mScreenTabList.get(i).newTab().setText(name));
                if (name.equals(selectType)){
                    mScreenTabList.get(i).getTabAt(i2).select();
                }
                if (name.equals("2018")){
                    mScreenTabList.get(i).getTabAt(i2).select();
                }
            }
        }
    }



    private void ininData(final int pageIndex){
        if (pageIndex == 1) mDataView.clear();
        mDataView.setRefreshing(true);
        cutIndex = pageIndex;
        String getdata = "?api=screen&id=" + mBion.id + "&page=" + cutIndex;
        for (int i=0;i<筛选条件.size();i++){
            getdata += "&"+ 筛选条件.get(i).name + "=" + 筛选条件.get(i).data[mTabSelectIndexList[i]];
        }
        final String url = API + getdata;
        System.out.println("fenye:"+url);
        io.reactivex.Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                final String code = Ghttp.getHttp(url);
                emitter.onNext(code);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String code) throws Exception {
                        try{
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<ItemList>>() {}.getType();
                            List<ItemList> data = gson.fromJson(code,type);
                            if (data != null){
                                mDataView.addList(data);
                                if (data.size() < 30){
                                    mDataView.loadMoreEnd(false);//表明加载全部完成。没有更多了
                                    cutIndex = -1; // 标识已经最后一页了
                                }else{
                                    mDataView.loadMoreComplete();//要记得调用，表示加载更多结束。下次才会再触发
                                }
                            }else {
                                cutIndex = -1; // 没有数据  估计出错了吧
                                mDataView.loadMoreEnd(false);
                            }
                        }catch (Exception e){
                            Log.i("eee-ee",e.toString());
                        }
                    }
                });

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(url);
                try{
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<ItemList>>() {}.getType();
                    List<ItemList> data = gson.fromJson(code,type);
                    if (data != null){
                        mDataView.addList(data);
                        if (data.size() < 30){
                            mDataView.loadMoreEnd(false);
                            cutIndex = -1; // 标识已经最后一页了
                        }
                    }else {
                        cutIndex = -1; // 没有数据  估计出错了吧
                        mDataView.loadMoreEnd(false);
                    }
                }catch (Exception e){
                    Log.i("eee-ee",e.toString());
                }
            }
        }).start();*/
    }

    // 获取电视直播的
    private void ininData2() {
        mDataView.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://wx168.ml0421.com/app" + 文本.get文本右边(mBion.url,mBion.url.length() - 1);
                String code = Ghttp.getHttp(url);
                try{
                    Document doc = Jsoup.parse(code);
                    Elements es = doc.select("div.mui-card ul.mui-table-view li");
                    for (int i = 0; i < es.size(); i++) {
                        if (0 == i) continue;
                        ItemList item = new ItemList();
                        item.name = es.get(i).text();
                        item.url = es.get(i).select("a").attr("href");
                        mDataView.addItem(item);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
