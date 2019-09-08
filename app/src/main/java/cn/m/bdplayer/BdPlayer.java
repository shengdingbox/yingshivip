package cn.m.bdplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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

/**
 * Created by H19 on 2018/5/25 0025.
 */

public class BdPlayer extends RelativeLayout implements View.OnClickListener, player_in {
    private View mView;
    private Activity activity;

    private BDCloudVideoView mPlayer;
    private SeekBar mSeekBar;
    private player_head mHead;
    private player_foot mFoot;
    private TextView mMsg;
    private ImageView mLock;
    private ImageView bt_play;
    private RelativeLayout mBack;
    private View mStartView; // 开始视图

    private boolean isLock; // 是否锁住
    private boolean isFull; // 是否全屏
    private boolean isMenu; // 是否显示菜单

    private int tinMenu; // 计时 菜单显示时间
    private int mWidth;

    // 记录手势的
    private float startX;
    private float startY;
    private int 手势类型;
    private int 单击次数;
    private long 单击时间;

    // 视频信息
    private String mv_url;
    private long mv_length;
    private String mv_time;
    private boolean isPlaying;

    public BdPlayer(Context context) {
        this(context,null);
    }
    public BdPlayer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public BdPlayer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadview();
    }
    public void setActivity(Activity activity){
        this.activity = activity;
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }
    public void loadview(){
        mView = View.inflate(getContext(), R.layout.m_player,this);
        BDCloudVideoView.setAK("f6e7dc00c5614e968af49816bdd8a1d6");
        //mPlayer = new BDCloudVideoView (getContext());
        //((LinearLayout)mView.findViewById(R.id.mPlayer)).addView(mPlayer);
        mPlayer = (BDCloudVideoView)findViewById(R.id.mvv);
        //mPlayer.setBufferingIndicator(mView.findViewById(R.id.loading));
        mStartView = mView.findViewById(R.id.mStartView);

        mBack = (RelativeLayout)mView.findViewById(R.id.mBack);
        mHead = findViewById(R.id.mHead);
        //mHead = new player_head(getContext());
        mHead.setIn(this);

        //mFoot = new player_foot(getContext());
        mFoot = findViewById(R.id.mFoot);
        mFoot.setIn(this);
        mFoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mSeekBar = (SeekBar)mFoot.findViewById(R.id.seekBar);
        mMsg = (TextView)mView.findViewById(R.id.mMsg);
        mLock = (ImageView)mView.findViewById(R.id.mLock);
        mLock.setOnClickListener(this);
        bt_play = (ImageView)mView.findViewById(R.id.bt_play);
        bt_play.setOnClickListener(this);

        设置监听();
        set全屏(false);
        new Handler().postDelayed(计时,500);//每0.5秒监听一次是否在播放视频
    }
    private void 设置监听(){
        // 手势监听
        mBack.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int msgcc = 0;
                if (event.getAction() != MotionEvent.ACTION_DOWN && isLock == true ){
                    return true; // 屏幕被锁住 不操作
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://手指按下 单击
                        startY = event.getY();
                        startX = event.getX();
                        手势类型 = 0;
                        单击次数 ++ ;
                        if (单击次数 == 1) {
                            单击时间 = System.currentTimeMillis();
                            if (isMenu == true){
                                显示菜单(false);
                            }else {
                                显示菜单(true);
                            }
                        } else if (单击次数 == 2) {
                            if (System.currentTimeMillis() - 单击时间 < 300) {
                                if (mListener != null) {mListener.click(player_in.双击);}
                                if (mState == 状态_播放中){
                                    暂停();
                                }else if (mState == 状态_暂停){
                                    继续();
                                }
                                单击次数 = 0;
                                单击时间 = 0;
                            } else {
                                if (手势类型 == 0){
                                    if (isMenu == true){
                                        显示菜单(false);
                                    }else {
                                        显示菜单(true);
                                    }
                                }
                                单击时间 = System.currentTimeMillis();
                                单击次数  = 1;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP: // 移开
                        if (手势类型 == 1){
                            tinMenu = 0;
                            设置进度(mSeekBar.getProgress());
                        }
                        putMsg("");
                        手势类型 = 0;
                        break;
                    case MotionEvent.ACTION_MOVE://手指移动
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
                                tinMenu = -2;
                                int position = mSeekBar.getProgress(); // 毫秒
                                if (distanceX > 0){
                                    // 后退
                                    position = position - 1;
                                }else{
                                    // 前进
                                    position = position + 1;
                                }
                                mSeekBar.setProgress(position);
                                startX = endX;
                                break;
                            case 2: // 亮度
                                tinMenu = -1;
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
                                tinMenu = -1;
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
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (tinMenu == -2){
                    double length = progress / 1000.00 * mv_length;
                    long ii = (long)length;
                    putMsg(Mon.stringForTime((int)ii)+" / " + mv_time);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tinMenu = -2;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tinMenu = 0;
                设置进度(seekBar.getProgress());
                putMsg("");
            }
        });

        mPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i){
                    case -10000: // 实际是啥 我也不知道
                        if (cutPosition == 0){
                            Toast.makeText(activity, "链接超时", Toast.LENGTH_SHORT).show();
                        }else {
                            mPlayer.seekTo(cutPosition);
                        }

                        break;
                }
                return false;
            }
            public void vvv(){};
        });
        mPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                mv_length = iMediaPlayer.getDuration();
                mv_time = Mon.stringForTime((int) mv_length);
                mFoot.tt_time_start.setText(mv_time);
                if (cutPosition > 0){
                    mPlayer.seekTo(cutPosition);
                }
                //mSeekBar.setProgress(iMediaPlayer.getCurrentPosition());
            }
        });
        mPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                播放完毕();
            }
        });
        mPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i){
                    case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                        // 音频开始播放
                        break;
                    case IMediaPlayer.MEDIA_ERROR_SERVER_DIED:

                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        mState = 状态_缓冲;
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        if (mPlayer.getCurrentPosition() < cutPosition - 5){
                            mPlayer.seekTo(cutPosition);
                        }
                        if (iMediaPlayer.isPlaying()) {
                            mState = 状态_播放中;
                        }else {
                            mState = 状态_暂停;
                            显示菜单(false);
                        }
                }

                if (i == 10002 || i == 3){
                    mStartView.setVisibility(GONE);
                    显示菜单(false);
                }
                return false;
            }
            private void dddd(){}
        });
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

            if (mState == 状态_暂停 && mStartView.getVisibility() == GONE){
                bt_play.setVisibility(VISIBLE);
            }else {
                bt_play.setVisibility(GONE);
            }

            isMenu = false;
            tinMenu = -1;
        }else{
            if (isFull == true){
                if (isLock == false){
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
            tinMenu = 0;
        }
    }
    public void 设置进度(int position){
        double length = position / 1000D * mv_length;
        long ii = (long)length;
        cutPosition = (int) ii;
        mPlayer.seekTo(cutPosition);
        if (ii != 0){
            if (mState == 状态_暂停 || mv_url.length() > 5){
                mPlayer.start();
            }
        }
        tinMenu = -1;
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
    private int cutPosition;
    Runnable 计时 = new Runnable() {
        @Override
        public void run() {

            if (tinMenu > -1){
                tinMenu = tinMenu + 1;
                if (mPlayer.getCurrentPlayerState() == BDCloudVideoView.PlayerState.STATE_PLAYING){
                    bt_play.setImageResource(R.drawable.ic_player_pause);
                }else {
                    bt_play.setImageResource(R.drawable.ic_player_play);
                }
                if (tinMenu > 10){
                    显示菜单(false);
                    tinMenu = -1;
                }
            }

            int ii = mPlayer.getCurrentPosition();
            if (ii > 0 && ii < cutPosition - 10){
                mPlayer.seekTo(cutPosition);
                return;
            }

            if (mPlayer.getCurrentPosition() > 0) cutPosition = mPlayer.getCurrentPosition();
            cutPosition = mPlayer.getCurrentPosition();
            if (mState == 状态_播放中){
                if (mv_length != 0L){
                    if (tinMenu != -2 && cutPosition > 1 && Mode != Locode.DISPLAYMODE_直播){
                        long pos = 1000L * cutPosition / mv_length;
                        mSeekBar.setProgress((int) pos);
                    }
                    mFoot.tt_time_start.setText(mv_time);
                    mFoot.tt_time_end.setText(Mon.stringForTime(cutPosition));
                }
            }else {

            }
            new Handler().postDelayed(计时,500);//每0.5秒监听一次是否在播放视
        }
        public void dd(){}
    };
    // 一些播放器命令
    public void 停止(){
        bt_play.setImageResource(R.drawable.ic_player_play);
        mState = 状态_暂停;
        mPlayer.stopPlayback();
        mFoot.tt_time_start.setText( "" );
        mFoot.tt_time_end.setText( "" );
        mSeekBar.setProgress(0);
    }
    public void 退出(){
        mState = 状态_暂停;
        暂停();
        mHead.结束();
    }
    public void 进入(){
        mHead.监听电量();
        if (mState == 状态_暂停) 继续();
    }
    public void 暂停(){
        bt_play.setImageResource(R.drawable.ic_player_play);
        mState = 状态_暂停;
        mPlayer.pause();
    }
    public void 继续(){
        bt_play.setImageResource(R.drawable.ic_player_pause);
        if (mState == 状态_暂停){
            mPlayer.start();
        }
        mState = 状态_播放中;
    }
    public void 播放(String url){
        bt_play.setImageResource(R.drawable.ic_player_pause);
        cutPosition = 0;
        mv_url = url;
        mv_length = 0L;
        mPlayer.setLogEnabled(true);
        mPlayer.setVideoPath(url);
        mPlayer.start();
        mState = 状态_播放中;
        显示菜单(false);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    public void 准备(String name,String msg){
        mState = 状态_准备;
        mStartView.setVisibility(VISIBLE);
        mSeekBar.setProgress(0);
        mHead.m_tt_name.setText(name);
        ((TextView)mStartView.findViewById(R.id.name)).setText(name);
        ((ImageView)mStartView.findViewById(R.id.img)).setImageResource(R.mipmap.ic_logo);
        ((TextView)mStartView.findViewById(R.id.msg)).setText(msg);
        显示菜单(false);
    }
    public void 准备(int imgResId,String name,String msg){
        mStartView.setVisibility(VISIBLE);
        bt_play.setVisibility(GONE);
        mSeekBar.setProgress(0);
        mHead.m_tt_name.setText(name);
        ((TextView)mStartView.findViewById(R.id.name)).setText(name);
        ((ImageView)mStartView.findViewById(R.id.img)).setImageResource(imgResId);
        ((TextView)mStartView.findViewById(R.id.msg)).setText(msg);
        显示菜单(false);
    }
    public void set全屏(boolean b){
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
    }
    public void 显示选集(boolean b){
        mFoot.isDisplayDrame = b;
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
            mSeekBar.setVisibility(VISIBLE);
            mSeekBar.setVisibility(GONE);
            mFoot.tt_time_start.setText(mv_time);
            mFoot.tt_time_end.setText(Mon.stringForTime(cutPosition));

        }
    }
    private int mState; // 0 准备  1 播放中  2 暂停
    public int 状态_播放中 = 1;
    public int 状态_暂停 = 2;
    public int 状态_准备 = 0;
    public int 状态_缓冲 = 3;


    public int 取当前状态(){
        return mState;
    }
    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }
    public void 回调(int v){
        switch (v){
            case player_in.PLAYER_BT_PLAY:
                if (mState == 状态_播放中){
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
                set全屏(isFull);
                break;
        }

        mListener.click(v);
    }
    @Override
    public String getVideoUrl() {
        return mv_url;
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
        }else if (v.getId() == bt_play.getId()){
            if (mState == 状态_播放中){
                暂停();
            }else if (mState == 状态_暂停){
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
}
