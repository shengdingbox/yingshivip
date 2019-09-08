package cn.m.cn;

import android.content.Context;

/**
 * Created by H19 on 2018/6/10 0010.
 */

public class 像素 {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static int dip2px(Context context,int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp*density+0.5);
    }

    /** px转换dip */
    public static int px2dip(Context context,int px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
    /** px转换sp */
    public static int px2sp(Context context,int pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
    /** sp转换px */
    public static int sp2px(Context context,int spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
