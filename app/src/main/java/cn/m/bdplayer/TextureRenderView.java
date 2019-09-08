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
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.baidu.cloud.media.player.IMediaPlayer;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TextureRenderView extends TextureView implements IRenderView {
    private static final String TAG = "TextureRenderView";
    private MeasureHelper mMeasureHelper;

    public TextureRenderView(Context context) {
        super(context);
        initView(context);
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallback(this);
        setSurfaceTextureListener(mSurfaceCallback);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean shouldWaitForResize() {
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
//        release(); // release surface when detach. if uncomment, not support float window
    }

    // --------------------
    // Layout & Measure
    // --------------------
    @Override
    public void setVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth > 0 && videoHeight > 0) {
            mMeasureHelper.setVideoSize(videoWidth, videoHeight);
            requestLayout();
        }
    }

    @Override
    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        if (videoSarNum > 0 && videoSarDen > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(videoSarNum, videoSarDen);
            requestLayout();
        }
    }

    @Override
    public void setVideoRotation(int degree) {
        mMeasureHelper.setVideoRotation(degree);
        setRotation(degree);
    }

    @Override
    public void setAspectRatio(int aspectRatio) {
        mMeasureHelper.setAspectRatio(aspectRatio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    private int mMediaPlayerCode = 0;

    public void setCurrentMediaPlayerCode(int currentCode) {
        mMediaPlayerCode = currentCode;
    }

    public int getCurrentMediaPlayerCode() {
        return mMediaPlayerCode;
    }

    private SurfaceTexture lastSurfaceTexture;

    public SurfaceTexture getLastSurfaceTexture() {
        return lastSurfaceTexture;
    }

    public void setLastSurfaceTexture(SurfaceTexture surfaceTexture) {
        lastSurfaceTexture = surfaceTexture;
    }

    // --------------------
    // TextureViewHolder
    // --------------------

    public ISurfaceHolder getSurfaceHolder() {
        return new InternalSurfaceHolder(this);
    }

    private static final class InternalSurfaceHolder implements ISurfaceHolder {
        private TextureRenderView mTextureView;

        public InternalSurfaceHolder(TextureRenderView textureView) {
            mTextureView = textureView;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void bindToMediaPlayer(IMediaPlayer mp) {
            if (mp == null || mTextureView.getSurfaceTexture() == null) {
                // if reSetRender is invoked twice during one playing, surfaceTexture maybe null
                return;
            }
            // Tricky: 由 Object 类定义的 hashCode 方法确实会针对不同的对象返回不同的整数。
            // （这一般是通过将该对象的内部地址转换成一个整数来实现的，但是 JavaTM 编程语言不需要这种实现技巧。）
            if (mp.hashCode() != mTextureView.getCurrentMediaPlayerCode()) {
                // new player
                mp.setSurface(openSurface());
            } else {
                // old player
                if (!mTextureView.getLastSurfaceTexture().equals(mTextureView.getSurfaceTexture())) {
                    mTextureView.setSurfaceTexture(mTextureView.getLastSurfaceTexture());
                }
            }
            mTextureView.setCurrentMediaPlayerCode(mp.hashCode());
        }

        @Override
        public IRenderView getRenderView() {
            return mTextureView;
        }

        @Override
        public Surface openSurface() {
            return new Surface(mTextureView.getSurfaceTexture());
        }
    }

    // -------------------------
    // SurfaceHolder.Callback
    // -------------------------

    @Override
    public void addRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.addRenderCallback(callback);
    }

    @Override
    public void removeRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.removeRenderCallback(callback);
    }

    @TargetApi(16)
    @Override
    public void release() {
        if (lastSurfaceTexture != null) {
            if (this.isAvailable()) {
                // let textureview to release surfacetexture
                mSurfaceCallback.setOwnSurfaceTexture(true);
            } else {
                lastSurfaceTexture.release();
                lastSurfaceTexture = null;
            }
        }
    }

    private SurfaceCallback mSurfaceCallback;

    private static final class SurfaceCallback implements SurfaceTextureListener {
        private SurfaceTexture mSurfaceTexture;
        private boolean mIsFormatChanged;
        private int mWidth;
        private int mHeight;

        private volatile boolean mOwnSurfaceTexture = false;
//        private boolean mWillDetachFromWindow = false;
//        private boolean mDidDetachFromWindow = false;

        private WeakReference<TextureRenderView> mWeakRenderView;
        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap<IRenderCallback, Object>();

        public SurfaceCallback(TextureRenderView renderView) {
            mWeakRenderView = new WeakReference<TextureRenderView>(renderView);
        }

        public void setOwnSurfaceTexture(boolean ownSurfaceTexture) {
            mOwnSurfaceTexture = ownSurfaceTexture;
        }

        public void addRenderCallback(IRenderCallback callback) {
            mRenderCallbackMap.put(callback, callback);

            ISurfaceHolder surfaceHolder = null;
            if (mSurfaceTexture != null) {
                if (surfaceHolder == null) {
                    surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get());
                }
                callback.onSurfaceCreated(surfaceHolder, mWidth, mHeight);
            }

            if (mIsFormatChanged) {
                if (surfaceHolder == null) {
                    surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get());
                }
                callback.onSurfaceChanged(surfaceHolder, 0, mWidth, mHeight);
            }
        }

        public void removeRenderCallback(IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @TargetApi(16)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            if (mWeakRenderView.get() == null) {
                // should never come in
                Log.e(TAG, "!!!!!Too bad, textureview in callback is released. function will not work normally");
            } else {
                // only set once
                if (mWeakRenderView.get().getLastSurfaceTexture() == null) {
                    mWeakRenderView.get().setLastSurfaceTexture(surface);
                }
            }

            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get());
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            mIsFormatChanged = true;
            mWidth = width;
            mHeight = height;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get());
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceChanged(surfaceHolder, 0, width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceTexture = surface;
            mIsFormatChanged = false;
            mWidth = 0;
            mHeight = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get());
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(surfaceHolder);
            }

            return mOwnSurfaceTexture;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }

    }

    // --------------------
    // Accessibility
    // --------------------

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TextureRenderView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TextureRenderView.class.getName());
    }
}
