package cn.m.cn;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by H19 on 2018/7/9 0009.
 */

public class 应用 {

    // 按包名打开指定应用
    public static boolean 打开(Context context,String t){
        try{
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(t);
            context.startActivity(intent);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean 是否存在(Context context,String packageName) {
        if (packageName == null || "".equals(packageName)){
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
