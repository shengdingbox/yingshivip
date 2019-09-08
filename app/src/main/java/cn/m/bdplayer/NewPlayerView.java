package cn.m.bdplayer;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

import com.baidu.cloud.media.player.IMediaPlayer;

public class NewPlayerView extends PlayerView {
    private static final String TAG = "NewPlayerView";
    private OnPreparedListener onPreparedListener;

    public NewPlayerView(Context context) {
        this(context,null);
    }

    public NewPlayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public NewPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnListener(new OnListener(){

            @Override
            public void 状态改变(int i) {
                Log.d(TAG, "状态改变: ");
            }

            @Override
            public void 进度改变(int i) {
                Log.d(TAG, "进度改变: ");
            }

            @Override
            public void 缓冲进度改变(IMediaPlayer iMediaPlayer, int i) {
                Log.d(TAG, "缓冲进度改变: ");
            }

            @Override
            public void 加载视频信息完毕(IMediaPlayer iMediaPlayer) {
                Log.d(TAG, "加载视频信息完毕: ");
                int duration = (int) iMediaPlayer.getDuration();
                if (onPreparedListener != null){
                    onPreparedListener.onPrepared(duration);
                }
            }

            @Override
            public void 播放完成(IMediaPlayer iMediaPlayer) {
                Log.d(TAG, "播放完成: ");
            }

            @Override
            public void 播放错误(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.d(TAG, "播放错误: ");
            }

            @Override
            public void 播放信息回调(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.d(TAG, "播放信息回调: ");
            }

            @Override
            public void 调节进度完毕(IMediaPlayer iMediaPlayer) {
                Log.d(TAG, "调节进度完毕: ");
            }
        });
    }

    public void setOnPreparedListener(OnPreparedListener l) {
        this.onPreparedListener = l;
    }

    public interface OnPreparedListener {
        void onPrepared(int duration);
    }
}
