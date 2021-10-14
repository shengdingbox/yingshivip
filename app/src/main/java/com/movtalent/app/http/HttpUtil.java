package com.movtalent.app.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtil {

    public static String getData(String urlPath) {
        StringBuilder sb = new StringBuilder();
        try {
            // 统一资源
            URL url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            // 设定请求的方法，默认是GET
            httpURLConnection.setRequestMethod("GET");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
//            httpURLConnection.setInstanceFollowRedirects(false);
            // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();

            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
            byte[] buf = new byte[1024];
            while (bin.read(buf) != -1) {
                String temp = new String(buf);
                sb.append(temp);
            }
            bin.close();
            httpURLConnection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();

    }

}
