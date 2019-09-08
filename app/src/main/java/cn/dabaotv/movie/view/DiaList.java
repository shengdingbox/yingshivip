package cn.dabaotv.movie.view;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.view.list.ItemList;

import java.util.List;

/**
 * Created by H19 on 2018/6/13 0013.
 */

public class DiaList {
    public static void 弹出(Context context, int ResId, String title, String btname, List<ItemList> list, final itenOnClickListener mListListener){
        View v = View.inflate(context,ResId ,null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Dialog dialog = builder.create();

        final IListView tlist = v.findViewById(R.id.list);
        tlist.setIsSwipeRefresh(false);
        tlist.setIsLoadMore(false);
        tlist.setLayout(R.layout.conl_list_play_selectcode);
        tlist.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                if (mListListener != null){
                    mListListener.onClick(dialog,itemList,position);
                }
                if (dialog != null) dialog.dismiss();
            }
            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });

        for (int i = 0; i < list.size(); i++) {
            tlist.addItem(list.get(i));
        }

        if (btname == null){
            v.findViewById(cn.m.cn.R.id.enter).setVisibility(View.GONE);
        }else {
            ((TextView)v.findViewById(R.id.enter)).setText(btname);
        }

        if (title == null){
            v.findViewById(cn.m.cn.R.id.title).setVisibility(View.GONE);
        }else {
            ((TextView)v.findViewById(cn.m.cn.R.id.title)).setText(title);
        }


        v.findViewById(cn.m.cn.R.id.enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListListener != null) mListListener.onClick(dialog,null,-1);
                if (dialog != null) dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        window.setContentView(v);



        dialog.show();
        dialog.getWindow().setContentView(v);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }
    public static interface itenOnClickListener{
        void onClick(Dialog dialog, ItemList name, int position);
    }
}
