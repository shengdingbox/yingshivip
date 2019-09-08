package cn.m.bdplayer;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.baidu.cloud.media.player.BDCloudMediaPlayer;
import com.baidu.cloud.media.player.IMediaPlayer;

/**
 * Created by H19 on 2018/7/6 0006.
 */

public class PlayerView extends BDCloudVideoView {

    public PlayerView(Context context) {
        this(context,null);
    }
    public PlayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public PlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadview();
    }

    public void loadview(){
        setLogEnabled(false); // 是否显示日志
        setLooping(false); // 是否循环播放

        ininSet();
        ininListener();
        new Handler().postDelayed(计时,500);//每0.5秒监听视频状态
    }

    public void ininSet(){
        setBufferSizeInBytes(1 * 1024 * 1024); // 起播数据字节长度 需要缓冲多大才播放 最大4M，单位B
        // setBufferTimeInMs(1000); //设置缓冲过程中，起播数据时长，
        setDecodeMode(BDCloudMediaPlayer.DECODE_AUTO); // 优先硬解码 // DECODE_SW 软解

        //setUseApmDetect(boolean useApmDetect); // 是否开启APM探测；若开启，需额外嵌入APM SDK
        setMaxCacheSizeInBytes( 5 * 1024 * 1024);// 设置最大缓存区大小；// 20 * 1024 * 1024 = 20971520
        setMaxProbeSize(4194304); //设置最大视频探测probe大小； 上限4M
        setMaxProbeTime(4000);//设置最大视频探测probe时长，单位为毫秒； 上限 4000


    }

    public void ininListener(){

        // 播放错误
        this.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                if (onListener != null) onListener.播放错误(iMediaPlayer,i,i1);
                return false;
            }
        });

        // 播放完成时回调
        this.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                if (onListener != null) onListener.播放完成(iMediaPlayer);
            }
        });

        // 播放器已经解析出播放源格式时回调
        this.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                if (onListener != null) onListener.加载视频信息完毕(iMediaPlayer);
            }
        });

        // 总体加载进度回调，返回为已加载进度占视频总时长的百分比


        // 缓冲进度监听
        this.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
                double d = i / 100D;
                int ii = (int) (d * m_length);
                if (onListener != null) onListener.缓冲进度改变(iMediaPlayer,(i + 1) * m_length / 100);
            }
        });


        // 播放器信息回调，如缓冲开始、缓冲结束
        this.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                if (onListener != null) onListener.播放信息回调(iMediaPlayer,i,i1);
                return false;
            }
        });

        // seek快速调节播放位置，完成后回调
        this.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                if (onListener != null) onListener.调节进度完毕(iMediaPlayer);
            }
        });

    }

    // 监听类
    private OnListener onListener;
    public void setOnListener(OnListener listener){
        onListener = listener;
    }
    public interface OnListener{
        void 状态改变(int i);
        void 进度改变(int i);
        void 缓冲进度改变(IMediaPlayer iMediaPlayer, int i);
        void 加载视频信息完毕(IMediaPlayer iMediaPlayer);
        void 播放完成(IMediaPlayer iMediaPlayer);
        void 播放错误(IMediaPlayer iMediaPlayer, int i, int i1);
        void 播放信息回调(IMediaPlayer iMediaPlayer, int i, int i1);
        void 调节进度完毕(IMediaPlayer iMediaPlayer);
    }

    // 初始化
    public void 初始化(){
        BDCloudVideoView.setAK("f6e7dc00c5614e968af49816bdd8a1d6");
    }

    public String m_url;
    public int m_state;
    public int m_progres; // 当前进度
    public int m_length; // 视频长度

    public final int STATE_READY = 0; // 就绪 无播放项目
    public final int STATE_PLAYING = 1; // 播放中
    public final int STATE_PAUSE = 2; // 暂停

    Runnable 计时 = new Runnable() {
        @Override
        public void run() {
            if (m_state != STATE_PLAYING && !PlayerView.this.isPlaying()){
                new Handler().postDelayed(计时,1000);//不在播放状态，那就久一点吧
                return; // 不处理非播放中进程
            }

            if (m_state != STATE_PLAYING) m_state = STATE_PLAYING;
            if (m_progres != getCurrentPosition()){
                if (m_progres > getCurrentPosition() + 3000){
                    seekTo(m_progres + 500);
                }else {
                    m_progres = getCurrentPosition();
                    if (onListener != null) onListener.进度改变(m_progres);
                }
            }

            new Handler().postDelayed(计时,500);
        }
        public void dd(){}
    };


    public void 停止(){
        stopPlayback();
        stateChange(STATE_READY);

        m_progres = 0;
        m_length = 0;
    }
    public void 继续(){
        if (m_url == null || m_url.isEmpty()) return;
        start();
        stateChange(STATE_PLAYING);
    }
    public void 开始(String url){
        停止();

        if (m_url != null && !m_url.isEmpty()){
            stopPlayback(); // 释放上一个视频源
            reSetRender(); // 清除上一个播放源的最后遗留的一帧
        }

        m_url = url;
        setVideoPath(url);
        继续();
    }
    public void 暂停(){
        if (m_state == STATE_PLAYING){
            pause();
        }
        stateChange(STATE_PAUSE);
    }

    public void stateChange(int i){
        m_state = i;
        if (onListener != null) onListener.状态改变(m_state);
    }


    public void 释放(){
        stopPlayback();
        release();
    }
    public void 重置数据(){
        m_length = 0;
        m_progres = 0;
    }

}
