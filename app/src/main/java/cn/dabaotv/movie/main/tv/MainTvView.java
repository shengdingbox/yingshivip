package cn.dabaotv.movie.main.tv;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.LinearLayout;

import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.movie.main.list.DLBlock;
import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.movie.view.list.ItemList;
import cn.dabaotv.movie.Conl.ListActivity;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.video.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Created by H19 on 2018/5/5 0005.
 */

public class MainTvView extends LinearLayout implements QConfig {
    public View mView;
    public IListView mListView;
    public List<DLBlock> mDList;

    private Intent WindowFullScreen;

    public MainTvView(Context context){
        super(context);
        ininView();

    }




    public void ininView(){
        mView = View.inflate(getContext(), R.layout.main_home_tv,this);
        mListView = mView.findViewById(R.id.ilist);
        mListView.setIsSwipeRefresh(false);
        mListView.setIsLoadMore(false);
        mListView.setLayout(R.layout.main_list_tv_item);
        mListView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ininData();
            }
        });
        mListView.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList itemList) {
                Intent intent = new Intent(getContext(), ListActivity.class);
                intent.putExtra("type","dszblist");
                intent.putExtra("name", itemList.name);
                intent.putExtra("url",itemList.url);
                getContext().startActivity(intent);
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });
        ininData();
    }


    public void ininData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://wx168.ml0421.com/app/index.php?i=32&c=entry&do=index&m=ruite_iptv";
                String code = Ghttp.getHttpx(url);
                try{
                    mListView.clear();
                    Document doc = Jsoup.parse(code);
                    Elements es = doc.select("div.mui-card ul.mui-table-view li");
                    for (int i = 0; i < es.size(); i++) {
                        if (i == 0) continue;
                        ItemList item = new ItemList();
                        item.name = es.get(i).text();
                        item.url = es.get(i).select("a").attr("href");
                        mListView.addItem(item);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void ininConl(){

    }
    private IListView getBalck(){
        IListView iv = new IListView(getContext());
        iv.setIsLoadMore(false);
        iv.setIsSwipeRefresh(false);
        iv.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList item) {
                Q.goPlayer(getContext(),item.url,Integer.toString( Qe.PLAYTYPE_普通单条));
            }

            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });

        iv.setListNestedScrollingEnabled(false);
        return iv;
    }



    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    private Intent WindowPlay;

}
