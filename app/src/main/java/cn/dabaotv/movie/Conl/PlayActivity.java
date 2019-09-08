package cn.dabaotv.movie.Conl;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import cn.dabaotv.movie.Conl.play.MDownlistHelper;
import cn.dabaotv.movie.DB.DBRecord;
import cn.dabaotv.movie.Function.Bion;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Function.Jiexi;
import cn.dabaotv.movie.Function.Push;
import cn.dabaotv.movie.MainActivity;
import cn.dabaotv.movie.MyApplication;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.movie.main.list.DLBlock;
import cn.dabaotv.movie.screen.SearchScreenDialog;
import cn.dabaotv.movie.utils.BaseActivity;
import cn.dabaotv.movie.utils.JsonUtils;
import cn.dabaotv.movie.utils.StringUtils;
import cn.dabaotv.movie.view.DiaList;
import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.movie.view.list.ItemList;
import com.google.gson.Gson;
import cn.dabaotv.movie.Conl.play.MPlaylistHelper;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.screen.ScreenActivity;
import com.qingfeng.clinglibrary.entity.ClingDevice;
import com.qingfeng.clinglibrary.entity.IDevice;
import com.qingfeng.clinglibrary.listener.BrowseRegistryListener;
import com.qingfeng.clinglibrary.listener.DeviceListChangedListener;
import com.qingfeng.clinglibrary.service.ClingUpnpService;
import com.qingfeng.clinglibrary.service.manager.ClingManager;
import com.qingfeng.clinglibrary.service.manager.DeviceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.m.bdplayer.MPlayer;
import cn.m.bdplayer.player_in;
import cn.m.cn.像素;
import cn.m.cn.文本;
import cn.m.cn.系统;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class PlayActivity extends BaseActivity implements QConfig, View.OnClickListener {

    private Bion mBion;
    private String mvUrl; // 当前播放的地址


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        getWindow().setBackgroundDrawableResource(R.color.white);

        if (MyApplication.user_state != Qe.会员_状态_正常) {
            ToastUtils.showShort("体验时间已结束，请开通VIP!");
            finish();
            ((MainActivity) MyApplication.aty).mainView.vipOpen();
            return;
        }

        mBion = Q.getBion(getIntent());
        Intent intent = getIntent();
        if (intent.getStringExtra("data") != null && !intent.getStringExtra("data").equals("")) {
            mVideoItem = JsonUtils.jsonStringToBean(intent.getStringExtra("data"), ItemList.class);
        }

        ininPlayerView();
        ininMplaylistHelper();// 跳窗的播放列表
        ininData();

    }

    private View mConlView;
    private LinearLayout mConlFrame;
    private TabLayout mConlTabPlaylist;
    private ItemList mVideoItem;
    private ArrayList<ArrayList<ItemList>> mPlayList = new ArrayList<>(); // 播放列表
    private ArrayList<ItemList> mPlayCodeList = new ArrayList<>(); // 播放源列表
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 2: // 得到视频

                    break;
            }
            return false;
        }

        public void iii() {
        }
    });

    private FrameLayout mvFrame;
    private MPlayer mPlayer;
    private MPlaylistHelper mPlaylistHp;
    private MDownlistHelper mDownlistHp;
    private Push mPush;
    private TextView tt_codename;

    // 刷新页面数据
    private void ininData() {
        if (mVideoItem == null) {
            final String url = API + "?api=vod&id=" + mBion.id; //获取某个影片信息
            io.reactivex.Observable.create(new ObservableOnSubscribe<ItemList>() {
                @Override
                public void subscribe(ObservableEmitter<ItemList> emitter) throws Exception {
                    final String code = Ghttp.getHttp(url);
                    try {
                        mVideoItem = new Gson().fromJson(code, ItemList.class);
                        mVideoItem.id = mBion.id;
                    } catch (Exception e) {
                        Log.i("ex-play", e.toString());
                        emitter.onError(e);
                    }
                    emitter.onNext(mVideoItem);
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                    .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                    .subscribe(new Consumer<ItemList>() {
                        @Override
                        public void accept(ItemList itemList) throws Exception {
                            if (mVideoItem == null) {
                                Q.echo(PlayActivity.this, "加载数据失败");
                                return;
                            }
                            ininView();
                        }
                    });
        } else {
            mVideoItem.id = mBion.id;
            ininView();
        }
    }

    @SuppressLint("SetTextI18n")
    private void ininView() {
        mConlView = View.inflate(this, R.layout.conl_play_conl, null);
        tt_codename = mConlView.findViewById(R.id.tt_codename);
        mConlFrame = (LinearLayout) findViewById(R.id.conlframe);
        mConlFrame.addView(mConlView, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        ((TextView) mConlView.findViewById(R.id.cTtName)).setText(StringUtils.replaceBlank(mVideoItem.name)); // 标题
        ((TextView) mConlView.findViewById(R.id.cTtMsg)).setText(mVideoItem.msg); // 副标题
        ((TextView) mConlView.findViewById(R.id.cTtScore)).setText(mVideoItem.score); // 评分
        ((TextView) mConlView.findViewById(R.id.cTtSort)).setText(mVideoItem.type); // 分类
        if (mVideoItem.info != null) {
            mVideoItem.info = mVideoItem.info.replace("<br>", "\n");
            ((TextView) mConlView.findViewById(R.id.cTtInfo)).setText(mVideoItem.info); // 简介
        }


        // 按钮
        mConlView.findViewById(R.id.cBtSc).setOnClickListener(this); // 收藏按钮
        mConlView.findViewById(R.id.cBtXz).setOnClickListener(this); //  下载按钮
        mConlView.findViewById(R.id.cBtCode).setOnClickListener(this); // 播放源 图标
        mConlView.findViewById(R.id.cBtInfo).setOnClickListener(this);  // 右上 简介按钮（展示收缩简介）
        mConlView.findViewById(R.id.bt_play_code).setOnClickListener(this); // 选择播放源
        mConlView.findViewById(R.id.iv_select_src).setOnClickListener(this);
        mConlView.findViewById(R.id.cBtDrama).setOnClickListener(this);
        mConlTabPlaylist = (TabLayout) findViewById(R.id.cTabPlayList);

        getRecord(Qe.RECORDTYPE_收藏); // 读取记录
        getRecord(Qe.RECORDTYPE_历史); // 读取记录
        ininPlayList(); // 加载播放列表

        if (mPlayList.size() > 0 && mPlayList.get(cutPlayCodeIndex).size() > 1) {
            ((TextView) mConlView.findViewById(R.id.ttDramaInfo)).setText("更新至 " + (mPlayList.get(cutPlayCodeIndex).size()) + "集"); // 分类
        } else {
            ((TextView) mConlView.findViewById(R.id.ttDramaInfo)).setText(""); // 分类
        }

        startFromRecord();
        ///////////////
        LinearLayout dbl_view = mConlView.findViewById(R.id.dbl_view);


        mConlTabPlaylist.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectPlayItem(tab.getPosition());
                mPlaylistHp.selectItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
//        List<DLBlock> list =  JsonUtils.jsonStringToList(ACache.get(getApplicationContext()).getAsString(DLBlock_key),DLBlock.class);
//        if(list!= null){
//            for (DLBlock dlBlock : list) {
//                View v = getBlock(dlBlock);
//                if (v != null) dbl_view.addView(v,new LinearLayout.LayoutParams(-1,100));
//            }
//        }

    }

    private void ininPlayerView() {
        mPlayer = new MPlayer(this);
        mPlayer.setActivity(this);
        mPlayer.设置全屏(false);
        mPlayer.hide全屏按钮(false);
        mPlayer.setClickListener(new MPlayer.PlayerClickListener() {
            @Override
            public void click(int id) {
                switch (id) {
                    case player_in.PLAYER_BT_DRAME:
                        mPlayer.显示菜单(false);
                        mPlaylistHp.shop(PlayActivity.this, isFull, 像素.dip2px(PlayActivity.this, 320), -1);
                        break;
                    case player_in.PLAYER_BT_LINE:
                        mPlayer.显示菜单(false);
                        //showPlayList(1);
                        break;
                    case player_in.PLAYER_BT_NEXT:
                        startNext();
                        break;
                    case player_in.PLAYER_BT_RETURN:
                        onReturn();
                        break;
                    case player_in.PLAYER_BT_PUSH:
                        mPlayer.显示菜单(false);
                        startSerachScreen();
//                        if (mPush == null) {
//                            mPush = new Push().push(PlayActivity.this);
//                            mPush.setListener(new Push.onListener() {
//                                @Override
//                                public void onSerechEnd(int state, String msg) {
//                                    if (state == 0) {
//                                        mPush.showList();
//                                    } else if (state == -1) {
//                                        echo(msg);
//                                    }
//                                }
//                            });
//                        }
//                        mPush.start(mvUr
                        break;
                    case player_in.PLAYER_BT_JUMP:
                        if (mvUrl == null || mvUrl.length() < 10) {
                            Q.echo(PlayActivity.this, "未发现相应软件");
                            return;
                        }
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String type = "video/*";
                        Uri uri = Uri.parse(mvUrl);
                        intent.setDataAndType(uri, type);
                        startActivity(intent);
                        break;
                    case player_in.PLAYER_BT_FULLSCREEN:
                        if (isFull) {
                            setFull(false);
                        } else {
                            setFull(true);
                        }
                        break;
                }
            }

            @Override
            public void 播放完毕() {
                if (cutPlayIndex + 1 < mPlayList.get(cutPlayCodeIndex).size()) {
                    startNext();
                }
            }
        });
        mPlayer.setOnSeekBarStopTrackingTouchListener(new MPlayer.OnSeekBarStopTrackingTouchListener() {
            @Override
            public void onSeekBarStopTrackingTouch(SeekBar seekBar) {
                // TODO: 2018/12/6 进度条拖动时候状态更新
                mPlayer.隐藏菜单();
                mPlayer.设置进度(seekBar.getProgress());
                mPlayer.putMsg("");
            }
        });
        mvFrame = findViewById(R.id.playdiv);
        mvFrame.addView(mPlayer, new FrameLayout.LayoutParams(-1, -1));
    }

    private void ininMplaylistHelper() {
        // 加载弹窗样式的播放列表
        mPlaylistHp = new MPlaylistHelper().inin(this, new MPlaylistHelper.OnListener() {
            @Override
            public void onClick(final int position) {
                // TODO: 2018/12/6  选中当前选集
                mPlaylistHp.hide();
                selectPlayItem(position);
                mConlTabPlaylist.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mConlTabPlaylist.getTabAt(position).select();
                    }
                }, 100);
            }
        });

        mDownlistHp = new MDownlistHelper().inin(this, mVideoItem, mBion.id);
    }

    // ------------- 开始播放选项 ---------------------
    // 开始下集
    private void startNext() {
        // TODO: 2018/12/6  开始播放下一集
        echo("开始播放下一集");
        selectPlayItem(cutPlayIndex + 1);
        mConlTabPlaylist.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mConlTabPlaylist.getTabAt(cutPlayIndex) != null)
                    mConlTabPlaylist.getTabAt(cutPlayIndex).select();
            }
        }, 100);
        mPlaylistHp.selectItem(cutPlayIndex); // 选中后cutintex 就会改变
    }

    private void startFromRecord() {
        echo("从历史记录开始");
        selectPlayItem(cutPlayIndex);
        mConlTabPlaylist.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mConlTabPlaylist.getTabAt(cutPlayIndex) != null)
                    mConlTabPlaylist.getTabAt(cutPlayIndex).select();
            }
        }, 100);
        mPlaylistHp.selectItem(cutPlayIndex); // 选中后cutintex 就会改变
    }

    private void selectPlayItem(int itemId) {
        if (itemId >= mPlayList.get(cutPlayCodeIndex).size()) {
            echo("已经是最后一集");
            return;
            /*itemId = mPlayList.get(cutPlayCodeIndex).size() - 1;
            if (itemId < 0) return;*/
        }

        cutPlayIndex = itemId;
        ItemList item = mPlayList.get(cutPlayCodeIndex).get(itemId);

        // 判断地址是否直链接
        mPlayer.停止();

        if (item.url != null) {
            if (item.url.endsWith(".m3u8") || item.url.endsWith(".mp4") ||
                    item.url.endsWith(".flv") || item.url.endsWith("avi")) {
                startPlay(mVideoItem.name + " " + item.name, item.url);
                return;
            }
        }

        mPlayer.准备(mVideoItem.name + " " + item.name, "正在获取视频信息");
        if (item.t == null || item.t.size() < 1) {
            paresItem(item);
        } else {
            startPlay(item);
        }
    }

    private int cutState = 0;
    private ItemList cutParseItem; // 当前解析中的项目
    private Jiexi mJiexi;
    private int iddd;

    private void paresItem(final ItemList item) {
        cutParseItem = item;

        if (item.z == PlayState.解析中) return;
        if (cutState == PlayState.解析中 && mJiexi != null) mJiexi.stop();

        cutState = PlayState.解析中; //将状态改成解析中
        item.z = PlayState.解析中;

        if (item.t == null) item.t = new ArrayList<>();
        item.t.clear(); // 清空原有的播放列表

        if (mJiexi == null) mJiexi = new Jiexi().inin(this, new Jiexi.OnListener() {
            @Override
            public void ent(Jiexi t, int errId, final String msg, String type) {
                if (errId == 0) {
                    for (String s : item.t) {
                        if (s.equals(msg)) return;
                    }
                    ent(msg);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            echo(msg);
                        }
                    });
                }
            }

            public void ent(String url) {
                Q.log("enturl-", url);
                cutParseItem.t.add(url);
                startPlay(cutParseItem);
                /*handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (cutState == PlayState.解析中){

                        }
                    }
                });*/
            }
        });
        mJiexi.start(item.url);
    }

    private void startPlay(final ItemList item) {
        if (item.t.size() < 1) {
            echo("解析失败");
            return;
        }

        String name = mVideoItem.name + " " + item.name;
        if (item.t.size() == 1) {
            startPlay(name, item.t.get(0));
        } else {
            startPlay(name, item.t.get(item.t.size() - 1));
        }
    }

    private void startPlay(String name, String url) {
        cutState = PlayState.等待播放;
        Log.e("PlayActivity", "PlayActivity:" + url);
        mPlayer.准备(name, "拼命加载中...");
        mPlayer.播放(url);
    }

    private void echo(String t) {

    }

    private int cutPlayIndex; // 当前播放项目索引
    private int cutPlayCodeIndex; // 当前播放源 索引

    // 加载播放列表
    public void ininPlayList() {
        if (mVideoItem == null || mVideoItem.playlist == null) return;
        mPlayList.clear();


        String name = "";
        String link = "";

        List<ItemList> playLists = new ArrayList<>();
        // 1$url#2$url$$$1$url#2$url
        String[] a1 = mVideoItem.playlist.split("[$][$][$]");

        for (int i = 0; i < a1.length; i++) {
            // 1$xxxx#2$xxxx#.....
            String[] a2 = a1[i].split("\r");
            mPlayList.add(new ArrayList<ItemList>());
            playLists.clear();
            for (int i2 = 0; i2 < a2.length; i2++) {
                name = 文本.get文本左边(a2[i2], "$");
                link = 文本.get文本右边(a2[i2], "$");
                mPlayList.get(i).add(new ItemList(name, link));
            }
        }

        mPlayCodeList.clear();
        String[] a3 = mVideoItem.playcode.split("[$][$][$]");
        List<ItemList> code = Q.getCode();
        for (int i = 0; i < a3.length; i++) {
            ItemList ic = new ItemList();
            for (int i2 = 0; i2 < code.size(); i2++) {
                if(a3[i].contains(code.get(i2).msg)) {
                    ic = code.get(i2);
                    break;
                }
            }
            if (ic.name == null) {
                if (a3[i].length() > 2) {
                    ic.name = a3[i];
                    ic.msg = a3[i];
                    ic.imgId = R.mipmap.ic_logo_video;
                }
            }
            if (ic.name != null) mPlayCodeList.add(ic);
        }
        if (mPlayList.size() < 1) return;

        //tt_codename.setText("切换播放源："+mPlayCodeList.get(cutPlayCodeIndex).msg);
        tt_codename.setText("切换播放源："+mPlayCodeList.get(cutPlayCodeIndex).msg);
        setPlayCode(cutPlayCodeIndex);//从播放历史中选择
    }

    // 切换播放源
    private void setPlayCode(int index) {
        cutPlayCodeIndex = index;
        if (mPlayCodeList.size() < 1) return;

        // 刷新Conl中的列表
        mConlTabPlaylist.removeAllTabs();
        int curcur = cutPlayIndex;
        for (ItemList itemList : mPlayList.get(cutPlayCodeIndex)) {
            mConlTabPlaylist.addTab(mConlTabPlaylist.newTab().setText(StringUtils.replaceBlank(itemList.name)));
        }

        // 刷新弹窗视图中的播放列表..
        mPlaylistHp.setList(mPlayList.get(cutPlayCodeIndex));
        cutPlayIndex = curcur;
        mDownlistHp.setList(mPlayList.get(cutPlayCodeIndex), mVideoItem);

    }

    // 打开选择播放源窗口
    private void selectCode() {
        List<ItemList> str = new ArrayList<>();
        for (ItemList it : mPlayCodeList) {
            ItemList item = new ItemList(it.name, it.imgId);
            str.add(item);
        }
        DiaList.弹出(this, R.layout.conl_play_view_selectcodelist, "选择媒体", "取消", str, new DiaList.itenOnClickListener() {
            @Override
            public void onClick(Dialog dialog, ItemList item, int position) {
                if (position > 0) {
                    setPlayCode(position);
                }
            }
        });
    }

    // 设置全屏
    private boolean isFull;

    private void setFull(boolean b) {
        isFull = b;
        mPlayer.设置全屏(isFull);

        ViewGroup.LayoutParams lp;
        lp = mvFrame.getLayoutParams();
        lp.width = -1;
        if (b) {
            if (mPlayList.size() > 0 && mPlayList.get(cutPlayCodeIndex).size() > 1) {
                mPlayer.显示选集(true);
            }

            findViewById(R.id.conlframe).setVisibility(View.GONE);
            系统.设置屏幕方向(this, 3, false);
            系统.hideNavigationBar(getWindow());
            lp.height = MATCH_PARENT;
            lp.width = MATCH_PARENT;
        } else {
            mPlayer.显示选集(false);
            系统.设置屏幕方向(this, 1, true);
            findViewById(R.id.conlframe).setVisibility(View.VISIBLE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            lp.height = 像素.dip2px(PlayActivity.this, 230);
            lp.width = MATCH_PARENT;
        }
        mvFrame.setLayoutParams(lp);
    }

    private DBRecord mRecord; // 收藏
    private DBRecord mRecord2; // 历史

    private void Record(boolean b) {
        if (b) {
            if (mRecord == null) {
                mRecord = new DBRecord();
            }
            mRecord.setUrl(mBion.id);
            mRecord.setName(mVideoItem.name);
            mRecord.setDrama(cutPlayIndex);
            mRecord.setImg(mVideoItem.img);
            mRecord.setChangetime(System.currentTimeMillis() / 1000);
            mRecord.setMsg("播放至" + cutPlayIndex + "集");
            mRecord.setType(Qe.RECORDTYPE_收藏);
            mRecord.setPlayCode(cutPlayCodeIndex);
            mRecord.save();
            ((ImageView) mConlView.findViewById(R.id.cBtSc)).setImageResource(R.drawable.conl_ic_play_conl_sc1);
            Q.echo(this, "添加收藏");
        } else {
            if (mRecord != null) {
                mRecord.delete();
            }
            mRecord = null;
            ((ImageView) mConlView.findViewById(R.id.cBtSc)).setImageResource(R.drawable.conl_ic_play_conl_sc);
        }
    }

    private void Record2() {
        if (mRecord2 == null) {
            mRecord2 = new DBRecord();
        }
        mRecord2.setUrl(mBion.id);
        mRecord2.setName(mVideoItem.name);
        mRecord2.setDrama(cutPlayIndex);
        mRecord2.setPlayindex(cutPlayIndex);
        mRecord2.setImg(mVideoItem.img);
        mRecord2.setChangetime(System.currentTimeMillis() / 1000);
        mRecord2.setType(Qe.RECORDTYPE_历史);
        mRecord2.setMsg("播放至" + cutPlayIndex + "集");
        mRecord2.setPlayCode(cutPlayCodeIndex);
        mRecord2.save();
    }

    private void getRecord(String type) {
        List<DBRecord> records = DataSupport
                .where("url=? and type=?", mBion.id, type)
                .find(DBRecord.class);
        if (type.equals(Qe.RECORDTYPE_收藏)) {
            if (records.size() > 0) {
                mRecord = records.get(0);
                cutPlayIndex = mRecord.getPlayindex();
                cutPlayCodeIndex = mRecord.getPlayCode();
                ((ImageView) mConlView.findViewById(R.id.cBtSc)).setImageResource(R.drawable.conl_ic_play_conl_sc1);
            } else {
                ((ImageView) mConlView.findViewById(R.id.cBtSc)).setImageResource(R.drawable.conl_ic_play_conl_sc);
            }
        } else if (type.equals(Qe.RECORDTYPE_历史)) {

            if (records.size() > 0) {
                mRecord2 = records.get(0);
                cutPlayIndex = mRecord2.getPlayindex();
                cutPlayCodeIndex = mRecord2.getPlayCode();
            }
        }

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cBtSc:
                if (mRecord != null) {
                    Record(false);
                } else {
                    Record(true);
                }
                break;
            case R.id.cBtXz:
                mDownlistHp.show(this,mConlFrame.getHeight());

                break;
            case R.id.cBtCode:
                selectCode();
                break;
            case R.id.cBtInfo:
                if (findViewById(R.id.cTtInfo).getVisibility() == View.GONE) {
                    findViewById(R.id.cTtInfo).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.cTtInfo).setVisibility(View.GONE);
                }
                break;
            case R.id.iv_select_src:
                selectCode();      //
                break;
            case R.id.cBtDrama:
                if (isFull) {
                    mPlaylistHp.shop(this, isFull, MATCH_PARENT, MATCH_PARENT);
                } else {
                    mPlaylistHp.shop(this, isFull, -1, mConlFrame.getHeight());
                }
                break;
        }
    }

    // 电视直播
    private void ininTVZB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://wx168.ml0421.com/app" + 文本.get文本右边(mBion.url, mBion.url.length() - 1);
                String code = Ghttp.getHttp(url);
                try {
                    Document doc = Jsoup.parse(code);
                    final String sss = doc.select("video").attr("src");
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPlayer.播放(sss);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //
    private View getBlock(final DLBlock item) {
        if (item != null) {
            View mv = View.inflate(getApplicationContext(), R.layout.main_home_conl_block, null);
            ((TextView) mv.findViewById(R.id.name)).setText(item.name);

            // 加载更多点击监听
            mv.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemList itemList = new ItemList(item.name);
                    itemList.msg = item.msg;
                    itemList.id = Integer.toString(item.id);
                    //if (onSubsortListener!=null)onSubsortListener.onClick(itemList,"全部");
                }
            });
            mv.findViewById(R.id.more2).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemList itemList = new ItemList(item.name);
                    itemList.msg = item.msg;
                    itemList.id = Integer.toString(item.id);
                    //if (onSubsortListener!=null)onSubsortListener.onClick(itemList,"全部");
                }
            });

            // 数据列表
            IListView ilist = mv.findViewById(R.id.ilist);
            ilist.setIsSwipeRefresh(false);
            ilist.setIsLoadMore(false);
            ilist.spanCount = 3;
            ilist.setLayout(R.layout.list_video, 1);
            ilist.setItemListener(new IListView.itemOnClickListener() {
                @Override
                public void onClick(View v, int position, ItemList itemList) {
                    Q.goPlayer(getApplicationContext(), itemList.id);
                }

                @Override
                public void startLoadMore(ItemAdapter adapter) {

                }
            });
            ilist.setList(item.data);
            if (item.data.size() > 0) {
                return mv;
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

    // 项目事件
    public boolean onReturn() {
        if (isFull) {
            setFull(false);
            return false;
        }
        finish();
        return true;
    }// 返回

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onReturn();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlayer.退出();

    }

    @Override
    public void onResume() {
        super.onResume();
//        if (isFull) 系统.隐藏虚拟按键(this,true);
        mPlayer.进入();
        if (!isFull)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSearchScreen();
        if (mPlayer != null) mPlayer.停止();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (mVideoItem != null) {
            Record2();
        }
        if (mRecord != null) Record(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        window.setAttributes(params);
    }

    @Override
    protected boolean setFitsSystemWindows() {
        return false;
    }

    //-------------用户监听-----------------
    private static final String TAG = "SearchScreenDevice";
    /**
     * 用于监听发现设备
     */

    private SearchScreenDialog searchScreenDialog;
    private BrowseRegistryListener mBrowseRegistryListener;
    private ServiceConnection mUpnpServiceConnection;

    public void startSerachScreen() {
        initServiceConnection();
        Intent upnpServiceIntent = new Intent(PlayActivity.this, ClingUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initServiceConnection() {
        mUpnpServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.e(TAG, "mUpnpServiceConnection onServiceConnected");

                ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
                ClingUpnpService beyondUpnpService = binder.getService();

                ClingManager clingUpnpServiceManager = ClingManager.getInstance();
                clingUpnpServiceManager.setUpnpService(beyondUpnpService);
                clingUpnpServiceManager.setDeviceManager(new DeviceManager());

                clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
                //Search on service created.
                clingUpnpServiceManager.searchDevices();
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");
                mUpnpServiceConnection = null;
            }
        };
        if (searchScreenDialog == null)
            searchScreenDialog = new SearchScreenDialog(this);
        searchScreenDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
//                stopSearchScreen();
            }
        });
        searchScreenDialog.setOnItemClick(new SearchScreenDialog.OnDeviceItemClickListener() {
            @Override
            public void onDeviceItemClick(SearchScreenDialog dialog, boolean isActived) {
                if (isActived) {
                    dialog.dismiss();
                    mPlayer.暂停();
                    Intent intent = new Intent(PlayActivity.this, ScreenActivity.class);
                    intent.putExtra("curItem", mVideoItem);
                    intent.putExtra("playList", mPlayList);
                    intent.putExtra("playCodeList", mPlayCodeList);
                    startActivity(intent);
                } else {
                    ToastUtils.showLong("未连接到设备");
                }

            }
        });
//        searchScreenDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//
//            }
//        });
        mBrowseRegistryListener = new BrowseRegistryListener();
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchScreenDialog != null)
                            searchScreenDialog.onDeviceAdded((ClingDevice) device);
                    }
                });

            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchScreenDialog != null)
                            searchScreenDialog.onDeviceRemoved((ClingDevice) device);
                    }
                });

            }
        });
        searchScreenDialog.show();
    }

    public void stopSearchScreen() {
        if (mUpnpServiceConnection != null) {
            unbindService(mUpnpServiceConnection);
        }
        if (searchScreenDialog != null) {
            searchScreenDialog.dismiss();
            searchScreenDialog = null;
        }

        if (mBrowseRegistryListener != null) {
            mBrowseRegistryListener = null;
        }
    }


}
