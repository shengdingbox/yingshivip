package cn.m.mslide;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by H19 on 2018/7/3 0003.
 */

public class MViewPager extends ViewPager {

    private ViewPageHelper helper;

    public MViewPager(Context context) {
        this(context,null);
    }

    public MViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        helper=new ViewPageHelper(this);

    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item,true);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        MScroller scroller=helper.getScroller();
        if(Math.abs(getCurrentItem()-item)>1){
            scroller.setNoDuration(true);
            super.setCurrentItem(item, smoothScroll);
            scroller.setNoDuration(false);
        }else{
            scroller.setNoDuration(false);
            super.setCurrentItem(item, smoothScroll);
        }
    }

}
