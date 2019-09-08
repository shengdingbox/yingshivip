/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.m.bdplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.cloud.media.player.BDCloudMediaPlayer;
import com.baidu.cloud.media.player.BDTimedText;
import com.baidu.cloud.media.player.IMediaPlayer;

import java.io.IOException;
import java.util.Map;

/**
 * 开源类-播放器视图VideoView
 * 可以作为控件增加到App中，该类的主要逻辑为：
 * 在Jelly_Bean(4.1)及以上系统，使用TextureView控件展示视频内容
 * 在Jelly_Bean(4.0)及以下系统，使用SurfaceView控件来展示视频内容
 */
public class BDCloudVideoView extends FrameLayout implements MediaController.MediaPlayerControl {
    private static final String TAG = "BDCloudVideoView";

    /**
     * 填充，保持视频内容的宽高比。视频与屏幕宽高不一致时，会留有黑边
     */
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    /**
     * 裁剪，保持视频内容的宽高比。视频与屏幕宽高不一致时，会裁剪部分视频内容
     */
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;

    /**
     * 铺满，不保证视频内容宽高比。视频显示与屏幕宽高相等
     */
    public static final int VIDEO_SCALING_MODE_SCALE_TO_MATCH_PARENT = 3;

    /**
     * 优先使用TextureView
     * 若为true，则在4.1及以上系统，使用TextureView控件展示视频内容，低版本使用SurfaceView；
     * 若为false，则始终使用SurfaceView控件展示视频内容；
     * 两者对比：TextureView更耗性能，但支持动画、截图等功能。
     */
    private boolean mUseTextureViewFirst = true;

    // 播放链接
    private Uri mUri;
    // 指定headers，默认不指定
    private Map<String, String> mHeaders;

    public void setLayoutParams(int matchParent, int matchParent1) {
    }

    ////////////////////////播放状态专区-起始///////////////////////////////////////////
    // all possible internal states
    public enum PlayerState {
        STATE_ERROR(-1),
        STATE_IDLE(0),
        STATE_PREPARING(1),
        STATE_PREPARED(2),
        STATE_PLAYING(3),
        STATE_PAUSED(4),
        STATE_PLAYBACK_COMPLETED(5);

        private int code;

        private PlayerState(int oCode) {
            code = oCode;
        }
    }

    // 播放器当前的状态
    private PlayerState mCurrentState = PlayerState.STATE_IDLE;

    public PlayerState getCurrentPlayerState() {
        return mCurrentState;
    }

    private void setCurrentState(PlayerState newState) {
        if (mCurrentState != newState) {
            mCurrentState = newState;
            if (mOnPlayerStateListener != null) {
                mOnPlayerStateListener.onPlayerStateChanged(mCurrentState);
            }
        }
    }

    public interface OnPlayerStateListener {
        public void onPlayerStateChanged(final PlayerState nowState);
    }

    public void setOnPlayerStateListener(OnPlayerStateListener listener) {
        mOnPlayerStateListener = listener;
    }

    private boolean isTryToPlaying = false;

    ////////////////////////播放状态专区-End///////////////////////////////////////////


    // All the stuff we need for playing and showing a video
    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    private BDCloudMediaPlayer mMediaPlayer = null;
    // private int         mAudioSession;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoRotationDegree;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private OnPlayerStateListener mOnPlayerStateListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;

    private String mDrmToken = null;

    private int mCurrentAspectRatio = IRenderView.AR_ASPECT_FIT_PARENT;

    private Context mAppContext;
    private IRenderView mRenderView;
    private int mVideoSarNum;
    private int mVideoSarDen;

    private int mCacheTimeInMilliSeconds = 0;
    private boolean mbShowCacheInfo = true;
    private int mDecodeMode = BDCloudMediaPlayer.DECODE_AUTO;
    private boolean mLogEnabled = false;
    private long mInitPlayPositionInMilliSec = 0;
    private int mWakeMode = 0;
    private float mLeftVolume = -1f;
    private float mRightVolume = -1f;
    private int mMaxProbeTimeInMs = -1;
    private int mMaxProbeSizeInBytes = 0;
    private int mMaxCacheSizeInBytes = 0;
    private boolean mLooping = false;
    private int mBufferSizeInBytes = 0;
    private int mFrameChasing = -1;
    private float mSpeed = 1.0f;

