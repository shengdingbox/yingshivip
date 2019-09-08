package cn.m.cn;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by H19 on 2018/8/16 0016.
 */

public class Cnview {
    public static void setTint(ImageView image,int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image.setImageTintList(ColorStateList.valueOf(color));
        }
    }

    // 直接用 color 文件中的图片
    public static void setTint(Context ctx,ImageView image,int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            image.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx,color)));

        }
    }

    public static void Tablayout_setTabLine(TabLayout e, int padding, int margin){
        try {
            int padding2px = 像素.dip2px(e.getContext(),padding);
            int margin2px = 像素.dip2px(e.getContext(),margin);
            //拿到tabLayout的mTabStrip属性
            LinearLayout mTabStrip = (LinearLayout) e.getChildAt(0);
            int dp10 = 像素.dip2px(e.getContext(), 15);
            int dp3 = 像素.dip2px(e.getContext(), 3);
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                View tabView = mTabStrip.getChildAt(i);
                //拿到tabView的mTextView属性  tab的字数不固定一定用反射取mTextView
                Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                mTextViewField.setAccessible(true);
                TextView mTextView = (TextView) mTextViewField.get(tabView);
                tabView.setPadding(0, 0, 0, 0);
                //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                int width = 0;
                width = mTextView.getWidth();
                if (width == 0) {
                    mTextView.measure(0, 0);
                    width = mTextView.getMeasuredWidth();
                }
                //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                params.width = width;
                params.leftMargin = margin2px;
                params.rightMargin = margin2px;
                tabView.setLayoutParams(params);
                tabView.invalidate();
            }
        } catch (NoSuchFieldException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
    } // 设置tablayout 指示器宽度适应字体



}
