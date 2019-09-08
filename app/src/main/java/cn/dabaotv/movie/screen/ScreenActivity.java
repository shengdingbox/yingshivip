package cn.dabaotv.movie.screen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import cn.dabaotv.movie.Conl.PlayState;
import cn.dabaotv.movie.Conl.play.MPlaylistHelper;
import cn.dabaotv.movie.DB.DBRecord;
import cn.dabaotv.movie.Function.Jiexi;
import cn.dabaotv.movie.MainActivity;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.movie.utils.BaseActivity;
import cn.dabaotv.movie.view.list.ItemList;
import cn.dabaotv.video.R;
import com.qingfeng.clinglibrary.Intents;
import com.qingfeng.clinglibrary.control.ClingPlayControl;
import com.qingfeng.clinglibrary.control.callback.ControlCallback;
import com.qingfeng.clinglibrary.control.callback.ControlReceiveCallback;
import com.qingfeng.clinglibrary.entity.DLANPlayState;
import com.qingfeng.clinglibrary.entity.IResponse;
import com.qingfeng.clinglibrary.service.manager.ClingManager;

import org.fourthline.cling.support.model.PositionInfo;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.m.bdplayer.Mon;
import cn.m.bdplayer.NewPlayerView;
import cn.m.bdplayer.player_foot;
import cn.m.bdplayer.player_head;
import cn.m.bdplayer.player_in;
import cn.m.cn.像素;

