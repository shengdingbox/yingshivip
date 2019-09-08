package cn.dabaotv.movie.novel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import cn.dabaotv.movie.MainActivity;
import cn.dabaotv.movie.MyApplication;
import cn.dabaotv.movie.DB.DBNovel;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.video.R;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.m.cn.系统;
import okhttp3.Cache;

public class NovelActivity extends AppCompatActivity implements View.OnClickListener {

    private DrawerLayout mDrawable;

    private RelativeLayout mReadHead;
    private ImageView iv_back;
    private LinearLayout mReadMenu;
    private TextView mReadTitle;


    // list
    private String mName;
    private RecyclerView mListView;
    private TextView mListName;
    private List<NovelListItem> mListData = new ArrayList<>();
    private NovelListAdapter mListAdapter;
    private int nowIndex = 0;

    private TextView 收藏标签;
    private ImageView 收藏图标;

    private String novelUrl;
    private String novelImg;
    private boolean ifRecord; //  是否已有记录

    private DBNovel mRecord; // 数据库


    private int TextSize;
    private List<NovelData> mReadData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel);

        if(MyApplication.user_state != Qe.会员_状态_正常){
            ToastUtils.showShort("体验时间已结束，请开通VIP!");
            finish();
            ((MainActivity)MyApplication.aty).mainView.vipOpen();
            return;
        }

        Intent intent = getIntent();
        novelUrl = intent.getStringExtra("url");

        系统.隐藏状态栏(this,true);
        loadview();

        // 加载记录再加载网络数据
        loadRecord();
        loadData();
    }

    // 菜单视图
    private TextView tt_menu_name;

    // 章节视图
    private SwipeRefreshLayout swipe;
    private NestedScrollView mConScroll;
    private LinearLayout mReadView;

    private void loadview(){
        mDrawable = findViewById(R.id.drawer);
        loadReadView();
        loadListView();
    }
    private void loadReadView(){
        mReadHead = findViewById(R.id.ReadHead);
        iv_back = findViewById(R.id.HeadReturn);
        iv_back.setOnClickListener(this);
        mReadMenu = findViewById(R.id.ReadMenu);
        findViewById(R.id.ReadBack).setOnClickListener(this);
        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (nowIndex == -1) nowIndex = 1;
                跳到章节(nowIndex);
            }
        });


        收藏标签 = (TextView)findViewById(R.id.menu_sc_text);
        收藏图标 = (ImageView)findViewById(R.id.menu_sc_img);

        // 功能键  上一个 下一个 目录 a+ a- 收藏
        findViewById(R.id.ReadFront).setOnClickListener(this);
        findViewById(R.id.ReadNext).setOnClickListener(this);
        findViewById(R.id.BtMenu).setOnClickListener(this);
        findViewById(R.id.BtA0).setOnClickListener(this);
        findViewById(R.id.BtA1).setOnClickListener(this);
        findViewById(R.id.BtSc).setOnClickListener(this);
        findViewById(R.id.BtNext).setOnClickListener(this);

        ConHideMenu(0); // 隐藏功能键

        mConScroll = (NestedScrollView)findViewById(R.id.ConScroll);
        mReadView = (LinearLayout)findViewById(R.id.ConView);
        mReadView.setOnClickListener(this);
        mReadView.setVisibility(View.GONE);
        swipe.setRefreshing(true);

        tt_menu_name = (TextView)findViewById(R.id.read_menu_name);
        tt_con_name = (TextView)findViewById(R.id.read_con_name);
        tt_con_content = (TextView)findViewById(R.id.read_con_content);
        sp = getSharedPreferences("novel.ini", Context.MODE_PRIVATE);
        TextSize = sp.getInt("texiSize",18);
        tt_con_content.setTextSize(TextSize);

    }
    SharedPreferences sp;
    private void loadListView(){
        findViewById(R.id.ListSort).setOnClickListener(this);
        mListName = findViewById(R.id.name);

        mListView = (RecyclerView)findViewById(R.id.recycler);
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListAdapter = new NovelListAdapter(this,R.layout.itemlayout_novellist,mListData,1);
        mListView.setAdapter(mListAdapter);
        mListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                跳到章节(position);
                mDrawable.closeDrawers();
            }
        });
    }
    private TextView tt_con_content;
    private TextView tt_con_name;
    public void ConHideMenu(int t) {
        boolean b = false;
        switch (t) {
            case 0:
                b = false;
                break;
            case 1:
                b = true;
                break;
            case 2:
                if (mReadMenu.getVisibility() == View.GONE) {
                    b = true;
                } else {
                    b = false;
                }
        }
        if (b == true){
            mReadMenu.setVisibility(View.VISIBLE);
            mReadHead.setVisibility(View.VISIBLE);
        }else {
            mReadMenu.setVisibility(View.GONE);
            mReadHead.setVisibility(View.GONE);
        }

    }
    public void 跳到章节(int index) {
        if (index>mListData.size()){
            Toast.makeText(this, "超出所含章节了", Toast.LENGTH_SHORT).show();
            return;
        }

        tt_menu_name.setText(tt_con_name.getText().toString());
        if (nowIndex >= 0) mListData.get(nowIndex).select = false;
        mListData.get(index).select = true;
        mListAdapter.notifyItemChanged(nowIndex);
        mListAdapter.notifyItemChanged(index);

        mRecord.setIndex(nowIndex);

        if (index >= mListData.size() || index < 0)  return;
        if (index == nowIndex + 1 && 倒序 == false){
            nowIndex = index;
            if (mReadData.size() >= 1){
                mReadData.remove(0);
            }

            mConScroll.setScrollY(0);
            if (mReadData.size() >= 1){
                tt_con_name.setText(mReadData.get(0).name);
                tt_con_content.setText(mReadData.get(0).content);
                tt_menu_name.setText(tt_con_name.getText());
                addConView(nowIndex + 1);
            }else {
                addConView(nowIndex);
            }
            return;
        }else {
            nowIndex = index;
            mReadData.clear();
            addConView(nowIndex); // 先添加主要章节
        }
    }
    public void addConView(final int i){
        int index = i;
        if (倒序 == true && index == nowIndex + 1){
            index = index - 1;
        }

        if (index > mListData.size()){
            if (倒序 == false && nowIndex == index){
                Toast.makeText(this, "已经是最后一章", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (index < 0 || index > mListData.size()) return;

        final int listIndex = index;
        if (index == nowIndex){
            mReadView.setVisibility(View.GONE);
            swipe.setRefreshing(true);
        }
        final String url = "https://www.xxbiquge.com" + mListData.get(listIndex).url;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(new Cache(getCacheDir(), 10240*1024),url);
                Message msg = new Message();
                org.jsoup.nodes.Element element = Jsoup.parse(code).body();
                String content = element.select("div#content").html();
                content = content.replaceAll("<br>\n<br>","\n");
                content = content.replaceAll("<br>","\n");
                content = content.replaceAll("&nbsp;"," ");
                NovelData d = new NovelData(mListData.get(listIndex).name,content);
                d.listId = listIndex;
                mReadData.add(d);
                msg.what = 2;
                msg.arg1 = listIndex;
                handler.sendMessage(msg);
            }
        }).start();
    }
    public void next(){
        if (倒序 == true){
            if (nowIndex == 0){
                Toast.makeText(this, "已经是最后一章", Toast.LENGTH_SHORT).show();
            }else {
                跳到章节(nowIndex - 1);
            }
        }else {
            if (nowIndex == mListData.size()-1){
                Toast.makeText(this, "已经是最后一章", Toast.LENGTH_SHORT).show();
            }else {
                跳到章节(nowIndex + 1);
            }
        }
    }
    public void front(){
        if (倒序 == false){
            if (nowIndex == 0){
                Toast.makeText(this, "已经是第一章", Toast.LENGTH_SHORT).show();
            }else {
                跳到章节(nowIndex - 1);
            }
        }else {
            if (nowIndex == mListData.size()-1){
                Toast.makeText(this, "已经是第一章", Toast.LENGTH_SHORT).show();
            }else {
                跳到章节(nowIndex + 1);
            }
        }
    }


    public void loadData(){
        final String url = novelUrl;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(url);
                Message msg = new Message();
                if (code.length() < 100){
                    msg.what = -2;
                    handler.sendMessage(msg);
                    return;
                }
                org.jsoup.nodes.Element element = Jsoup.parse(code).body();

                // 获取内容
                org.jsoup.nodes.Element con = element.select("div.box_con").first();
                // 如果没有记录 则创建一个变量记录
                if (mRecord == null){
                    String img = con.select("img").attr("src");
                    String name = con.select("h1").text();
                    mRecord = new DBNovel(img,name,novelUrl,0);
                }

                // 获取列表
                org.jsoup.nodes.Element e = element.select("div#list").first();
                mName = e.select("dt").text();

                Elements es = e.select("dd");
                for (org.jsoup.nodes.Element e1 : es) {
                    NovelListItem item = new NovelListItem();
                    item.url = e1.select("a").attr("href");
                    item.name = e1.select("a").text();
                    mListData.add(item);
                }
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    TextView t1 = (TextView)findViewById(R.id.HeadTitle);
                    t1.setText(mName);
                    mListAdapter.notifyDataSetChanged();
                    跳到章节(nowIndex);
                    break;
                case 2:
                    if (msg.arg1 == nowIndex){
                        tt_con_name.setText(mReadData.get(0).name);
                        tt_con_content.setText(mReadData.get(0).content);
                        tt_menu_name.setText(tt_con_name.getText());
                        mConScroll.setScrollY(0);
                        if (倒序 == true){
                            addConView(nowIndex - 1);
                        }else {
                            addConView(nowIndex + 1);
                        }
                    }
                    mReadView.setVisibility(View.VISIBLE);
                    break;
            }
            swipe.setRefreshing(false);
        }
        public void llll(){}
    };
    // 加载本地书架
    public void loadRecord(){
        List<DBNovel> items = DataSupport
                .where("url=?",novelUrl)
                .find(DBNovel.class);

        if (items.size() > 0){
            mRecord = items.get(0);
            ifRecord = true;
            nowIndex = mRecord.getIndex();
            收藏标签.setTextColor(0xffFF4081);
            收藏图标.setImageResource(R.drawable.home_ic_my_collect);
            收藏图标.clearColorFilter();
        }else {
            ifRecord = false;
            收藏图标.setImageResource(R.drawable.home_ic_collect);
            收藏图标.setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
            收藏标签.setTextColor(0xFFEEEEEE);
        }
    }
    public void 收藏(boolean b){
        if (b == true){
            mRecord.save();
            ifRecord = true;
            收藏标签.setTextColor(0xffFF4081);
            收藏图标.setImageResource(R.drawable.home_ic_my_collect);
            收藏图标.clearColorFilter();

        }else {
            mRecord.delete();
            ifRecord = false;
            收藏图标.setImageResource(R.drawable.home_ic_collect);
            收藏图标.setColorFilter(0xFFFFFFFF,PorterDuff.Mode.SRC_ATOP);
            收藏标签.setTextColor(0xFFFFFFFF);
        }
        ifRecord = b;
    }
    private boolean 倒序;
    public void onClick(View v){
        switch (v.getId()){
            case R.id.HeadReturn:
                if (ifRecord != true){
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("是否加入书架")//设置对话框的内容
                            //设置对话框的按钮
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ifRecord = true;
                                    收藏(true);
                                    dialog.dismiss();
                                    finish();
                                }
                            }).create();
                    dialog.show();
                }else {
                    finish();
                }
                break;
            case R.id.BtSc:
                if (ifRecord == true){
                    收藏(false);
                    Toast.makeText(this, "已从书架中移除", Toast.LENGTH_SHORT).show();
                }else {
                    收藏(true);
                    Toast.makeText(this, "已添加到书架", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ReadBack:
                ConHideMenu(2);
                break;
            case R.id.ConView:
                ConHideMenu(2);
                break;
            case R.id.ReadFront:
                front();
                break;
            case R.id.ReadNext:
                next();
                break;
            case R.id.BtMenu:
                ConHideMenu(0);
                mDrawable.openDrawer(Gravity.START);
                break;
            case R.id.BtA0:
                TextSize = TextSize - 1;
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("textSize",TextSize);
                editor.commit();
                tt_con_content.setTextSize(TextSize);
                break;
            case R.id.BtA1:
                TextSize = TextSize + 1;
                sp.edit().putInt("textSize",TextSize).commit();
                tt_con_content.setTextSize(TextSize);
                break;
            case R.id.BtNext:
                next();
                break;
            case R.id.ListSort:
                List<NovelListItem> itemcs = new ArrayList<>();
                if (倒序 == true){
                    倒序 = false;
                }else {
                    倒序 = true;
                }
                for (NovelListItem mListDatum : mListData) {
                    itemcs.add(0,mListDatum);
                }
                mListData.clear();
                mListData.addAll(itemcs);
                mListAdapter.notifyDataSetChanged();
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (ifRecord != true){
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("是否加入书架")//设置对话框的内容
                        //设置对话框的按钮
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ifRecord = true;
                                收藏(true);
                                dialog.dismiss();
                                finish();
                            }
                        }).create();
                dialog.show();
            }
            //return false;
        }else {
            //return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onPause(){
        super.onPause();
        if (ifRecord == true){
            mRecord.setIndex(nowIndex);
            mRecord.save();
        }
    }

}
