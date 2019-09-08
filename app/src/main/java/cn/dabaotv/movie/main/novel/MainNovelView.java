package cn.dabaotv.movie.main.novel;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;

import cn.dabaotv.movie.novel.NovelActivity;
import cn.dabaotv.movie.search.SearchActivity;
import cn.dabaotv.movie.view.list.IListBookView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.movie.DB.DBNovel;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.view.list.ItemList;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by H19 on 2018/5/4 0004.
 */

public class MainNovelView extends LinearLayout implements View.OnClickListener {
    private View mView;
    private IListBookView mIlist;
    public MainNovelView(Context context){
        super(context);
        ininView();
        loadRecord();
    }
    private Intent WindowNovel;
    private Intent WindowSearch;
    public void ininView(){
        mView = View.inflate(getContext(), R.layout.main_novel,this);
        mView.findViewById(R.id.search).setOnClickListener(this);

        mIlist = mView.findViewById(R.id.ilist);
        mIlist.setIsLoadMore(false);
        mIlist.setIsSwipeRefresh(true);
        mIlist.spanCount = 4;
        mIlist.setLayout(R.layout.item_home_novel_list,1);
        mIlist.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadRecord();
            }
        });
        mIlist.setItemListener(new IListBookView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                if (position == mIlist.size() - 1){
                    JumpSearch();
                }else {
                    if (WindowNovel == null) WindowNovel = new Intent(getContext(), NovelActivity.class);
                    WindowNovel.putExtra("url",mIlist.getItem(position).url);
                    WindowNovel.putExtra("index",mIlist.getItem(position).z);
                    getContext().startActivity(WindowNovel);
                }
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });
    }

    public void loadRecord(){
        mIlist.clear();
        List<DBNovel> items = DataSupport.findAll(DBNovel.class);
        for (DBNovel item : items) {
            ItemList c = new ItemList();
            c.name = item.getName();
            c.url = item.getUrl();
            c.img = item.getImg();
            c.Id = item.getId();
            c.z = item.getIndex();
            mIlist.addItem(c);
        }


        ItemList c = new ItemList();
        c.name = "添加小说";
        c.url = "add";
        c.imgId = R.drawable.ic_add_book;
        c.Id = -1;
        c.z = 0;
        mIlist.addItem(c);

        mIlist.setRefreshing(false);
    }

    public void JumpSearch(){
        if (WindowSearch == null) WindowSearch = new Intent(getContext(), SearchActivity.class);
        WindowSearch.putExtra("type","novel");
        getContext().startActivity(WindowSearch);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.search:
                JumpSearch();
                break;
        }

    }

    public void reload() {
            if(mIlist != null)loadRecord();
    }
}