    /**
     * 以下三个类负责『加载中』的提示界面，如想定制加载中界面，修改下面的Bar和Hint控件即可
     */
    private RelativeLayout cachingHintViewRl = null;
    private ProgressBar cachingProgressBar = null;
    private TextView cachingProgressHint = null;

    // render target view is on this
    private FrameLayout renderRootView = null;

    private static final int MESSAGE_CHANGE_CACHING = 1;
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_CHANGE_CACHING) {
                setCachingHintViewVisibility(msg.arg1 == 1);
            }
//            super.handleMessage(msg);
        }
    };

    private TextView subtitleDisplay;

    /**
     * 代码构造函数
     *
     * @param context
     */
    public BDCloudVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    /**
     * 代码构造函数，可指定是否优先使用TextureView
     *
     * @param context
     * @param useTextureViewFirst
     */
    public BDCloudVideoView(Context context, boolean useTextureViewFirst) {
        super(context);
        this.mUseTextureViewFirst = useTextureViewFirst;
        initVideoView(context);
    }

    /**
     * xml构造函数
     *
     * @param context
     * @param attrs
     */
    public BDCloudVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    /**
     * xml构造函数
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public BDCloudVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    /**
     * xml构造函数
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BDCloudVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initVideoView(context);
    }

    private void initVideoView(Context context) {
        mAppContext = context.getApplicationContext();

        renderRootView = new FrameLayout(context);
        LayoutParams fllp = new LayoutParams(-1, -1);
        addView(renderRootView, fllp);

        reSetRender();

        addSubtitleView();
        addCachingHintView();

        mVideoWidth = 0;
        mVideoHeight = 0;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setCurrentState(PlayerState.STATE_IDLE);
    }

    /**
     * 增加字幕显示控件
     */
    private void addSubtitleView() {
        subtitleDisplay = new TextView(this.getContext());
        subtitleDisplay.setTextSize(24);
        subtitleDisplay.setGravity(Gravity.CENTER);
        LayoutParams layoutParamsTxt = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        addView(subtitleDisplay, layoutParamsTxt);
    }

    /**
     * 增加『加载中』控件
     */
    private void addCachingHintView() {
        cachingHintViewRl = new RelativeLayout(this.getContext());
        LayoutParams fllp = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        cachingHintViewRl.setVisibility(View.GONE);
        addView(cachingHintViewRl, fllp);

        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rllp.addRule(RelativeLayout.CENTER_IN_PARENT);

        cachingProgressBar = new ProgressBar(this.getContext());
        cachingProgressBar.setId(android.R.id.text1); // setId() param can be random number, use text1 to avoid lints
        cachingProgressBar.setMax(100);
        cachingProgressBar.setProgress(10);
        cachingProgressBar.setSecondaryProgress(100);
        cachingHintViewRl.addView(cachingProgressBar, rllp);

        rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rllp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rllp.addRule(RelativeLayout.BELOW, android.R.id.text1);
        cachingProgressHint = new TextView(this.getContext());
        cachingProgressHint.setTextColor(0xffffffff);
        cachingProgressHint.setText("正在缓冲...");
        cachingProgressHint.setGravity(Gravity.CENTER_HORIZONTAL);
        cachingHintViewRl.addView(cachingProgressHint, rllp);
    }

    /**
     * 显示或隐藏『加载中』消息，该函数必须在主线程调用
     *
     * @param bShow
     */
    private void setCachingHintViewVisibility(boolean bShow) {
        if (bShow) {
            cachingHintViewRl.setVisibility(View.VISIBLE);
        } else {
            cachingHintViewRl.setVisibility(View.GONE);
        }
    }

    /**
     * 当一些操作无法保证主线程时，使用该方法来达到显示隐藏『加载中』消息的目的
     *
     * @param bShow
     */
    private void sendCachingHintViewVisibilityMessage(boolean bShow) {
        // 自定义『加载中』状态时，会调用 showCacheInfo(false) 将mbShowCacheInfo置为false
        if (mbShowCacheInfo) {
            Message msg = mainThreadHandler.obtainMessage();
            msg.what = MESSAGE_CHANGE_CACHING;
            msg.arg1 = bShow ? 1 : 0;
            mainThreadHandler.sendMessage(msg);
        }

    }

    /**
     * 设置renderview
     * 多数时候您不需使用
     *
     * @param renderView
     */
    protected void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(null);
            }

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView.release();
            mRenderView = null;
            mSurfaceHolder = null;
            renderRootView.removeView(renderUIView);
        }

        if (renderView == null) {
            return;
        }

        mRenderView = renderView;
        renderView.setAspectRatio(mCurrentAspectRatio);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            renderView.setVideoSize(mVideoWidth, mVideoHeight);
        }
        if (mVideoSarNum > 0 && mVideoSarDen > 0) {
            renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
        }

        View renderUIView = mRenderView.getView();
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        renderRootView.addView(renderUIView);

        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);
    }

    /**
     * 重新设置render渲染目标，该方法能达到抹去之前视频最后一帧的效果<br>
     * 一般在stopPlayBack后，设置新播放源之前调用。
     */
    public void reSetRender() {
        if (mUseTextureViewFirst && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TextureRenderView renderView = new TextureRenderView(getContext());
            if (mMediaPlayer != null) {
                renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
                renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
                renderView.setAspectRatio(mCurrentAspectRatio);
            }
            setRenderView(renderView);
        } else {
            SurfaceRenderView renderView = new SurfaceRenderView(getContext());
            setRenderView(renderView);
        }
    }


    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoPathWithToken(path, null);
    }

    /**
     * 设置百度加密视频的视频源和加密token
     *
     * @param path
     * @param token
     */
    public void setVideoPathWithToken(String path, String token) {
        this.mDrmToken = token;
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets specific headers.
     * 需在setVideoPath之前调用
     *
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    public void setHeaders(Map<String, String> headers) {
        mHeaders = headers;
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri the URI of the video.
     */
    private void setVideoURI(Uri uri) {
        mUri = uri;
        openVideo();
        requestLayout();
        invalidate();
    }

    /**
     * 停止播放并释放资源
     * 如果仅想停止播放，请调用pause()
     */
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            release(true);
        }
    }

    @TargetApi(14)
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);

        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        try {

            mMediaPlayer = createPlayer();
            if (!TextUtils.isEmpty(this.mDrmToken)) {
                mMediaPlayer.setDecryptTokenForHLS(mDrmToken);
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnTimedTextListener(mOnTimedTextListener);
            mMediaPlayer.setOnMetadataListener(mOnMetadataListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(mAppContext, mUri, mHeaders);
            bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setTimeoutInUs(15000000);
            mMediaPlayer.prepareAsync();
            sendCachingHintViewVisibilityMessage(true);

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            setCurrentState(PlayerState.STATE_PREPARING);
//            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            setCurrentState(PlayerState.STATE_ERROR);
//            mTargetState = STATE_ERROR;
            isTryToPlaying = false;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            setCurrentState(PlayerState.STATE_ERROR);
//            mTargetState = STATE_ERROR;
            isTryToPlaying = false;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } finally {
            // REMOVED: mPendingSubtitleTracks.clear();
        }
    }

    public BDCloudMediaPlayer createPlayer() {
        BDCloudMediaPlayer bdCloudMediaPlayer = new BDCloudMediaPlayer(this.getContext());

        bdCloudMediaPlayer.setLogEnabled(mLogEnabled); // 打开播放器日志输出
        bdCloudMediaPlayer.setDecodeMode(mDecodeMode); // 设置解码模式
        if (mInitPlayPositionInMilliSec > -1) {
            bdCloudMediaPlayer.setInitPlayPosition(mInitPlayPositionInMilliSec); // 设置初始播放位置
            mInitPlayPositionInMilliSec = -1; // 置为-1，防止影响下个播放源
        }
        if (mWakeMode > 0) {
            bdCloudMediaPlayer.setWakeMode(this.getContext(), mWakeMode);
        }
        if (mLeftVolume > -1 && mRightVolume > -1) {
            bdCloudMediaPlayer.setVolume(mLeftVolume, mRightVolume);
        }
        if (mCacheTimeInMilliSeconds > 0) {
            bdCloudMediaPlayer.setBufferTimeInMs(mCacheTimeInMilliSeconds); // 设置『加载中』的最长缓冲时间
        }

        if (mMaxProbeTimeInMs >= 0) {
            bdCloudMediaPlayer.setMaxProbeTime(mMaxProbeTimeInMs);
        }
        if (mMaxProbeSizeInBytes > 0) {
            bdCloudMediaPlayer.setMaxProbeSize(mMaxProbeSizeInBytes);
        }
        if (mMaxCacheSizeInBytes > 0) {
            bdCloudMediaPlayer.setMaxCacheSizeInBytes(mMaxCacheSizeInBytes);
        }
        if (mLooping) {
            bdCloudMediaPlayer.setLooping(mLooping);
        }
        if (mBufferSizeInBytes > 0) {
            bdCloudMediaPlayer.setBufferSizeInBytes(mBufferSizeInBytes);
        }

        if (mFrameChasing >= 0) {
            bdCloudMediaPlayer.toggleFrameChasing(mFrameChasing == 1);
        }

        bdCloudMediaPlayer.setSpeed(mSpeed);

        return bdCloudMediaPlayer;
    }

    /**
     * 设置"加载中"触发时，缓存多长时间的数据才结束
     * * 注意：若mMediaPlayer为null时，实际上会在createPlayer()中设置，这是mCacheTimeInMilliSeconds成员在此赋值的原因；
     *
     * @param milliSeconds
     */
    public void setBufferTimeInMs(int milliSeconds) {
        this.mCacheTimeInMilliSeconds = milliSeconds;
        if (mMediaPlayer != null) {
            mMediaPlayer.setBufferTimeInMs(mCacheTimeInMilliSeconds);
        }
    }

    /**
     * 设置"加载中"触发时，需要缓冲多大的数据才结束
     *
     * @param sizeInBytes
     */
    public void setBufferSizeInBytes(int sizeInBytes) {
        this.mBufferSizeInBytes = sizeInBytes;
        if (mMediaPlayer != null) {
            mMediaPlayer.setBufferSizeInBytes(mBufferSizeInBytes);
        }
    }

    /**
     * 设置是否循环播放
     *
     * @param isLoop
     */
    public void setLooping(boolean isLoop) {
        this.mLooping = isLoop;
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(mLooping);
        }
    }

    /**
     * 设置最大缓存数据大小
     *
     * @param sizeInBytes
     */
    public void setMaxCacheSizeInBytes(int sizeInBytes) {
        mMaxCacheSizeInBytes = sizeInBytes;
        if (mMediaPlayer != null) {
            mMediaPlayer.setMaxCacheSizeInBytes(mMaxCacheSizeInBytes);
        }
    }

    /**
     * 设置最大探测的数据大小
     *
     * @param maxProbeSizeInBytes
     */
    public void setMaxProbeSize(int maxProbeSizeInBytes) {
        mMaxProbeSizeInBytes = maxProbeSizeInBytes;
        if (mMediaPlayer != null) {
            mMediaPlayer.setMaxProbeSize(mMaxProbeSizeInBytes);
        }
    }

    /**
     * 设置最大探测时长
     * 类似于老接口 setFirstBufferingTime
     *
     * @param maxProbeTimeInMs
     */
    public void setMaxProbeTime(int maxProbeTimeInMs) {
        mMaxProbeTimeInMs = maxProbeTimeInMs;
        if (mMediaPlayer != null) {
            mMediaPlayer.setMaxProbeTime(mMaxProbeTimeInMs);
        }
    }

    public void setTimeoutInUs(int timeout) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setTimeoutInUs(timeout);
        }
    }

    /**
     * 设置左右声道的音量
     *
     * @param leftVolume
     * @param rightVolume
     */
    public void setVolume(float leftVolume, float rightVolume) {
        mLeftVolume = leftVolume;
        mRightVolume = rightVolume;
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(mLeftVolume, mRightVolume);
        }
    }

    /**
     * 设置播放速率，为保证机器兼容性，取值区间为 [0.1f, 1.9f]
     * 有的机器设置速率为2.0f时，会出现剧烈嘈杂声
     *
     * @param playSpeed
     */
    public void setSpeed(float playSpeed) {
        mSpeed = playSpeed;
        if (mMediaPlayer != null) {
            mMediaPlayer.setSpeed(mSpeed);
        }
    }

    /**
     * 设置唤醒Mode
     *
     * @param context
     * @param mode
     */
    public void setWakeMode(Context context, int mode) {
        mWakeMode = mode;
        if (mMediaPlayer != null) {
            mMediaPlayer.setWakeMode(context, mWakeMode);
        }
    }


    /**
     * 设置初始播放位置
     * 注意：若mMediaPlayer为null时，实际上会在createPlayer()中设置，这是mInitPlayPositionInMilliSec成员在此赋值的原因；
     *
     * @param milliSeconds
     */
    public void setInitPlayPosition(long milliSeconds) {
        mInitPlayPositionInMilliSec = milliSeconds;
        if (mMediaPlayer != null) {
            mMediaPlayer.setInitPlayPosition(mInitPlayPositionInMilliSec);
            // 设置给mediaplayer后重置为-1，防止影响新播放源的播放
            mInitPlayPositionInMilliSec = -1;
        }
    }


    /**
     * 设置是否显示日志
     * 注意：若mMediaPlayer为null时，实际上会在createPlayer()中设置，这是mLogEnable成员在此赋值的原因；
     *
     * @param enabled
     */
    public void setLogEnabled(boolean enabled) {
        mLogEnabled = enabled;
        if (mMediaPlayer != null) {
            mMediaPlayer.setLogEnabled(mLogEnabled);
        }
    }


    /**
     * 设置解码模式
     * 注意：若mMediaPlayer为null时，实际上会在createPlayer()中设置，这是mDecodeMode成员在此赋值的原因；
     *
     * @param decodeMode DECODE_AUTO(优先硬解，找不到硬解解码器则软解)或DECODE_SW(软解)
     */
    public void setDecodeMode(int decodeMode) {
        mDecodeMode = decodeMode;
        if (mMediaPlayer != null) {
            mMediaPlayer.setDecodeMode(mDecodeMode);
        }
    }

    /**
     * 设置是否开启追帧播放功能
     *
     * @param isEnable 是否开启
     */
    public void toggleFrameChasing(boolean isEnable) {
        mFrameChasing = isEnable ? 1 : 0;
        if (mMediaPlayer != null) {
            mMediaPlayer.toggleFrameChasing(isEnable);
        }
    }

    /**
     * 设置是否显示默认的『加载中』信息
     *
     * @param bShow
     */
    public void showCacheInfo(boolean bShow) {
        mbShowCacheInfo = bShow;
    }

    /**
     * 获取当前的mediaplayer，可能为null
     *
     * @return 返回当前的player对象，可能为null
     */
    public IMediaPlayer getCurrentMediaPlayer() {
        return mMediaPlayer;
    }

    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    Log.d(TAG, "onVideoSizeChanged width=" + width + ";height=" + height + ";sarNum="
                            + sarNum + ";sarDen=" + sarDen);
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    mVideoSarNum = mp.getVideoSarNum();
                    mVideoSarDen = mp.getVideoSarDen();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        if (mRenderView != null) {
                            mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                            mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                        }
                        // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };

    IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        public void onPrepared(IMediaPlayer mp) {
            Log.d(TAG, "onPrepared");
            setCurrentState(PlayerState.STATE_PREPARED);

            sendCachingHintViewVisibilityMessage(false);


            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            Log.d(TAG, "onPrepared: mVideoWidth=" + mVideoWidth + ";mVideoHeight="
                    + mVideoHeight + ";mSurfaceWidth=" + mSurfaceWidth + ";mSurfaceHeight=" + mSurfaceHeight);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                if (mRenderView != null) {
                    mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                    mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                    if (!mRenderView.shouldWaitForResize() || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        // We didn't actually change the size (it was already at the size
                        // we need), so we won't get a "surface changed" callback, so
                        // start the video here instead of in the callback.
                        if (isTryToPlaying) {
                            start();
                        }
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (isTryToPlaying) {
                    start();
                }
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener =
            new IMediaPlayer.OnCompletionListener() {
                public void onCompletion(IMediaPlayer mp) {
                    Log.d(TAG, "onCompletion");
                    sendCachingHintViewVisibilityMessage(false);
                    setCurrentState(PlayerState.STATE_PLAYBACK_COMPLETED);
                    isTryToPlaying = false;
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };

    private IMediaPlayer.OnInfoListener mInfoListener =
            new IMediaPlayer.OnInfoListener() {
                public boolean onInfo(IMediaPlayer mp, int arg1, int arg2) {
                    Log.d(TAG, "onInfo: arg1=" + arg1 + "; arg2=" + arg2);
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    switch (arg1) {
                        case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                            sendCachingHintViewVisibilityMessage(true);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                            Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                            sendCachingHintViewVisibilityMessage(false);
                            break;
                        case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                            Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                            Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                            Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                            Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                            Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                            Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                            break;
                        case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                            mVideoRotationDegree = arg2;
                            Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                            if (mRenderView != null)
                                mRenderView.setVideoRotation(arg2);
                            break;
                        case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                            Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                            break;
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "onError: " + framework_err + "," + impl_err);
                    setCurrentState(PlayerState.STATE_ERROR);
                    isTryToPlaying = false;

                    sendCachingHintViewVisibilityMessage(false);

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
//                    Log.d(TAG, "onBufferingUpdate: percent=" + percent);
                    mCurrentBufferPercentage = percent;
                    if (mOnBufferingUpdateListener != null) {
                        mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
                    }
                }
            };

    private IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            Log.d(TAG, "onSeekComplete");
            sendCachingHintViewVisibilityMessage(false);
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(mp);
            }
        }
    };

    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, BDTimedText text) {
            Log.d(TAG, "onTimedText text=" + text.getText());
            if (text != null) {
                subtitleDisplay.setText(text.getText());
            }
        }
    };

    private IMediaPlayer.OnMetadataListener mOnMetadataListener = new IMediaPlayer.OnMetadataListener() {
        @Override
        public void onMetadata(IMediaPlayer mp, Bundle meta) {
            for (String key : meta.keySet()) {
                Log.d(TAG, "onMetadata: key = " + key + ", value = " + meta.getString(key));
            }
        }
    };

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null)
            return;

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }

        holder.bindToMediaPlayer(mp);
    }

    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceChanged(IRenderView.ISurfaceHolder holder, int format, int w, int h) {
            Log.d(TAG, "mSHCallback onSurfaceChanged");
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceChanged: unmatched render callback\n");
                return;
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = isTryToPlaying;
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                start();
            }
        }

        @Override
        public void onSurfaceCreated(IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceCreated: unmatched render callback\n");
                return;
            }

            mSurfaceHolder = holder;
            if (mMediaPlayer != null)
                bindSurfaceHolder(mMediaPlayer, holder);
            else
                openVideo();
        }

        @Override
        public void onSurfaceDestroyed(IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceDestroyed: unmatched render callback\n");
                return;
            }

            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            releaseWithoutStop();
        }
    };

    private void releaseWithoutStop() {
        if (mMediaPlayer != null) {
            if (mRenderView instanceof SurfaceRenderView) {
                mMediaPlayer.setDisplay(null);
            }

        }

    }

    /**
     * 释放后不能再使用该BDCloudVideoView对象
     */
    public void release() {
        // 释放播放器player
        release(true);
        // 释放显示资源
        if (mRenderView != null) {
            mRenderView.release();
        }
    }

    /*
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.setDisplay(null);
            synchronized (mMediaPlayer) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            setCurrentState(PlayerState.STATE_IDLE);
            if (cleartargetstate) {
                isTryToPlaying = false;
            }
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }


    /**
     * 开始或继续播放
     * <p>
     * complete状态时不支持拖动，因为该函数重新prepareAsync了。
     * 为何complete需要重新prepare：有些情况下，直播中断会返回complete，此时需要重新prepareAsync.
     */
    @Override
    public void start() {
        if (mMediaPlayer != null && (mCurrentState == PlayerState.STATE_ERROR)
                || mCurrentState == PlayerState.STATE_PLAYBACK_COMPLETED) {

            // if your link is not live link, you can comment the following if block
            if (mCurrentState == PlayerState.STATE_PLAYBACK_COMPLETED) {
                // complete --> stop --> prepareAsync
                mMediaPlayer.stop();
            }

            mMediaPlayer.prepareAsync(); // will start() in onPrepared, because isTryToPlaying = true
            sendCachingHintViewVisibilityMessage(true);
            setCurrentState(PlayerState.STATE_PREPARING);
        } else if (isInPlaybackState()) {
            mMediaPlayer.start();
            setCurrentState(PlayerState.STATE_PLAYING);
        }
        isTryToPlaying = true;
    }

    /**
     * 暂停播放
     */
    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                setCurrentState(PlayerState.STATE_PAUSED);
            }
        }
        isTryToPlaying = false;
    }

    public String getCurrentPlayingUrl() {
        if (this.mUri != null) {
            return this.mUri.toString();
        }
        return null;
    }

    /**
     * 获得视频时长，单位为毫秒！
     *
     * @return
     */
    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }

        return 0;
    }

    /**
     * 获取当前播放进度，单位为毫秒！
     *
     * @return
     */
    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 将播放器指定到某个播放位置
     *
     * @param msec
     */
    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            this.sendCachingHintViewVisibilityMessage(true);
        }
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != PlayerState.STATE_ERROR &&
                mCurrentState != PlayerState.STATE_IDLE &&
                mCurrentState != PlayerState.STATE_PREPARING);
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    /**
     * 获取视频宽度
     *
     * @return
     */
    public int getVideoWidth() {
        return mVideoWidth;
    }

    /**
     * 获取视频高度
     *
     * @return
     */
    public int getVideoHeight() {
        return mVideoHeight;
    }

    /**
     * 设置视频拉伸模式
     * 可设置以下三种：
     * 填充模式 VIDEO_SCALING_MODE_SCALE_TO_FIT
     * 裁剪模式 VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
     * 铺满模式 VIDEO_SCALING_MODE_SCALE_TO_MATCH_PARENT
     *
     * @param mode
     */
    public void setVideoScalingMode(int mode) {
        if (mode == VIDEO_SCALING_MODE_SCALE_TO_FIT || mode == VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                || mode == VIDEO_SCALING_MODE_SCALE_TO_MATCH_PARENT) {
            if (mode == VIDEO_SCALING_MODE_SCALE_TO_FIT) {
                // 填充
                mCurrentAspectRatio = IRenderView.AR_ASPECT_FIT_PARENT;
            } else if (mode == VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING) {
                // 裁剪
                mCurrentAspectRatio = IRenderView.AR_ASPECT_FILL_PARENT;
            } else {
                // 铺满
                mCurrentAspectRatio = IRenderView.AR_MATCH_PARENT;
            }
            if (mRenderView != null) {
                mRenderView.setAspectRatio(mCurrentAspectRatio);
            }
        } else {
            Log.e(TAG, "setVideoScalingMode: param should be VID");
        }
    }

    /**
     * 获取多码率的字串数组
     * 每个String的格式为
     *
     * @return
     */
    public String[] getVariantInfo() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVariantInfo();
        }
        return null;
    }

    /**
     * 获取当前网络内容下载速率
     *
     * @return
     */
    public long getDownloadSpeed() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDownloadSpeed();

        }
        return 0L;
    }

    /**
     * 多分辨率切换（仅适用于Master m3u8文件）
     * mMediaPlayer.selectResolutionByIndex函数处理过程中，mediaplayer的内部逻辑会stop-->选择码率-->prepareAsync
     * 当处理完成后，您会收到一个onPrepared事件
     *
     * @param index 该index为getVariantInfo()数组的合法下标
     */
    public boolean selectResolutionByIndex(int index) {
        boolean selectRight = false;
        if (mMediaPlayer != null) {
            this.sendCachingHintViewVisibilityMessage(true);
            selectRight = mMediaPlayer.selectResolutionByIndex(index);
            if (!selectRight) {
                this.sendCachingHintViewVisibilityMessage(false);
            }
        }
        return selectRight;
    }

    /**
     * 获取截图接口
     * <p>
     * 截图原理：
     * TextureView系统接口支持getBitmap截图
     * SurfaceView暂不支持截图(影响4.0及以下系统的用户)
     */
    public Bitmap getBitmap() {
        if (mRenderView != null) {
            return mRenderView.getBitmap();
        }
        return null;
    }

    public static void setAK(String ak) {
        BDCloudMediaPlayer.setAK(ak);
    }

    /**
     * tell BDCloudVideoView that activity will be put into background
     * should invoke before super.onStop() in an activity
     * avoid video frame blocked in small probability on some device when using TextureRenderView
     */
    public void enterBackground() {
        if (mRenderView != null && !(mRenderView instanceof SurfaceRenderView)) {
            renderRootView.removeView(mRenderView.getView());
        }
    }

    /**
     * tell BDCloudVideoView that activity will come back to foreground
     * avoid video frame blocked in small probability on flyme device when using TextureRenderView
     */
    public void enterForeground() {
        if (mRenderView != null && !(mRenderView instanceof SurfaceRenderView)) {
            View renderUIView = mRenderView.getView();
            // getParent() == null : is removed in enterBackground()
            if (renderUIView.getParent() == null) {
                LayoutParams lp = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);
                renderUIView.setLayoutParams(lp);
                renderRootView.addView(renderUIView);
            } else {
                Log.d(TAG, "enterForeground; but getParent() is not null");
            }

        }
    }
}
