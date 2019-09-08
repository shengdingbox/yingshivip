package cn.m.cn;

import android.app.Activity;

/**
 * Created by H19 on 2018/8/14 0014.
 */

public class Actionbar {
    public static int getStatusBarHeight(Activity aty) {
        int result = 0;
        int resourceId = aty.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = aty.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
