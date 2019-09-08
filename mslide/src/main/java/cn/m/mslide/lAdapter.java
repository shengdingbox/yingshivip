package cn.m.mslide;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.m.cn.itemList.ItemTabPager;

/**
 * Created by H19 on 2018/1/5.
 */

public class lAdapter extends android.support.v4.view.PagerAdapter {
    private List<ItemTabPager> mTabPagers;

    public lAdapter(List<ItemTabPager> item) {
        this.mTabPagers = item;
    }

    @Override
    public int getCount() {
        return mTabPagers.size();//页卡数
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;//官方推荐写法
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mTabPagers.get(position).view, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);
        return mTabPagers.get(position).view;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mTabPagers.get(position).view);//删除页卡
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabPagers.get(position).title;//页卡标题
    }
}
