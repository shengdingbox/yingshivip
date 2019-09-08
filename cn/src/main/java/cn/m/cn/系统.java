package cn.m.cn;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by H19 on 2018/6/21 0021.
 */

public class 系统 {

    /*

        系统UI 相关

     */
    // 1 竖屏 2 横屏（固定） 3 横屏（随屏幕转向）
    public static void 设置屏幕方向(Activity activity,int 方向,boolean 显示状态栏){
        switch (方向){
            case 1:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 2:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 3:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
        }
    }
    public static void 隐藏状态栏(Activity activity,boolean b){

    }
//    public static void 隐藏虚拟按键(Activity aty,boolean b) {
//        if (b){
//            Window window = aty.getWindow();
//            WindowManager.LayoutParams params = window.getAttributes();
//            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//            window.setAttributes(params);
//        }else {
//            Window window = aty.getWindow();
//            WindowManager.LayoutParams params = window.getAttributes();
//            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//            window.setAttributes(params);
//        }
//
//    }



    public static void hideNavigationBar(Window window) {
        // TODO Auto-generated method stub
        final View decorView = window.getDecorView();
        final int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }else {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
        }else {
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        decorView.setSystemUiVisibility(flags);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
    }


    // 剪辑版
    public static void 置剪辑版文本(Activity activity,String t){
        ClipboardManager cmb = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(t);
    }





}
