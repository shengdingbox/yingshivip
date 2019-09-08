package cn.dabaotv.movie.view.list;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import cn.dabaotv.video.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by H19 on 2018/6/15 0015.
 */

public class IListBookView extends LinearLayout {
    public ItemAdapter mAdapter;
    public RecyclerView mRecycler;
    public SwipeRefreshLayout mSwipe;
    public int spanCount = 2;
    private List<ItemList> mList = new ArrayList<>();
    public IListBookView(Context context){
        super(context);
    }
    public IListBookView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public IListBookView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isLoadMore = true;
    public boolean isEmptyBack = true;
    private int mStyle;

    public void setLayout(int ResId,int style){
        if (mRecycler == null) loadView();
        if (spanCount < 2) spanCount = 2;
        mStyle = style;
        if (style == 2) {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
            mRecycler.setLayoutManager(layoutManager);
            mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    //layoutManager.invalidateSpanAssignments(); //防止第一行到顶部有空白区域
                }
            });

        }else if (style == 1){
            final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
            mRecycler.setLayoutManager(layoutManager);
        }else {
            mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        mRecycler.setPadding(0, 0, 0, 0);

        // 取消动画 防止图片闪烁
        RecyclerView.ItemAnimator animator = mRecycler.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        setLayout(ResId);

    }
    public void setLayout(int ResId){
        if (mRecycler == null) loadView();
        mAdapter = new ItemAdapter(ResId,mList);
        mAdapter.removeAllHeaderView();

        /*mEmptyView = LayoutInflater.from(getContext()).inflate(R.layout.listback_loading, null);
        mAdapter.setEmptyView(mEmptyView);*/

        mRecycler.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (mListener != null) mListener.onClick(view,position,mList.get(position));
            }
        });
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (mListener != null) mListener.onClick(view,position,mList.get(position));
            }
        });

        if (isLoadMore){
            mAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
                @Override
                public void onLoadMoreRequested() {
                    if (mListener != null) mListener.startLoadMore(mAdapter);
                }
            },mRecycler);
        }
    }
    private View mEmptyView;

    public void setEmptyCon(String t){
        if (mEmptyView != null)
        ((TextView)mEmptyView.findViewById(R.id.msg)).setText(t);
    }
    // 加载成功
    public void loadMoreComplete(){
        mRecycler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.loadMoreComplete();
            }
        });
    }
    public void loadMoreEnd(final boolean b){
        mRecycler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.loadMoreEnd();
            }
        });
    }
    public void setOverScrollMode(int mode){
        if (mRecycler!=null) mRecycler.setOverScrollMode(mode);
    }
    private void loadView(){
        if (isAbleSwipeRefresh == false){
            mRecycler = new RecyclerView(getContext());

            mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            this.addView(mRecycler,new LayoutParams(-1,-1));
            return;
        }

        mSwipe = new SwipeRefreshLayout(getContext());
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mRefreshListener == null || isAbleSwipeRefresh == false){
                    mSwipe.setRefreshing(false);
                }else {
                    mRefreshListener.onRefresh();
                }
            }
        });
        mRecycler = new RecyclerView(getContext());
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mSwipe.addView(mRecycler,new LayoutParams(-1,-1));
        this.addView(mSwipe,new LayoutParams(-1,-1));
    }
    public boolean isAbleSwipeRefresh = false;
    public void setIsSwipeRefresh(final boolean b){
        isAbleSwipeRefresh = b;
        if (mSwipe == null) return;
        mSwipe.post(new Runnable() {
            @Override
            public void run() {
                mSwipe.setRefreshing(b);
            }
        });

    }
    public void setRefreshing(boolean b){
        if (mSwipe != null) mSwipe.setRefreshing(b);


    }
    public void setIsLoadMore(boolean b){
        isLoadMore = b;
        if (mAdapter != null){
            mAdapter.setEnableLoadMore(b);
        }

    }

    public int size(){return mList.size();}
    // 清空
    public void clear(){
        mList.clear();
        mAdapter.notifyDataSetChanged();
    }

    private itemOnClickListener mListener;
    public void setOnItemLongClickListener(BaseQuickAdapter.OnItemLongClickListener listener){
        mAdapter.setOnItemLongClickListener(listener);
    }
    public void setItemListener(itemOnClickListener itemListener){
        mListener = itemListener;
    }

    public void setStyle(int style) {
        mAdapter.setListStyle(style);
    }

    public void select(int id,boolean b) {
        if (id < mList.size()){
            mList.get(id).select = b;
            mAdapter.notifyItemChanged(id);
        }
    }

    // 多选模式  默认0，非，1是但无进入多选模式， 2 是并接入编辑模式
    public void setMultiSelectionMode(int editState) {
        mAdapter.isMultiSelection = editState;
        mAdapter.notifyDataSetChanged();
    }

    public interface itemOnClickListener{
        void onClick(View v, int position, ItemList itemList);
        void startLoadMore(ItemAdapter adapter);
    }
    public void setListNestedScrollingEnabled(boolean b){
        if (mRecycler != null) mRecycler.setNestedScrollingEnabled(b);
    }

    // 下拉刷新
    private SwipeRefreshLayout.OnRefreshListener mRefreshListener;
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener onRefreshListener){
        mRefreshListener = onRefreshListener;
    }
    public void setList(List<ItemList> list){
        mList.clear();
        mList.addAll(list);
        mRecycler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }
    public void addList(List<ItemList> list){
        mList.addAll(list);

        mRecycler.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipe != null) mSwipe.setRefreshing(true);
                mAdapter.notifyDataSetChanged();
            }
        });
    }


    public void addItem(ItemList item){
        mList.add(item);
        mRecycler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemChanged(mList.size() - 1);
            }
        });

    }
    public void addItem2(ItemList item){
        mList.add(item);
    }
    public void Changed(){
        mRecycler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }
    public void deleteItem(int i){
        mList.remove(i);
        mAdapter.notifyDataSetChanged();
    }
    public String getName(int i){
        if (mList.size() < 1) return null;
        if (mList.size() <= i) return null;
        return mList.get(i).name;
    }

    public void refresh(){
        mAdapter.notifyDataSetChanged();
    }
    // 获取列表
    public ItemList getItem(int i){
        if (i > -1 && i < mList.size()){
            return mList.get(i);
        }else {
            return null;
        }
    }

    public void refreshItem(int itemPostion){
        mAdapter.notifyItemChanged(itemPostion);
    }

    public List<ItemList> getList(){
        return mList;
    }

    @Override
    public void setGravity(int gravity) {
        super.setGravity(Gravity.CENTER);
    }

    public void reload(){
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }
}
