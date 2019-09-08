package cn.dabaotv.movie.main;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cn.dabaotv.movie.main.home.MainHomeView;
import cn.dabaotv.movie.main.my.MyView;
import cn.dabaotv.movie.main.novel.MainNovelView;
import cn.dabaotv.video.R;

import java.util.ArrayList;
import java.util.List;

import cn.m.cn.Adapter.CnViewPagerAdapter;
import cn.m.cn.itemList.ItemTabPager;

/**
 * Created by H19 on 2018/9/3 0003.
 */

public class MainView extends FrameLayout implements View.OnClickListener {

    public MainView(@NonNull Context context) {
        super(context);
        ininView();
    }

    public MainView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ininView();
    }

    public MainView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ininView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MainView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        ininView();
    }

    private View mView;
    private QViewPager mPager;
    private CnViewPagerAdapter mPagerAdapter;
    private List<ItemTabPager> mPagerList = new ArrayList<>();

    private MainHomeView mHomeView;
    private MainNovelView mNovelView;
   // private MainTvView mTvView;
    private MyView mMyView;
    private JHView mJHView;
    private void ininView(){
        mView = View.inflate(getContext(), R.layout.main,this);

        mPager = mView.findViewById(R.id.frame);
        mPager.setSlide(false);
        mPagerAdapter = new CnViewPagerAdapter(mPagerList);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mView.findViewById(R.id.nav_sy).setOnClickListener(this);
        mView.findViewById(R.id.nav_xs).setOnClickListener(this);
        mView.findViewById(R.id.nav_zb).setOnClickListener(this);
        mView.findViewById(R.id.nav_wd).setOnClickListener(this);
        mNavImgSy = (ImageView)findViewById(R.id.nav_img_sy);
        mNavImgXs = (ImageView)findViewById(R.id.nav_img_xs);
        mNavImgZb = (ImageView)findViewById(R.id.nav_img_zb);
        mNavImgWd = (ImageView)findViewById(R.id.nav_img_wd);


        ininHome();
        ininNovel();
        ininLive();
        ininMy();
    }
    private void ininHome(){
        mHomeView = new MainHomeView(getContext());
        mPagerList.add(new ItemTabPager(mHomeView,""));
        mPagerAdapter.notifyDataSetChanged();
    }
    private void ininNovel(){
        mNovelView = new MainNovelView(getContext());
        mPagerList.add(new ItemTabPager(mNovelView,""));
        mPagerAdapter.notifyDataSetChanged();
    }
    private void ininLive(){

      mJHView = new JHView(getContext());
        mPagerList.add(new ItemTabPager(mJHView,""));
        mPagerAdapter.notifyDataSetChanged();
    }
    private void ininMy(){
        mMyView = new MyView(getContext());
        mPagerList.add(new ItemTabPager(mMyView,""));
        mPagerAdapter.notifyDataSetChanged();
    }

    private ImageView mNavImgSy;
    private ImageView mNavImgXs;
    private ImageView mNavImgZb;
    private ImageView mNavImgWd;

    public void onClick(View v){

            switch (v.getId()) {
                case R.id.nav_sy:
                    mPager.setCurrentItem(0);
                    mNavImgSy.setImageResource(R.drawable.ic_home_nav_sy1);

                    mNavImgXs.setImageResource(R.drawable.ic_home_nav_xs0);
                mNavImgZb.setImageResource(R.drawable.ic_home_nav_zb0);
                    mNavImgWd.setImageResource(R.drawable.ic_home_nav_wd0);
                    break;
                case R.id.nav_xs:
                        mPager.setCurrentItem(1);
                        mNavImgXs.setImageResource(R.drawable.ic_home_nav_xs1);
                        mNavImgSy.setImageResource(R.drawable.ic_home_nav_sy0);
                        mNavImgZb.setImageResource(R.drawable.ic_home_nav_zb0);
                        mNavImgWd.setImageResource(R.drawable.ic_home_nav_wd0);

                    break;
               case R.id.nav_zb:
                    if(mMyView.isVip()) {
                mPager.setCurrentItem(2);
                mNavImgZb.setImageResource(R.drawable.ic_home_nav_zb1);

                mNavImgSy.setImageResource(R.drawable.ic_home_nav_sy0);
                mNavImgXs.setImageResource(R.drawable.ic_home_nav_xs0);
                mNavImgWd.setImageResource(R.drawable.ic_home_nav_wd0);
        } else {
            mMyView.弹出激活窗口();
        }
                break;
                case R.id.nav_wd:
                    mPager.setCurrentItem(3);
                    mNavImgWd.setImageResource(R.drawable.ic_home_nav_wd1);


                    mNavImgSy.setImageResource(R.drawable.ic_home_nav_sy0);
                    mNavImgXs.setImageResource(R.drawable.ic_home_nav_xs0);
                mNavImgZb.setImageResource(R.drawable.ic_home_nav_zb0);
                    break;
            }
    }

    public void reload(){
        if(mNovelView != null){
            mNovelView.reload();
        }
    }

    public void vipOpen(){
        mPager.setCurrentItem(3);
        mNavImgWd.setImageResource(R.drawable.ic_home_nav_wd1);
        mNavImgSy.setImageResource(R.drawable.ic_home_nav_sy0);
        mNavImgXs.setImageResource(R.drawable.ic_home_nav_xs0);
        mMyView.弹出激活窗口();
    }


}
