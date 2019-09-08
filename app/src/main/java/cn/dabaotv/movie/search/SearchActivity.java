package cn.dabaotv.movie.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cn.dabaotv.movie.DB.DBRecord;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.novel.NovelActivity;
import cn.dabaotv.movie.view.list.ItemList;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.m.cn.styles.StyleStatusBar;
import cn.m.cn.像素;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SearchActivity extends SwipeBackActivity implements QConfig,View.OnClickListener{
    private ImageView HeadBtSoEsc;
    private TextView HeadBtSo;

    private EditText HeadEtEdit;

    private LinearLayout SearchHome; // 首页 历史记录等！！！
    private IListView mListView;

    private String mKeyWord = "";
    private int mSearchIndex;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    mListView.setRefreshing(false);
                    SearchHome.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    break;
            }
        }
        public void llll(){}
    };

    private Intent WindowPlay;
    private Intent WindowNovel;


    private List<DBRecord> SearchHistoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        StyleStatusBar.setWhiteBar(this);

        Intent intent = getIntent();
        mType = intent.getStringExtra("type");

        loadview();
        loadHistory();

        TypeChange(); // 生成不同的首页
        HeadEtEdit.setFocusable(true);
        HeadEtEdit.setFocusableInTouchMode(true);
        HeadEtEdit.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void loadview(){
        HeadBtSoEsc = (ImageView)findViewById(R.id.HeadBtSoEsc);
        HeadBtSoEsc.setVisibility(View.GONE);
        HeadBtSo = (TextView)findViewById(R.id.HeadBtSo);
        HeadBtSo.setOnClickListener(this);
        HeadBtSoEsc.setOnClickListener(this);
        findViewById(R.id.HeadBtType).setOnClickListener(this);

        HeadEtEdit = (EditText)findViewById(R.id.HeadEtEdit);
        HeadEtEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0){
                    HeadBtSoEsc.setVisibility(View.VISIBLE);
                    HeadBtSo.setText("搜索");
                }else{
                    HeadBtSo.setText("取消");
                    HeadBtSoEsc.setVisibility(View.GONE);
                }
            }
        });
        HeadEtEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER){
                    if (event.getAction()==KeyEvent.ACTION_UP){
                        startSearch();
                    }
                    return true;
                }
                return false;
            }
            public void nullll(){};
        });


        SearchHome = (LinearLayout)findViewById(R.id.SearchHome);
        mListView = (IListView) findViewById(R.id.ilist);
        mListView.setIsSwipeRefresh(true);
        mListView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startSearch();
            }
        });
        mListView.setIsLoadMore(false);
        mListView.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList item) {
                if (mType.equals("video")){
                    Q.goPlayer(SearchActivity.this,item.url);
                }else{
                    if (WindowNovel == null) WindowNovel = new Intent(SearchActivity.this,NovelActivity.class);
                    WindowNovel.putExtra("url",item.url);
                    startActivity(WindowNovel);
                }
            }
            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });


        HistoryList = (XCFlowLayout) findViewById(R.id.HistoryList);
        findViewById(R.id.more).setOnClickListener(this);
    }

    public void startSearch(){
        // 隐藏输入法
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(HeadEtEdit.getWindowToken(),0);

        mListView.setRefreshing(true);
        SearchHome.setVisibility(View.GONE);
        mKeyWord = HeadEtEdit.getText().toString();
        if (mKeyWord.length() < 1) return;
        mListView.clear();

        if (mType.equals("video")){
            searchVod();
        }else {
            searchNovel();
        }
        // 判断是否已有记录  否则添加
        List<DBRecord> historys = DataSupport
                .where("name=? and type=?",mKeyWord,"historysearch_" + mType)
                .find(DBRecord.class);
        if (historys.size() < 1) {
            DBRecord record = new DBRecord();
            record.setName(mKeyWord);
            record.setType("historysearch_" + mType);
            record.save();
        }
    }
    public void searchVod(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = API+"?api=search&key=" + mKeyWord;//搜索影片
                String code = Ghttp.getHttp(url);
                Message msg = new Message();
                try{
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<ItemList>>() {}.getType();
                    List<ItemList> data = gson.fromJson(code,type);
                    if (data != null){
                        if (data.size() >= 30){
                            mSearchIndex = mSearchIndex + 1;
                        }else {
                            mSearchIndex = -1;
                        }
                    }else {
                        msg.arg1 = -1;
                        mSearchIndex = -1;
                    }

                    mListView.addList(data);
                }catch (Exception e){

                }
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }
    public void searchNovel(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://www.xxbiquge.com/search.php?keyword=" + mKeyWord;
                String code = Ghttp.getHttp(url);
                org.jsoup.nodes.Element element = Jsoup.parse(code).body();
                org.jsoup.nodes.Element e = element.select("div.result-list").first();
                Elements es = e.select("div.result-item");
                for (org.jsoup.nodes.Element e1 : es) {
                    ItemList item = new ItemList();
                    item.img = e1.select("img").attr("src");
                    item.url = e1.select("a").attr("href");
                    item.name = e1.select("h3").text();
                    item.msg = e1.select(".result-game-item-desc").text();
                    mListView.addItem(item);
                }
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private XCFlowLayout HistoryList;
    public void loadHistory(){
        HistoryList.removeAllViews();
        List<DBRecord> list = DataSupport
                .where("type=?","historysearch_" + mType)
                .order("id desc")
                .find(DBRecord.class);

        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 5;
        lp.rightMargin = 5;
        lp.topMargin = 5;
        lp.bottomMargin = 5;

        for (int i=0;i < list.size();i++){
            final String name = list.get(i).getName();
            View v =  LayoutInflater.from(this).inflate(R.layout.layout_control,null);
            LinearLayout vvv = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.layout_control,null);
            TextView tv = vvv.findViewById(R.id.SearchFlowStryle);
            tv.setText(name);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HeadEtEdit.setText(name);
                    startSearch();
                }
            });
            vvv.removeAllViews();
            HistoryList.addView(tv,lp);
        }

        if (HistoryList.getHeight() > 像素.dip2px(this,200)){
            HistoryList.setLayoutParams(new LinearLayout.LayoutParams(-1,像素.dip2px(this,200)));
            findViewById(R.id.more).setVisibility(View.VISIBLE);
        }else {
            HistoryList.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
        }
    }

    public void deleteList(){
        List<DBRecord> list = DataSupport
                .where("type=?","historysearch_" + mType)
                .order("id desc")
                .find(DBRecord.class);

        for (DBRecord record : list) {
            record.delete();
        }
        HistoryList.removeAllViews();
    }
    public void return1(){
        finish();
    }

    private String mType;
    public void TypeChange(){
        String t1;
        if (mType.equals("novel")){
            t1 = "小说";
            mListView.setLayout(R.layout.item_search2);
        }else {
            t1 = "影视";
            mListView.setLayout(R.layout.item_search);

        }
        TextView TypeTt = (TextView)findViewById(R.id.HeadTtType);
        TypeTt.setText(t1);
        loadHistory();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.HeadBtSo:
                if (HeadBtSo.getText().equals("取消")){
                    return1();
                }else {
                    startSearch();
                }
                break;
            case R.id.HeadBtSoEsc:
                HeadEtEdit.setText("");
                HeadBtSoEsc.setVisibility(View.GONE);
                HeadBtSo.setText("取消");
                break;
            case R.id.BtHistoryDelete:
                deleteList();
                break;
            case R.id.HeadBtType:
                if (mType.equals("video")){
                    mType = "novel";
                }else {
                    mType = "video";
                }
                TypeChange();
                InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                //同时再使用该方法之前，view需要获得焦点，可以通过requestFocus()方法来设定。
                HeadEtEdit.requestFocus();
                inputMethodManager.showSoftInput(HeadEtEdit, inputMethodManager.SHOW_FORCED);
                break;
            case R.id.more:
                findViewById(R.id.more).setVisibility(View.GONE);
                HistoryList.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
                break;
        }

    }
}