public class ScreenActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * 连接设备状态: 播放状态
     */
    public static final int PLAY_ACTION = 0xa1;
    /**
     * 连接设备状态: 暂停状态
     */
    public static final int PAUSE_ACTION = 0xa2;
    /**
     * 连接设备状态: 停止状态
     */
    public static final int STOP_ACTION = 0xa3;
    /**
     * 连接设备状态: 转菊花状态
     */
    public static final int TRANSITIONING_ACTION = 0xa4;
    /**
     * 获取进度
     */
    public static final int EXTRA_POSITION = 0xa5;
    /**
     * 投放失败
     */
    public static final int ERROR_ACTION = 0xa6;
    /**
     * tv端播放完成
     */
    public static final int ACTION_PLAY_COMPLETE = 0xa7;

    public static final int ACTION_POSITION_CALLBACK = 0xa8;

    private player_head head;
    private player_foot foot;
    private NewPlayerView mPlayer;
    private LinearLayout llVideoStatus;
    private TextView tvVideoName;
    private TextView tvVideoStatus;

    private Context mContext;
    private Handler mHandler = new InnerHandler();
    private Timer timer = null;
    private ItemList mVideoItem;
    private ItemList cutParseItem;
    private ArrayList<ArrayList<ItemList>> mPlayList = new ArrayList<>(); // 播放列表
    private List<ItemList> mPlayCodeList = new ArrayList<>(); // 播放源列表
    private DBRecord mRecord; // 收藏
    private DBRecord mRecord2; // 历史

    private int cutPlayIndex; // 当前播放项目索引
    private int cutPlayCodeIndex; // 当前播放源 索引
    private int cutState = 0;//当前状态
    private MPlaylistHelper mPlaylistHp;
    private Jiexi mJiexi;

    private boolean isPlaying = false;
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();//投屏控制器
    private BroadcastReceiver mTransportStateBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        setContentView(R.layout.activity_screen);
        initView();
        initListener();
        initData();
        mContext = this;
        registerReceivers();
    }

    private void initStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initView() {
        head = findViewById(R.id.player_head);
        head.setPushButtonVisibility(View.GONE);
        foot = findViewById(R.id.player_foot);
        foot.setFullButtonVisibility(View.GONE);
        mPlayer = findViewById(R.id.video_view);
        llVideoStatus = findViewById(R.id.ll_video_status);
        tvVideoName = findViewById(R.id.tv_name);
        tvVideoStatus = findViewById(R.id.tv_status);
    }

    private void initData() {
        Intent intent = getIntent();
        mVideoItem = (ItemList) intent.getSerializableExtra("curItem");
        mPlayList = (ArrayList) intent.getSerializableExtra("playList");
        mPlayCodeList = (ArrayList) intent.getSerializableExtra("playCodeList");
        if (mVideoItem == null) {
            ToastUtils.showLong("解析失败，请重试");
            finish();
        }
        if (mPlayList == null) {
            foot.tt_drame.setVisibility(View.GONE);
        } else {
            if (mPlayList.size() == 0 || mPlayList.get(0).size() == 1) {
                foot.tt_drame.setVisibility(View.GONE);
            } else {
                foot.tt_drame.setVisibility(View.VISIBLE);
            }
        }

        head.m_tt_name.setText(mVideoItem.name == null ? "" : mVideoItem.name);
        tvVideoName.setText(mVideoItem.name == null ? "" : mVideoItem.name);

        getRecord(Qe.RECORDTYPE_收藏); // 读取记录
        getRecord(Qe.RECORDTYPE_历史); // 读取记录

        startFromRecord();

        setPlayCode(0);
    }

    private void initListener() {
        mPlaylistHp = new MPlaylistHelper().inin(this, new MPlaylistHelper.OnListener() {
            @Override
            public void onClick(final int position) {
                mPlaylistHp.hide();
                selectPlayItem(position);
            }
        });

        mPlayer.setOnPreparedListener(new NewPlayerView.OnPreparedListener() {
            @Override
            public void onPrepared(int duration) {
                foot.setMaxProgress(duration);
                foot.setCurProgress(0);
                String mv_time = Mon.stringForTime(duration);
                foot.tt_time_start.setText("00:00");
                foot.tt_time_end.setText(mv_time);

            }
        });
        head.setIn(new player_in() {
            @Override
            public void 回调(int v) {
                if (v == player_in.PLAYER_BT_RETURN) {
                    onBackPressed();
                }
            }

            @Override
            public boolean isFull() {
                return false;
            }

            @Override
            public boolean hide全屏按钮() {
                return false;
            }

            @Override
            public String getVideoUrl() {
                return null;
            }
        });
        foot.setIn(new player_in() {
            @Override
            public void 回调(int v) {
                switch (v) {
                    case player_in.PLAYER_BT_DRAME:
                        mPlaylistHp.shop(ScreenActivity.this, true, 像素.dip2px(ScreenActivity.this, 320), -1);
                        break;
                    case player_in.PLAYER_BT_NEXT:
                        startNext();
                        break;
                    case player_in.PLAYER_BT_PLAY:
                        if (!isPlaying) {
                            continuePlay();
                        } else {
                            pause();
                        }
                        break;
                }
            }

            @Override
            public boolean isFull() {
                return false;
            }

            @Override
            public boolean hide全屏按钮() {
                return false;
            }

            @Override
            public String getVideoUrl() {
                return null;
            }
        });
        foot.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int currentProgress = seekBar.getProgress() ; // 转为毫秒
                mClingPlayControl.seek(currentProgress, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "seek success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "seek fail");
                    }
                });
            }
        });

    }

    private void setPlayCode(int index) {
        cutPlayCodeIndex = index;
        if (mPlayCodeList.size() < 1) return;
        // 刷新Conl中的列表

        int curcur = cutPlayIndex;

        // 刷新弹窗视图中的播放列表..
        mPlaylistHp.setList(mPlayList.get(cutPlayCodeIndex));
        cutPlayIndex = curcur;
    }

    private void selectPlayItem(int itemId) {
        if (itemId >= mPlayList.get(cutPlayCodeIndex).size()) {
            ToastUtils.showShort("已经是最后一集");
            return;
            /*itemId = mPlayList.get(cutPlayCodeIndex).size() - 1;
            if (itemId < 0) return;*/
        }

        cutPlayIndex = itemId;
        ItemList item = mPlayList.get(cutPlayCodeIndex).get(itemId);

        mPlayer.停止();
        tvVideoStatus.setText("缓冲中...");
        if (item.url != null) {
            String videoUrl = item.url;
            if (videoUrl.endsWith(".m3u8") || videoUrl.endsWith(".mp4") ||
                    videoUrl.endsWith(".flv") || videoUrl.endsWith("avi")) {
                mPlayer.setVideoPath(videoUrl);//初始化视频信息

                String name = mVideoItem.name + " " + item.name;
                head.m_tt_name.setText(name);
                tvVideoName.setText(name);
                playNew(videoUrl);
                return;
            }
        }

        if (item.t == null || item.t.size() < 1) {
            paresItem(item);
        } else {
            startPlay(item);
        }
    }

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("视频格式不对");
                        }
                    });
                }
            }

            public void ent(String url) {
                Q.log("enturl-", url);
                cutParseItem.t.add(url);
                startPlay(cutParseItem);

            }
        });
        mJiexi.start(item.url);
    }

    private void startPlay(final ItemList item) {
        if (item.t.size() < 1) {
            ToastUtils.showShort("解析失败");
            return;
        }

        String videoUrl = "";
        if (item.t.size() == 1) {
            videoUrl = item.t.get(0);

        } else {
            videoUrl = item.t.get(item.t.size() - 1);
        }

        mPlayer.setVideoPath(videoUrl);//初始化视频信息

        String name = mVideoItem.name + " " + item.name;
        head.m_tt_name.setText(name);

        playNew(videoUrl);
    }

    private void startNext() {
        selectPlayItem(cutPlayIndex + 1);
        mPlaylistHp.selectItem(cutPlayIndex); // 选中后cutintex 就会改变
    }

