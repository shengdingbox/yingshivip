package cn.m.cn;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

/**
 * Created by H19 on 2018/8/15 0015.
 */

public class Mpop {

    private Activity aty;
    private View parent;
    public PopupWindow popupWindow;
    public FrameLayout mFrame;

    public Mpop inin(Activity ctx){
        aty = ctx;
        parent = ((ViewGroup) aty.findViewById(android.R.id.content)).getChildAt(0);
        mFrame = new FrameLayout(aty);
        mFrame.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));

        return this;
    }

    public void show(View view){
        if (popupWindow != null) popupWindow.dismiss();
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setContentView(view);
        //点击空白区域PopupWindow消失，这里必须先设置setBackgroundDrawable，否则点击无反应
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x30000000));
        popupWindow.setOutsideTouchable(true);
        //设置PopupWindow动画
        //popupWindow.setAnimationStyle(R.style.AnimDown);

        //设置是否允许PopupWindow的范围超过屏幕范围
        popupWindow.setClippingEnabled(true);

        //设置PopupWindow消失监听
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        backgroundAlpha(1f);
        popupWindow.setFocusable(true); // EditText 无法输入问题解决
        //PopupWindow在targetView下方弹出
        popupWindow.showAtLocation(parent, Gravity.BOTTOM,0,0);
    }

    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = aty.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        aty.getWindow().setAttributes(lp);
    }



}
