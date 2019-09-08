package cn.dabaotv.movie.main.home;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import cn.dabaotv.movie.Function.Bion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Conl.ListActivity;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.view.list.ItemList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.m.cn.Adapter.CnViewPagerAdapter;
import cn.m.cn.Cnview;
import cn.m.cn.itemList.ItemTabPager;
import cn.m.cn.Function;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by H19 on 2018/9/3 0003.
 */

public class MainHomeView extends LinearLayout implements View.OnClickListener,QConfig {
    private static final String TAG = "MainHomeView";

    public MainHomeView(Context ctx){
        super(ctx);
        ininView();

    }
    public MainHomeView(Context ctx, AttributeSet attr){
        super(ctx,attr);
        ininView();
    }
    public MainHomeView(Context ctx, AttributeSet attr, int i){
        super(ctx,attr,i);
        ininView();
    }

    private View mView;

    // tab
    private TabLayout mTablayout;
    private ViewPager mTabPager;
    private CnViewPagerAdapter mTabAdapter;
    private List<ItemTabPager> mTabList = new ArrayList<>();


    private void ininView(){
        mView = View.inflate(getContext(), R.layout.main_home,this);

    /*    final SwipeRefreshLayout mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_ly);
        //设置在listview上下拉刷新的监听
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //这里可以做一下下拉刷新的操作
                mTabAdapter.notifyDataSetChanged();
                mSwipeLayout.setRefreshing(false);
            }
        });*/

        // 导航 nav
        mTablayout = mView.findViewById(R.id.tablayout);
        mTabPager = mView.findViewById(R.id.viewpager);

        mView.findViewById(R.id.searchBox).setOnClickListener(this); // 搜索框
        mView.findViewById(R.id.bt_sc).setOnClickListener(this); // 搜索
        mView.findViewById(R.id.bt_ls).setOnClickListener(this); // 历史
        mView.findViewById(R.id.bt_xz).setOnClickListener(this); // 下载
        mView.findViewById(R.id.bt_screen).setOnClickListener(this); // 筛选

        //mView.findViewById(R.id.bt_menu).setOnClickListener(this);

        mTabAdapter = new CnViewPagerAdapter(mTabList);
        mTabPager.setAdapter(mTabAdapter);//给ViewPager设置适配器
        mTablayout.setupWithViewPager(mTabPager);//将TabLayout和ViewPager关联起来。

        mTablayout.post(new Runnable() {
            @Override
            public void run() {
                Cnview.Tablayout_setTabLine(mTablayout,0,10);
            }
        });
        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTabPosition(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        ininData();
    }

    private int cutTabPisiton;
    public void setTabPosition(int i){
        cutTabPisiton = i;
        if (cutTabPisiton > 0){
            mView.findViewById(R.id.nav2).setVisibility(View.GONE);
            mView.findViewById(R.id.bt_screen).setVisibility(View.VISIBLE);
        }else {
            mView.findViewById(R.id.nav2).setVisibility(View.VISIBLE);
            mView.findViewById(R.id.bt_screen).setVisibility(View.GONE);
        }
        mTabPager.setVerticalScrollbarPosition(cutTabPisiton);
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.searchBox:
                Q.goIntent(getContext(), Qe.INTENT_搜索,"video");
                break;
            case R.id.bt_sc:
                Q.goIntent(getContext(), Qe.INTENT_收藏);
                break;
            case R.id.bt_ls:
                Q.goIntent(getContext(), Qe.INTENT_历史);
                break;
            case R.id.bt_xz:

                Q.goIntent(getContext(), Qe.INTENT_下载);
                break;
            case R.id.bt_screen:
                Intent intent = new Intent(getContext(),ListActivity.class);
                ItemList item = mSorts.get(cutTabPisiton);
                goScreen(cutTabPisiton,item,"");
                break;
//            case R.id.bt_menu:
//                if (mSortDia == null){
//                    mSortDia = new MainHomeSortDia().inin(getContext());
//                    mSortDia.setList(mSorts);
//                }
//
//                mSortDia.show();
//                break;
        }
    }
    private MainHomeSortDia mSortDia;
    public void ininData(){
        final String url = API + "?api=home_nav";  //获取分类和幻灯图
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
                        if (code.length() > 5){
                            parseData(code);
                        }else {
                            parseData("");
                            Q.echo(getContext(),"链接服务器失败");
                        }
                    }
                });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final String code = Ghttp.getHttp(url);
//                Log.d(TAG,code);
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (code.length() > 5){
//                            parseData(code);
//                        }else {
//                            parseData("");
//                            Q.echo(getContext(),"链接服务器失败");
//                        }
//                    }
//                });
//
//                //Message msg = new Message();
//
//                //handler.sendMessage(msg);
//            }
//        }).start();
    }
    private void parseData(String code){
        String coo = code;
        if (code.length() < 5){
            coo = Function.getCache(getContext(),"HomeViewSortData");
        }else {
            Function.saveCache(getContext(),"HomeViewSortData",code);
        }

        if (coo.length() > 10) {
            try{
                Type type = new TypeToken<List<ItemList>>() {}.getType();
                mSorts = new Gson().fromJson(coo,type);
                Refresh();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else {
            Q.echo(getContext(),"没有找到数据，请联系管理员");
        }


    }
    private List<ItemList> mSorts = new ArrayList<>();

    // ------------------- 内容视图 ---------------------------
    private List<CnViewPagerAdapter> mConPagerAdapter;

    //private List<MainHomeConlView> HomeSortView = new ArrayList<>();
    public void Refresh(){
        for (int i = 0; i < mSorts.size() ; i++){
            final int sortIndex = i;
            // 生成内容界面
            MainHomeConlView homeSortView = new MainHomeConlView(getContext());
            homeSortView.inin(mSorts.get(i).id);

            String name = mSorts.get(i).name;
            if (mSortDia != null) mSortDia.setList(mSorts);
            homeSortView.setOnItemClick(new MainHomeConlView.onSubsortClickListener() {
                @Override
                public void onClick(ItemList item,String type) {
                    goScreen(sortIndex,item,type);
                }
            });
            // 将数据添加到界面中
            mTablayout.addTab(mTablayout.newTab().setText(name));
            mTabList.add(new ItemTabPager(homeSortView,name));
        }
        mTabAdapter.notifyDataSetChanged();

    }
    private Bion mBion;
    public void goScreen(int sortIndex,ItemList item,String type){
        if (type.equals("全部")) type = "";
        Intent intent = new Intent(getContext(),ListActivity.class);
        if (sortIndex == 0){
            intent.putExtra("name",item.name);
            intent.putExtra("screen",item.msg);
            intent.putExtra("id",item.id);
        }else {
            intent.putExtra("name",mTabAdapter.getPageTitle(sortIndex));
            intent.putExtra("screen",mSorts.get(sortIndex).msg);
            intent.putExtra("id",mSorts.get(sortIndex).id);
        }

        intent.putExtra("type",type);
        getContext().startActivity(intent);
    }

    //  --------------- 创建界面 --------------------------

    public void addPagerView(){




    }





}
