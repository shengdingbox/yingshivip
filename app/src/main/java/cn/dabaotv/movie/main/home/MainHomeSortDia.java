package cn.dabaotv.movie.main.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;

import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.view.list.ItemList;

import java.util.List;

import cn.m.cn.Mdia;

/**
 * Created by 幻陌. on 2018/9/3 0003.
 * 弹窗分类
 */

public class MainHomeSortDia {


    private View mView;
    private IListView mIlistView;

    private Context ctx;
    private Dialog dialog;
    private Mdia mDia;

    public MainHomeSortDia inin(Context context){
        ctx = context;
        ininView();

        if (mDia == null) mDia = new Mdia().inin((Activity)ctx);

        return this;
    }
    public void show(){
        mDia.show2(mView, Gravity.CENTER,-1,-1);
    }
    private void ininView(){
        mView = View.inflate(ctx, R.layout.main_home_sortlist,null);
        mView.findViewById(R.id.bt_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDia.dialog.dismiss();
            }
        });
        mIlistView = mView.findViewById(R.id.ilist);
        mIlistView.setIsLoadMore(false);
        mIlistView.setIsSwipeRefresh(false);
        mIlistView.spanCount = 2;
        mIlistView.setLayout(R.layout.home_list_sort,1);
    }
    public void setList(List<ItemList> ilist){
        mIlistView.setList(ilist);
    }

}
