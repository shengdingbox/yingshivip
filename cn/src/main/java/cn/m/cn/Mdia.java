package cn.m.cn;

import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

/**
 * Created by H19 on 2018/8/15 0015.
 */

public class Mdia {

    private Activity aty;
    private View parent;
    public Dialog dialog;
    public FrameLayout mFrame;

    public Mdia inin(Activity ctx){
        aty = ctx;
        parent = ((ViewGroup) aty.findViewById(android.R.id.content)).getChildAt(0);
        return this;
    }

    public Dialog show(View view,int gravity){

        if (dialog != null){
            dialog.dismiss();
            dialog.cancel();
            dialog = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(aty);
        dialog = new Dialog(aty, R.style.Theme_Light_Dialog);
        //获得dialog的window窗口
        Window window = dialog.getWindow();
        //设置dialog在屏幕底部
        window.setGravity(Gravity.BOTTOM);
        //设置dialog弹出时的动画效果，从屏幕底部向上弹出
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0, 0, 0, 0);
        //获得window窗口的属性
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        //设置窗口宽度为充满全屏
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0.0f;
        //设置窗口高度为包裹内容
        //lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        //将设置好的属性set回去
        window.setAttributes(lp);
        //将自定义布局加载到dialog上
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return dialog;
    }
    public Dialog show(View view,int gravity,int width,int height){
        if (dialog != null){
            dialog.dismiss();
            dialog.cancel();
            dialog = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(aty);
        dialog = new Dialog(aty, R.style.Theme_Light_Dialog);
        Window window = dialog.getWindow();
        window.setGravity(gravity);
        //设置dialog弹出时的动画效果，从屏幕底部向上弹出
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0, 0, 0, 0);
        //获得window窗口的属性
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = width;
        lp.height = height;
        lp.dimAmount = 0.0f;
        //将设置好的属性set回去
        window.setAttributes(lp);
        //将自定义布局加载到dialog上
        mDiaView = view;
        dialog.setContentView(view);
        //if( dialog.getWindow() != null) dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return dialog;
    }

    private View mDiaView;
    public Dialog show2(View view,int gravity,int width,int height){
        if (dialog == null){
            return show(view,gravity,width,height);
        }

        Window window = dialog.getWindow();
        window.setGravity(gravity);
        //设置dialog弹出时的动画效果，从屏幕底部向上弹出
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0, 0, 0, 0);
        //获得window窗口的属性
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        //设置窗口宽度为充满全屏
        lp.width = width;
        lp.height = height;
        lp.dimAmount = 0.0f;
        //将设置好的属性set回去
        window.setAttributes(lp);
        //将自定义布局加载到dialog上
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return dialog;
    }

    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = aty.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        aty.getWindow().setAttributes(lp);
    }

    public void hide(){
        if(dialog != null)
            dialog.dismiss();
    }


}