//    private void play(String url) {
//        if (isFirstPlay) {
//            mClingPlayControl.playNew(url, new ControlCallback() {
//
//                @Override
//                public void success(IResponse response) {
//                    isPlaying = true;
//                    foot.ivPlay.setImageResource(R.drawable.icon_video_pause);
//                    ClingManager.getInstance().registerAVTransport(mContext);
//                    ClingManager.getInstance().registerRenderingControl(mContext);
//                    endGetProgress();
//                    startGetProgress();
//                }
//
//                @Override
//                public void fail(IResponse response) {
//                    isFirstPlay = true;
//                    mHandler.sendEmptyMessage(ERROR_ACTION);
//                }
//            });
//        } else {
//            mClingPlayControl.play(new ControlCallback() {
//                @Override
//                public void success(IResponse response) {
//                    isPlaying = true;
//                    foot.ivPlay.setImageResource(R.drawable.icon_video_pause);
//                    Log.e(TAG, "play success");
//                }
//
//                @Override
//                public void fail(IResponse response) {
//                    Log.e(TAG, "play fail");
//                    mHandler.sendEmptyMessage(ERROR_ACTION);
//                }
//            });
//        }
//    }

    private void playNew(String url){
        mClingPlayControl.playNew(url, new ControlCallback() {

            @Override
            public void success(IResponse response) {
                isPlaying = true;
                foot.ivPlay.setImageResource(R.drawable.icon_video_pause);
                tvVideoStatus.setText("正在投屏中");
                ClingManager.getInstance().registerAVTransport(mContext);
                ClingManager.getInstance().registerRenderingControl(mContext);
                endGetProgress();
                startGetProgress();
            }

            @Override
            public void fail(IResponse response) {
                mHandler.sendEmptyMessage(ERROR_ACTION);
            }
        });
    }

    private void continuePlay(){
        mClingPlayControl.play(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                isPlaying = true;
                tvVideoStatus.setText("正在投屏中");
                foot.ivPlay.setImageResource(R.drawable.icon_video_pause);
                Log.e(TAG, "play success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "play fail");
                mHandler.sendEmptyMessage(ERROR_ACTION);
            }
        });

    }
    private void startFromRecord() {
        selectPlayItem(cutPlayIndex);
        mPlaylistHp.selectItem(cutPlayIndex); // 选中后cutintex 就会改变
    }


    private void Record(boolean b) {
        if (b) {
            if (mRecord == null) {
                mRecord = new DBRecord();
            }
            mRecord.setUrl(mVideoItem.id);
            mRecord.setName(mVideoItem.name);
            mRecord.setDrama(cutPlayIndex);
            mRecord.setImg(mVideoItem.img);
            mRecord.setChangetime(System.currentTimeMillis() / 1000);
            mRecord.setMsg("播放至" + cutPlayIndex + "集");
            mRecord.setType(Qe.RECORDTYPE_收藏);
            mRecord.save();
        } else {
            if (mRecord != null) {
                mRecord.delete();
            }
            mRecord = null;
        }
    }

    private void Record2() {
        if (mRecord2 == null) {
            mRecord2 = new DBRecord();
        }
        mRecord2.setUrl(mVideoItem.id);
        mRecord2.setName(mVideoItem.name);
        mRecord2.setDrama(cutPlayIndex);
        mRecord2.setPlayindex(cutPlayIndex);
        mRecord2.setImg(mVideoItem.img);
        mRecord2.setChangetime(System.currentTimeMillis() / 1000);
        mRecord2.setType(Qe.RECORDTYPE_历史);
        mRecord2.setMsg("播放至" + cutPlayIndex + "集");
        mRecord2.save();
    }

    private void getRecord(String type) {
        List<DBRecord> records = DataSupport
                .where("url=? and type=?", mVideoItem.id, type)
                .find(DBRecord.class);
        if (type.equals(Qe.RECORDTYPE_收藏)) {
            if (records.size() > 0) {
                mRecord = records.get(0);
                cutPlayIndex = mRecord.getPlayindex();
            }
        } else if (type.equals(Qe.RECORDTYPE_历史)) {

            if (records.size() > 0) {
                mRecord2 = records.get(0);
                cutPlayIndex = mRecord2.getPlayindex();
            }
        }

    }

    /**
     * 停止
     */
    private void stop() {
        mClingPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
            }

            @Override
            public void fail(IResponse response) {
            }
        });
    }

    /**
     * 暂停
     */
    private void pause() {
        mClingPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                isPlaying = false;
                tvVideoStatus.setText("暂停投屏中");
                foot.ivPlay.setImageResource(R.drawable.icon_video_play);
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "pause fail");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
        mPlayer.release();
        mHandler.removeCallbacksAndMessages(null);
        endGetProgress();
        unregisterReceiver(mTransportStateBroadcastReceiver);

