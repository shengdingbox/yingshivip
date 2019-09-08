package cn.m.cn;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;


/**
 * Created by H19 on 2018/6/10 0010.
 */

public class 信息框 {
    public String t1;
    private static Dialog dialog = null;
    public static Dialog 自定义(Context context, View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(v);

        // 宽度全屏
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }
    public static void 输入框(Context context,String title,String hint,final diaListener diaListener){
        View v = View.inflate(context,R.layout.view_input_box,null);
        final EditText et_input = v.findViewById(R.id.input);
        et_input.setHint(hint);
        ((TextView)v.findViewById(R.id.title)).setText(title);
        v.findViewById(R.id.enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaListener.text(et_input.getText().toString());
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });
        dialog = 自定义(context,v);
    }
    public static void 输入框2(Context context,String title,String text,final diaListener diaListener){
        View v = View.inflate(context,R.layout.view_input_box,null);
        final EditText et_input = v.findViewById(R.id.input);
        et_input.setText(text);
        ((TextView)v.findViewById(R.id.title)).setText(title);
        v.findViewById(R.id.enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaListener.text(et_input.getText().toString());
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });
        dialog = 自定义(context,v);
    }

    // 数字
    public static void 输入框3(Context context,String title,String hint,final diaListener diaListener){
        View v = View.inflate(context,R.layout.view_input_box,null);
        final EditText et_input = v.findViewById(R.id.input);
        et_input.setInputType(InputType.TYPE_CLASS_NUMBER);
        et_input.setHint(hint);
        ((TextView)v.findViewById(R.id.title)).setText(title);
        v.findViewById(R.id.enter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaListener.text(et_input.getText().toString());
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });
        dialog = 自定义(context,v);
    }
    public static void 文本框(Context context,String title,String con,String bt0,String bt1,final OnClickListener onClickListener){
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        if (title != null) normalDialog.setTitle(title);
        normalDialog.setMessage(con);

        if (bt0 != null){
            normalDialog.setPositiveButton(bt0,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                            onClickListener.onClick(0,dialog);
                        }
                    });
        }

        if (bt1 != null){
            normalDialog.setNegativeButton(bt1,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                            onClickListener.onClick(1,dialog);
                        }
                    });
            // 显示
            normalDialog.show();
        }
    }

    public interface OnClickListener{
        void onClick(int bt,DialogInterface v);
    }

    public interface diaListener{
        void text(String t);
    }
    private listListener mListListener;
    public void setItemListener(listListener listener){
        mListListener = listener;
    }
    private interface listListener{
        void itenOnClickListener(String name,int position);
    }
    private static PopupWindow popWin;
    public static PopupWindow showPopunWinidow(Activity atx,View view,View attachOnView){
        View parent = ((ViewGroup) atx.findViewById(android.R.id.content)).getChildAt(0);
        int width = atx.getResources().getDisplayMetrics().widthPixels;
        int height = atx.getResources().getDisplayMetrics().heightPixels;

        if (popWin != null) popWin.dismiss();

        popWin = new PopupWindow(view,width,ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popWin.setFocusable(true);
        popWin.setOutsideTouchable(true);

        popWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                popWin.dismiss();
                popWin = null;// 当点击屏幕时，使popupWindow消失
            }
        });

        ColorDrawable dw = new ColorDrawable(0x33000000);
        popWin.setBackgroundDrawable(dw);

        int x = 0,y = 0;
        y = attachOnView.getHeight() + attachOnView.getTop();
        popWin.setAnimationStyle(R.style.AnimTOP);
        popWin.showAsDropDown(attachOnView); // 指定在该控件的下方

         popWin.showAtLocation(parent, Gravity.NO_GRAVITY, 0, 0);
        return popWin;
    }

    public static Dialog 等待框(Context ctx,String t){
        View view = View.inflate(ctx,R.layout.dia_loading,null);
        Dialog dia = 自定义(ctx,view);
        return dia;
    }



}
