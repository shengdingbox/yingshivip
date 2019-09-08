package cn.m.cn;

import android.app.Activity;
import android.view.WindowManager;

/**
 * Created by H19 on 2018/6/25 0025.
 */

public class 状态栏 {
    public static void 隐藏(Activity activity){
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
    }
    public static void 显示(Activity activity){
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //显示状态栏
    }
}
