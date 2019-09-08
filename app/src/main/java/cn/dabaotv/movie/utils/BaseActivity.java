package cn.dabaotv.movie.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;



/**
 * Created by 奈蜇 on 2018/7/18.
 * 活动基础类
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //是否设置Activity布局文件的FitsSystemWindows,控件是否从状态栏下面开始.
        if (setFitsSystemWindows()) {
            //统一设置xml属性
            ViewGroup contentFrameLayout = findViewById(Window.ID_ANDROID_CONTENT);
            View parentView = contentFrameLayout.getChildAt(0);
            if (parentView != null) {
                parentView.setFitsSystemWindows(true);
            }
        }

        Log.v(TAG, "onCreate:  " + this.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v(TAG, "onStart:  " + this.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume:  " + this.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause:  " + this.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.v(TAG, "onStop:  " + this.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy:  " + this.toString());
    }
    /**
     * 是否设置Activity布局文件的FitsSystemWindows
     *
     * @return boolean
     */
    abstract protected boolean setFitsSystemWindows();


}
