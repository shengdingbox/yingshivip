package cn.m.cn;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.DeflaterInputStream;

/**
 * Created by H19 on 2018/6/4 0004.
 */

public class 文件 {
    public static boolean 创建目录(Activity activity, String dir){
        File file = new File(dir);
        if (!file.exists()){
            权限.申请文件操作权限(activity);
            return file.mkdirs();
        }
        return true;
        // 已经有了则不操作
    }
    public static boolean 是否存在(String filePath){
        try {
            File f = new File(filePath);
            if(f.exists()) {
                return true;
            }else {
                return false;
            }
        }
        catch (Exception e) {
            Log.i("eee-e.exc",e.toString());
            return false;

        }
    }
    public static String 读取文本内容(Activity activity,String path){
        if (activity == null) return "";
        权限.申请文件操作权限(activity);
        try {
            File file = new File(path);
            FileInputStream is = new FileInputStream(file);
            byte[] b = new byte[is.available()];
            is.read(b);
            String result = new String(b);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean 写出文本文件(Activity activity,String dir,String fileName,String con){
        权限.申请文件操作权限(activity);
        File file = new File(dir + fileName);
        File fileWrite = new File(dir + File.separator + fileName);
        try{
            if (!fileWrite.exists()) {
                if (!fileWrite.createNewFile()) {   // 文件不存在则创建文件
                    //Toast.makeText(getApplicationContext(), "文件创建失败", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            // 首先判断文件是否存在

            // 实例化对象：文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(fileWrite);
            // 写入文件
            fileOutputStream.write(con.getBytes());
            // 清空输出流缓存
            fileOutputStream.flush();
            // 关闭输出流
            fileOutputStream.close();
            return true;

        }catch (Exception e){

        }

        return false;
    }
    public static boolean 写出图片文件(Activity activity,String dir,String fileName,Bitmap bit){
        权限.申请文件操作权限(activity);
        File file = new File(dir + fileName);
        File fileWrite = new File(dir + File.separator + fileName);
        try{
            if (!fileWrite.exists()) {
                if (!fileWrite.createNewFile()) {   // 文件不存在则创建文件
                    //Toast.makeText(getApplicationContext(), "文件创建失败", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            // 实例化对象：文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(fileWrite);


            bit.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            // 写入文件
            //fileOutputStream.write();
            // 清空输出流缓存
            fileOutputStream.flush();
            // 关闭输出流
            fileOutputStream.close();
            return true;

        }catch (Exception e){

        }

        return false;
    }
}
