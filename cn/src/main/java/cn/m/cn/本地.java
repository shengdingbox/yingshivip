package cn.m.cn;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by H19 on 2018/6/5 0005.
 */

public class 本地 {
    public final static String SETTING = "Setting";

    public static void save设置(Context context, String key, int value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                .edit();
        sp.putInt(key, value);
        sp.commit();
    }
    public static void save设置(Context context, String key, boolean value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                .edit();
        sp.putBoolean(key, value);
        sp.commit();
    }
    public static void save设置(Context context, String key, String value) {
        SharedPreferences.Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                .edit();
        sp.putString(key, value);
        sp.commit();
    }
    public static int get设置(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        int value = sp.getInt(key, defValue);
        return value;
    }
    public static boolean get设置(Context context, String key, boolean defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING,
                Context.MODE_PRIVATE);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }
    public static String get设置(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING,
                Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        if (value == null) value = defValue;
        return value;
    }




}
