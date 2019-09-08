package cn.m.bdplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.cloud.media.player.IMediaPlayer;
import cn.dabaotv.video.R;

import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.m.cn.像素;

/**
 * 百度播放器读取视频，main
 */

public class MPlayer extends RelativeLayout implements View.OnClickListener, player_in {
    private String TAG = "MPlayer";
    private View mView;
    private Activity activity;
    private PlayerView mPlayer;


    private SeekBar mSeekBar;
    private player_head mHead;
    private player_foot mFoot;
    private TextView mMsg;
    private ImageView mLock;
    private ImageView bt_play;
    private ImageView foot_bt_play;
    private RelativeLayout mBack;
    private View mStartView; // 开始视图

    private boolean isLock; // 是否锁住
    private boolean isFull; // 是否全屏
    private boolean isMenu; // 是否显示菜单
    private boolean isUpdateProgressView = true; // 是否更新进度条视图 再进行移动进度条时，就不需要更新进度

    private int mWidth;

    // 记录手势的
    private float startX;
    private float startY;
    private int 手势类型;

    // 视频信息
    private String mv_url;
    private String mv_time;
    private boolean isPlaying;

    public MPlayer(Context context) {
        this(context,null);
    }
    public MPlayer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public MPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadview();
    }
    public void setActivity(Activity activity){
        this.activity = activity;
        // 开启硬解码支持
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }
    private String[] availableResolution = null;

    public String[] getAvailableResolution() {
        return availableResolution;
    }
    public void setAvailableResolution(String[] fetchResolution) {
        Log.d("MPlayer", "setAvailableResolution = " + Arrays.toString(fetchResolution));
        if (fetchResolution != null && fetchResolution.length > 1) {
            String[] availableResolutionDesc = new String[fetchResolution.length];
            for (int i = 0; i < fetchResolution.length; ++i) {
                availableResolutionDesc[i] = getDescriptionOfResolution(fetchResolution[i]);
            }
            this.availableResolution = availableResolutionDesc;
        }

    }
    public void loadview(){
        mView = View.inflate(getContext(), R.layout.m_player,this);
        BDCloudVideoView.setAK("f6e7dc00c5614e968af49816bdd8a1d6");

        mPlayer = (PlayerView) findViewById(R.id.mvv);
        mPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.e(TAG,"erroriMediaPlayer:"+ i + "  " + "   " + i1 + "  " + iMediaPlayer.getDataSource());
                return false;
            }
        });
        mPlayer.selectResolutionByIndex(0);
        mStartView = mView.findViewById(R.id.mStartView); // 开始视图

        mBack = (RelativeLayout)mView.findViewById(R.id.mBack); // 背景
        mHead = findViewById(R.id.mHead); // 头部
        mHead.setIn(this);

        mFoot = findViewById(R.id.mFoot); // 底部
        mFoot.setIn(this);
        foot_bt_play = mFoot.findViewById(R.id.foot_bt_play);
        foot_bt_play.setOnClickListener(this);

        mSeekBar = (SeekBar)mFoot.findViewById(R.id.seekBar); // 进度条

        mMsg = (TextView)mView.findViewById(R.id.mMsg); // 中间提示框

        mLock = (ImageView)mView.findViewById(R.id.mLock); // 锁屏按钮
        mLock.setOnClickListener(this);

        bt_play = (ImageView)mView.findViewById(R.id.bt_play); // 中间播放按钮
        bt_play.setOnClickListener(this);

        设置监听();
        设置全屏(true);
    }

    // 监听单击
    // 判断单击的项  有点多...
    private Handler clickHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0){
                单击时间 = System.currentTimeMillis();
            }else if(msg.what==1){
                if (System.currentTimeMillis() < 单击时间 + 200){
                    log("单击");
                    单击();
                }
                else 单击时间=0;
            }
        }
        public void  ddd(){}
    };
    private int 单击次数;
    private long 单击时间;
    private long 单击结束;

    public void 单击(){
        单击次数 ++ ;
        if (单击次数 == 1) {
            if (isMenu == true){
                显示菜单(false);
            }else {
                显示菜单(true);
            }
            单击结束 = System.currentTimeMillis();
        } else if (单击次数 == 2) {
            // 本次单击时间与上次单击结束时间相差不大  则为双击
            if (System.currentTimeMillis() - 单击结束 < 200) {
                if (mListener != null) {mListener.click(player_in.双击);}
                if (mPlayer.m_state == mPlayer.STATE_PLAYING){
                    暂停();
                }else{
                    继续();
                }
                单击次数 = 0;
            } else {
                if (手势类型 == 0){
                    if (isMenu == true){
                        显示菜单(false);
                    }else {
                        显示菜单(true);
                    }
                }
                单击次数  = 1;
                单击结束 = System.currentTimeMillis();
            }
        }
    }



    private void 设置监听(){
        // 手势监听
        mBack.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int msgcc = 0;


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://手指按下 单击
                        clickHandler.sendEmptyMessage(0); // 判断单击

                        startY = event.getY();
                        startX = event.getX();

                        手势类型 = 0;
                        break;
                    case MotionEvent.ACTION_UP: // 移开
                        clickHandler.sendEmptyMessage(1); // 判断单击

                        if (手势类型 == 1){
                            设置进度(mSeekBar.getProgress());
                            isUpdateProgressView = true;
                        }

                        putMsg("");
                        手势类型 = 0;
                        break;
                    case MotionEvent.ACTION_MOVE://手指移动
                        if (isLock == true) return true;
                        if (startY < 像素.dip2px(getContext(),32))return true;
                        float endY = event.getY();
                        float endX = event.getX();
                        float distanceY = startY - endY;
                        float distanceX = startX - endX;

                        // 判断上下滑动还是左右滑动。
                        if (手势类型 == 0){
                            if (Math.abs(distanceX) > Math.abs(distanceY) ){
                                手势类型 = 1;
                            }else{
                                if (endX < mWidth / 2) {
                                    手势类型 = 2;
                                }else{
                                    手势类型 = 3;
                                }
                            }
                        }

                        switch (手势类型){
                            case 1: // 进度
                                isUpdateProgressView = false;
                                int position = mSeekBar.getProgress(); // 毫秒
                                if (distanceX > 0){// 后退
                                    position = position - 1000;
                                }else{// 前进
                                    position = position + 1000;
                                }
                                mSeekBar.setProgress(position);

                                startX = endX;
                                break;
                            case 2: // 亮度
                                // 这里的50 是为了防止和状态栏产生碰撞
                                if (Math.abs(distanceY) > 50){
                                    float xxx;
                                    if (distanceY > 0){
                                        xxx = Mon.setBrightness(activity,5);
                                    }else{
                                        xxx = Mon.setBrightness(activity,-5);
                                    }
                                    startY = endY;
                                    putMsg("亮度："+ (int)(xxx * 100));
                                }
                                break;
                            case 3: // 声音
                                if (Math.abs(distanceY) > 50){
                                    if (distanceY > 0){
                                        msgcc = Mon.setVolume(activity,true);
                                    }else{
                                        msgcc = Mon.setVolume(activity,false);
                                    }
                                    startY = endY;
                                    putMsg("音量："+msgcc);
                                }
                                break;
                        }
                        break;
                }
                //return super.onTouchEvent(event);
                return true;
            }
            public void nullllll(){}
        });

        // 进度条监听
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isUpdateProgressView == false){
                    putMsg(Mon.stringForTime(progress)+" / " + mv_time);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUpdateProgressView = false;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUpdateProgressView = true;
                if(onSeekBarStopTrackingTouchListener != null)
                    onSeekBarStopTrackingTouchListener.onSeekBarStopTrackingTouch(seekBar);
            }
        });

        // 播放器监听
        mPlayer.setOnListener(new PlayerView.OnListener() {

            @Override
            public void 状态改变(int state){
                if (state == mPlayer.STATE_PLAYING) {
                    bt_play.setImageResource(R.drawable.ic_player_pause);
                    foot_bt_play.setImageResource(R.drawable.icon_video_pause);
                }else if (state == mPlayer.STATE_READY){
                    if (Mode == 0){
                        mFoot.tt_time_start.setText("00:00");
                    }

                    mSeekBar.setProgress(0);
                    mSeekBar.setSecondaryProgress(0);
                    bt_play.setImageResource(R.drawable.ic_player_play);
                    foot_bt_play.setImageResource(R.drawable.icon_video_play);
                }else {
                    bt_play.setImageResource(R.drawable.ic_player_play);
                    foot_bt_play.setImageResource(R.drawable.icon_video_play);foot_bt_play.setImageResource(R.drawable.icon_video_play);
                }
            }

            @Override
            public void 进度改变(int i){
                if (isUpdateProgressView){
                    mSeekBar.setProgress(i);
                    if (Mode == 0){
                        mFoot.tt_time_start.setText( Mon.stringForTime(i));
                        mFoot.tt_time_end.setText(mv_time);
                    }
                }

            }
            @Override
            public void 缓冲进度改变(IMediaPlayer iMediaPlayer, int i) {
                mSeekBar.setSecondaryProgress(i);
            }

            @Override
            public void 加载视频信息完毕(IMediaPlayer iMediaPlayer) {
                mPlayer.m_length = (int) iMediaPlayer.getDuration();
                mSeekBar.setMax((int)mPlayer.m_length);
                mv_time = Mon.stringForTime((int)mPlayer.m_length);
                if (Mode == 0){
                    mFoot.tt_time_start.setText("00:00");
                    mFoot.tt_time_end.setText(mv_time);
                }

            }

            @Override
            public void 播放完成(IMediaPlayer iMediaPlayer) {
                播放完毕();
            }

            @Override
            public void 播放错误(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.i("eee-err",i + " - " + i1);
                switch (i){
                    case -10000: // 实际是啥 官方没数据
                        if (mPlayer.m_progres == 0 && i1 == iMediaPlayer.MEDIA_ERROR_TIMED_OUT){
                            Toast.makeText(activity, "链接超时", Toast.LENGTH_SHORT).show();
                        }else {
                            mPlayer.seekTo(mPlayer.m_progres);
                        }
                        break;
                }
            }

            @Override
            public void 播放信息回调(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i){
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        // 音频开始播放
                        break;
                    case IMediaPlayer.MEDIA_ERROR_SERVER_DIED:

                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START: // 缓冲开始

                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END: // 缓冲失败

                }

                if (i == 10002 || i == 3){
                    mStartView.setVisibility(GONE);
                    显示菜单(false);
                }
            }

            @Override
            public void 调节进度完毕(IMediaPlayer iMediaPlayer) {
                log("调节进度完毕 " + iMediaPlayer.getCurrentPosition());
            }
        });
    }

    private Timer mMenuTimer = null;
    public void 隐藏菜单(){
        if (mMenuTimer != null){
            mMenuTimer.cancel();
            mMenuTimer = null;
        }

        mMenuTimer = new Timer();
        try {
            mMenuTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            显示菜单(false);
                        }
                    });
                    //
                }
            },6000,6000);
        }catch (Exception e){
            log("ERR-mMenuTimer" + e.toString());
        }
    }

    private void log(String t){
        Log.d("BDPLAYER- ",t+ " ");
    }
    public boolean isFull(){return isFull;}
    public void 显示菜单(boolean b){
        if (b == false){
            mFoot.setVisibility(View.GONE);
            mHead.setVisibility(View.GONE);
            mLock.setVisibility(GONE);

            if (mPlayer.m_state == mPlayer.STATE_PAUSE && mStartView.getVisibility() == GONE){
                bt_play.setVisibility(VISIBLE);
            }else {
                bt_play.setVisibility(GONE);
            }

            isMenu = false;
        }else{
            if (isFull){
                if (!isLock){
                    mHead.setVisibility(View.VISIBLE);
                    mFoot.setVisibility(View.VISIBLE);
                    if (mStartView.getVisibility() == GONE) bt_play.setVisibility(VISIBLE);
                }else {
                    显示菜单(false);
                }
                mLock.setVisibility(VISIBLE);
            }else {
                显示菜单(false);
                mHead.setVisibility(View.VISIBLE);
                mFoot.setVisibility(View.VISIBLE);
            }
            isMenu = true;

            隐藏菜单();
        }
    }
    public void 设置进度(int progres){
        if (mPlayer.m_state == mPlayer.STATE_PAUSE) mPlayer.继续();
        mPlayer.m_progres = progres;
        mPlayer.seekTo(progres);
    }
    public void putMsg(String t){
        if (t.equals("")){
            mMsg.setVisibility(View.GONE);
            return;
        }
        mMsg.setText(t);
        mMsg.setVisibility(View.VISIBLE);
    }
    // 播放操作
    private int Mode;
    private int Mode_直播;
    private int Mode_本地;
    private int Mode_多集;
    // 一些播放器命令
    public void 停止(){
        mPlayer.停止();
    }
    public void 退出(){
        暂停();
        mHead.结束();
    }
    public void 进入(){
        mHead.监听电量();
        继续();
    }
    public void 暂停(){
        mPlayer.暂停();
    }
    public void 继续(){
        bt_play.setImageResource(R.drawable.ic_player_pause);
        foot_bt_play.setImageResource(R.drawable.icon_video_pause);
        mPlayer.继续();
    }
    public void 播放(String url){
        bt_play.setImageResource(R.drawable.ic_player_pause);
        foot_bt_play.setImageResource(R.drawable.icon_video_pause);
        mv_url = url;

        mPlayer.开始(url);
        显示菜单(true);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!isFull) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
    public void 准备(String name,String msg){
        mStartView.setVisibility(VISIBLE);
        mSeekBar.setProgress(0);
        mHead.m_tt_name.setText(name);
        ((TextView)mStartView.findViewById(R.id.name)).setText(name);
        ((ImageView)mStartView.findViewById(R.id.img)).setImageResource(R.mipmap.ic_logo);
        ((TextView)mStartView.findViewById(R.id.msg)).setText(msg);
        显示菜单(true);
    }
    public void 设置标题(String name){
        mHead.m_tt_name.setText(StringUtils.replaceBlank(name));
    }
    public void 准备(int imgResId,String name,String msg){
        mStartView.setVisibility(VISIBLE);
        bt_play.setVisibility(GONE);
        mSeekBar.setProgress(0);
        ((TextView)mStartView.findViewById(R.id.name)).setText(name);
        ((ImageView)mStartView.findViewById(R.id.img)).setImageResource(imgResId);
        ((TextView)mStartView.findViewById(R.id.msg)).setText(msg);
        显示菜单(false);
    }
    public void 设置全屏(boolean b){
        isFull = b;
        mFoot.refresh();
        mHead.refresh();
        显示菜单(true);
        mView.post(new Runnable() {
            @Override
            public void run() {
                mWidth = mView.getMeasuredWidth();
            }
        });
    }

    // 按钮  全屏按钮
    private boolean hideFullButton;
    public void hide全屏按钮(boolean b){
        hideFullButton = b;
        mFoot.refresh();
    }
    public boolean hide全屏按钮(){return hideFullButton;}

    public void 显示下集(boolean b){
        mFoot.isDisplayNext = b;
    }
    public void 显示线路(boolean b){
        mFoot.isDisplayLine = b;
        if (b){
            mFoot.tt_line.setVisibility(VISIBLE);
        }else {
            mFoot.tt_line.setVisibility(GONE);
        }
    }
    public void 显示选集(boolean b){
        mFoot.isDisplayDrame = b;
        if (isMenu) mFoot.tt_drame.setVisibility(VISIBLE);
        if(!b) mFoot.tt_drame.setVisibility(GONE);
    }
    public void 设置显示模式(int b){
        Mode = b;
        mFoot.设置显示模式(b);
        if (Mode == Locode.DISPLAYMODE_直播){
            // 处理foot中的控件变化
            mSeekBar.setVisibility(GONE);
            mFoot.tt_time_start.setText("直播");
        }else {
            // 处理 FOOT 中的按钮变化
            mSeekBar.setVisibility(GONE);
            mFoot.tt_time_end.setText("/");
        }
    }
    public void setCollection(boolean b){
        if (b){
            mHead.bt_sc.setImageResource(R.drawable.ic_player_sc_on);
        }else {
            mHead.bt_sc.setImageResource(R.drawable.ic_player_sc);
        }
    }

    public int 取当前状态(){
        return mPlayer.m_state;
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }
    public void 回调(int v){
        switch (v){
            case player_in.PLAYER_BT_PLAY:
                if (mPlayer.m_state == mPlayer.STATE_PLAYING){
                    暂停();
                }else {
                    继续();
                }
                break;
            case player_in.PLAYER_BT_FULLSCREEN:
                if (isFull){
                    isFull = false;
                }else {
                    isFull = true;
                }
                设置全屏(isFull);
                break;
        }

        mListener.click(v);
    }
    @Override
    public String getVideoUrl() {
        return mv_url;
    }

    public void setHeaders(Map<String, String> headers) {
        mPlayer.setHeaders(headers);
    }

    @Override
    public void onClick(View v){
        if (v.getId() == mLock.getId()){
            if (isFull == true){
                if (isLock == true){
                    isLock = false;
                    mLock.setImageResource(R.drawable.ic_player_lock0);
                    显示菜单(true);
                }else {
                    isLock = true;
                    mLock.setImageResource(R.drawable.ic_player_lock1);
                    显示菜单(false);
                }
            }
        }else if (v.getId() == bt_play.getId() || v.getId() == foot_bt_play.getId()){
            if (mPlayer.m_state == mPlayer.STATE_PLAYING){
                暂停();
            }else{
                继续();
            }
        }
    }

    public void 播放完毕(){
        if (mListener != null) mListener.播放完毕();
    }
    private PlayerClickListener mListener;
    public void setClickListener(PlayerClickListener clickListener) {
        this.mListener = clickListener;
    }
    public interface PlayerClickListener{
        void click(int id);// 1 播放 | 2 下集 | 3 选集 | 4 全屏 | 5 返回 | 6 投屏 | 7 下载 | 8 双击
        void 播放完毕();
    }
    public String getDescriptionOfResolution(String resolutionType) {
        String result = "未知";
        try {
            // resolutionType is like 1920x1080,3541000
            // sometimes value is ,232370 if there is no resolution desc in master m3u8 file
            String[] cuts1 = resolutionType.trim().split(",");
            if (cuts1[0].length() > 0) {
                // cuts1[0] has resultion string
                String[] cuts2 = cuts1[0].trim().split("[xX]");
                if (cuts2.length == 2) {
                    // get the height size
                    int iResult = Integer.parseInt(cuts2[1]);
                    if (iResult <= 0) {
                        result = "未知";
                    } else if (iResult <= 120) {
                        result = "120P";
                    } else if (iResult <= 240) {
                        result = "240P";
                    } else if (iResult <= 360) {
                        result = "360P";
                    } else if (iResult <= 480) {
                        result = "480P";
                    } else if (iResult <= 800) {
                        result = "720P";
                    } else {
                        result = "1080P";
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "getDescriptionOfResolution exception:" + e.getMessage());
        }
        Log.d(TAG, "getDescriptionOfResolution orig=" + resolutionType + ";result=" + result);

        return result;
    }
    private OnSeekBarStopTrackingTouchListener onSeekBarStopTrackingTouchListener;

    public void setOnSeekBarStopTrackingTouchListener(OnSeekBarStopTrackingTouchListener onSeekBarStopTrackingTouchListener) {
        this.onSeekBarStopTrackingTouchListener = onSeekBarStopTrackingTouchListener;
    }

    public interface OnSeekBarStopTrackingTouchListener{
        void onSeekBarStopTrackingTouch(SeekBar seekBar);
    }
}
