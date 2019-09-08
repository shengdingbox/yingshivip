package cn.m.cn;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.m.cn.文件;

/**
 * Created by H19 on 2018/7/17 0017.
 */

public class Function {
    public static boolean SD_是否存在(String fileName){return 文件.是否存在(fileName);}
    public static String SD_读取文件(Activity aty, String path){return  文件.读取文本内容(aty,path);}
    public static boolean SD_写出文件(Activity activity,String dir,String fileName,String con){return 文件.写出文本文件(activity,dir,fileName,con);}
    public static boolean SD_写出文件(Activity activity,String dir,String fileName,Bitmap bit){return 文件.写出图片文件(activity,dir,fileName,bit);}
    public static boolean SD_创建目录(Activity activity, String dir){return 文件.创建目录(activity,dir);}

    public static int  getResource(Context ctx,String name,String defType){
        int resId = ctx.getResources().getIdentifier(name, defType, ctx.getPackageName());
        //如果没有在"mipmap"下找到imageName,将会返回0
        return resId;
    }
    public static String getMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public static String getMD5(String text){
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(text.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public static Object 深度复制2(Object o){
        Object o1 = null;
        try {
            o1 = 深度复制(o);
        }catch (Exception io){

        }
        return o1;
    }
    public static Object 深度复制(Object o) throws IOException, ClassNotFoundException {
//		//先序列化，写入到流里
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(o);
        //然后反序列化，从流里读取出来，即完成复制
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return oi.readObject();
    }

    public static Object deepClone(Object object) throws IOException, OptionalDataException,ClassNotFoundException {
        // 将对象写到流里
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(object);
        // 从流里读出来
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return (oi.readObject());
    }

    // 获取底部功能栏高度
    public static float 获取虚拟键盘高度(Activity aty) {
        float result = 0;
        int resourceId = aty.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = aty.getResources().getDimension(resourceId);
        }
        return result;
    }

    public static boolean 是否有虚拟键盘(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;

    }


    // ------------ 缓存相关 ----------------------------
    public static void saveCache(Context context,String name,String data){
        try{
            FileOutputStream out = context.openFileOutput(name, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getCache(Context context,String name){
        StringBuilder content = new StringBuilder();
        try{
            FileInputStream in = context.openFileInput(name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null){
                content.append(line);
            }
        }catch (IOException e){

        }
        return content.toString();
    }

    public void 删除缓存数据(){

    }


}
