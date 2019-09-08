package cn.dabaotv.movie.Conl.play;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import cn.dabaotv.movie.MyApplication;
import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.movie.Conl.PlayActivity;
import cn.dabaotv.movie.DB.DBDown;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.view.list.ItemList;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.m.cn.Function;
import cn.m.cn.Mdia;

/**
 * Created by 幻陌 on 2018/9/5 0005.
 */

public class MDownlistHelper {

    private Context ctx;
    private int style;
    private ItemList mVideoItem;

    public MDownlistHelper inin(Context ctx,ItemList videoinfo,String urlId){
        this.ctx = ctx;
        this.urlId = urlId;
        this.mVideoItem = videoinfo;
        ininView();
        return this;
    }

    private View mView;
    private IListView mListView;
    private String urlId;

    private int cutIndex; // 当前选中的值

    private void ininView(){
        mView = View.inflate(ctx, R.layout.conl_view_downlist,null);
        mListView = mView.findViewById(R.id.ilist);
        mListView.setIsSwipeRefresh(false);
        mListView.setIsLoadMore(false);
        mListView.spanCount = 4;
        mListView.setLayout(R.layout.conl_list_play_down,1);
        mListView.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                if (!itemList.select){
                    itemList.select = true;
                    MyApplication.AddDown(mVideoItem,Integer.toString(position),itemList);
                    mListView.mAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });
        mListView.setStyle(Qe.LISTTYPE_缓存列表);
    }

    public void setList(List<ItemList> list,ItemList videoinfo){
        mListView.clear();
        this.mVideoItem = videoinfo;
        // 判断是否下载
        for (int i = 0; i < list.size(); i++) {
            ItemList item = null;
            try {
                item = (ItemList) Function.深度复制(list.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (item != null){
                List<DBDown> dblist = DataSupport.where("url=? and index=?",urlId,Integer.toString(i)).find(DBDown.class);
                if (dblist.size() > 0){
                    item.select = true;
                }else {
                    item.select = false;
                }
                mListView.addItem(item);
            }
        }

        List<ItemList> xList = new ArrayList<>();

    }

    private Mdia mDia;
    public void show(PlayActivity atx, int height) {
        if (mDia == null) mDia = new Mdia().inin(atx);
        mDia.show2(mView, Gravity.BOTTOM,-1,height);
    }
}