//        ClingManager.getInstance().destroy();
//        ClingDeviceList.getInstance().destroy();
    }

    private void registerReceivers() {
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        filter.addAction(Intents.ACTION_POSITION_CALLBACK);
        filter.addAction(Intents.ACTION_PLAY_COMPLETE);
        registerReceiver(mTransportStateBroadcastReceiver, filter);
    }

    @Override
    protected boolean setFitsSystemWindows() {
        return false;
    }



    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    Log.i(TAG, "Execute PLAY_ACTION");
                    Toast.makeText(mContext, "正在投放", Toast.LENGTH_SHORT).show();

                    startGetProgress();
                    mClingPlayControl.setCurrentState(DLANPlayState.PLAY);
                    break;
                case PAUSE_ACTION:
                    Log.i(TAG, "Execute PAUSE_ACTION");

                    mClingPlayControl.setCurrentState(DLANPlayState.PAUSE);
                    break;
                case STOP_ACTION:
                    Log.i(TAG, "Execute STOP_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.STOP);
                    foot.ivPlay.setImageResource(R.drawable.icon_video_pause);
                    break;
                case TRANSITIONING_ACTION:
                    Log.i(TAG, "Execute TRANSITIONING_ACTION");
                    Toast.makeText(mContext, "正在连接", Toast.LENGTH_SHORT).show();
                    break;

                case ACTION_POSITION_CALLBACK:
                    foot.setCurProgress(msg.arg1);
                    break;
                case ACTION_PLAY_COMPLETE:
                    Log.i(TAG, "Execute GET_POSITION_INFO_ACTION");
                    ToastUtils.showLong("播放完成");
                    break;

                case ERROR_ACTION:
                    Log.e(TAG, "Execute ERROR_ACTION");
                    Toast.makeText(mContext, "投放失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void startGetProgress() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mClingPlayControl != null)
                    mClingPlayControl.getPositionInfo(new ControlReceiveCallback() {
                        @Override
                        public void receive(IResponse response) {
                            Object responseResponse = response.getResponse();
                            Log.d(TAG, "success: IResponse" + responseResponse);
                            if (responseResponse instanceof PositionInfo) {
                                final PositionInfo positionInfo = (PositionInfo) responseResponse;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (foot != null) {
                                            foot.tt_time_start.setText(positionInfo.getRelTime());
                                            foot.setCurProgress(timeToSec(positionInfo.getRelTime()));
                                        }
                                        Log.d(TAG, "success: PositionInfo" + positionInfo);
                                    }
                                });

                            }
                        }

                        @Override
                        public void success(IResponse response) {

                        }

                        @Override
                        public void fail(IResponse response) {

                        }
                    });

            }
        }, 1000, 1000);
    }

    public static int timeToSec(String time) {
        String[] timeArray = time.split(":");
        int hour = Integer.parseInt(timeArray[0]) * 3600;
        int min = Integer.parseInt(timeArray[1]) * 60;
        int sec = Integer.parseInt(timeArray[2]);
        return (hour + min + sec) * 1000;
    }


    private void endGetProgress() {
        if (timer != null)
            timer.cancel();
        timer = null;
    }

    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive playback intent:" + action);
            if (Intents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(PLAY_ACTION);

            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(PAUSE_ACTION);

            } else if (Intents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(STOP_ACTION);

            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                mHandler.sendEmptyMessage(TRANSITIONING_ACTION);
            }else if (Intents.ACTION_POSITION_CALLBACK.equals(action)) {
                Message msg = Message.obtain();
                msg.what = ACTION_POSITION_CALLBACK;
                msg.arg1 = intent.getIntExtra(Intents.EXTRA_POSITION, -1);
                mHandler.sendMessage(msg);
            } else if (Intents.ACTION_PLAY_COMPLETE.equals(action)) {
                mHandler.sendEmptyMessage(ACTION_PLAY_COMPLETE);
            }
        }
    }
}