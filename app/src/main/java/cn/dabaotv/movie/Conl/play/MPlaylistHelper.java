package cn.dabaotv.movie.Conl.play;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.movie.view.list.ItemList;
import cn.dabaotv.video.R;

import java.util.ArrayList;
import java.util.List;

import cn.m.cn.Adapter.CnViewPagerAdapter;
import cn.m.cn.Function;
import cn.m.cn.Mdia;
import cn.m.cn.itemList.ItemTabPager;

/**
 * Created by 幻陌 on 2018/9/5 0005.
 */

public class MPlaylistHelper {

    private Context ctx;
    private int style;

    public MPlaylistHelper inin(Context ctx, OnListener listener) {
        this.ctx = ctx;
        this.mListener = listener;
        ininView();
        return this;
    }

    private View mView;
    private TabLayout mTabLayout;  // Tab
    private ViewPager mTabPager; // Tab视图
    private CnViewPagerAdapter mTabAdapter;
    private List<ItemTabPager> mTabList = new ArrayList<>();
    private List<List<ItemList>> mPlayList = new ArrayList<List<ItemList>>(); // 35条一页

    private int cutIndex; // 当前选中的值

    private void ininView() {
        mView = View.inflate(ctx, R.layout.conl_view_playlist, null);

        mTabLayout = mView.findViewById(R.id.PlayListTab);
        mTabPager = mView.findViewById(R.id.PlayListPager);
        mTabAdapter = new CnViewPagerAdapter(mTabList);
        mTabPager.setAdapter(mTabAdapter);
        mTabLayout.setupWithViewPager(mTabPager);//将TabLayout和ViewPager关联起来。
    }

    public void setList(List<ItemList> listdata) {
        mPlayList.clear();
        mTabList.clear();
        mTabLayout.removeAllTabs();

        List<ItemList> liss = new ArrayList<>();
        int size = 0;
        for (ItemList item : listdata) {
            if (size == 30) {
                List<ItemList> iss = new ArrayList<>();
                try {
                    iss = (List<ItemList>) Function.深度复制(liss);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (iss != null && iss.size() > 0) {
                    mPlayList.add(iss);
                }

                liss.clear();
                size = 0;
            }
            liss.add(item);
            size++;
        }
        if (liss.size() > 0) mPlayList.add(liss);
        // 生成界面
        for (int i = 0; i < mPlayList.size(); i++) {
            final IListView view = new IListView(ctx);
            view.setIsLoadMore(false);
            view.setIsSwipeRefresh(false);
            view.spanCount = 4;
            view.setLayout(R.layout.conl_list_playlist, 1);
            final int itemposition = i * 30;
            view.setItemListener(new IListView.itemOnClickListener() {
                @Override
                public void onClick(View v, int position, ItemList itemList) {
                    int posi = position + itemposition;
                    selectItem(posi);
                    mListener.onClick(posi);
                }

                @Override
                public void startLoadMore(ItemAdapter adapter) {

                }
            });
            view.setList(mPlayList.get(i));
            mListViewDl.add(view);
            String name = ((i * 30) + 1) + " - " + ((i * 30) + mPlayList.get(i).size());
            mTabList.add(new ItemTabPager(view, name));
            mTabLayout.addTab(mTabLayout.newTab().setText(name));
        }

        mTabAdapter.notifyDataSetChanged();
    }


    private int[] selectData = new int[]{0, 0};

    public void selectItem(IListView listview, int id, boolean b) {
        listview.select(id, b);
    }

    public void selectItem(int id) {
        cutIndex = id;
        // 将原来选中的项目删除焦点
        if (selectData[0] < mListViewDl.size() && selectData[1] < mListViewDl.get(selectData[0]).size()) {
            selectItem(mListViewDl.get(selectData[0]), selectData[1], false);
        }
        // 选中当前
        if (id > 0) {
            selectData[0] = id / 30; // 取商  获取第几个列表
            selectData[1] = id % 30; // 取余
        } else {
            selectData[0] = 0;
            selectData[1] = 0;
        }
        if (mListViewDl.size() <= selectData[0]) return; // 项目超出
        if (mListViewDl.get(selectData[0]).size() < selectData[1]) return; // 项目超出
        selectItem(mListViewDl.get(selectData[0]), selectData[1], true);
    }

    // 设置样式 ， 全屏时需要透明，而非全屏则白色
    public void setStyle(boolean isFull) {
        LinearLayout BackView = mView.findViewById(R.id.PlayListView);

        if (!isFull) {
            mTabLayout.setTabTextColors(0xFF000000, ctx.getResources().getColor(R.color.colorOn));
            BackView.setBackgroundColor(0xFFFFFFFF);
        } else {
            mTabLayout.setTabTextColors(0xFFFFFFFF, ctx.getResources().getColor(R.color.colorOn));
            BackView.setBackgroundColor(0x50000000);
            //BackView.getBackground().setAlpha(0);//0~255透明度值
        }

        int styletype = Qe.LISTTYPE_播放列表_竖屏;
        if (isFull) styletype = Qe.LISTTYPE_播放列表_全屏;

        for (IListView iListView : mListViewDl) {
            iListView.setStyle(styletype);
        }

        // 设置样式后必须重新选择ID 样式
        selectItem(cutIndex);
    }

    //  ------------ 播放列表 ----------------
    private List<IListView> mListViewDl = new ArrayList<>();


    private Mdia mdia;

    public void shop(Activity acx, boolean isFull, int width, int height) {
        for (IListView listView : mListViewDl) {
            listView.refresh();
        }
        if (mdia == null) mdia = new Mdia().inin(acx);
        setStyle(isFull);
        if (isFull) {
            mdia.show2(mView, Gravity.RIGHT, width, height);
        } else {
            mdia.show2(mView, Gravity.BOTTOM, width, height);
        }

    }


    // 创建一个监听器  让play activity 调用
    public OnListener mListener;

    public interface OnListener {
        void onClick(int position);
    }

    public void hide(){
        if(mdia != null){
            mdia.hide();
        }
    }

}
