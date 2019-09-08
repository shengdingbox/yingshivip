package cn.m.cn;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by H19 on 2018/7/16 0016.
 */

public class 控件 {
    public static void set水波纹点击效果(Context ctx, View v){
        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        int[] attribute = new int[]{android.R.attr.selectableItemBackground};
        TypedArray typedArray = ctx.getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(typedArray.getDrawable(0));
        }
    }

    public static void setImgTint(Context ctx, ImageView img){


    }
}
