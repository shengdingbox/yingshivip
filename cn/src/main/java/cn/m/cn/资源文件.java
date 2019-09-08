package cn.m.cn;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by H19 on 2018/7/1 0001.
 */

public class 资源文件 {
    // 读取assets中的文本
    public static String getAssetsString(Context ctx,String name) {
        InputStream abpath = ctx.getClass().getResourceAsStream("/assets/" + name);
        String path = null;
        try {
            path = new String(InputStreamToByte(abpath));
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("eee-err",e.toString());
        }
        return path;
    }
    public static byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }
}
