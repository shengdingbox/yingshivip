package cn.dabaotv.movie.main.home;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.main.list.DLBlock;
import cn.dabaotv.movie.search.SearchActivity;
import cn.dabaotv.movie.utils.JsonUtils;
import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import com.google.gson.Gson;
import cn.dabaotv.movie.Conl.ListActivity;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.utils.ACache;
import cn.dabaotv.movie.view.list.ItemList;

import java.util.ArrayList;
import java.util.List;

import cn.m.cn.像素;
import cn.m.cn.文本;
import cn.m.mslide.SlideItem;
import cn.m.mslide.SlideView;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by H19 on 2018/9/3 0003.
 */

public class MainHomeConlView extends LinearLayout implements QConfig {

    public final static String DLBlock_key = "DLBlock_key";

    public MainHomeConlView(Context ctx){
        super(ctx);
        ininView();
    }
    public MainHomeConlView(Context ctx, AttributeSet attr){
        super(ctx,attr);
        ininView();
    }
    public MainHomeConlView(Context ctx, AttributeSet attr, int i){
        super(ctx,attr,i);
        ininView();
    }
    private DLMainHomeConl mData;
    private SlideView mSlideView; // 幻灯片
    public ItemAdapter mAdapter;
    private int mSortId;


    private void ininView(){
        final SwipeRefreshLayout swipeRefreshLayout = new SwipeRefreshLayout(getContext());
        RecyclerView recycler = new RecyclerView(getContext());
        List<ItemList> lists = new ArrayList<>();
        mAdapter = new ItemAdapter(lists);
        recycler.setAdapter(mAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout.addView(recycler);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                MainActivity mainActivity = (MainActivity) MyApplication.aty;
//                mainActivity.recreate();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        addView(swipeRefreshLayout,new LayoutParams(-1,-1));

        // ---- start 幻灯片
        mSlideView = new SlideView(getContext());
        mSlideView.setOnItemClickListener(new SlideView.OnItemClickListener() {
            @Override
            public void onClick(SlideItem item) {
                Q.goPlayer(getContext(),item.url);
            }
        });

        mSubsortViewList = new IListView(getContext()); // 子分类
        mSubsortViewList.setIsLoadMore(false);
        mSubsortViewList.setIsSwipeRefresh(false);
        mSubsortViewList.spanCount = 4;
        mSubsortViewList.setLayout(R.layout.home_content_subsorttype,1);

        mSubsortViewList.setListNestedScrollingEnabled(false);

    }

    // ---------- start load data -----------------------
    public void inin(String sortId){
        mSortId = Integer.parseInt(sortId);
        final String url = API + "?api=home_data&id=" + sortId; //首页数据,幻灯片

        io.reactivex.Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                final String code = Ghttp.getHttp(url);
                if (code.length() > 10) {
                    try{
                        mData = new Gson().fromJson(code,DLMainHomeConl.class);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    emitter.onNext(code);
                    emitter.onComplete();
                }else {
                    emitter.onComplete();
                    Q.echo(getContext(), "无网络数据");
                }

            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String code) throws Exception {
                        if (mData!=null) Refresh();
                    }
                });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final String code = Ghttp.getHttp(url);
//                if (code.length() > 10) {
//                    try{
//                        mData = new Gson().fromJson(code,DLMainHomeConl.class);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        err();
//                    }
//                    post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mData!=null) Refresh();
//                        }
//                    });
//
//                }else {
//                    err();
//                }
//            }
//            public void err(){
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Q.echo(getContext(), "无网络数据");
//                    }
//                });
//
//            }
//        }).start();
    }
    private IListView mSubsortViewList;
    public void Refresh(){
        // --- start 幻灯片
        List<SlideItem> slideItemList = new ArrayList<>();
        for (ItemList item : mData.slide) {
            SlideItem sitem = new SlideItem(item.name,item.url,item.img);
            slideItemList.add(sitem);
        }
        mSlideView.setList(slideItemList);
        mSlideView.start(); // 幻灯片开始循环
        mSlideView.setLayoutParams(new LayoutParams(-1, 像素.dip2px(getContext(),180)));
        mAdapter.addFooterView(mSlideView);

        // --- start 分类导航
        getTypeView();

        Log.d("mSortId:",mSortId+"");
        if(mSortId == 0){
            String data = JsonUtils.toJson(mData.video);
            ACache.get(getContext()).put(DLBlock_key,JsonUtils.toJson(mData.video));
        }
        // 列表块
        for (DLBlock dlBlock : mData.video) {
            View v = getBlock(dlBlock);
            if (v != null) mAdapter.addFooterView(v);
        }
    }
    private View getBlock(final DLBlock item){
        View mv = View.inflate(getContext(), R.layout.main_home_conl_block,null);
        ((TextView)mv.findViewById(R.id.name)).setText(item.name);

        // 加载更多点击监听
        mv.findViewById(R.id.more).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemList itemList = new ItemList(item.name);
                itemList.msg = item.msg;
                itemList.id = Integer.toString(item.id);
//                if (onSubsortListener!=null)onSubsortListener.onClick(itemList,"全部");
                if (onSubsortListener!=null)onSubsortListener.onClick(itemList,item.name);
            }
        });
        mv.findViewById(R.id.more2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemList itemList = new ItemList(item.name);
                itemList.msg = item.msg;
                itemList.id = Integer.toString(item.id);
//                if (onSubsortListener!=null)onSubsortListener.onClick(itemList,"全部");
                if (onSubsortListener!=null)onSubsortListener.onClick(itemList,item.name);
            }
        });

        // 数据列表
        IListView ilist = mv.findViewById(R.id.ilist);
        ilist.setIsSwipeRefresh(false);
        ilist.setIsLoadMore(false);
        ilist.spanCount = 3;
        ilist.setLayout(R.layout.list_video,1);
        ilist.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                Q.goPlayer(getContext(),itemList.id);
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });
        ilist.setList(item.data);
        if (item.data.size() > 0){
            return mv;
        }else {
            return null;
        }

    }
    // 推荐页面的分类
    public void getTypeView(){
        mSubsortViewList.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList item) {
                if (mSortId == 0){
                    if(item.id.equals("5")){
                        Intent intent = new Intent(getContext(), SearchActivity.class);
                        intent.putExtra("type","novel");
                        getContext().startActivity(intent);
                        return;
                    }

                    Intent intent = new Intent(getContext(),ListActivity.class);
                    intent.putExtra("name",item.name);
                    intent.putExtra("id",item.id);
                    intent.putExtra("type","$$spa$$");
                    getContext().startActivity(intent);
                }else {
                    if (onSubsortListener!=null)onSubsortListener.onClick(item,item.name);
                }

            }
            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });

        mSubsortViewList.clear();

        if (mSortId == 0){
//            mSubsortViewList.spanCount = 5;
//            mSubsortViewList.setLayout(R.layout.home_list_conl_home_sort,1);
//            ItemList item = new ItemList("欧美大片",R.drawable.home_ic_conlsort_om); item.id = "1";
//            mSubsortViewList.addItem(item);
//            item = new ItemList("网红大片",R.drawable.home_ic_conlsort_wh); item.id = "2";
//            mSubsortViewList.addItem(item);
//            item = new ItemList("抢版电影",R.drawable.home_ic_conlsort_qb); item.id = "3";
//            mSubsortViewList.addItem(item);
//            item = new ItemList("午夜看",R.drawable.home_ic_conlsort_wy); item.id = "4";
//            mSubsortViewList.addItem(item);
//            item = new ItemList("小说搜索",R.drawable.home_ic_conlsort_xs); item.id = "5";
//            mSubsortViewList.addItem(item);
        }else {
            if (mData.video.size() > 2){
                int cou = 3;  // 获取子分类个数，最多两行 每行4个，这里3、7 后面均需要加入 《全部》
                if (mData.video.size() > 6) cou = 7; //
                for (int i=0;i < cou;i++){
                    String name = mData.video.get(i).name;
                    if (name.length() > 4) 文本.get文本左边(name,4);
                    mSubsortViewList.addItem(new ItemList(name));
                }
                mSubsortViewList.addItem(new ItemList("全部")); // 子分类最后添加一个全部按钮
            }
        }

        LayoutParams subparams = new LayoutParams(-1, -2);
        subparams.setMargins(0,像素.dip2px(getContext(),20),0,像素.dip2px(getContext(),20));
        mSubsortViewList.setLayoutParams(subparams);
        mAdapter.addFooterView(mSubsortViewList);




    }


    // 设置回调事件，这个类只显示界面
    private onSubsortClickListener onSubsortListener;
    public interface onSubsortClickListener{
        void onClick(ItemList item,String type);
    }
    public void setOnItemClick(onSubsortClickListener onitemclick){
        this.onSubsortListener = onitemclick;
    }


}
