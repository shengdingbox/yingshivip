package cn.m.mslide;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.m.cn.像素;

/**
 * author:Created by ZhangPengFei on 2017/12/2.
 */

public class SlideView extends LinearLayout {
    private MViewPager mPager;
    private List<SlideItem> mList = new ArrayList<>();
    private SlideAdapter mAdapter;
    private List<ImageView> mDotList = new ArrayList<>();
    private LinearLayout mDotDiv;

    private void log(String t){
        Log.d("SlideView",t + "  ");
    }


    private int pei;

    public int item = 0;
    private int cou = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (mList == null || mList.size() < 1) return;
            int i = item - 1;

            // 替换旧的
            if (i > 0) i = i % mList.size();
            if (mDotList.size() > i) mDotList.get(i).setImageResource(R.drawable.icon_dot);


            i = item % mList.size();
            if (mDotList.size() > i) mDotList.get(i).setImageResource(R.drawable.icon_dot_select);


            mPager.setCurrentItem(item);

        }
    };

    public SlideView(Context context) {
        super(context);
        loadView();
    }
    public SlideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        loadView();
    }
    public SlideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void loadView(){
        View inflate = View.inflate(getContext(), R.layout.mview, this);
        mPager = inflate.findViewById(R.id.vp);
        mPager.setBackgroundResource(R.drawable.back_home_back);
        // 手势
        mPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 如果移开 则继续轮播 否则 暂停处理
                if (event.getAction() == 1){
                    start();
                }else {
                    stop();
                }
                return false;
            }
            public void dd(){}
        });
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mList.size() < 1) return;
                int i = item % mList.size();
                if (mDotList.size() > i) mDotList.get(i).setImageResource(R.drawable.icon_dot);

                item = position;
                i = item % mList.size();
                if (mDotList.size() > i) mDotList.get(i).setImageResource(R.drawable.icon_dot_select);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        mDotDiv = inflate.findViewById(R.id.dotdiv);

        mAdapter = new SlideAdapter(mList,getContext());
        mAdapter.setOnItemClickListener(new SlideAdapter.onListener() {
            @Override
            public void onClick(View v, SlideItem t) {
                if (mItemClickListener != null) mItemClickListener.onClick(t);
            }
        });
        mPager.setAdapter(mAdapter);
    }
    public void setList(List<SlideItem> slideItems){
        mList.clear();
        mList.addAll(slideItems);
        Refresh();
    }
    private OnItemClickListener mItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onClick(SlideItem item);
    }
    public void addSlide(SlideItem item){
        mList.add(item);
        Refresh();
    }
    Timer timer;
    private int listSize;
    public void start(){
        if (timer == null) timer = new Timer();
        try {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    item = item + 1;
                    handler.sendEmptyMessage(0);
                }
            },5000,5000);
        }catch (Exception e){
            log("err12"+e.toString());
        }
    }
    public void 计时操作(){

    }
    public void stop(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }

    }
    public boolean isPause;
    private boolean mstate;
    public void setSelectStete(boolean b){
        mstate = b;
    }

    public void Refresh(){

        stop();
        listSize = mList.size();
        mDotList.clear();
        mDotDiv.removeAllViews();
        for (int i = 0; i < listSize; i++) {
            mDotList.add(addDot());
        }
        pei = listSize * 5;
        mAdapter.notifyDataSetChanged();
        start();
    }
    public ImageView addDot(){
        ImageView img = new ImageView(getContext());
        int height = 像素.dip2px(getContext(),6);
        LayoutParams layoutParams = new LayoutParams(height,height);
        layoutParams.setMargins(像素.dip2px(getContext(),3),0,像素.dip2px(getContext(),3),0);
        img.setLayoutParams(layoutParams);
        img.setImageResource(R.drawable.icon_dot);
        mDotDiv.addView(img);
        return img;
    }



}