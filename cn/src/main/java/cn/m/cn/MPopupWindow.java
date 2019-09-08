package cn.m.cn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

public class MPopupWindow {
    public static PopupWindow popupWindow;
    private DismissCallBack dismissCallBack;
    private FrameLayout frameLayout;
    private int gravity;
    private int screenWidth;
    private int screenHight;
    private boolean hasSetGravity;
    private Context context;

    public MPopupWindow(Context context) {
        popupWindow = new PopupWindow();
        this.frameLayout = new FrameLayout(context);
        //this.frameLayout.setBackgroundColor(ContextCompat.getColor(context, color.defaultBackground));
        this.frameLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MPopupWindow.popupWindow.dismiss();
            }
        });
        this.screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.screenHight = context.getResources().getDisplayMetrics().heightPixels;
        this.gravity = 17;
        this.context = context;
    }

    public void setDismissListener(DismissCallBack listener) {
        this.dismissCallBack = listener;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
        this.hasSetGravity = true;
    }

    public PopupWindow showOnBottom(View parentView, View contentView, int style, int width, int height, int xOff, int yOff) {
        this.frameLayout.removeAllViews();
        this.frameLayout.addView(contentView, new LayoutParams(width, height));
        this.setContentViewLayoutParams(contentView, xOff, yOff);
        this.createPopupWindow(this.frameLayout, -1, -1, style);
        popupWindow.showAsDropDown(parentView, 0, 0);
        return popupWindow;
    }

    public void showOnTop(View parentView, View contentView, int style, int width, int height, int xOff, int yOff) {
        int[] location = new int[2];
        parentView.getLocationOnScreen(location);
        this.frameLayout.removeAllViews();
        this.frameLayout.addView(contentView, new LayoutParams(width, height));
        this.setContentViewLayoutParams(contentView, xOff, yOff);
        this.createPopupWindow(this.frameLayout, -1, location[1], style);
        popupWindow.showAtLocation(parentView, 0, location[0], location[1] - popupWindow.getHeight());
    }

    public void showOnLeft(View parentView, View contentView, int style, int width, int height, int xOff, int yOff) {
        int[] location = new int[2];
        parentView.getLocationOnScreen(location);
        this.frameLayout.removeAllViews();
        this.frameLayout.addView(contentView, new LayoutParams(width, height));
        this.setContentViewLayoutParams(contentView, xOff, yOff);
        this.createPopupWindow(this.frameLayout, location[0], -1, style);
        popupWindow.showAtLocation(parentView, 0, location[0] - popupWindow.getWidth(), 0);
    }

    public void showOnRight(View parentView, View contentView, int style, int width, int height, int xOff, int yOff) {
        int[] location = new int[2];
        parentView.getLocationOnScreen(location);
        this.frameLayout.removeAllViews();
        this.frameLayout.addView(contentView, new LayoutParams(width, height));
        this.setContentViewLayoutParams(contentView, xOff, yOff);
        this.createPopupWindow(this.frameLayout, this.screenWidth - location[0] - parentView.getWidth(), -1, style);
        popupWindow.showAtLocation(parentView, 0, location[0] + parentView.getMeasuredWidth(), 0);
    }

    public void showOnScreenBottom(View parentView, View contentView, int style, int width, int height, int xOff, int yOff) {
        this.frameLayout.removeAllViews();
        this.frameLayout.addView(contentView, new LayoutParams(width, height));
        this.setGravity(81);
        this.setContentViewLayoutParams(contentView, xOff, yOff);
        this.createPopupWindow(this.frameLayout, -1, -1, style);
        popupWindow.showAtLocation(parentView, 0, 0, 0);
    }

    public void showFullScreen(View parentView, View contentView, int style, int width, int height, int xOff, int yOff) {
        this.frameLayout.removeAllViews();
        this.frameLayout.addView(contentView, new LayoutParams(width, height));
        this.setContentViewLayoutParams(contentView, xOff, yOff);
        this.createPopupWindow(this.frameLayout, -1, -1, style);
        popupWindow.showAtLocation(parentView, 0, 0, 0);
    }

    private void tryClearGravity() {
        if(!this.hasSetGravity) {
            this.gravity = 17;
        }

    }

    private void setContentViewLayoutParams(View contentView, int xOff, int yOff) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)contentView.getLayoutParams();
        this.tryClearGravity();
        this.hasSetGravity = false;
        lp.gravity = this.gravity;
        if((this.gravity & 5) == 5) {
            lp.rightMargin = -xOff;
        } else {
            lp.leftMargin = xOff;
        }

        if((this.gravity & 80) == 80) {
            lp.bottomMargin = -yOff;
        } else {
            lp.topMargin = yOff;
        }

        contentView.setLayoutParams(lp);
        contentView.setOnClickListener((OnClickListener)null);
    }

    private void createPopupWindow(View popupView, int width, int height, int animationStyle) {
        popupWindow = new PopupWindow(popupView, width, height, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                if(MPopupWindow.this.dismissCallBack != null) {
                    MPopupWindow.this.dismissCallBack.dismissCallBack();
                }

            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable(this.context.getResources(), (Bitmap)null));
        popupWindow.setAnimationStyle(animationStyle);
    }

    public interface DismissCallBack {
        void dismissCallBack();
    }
}
