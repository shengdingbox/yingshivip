package cn.m.cn.styles;

import java.net.MalformedURLException;

/**
 * Created by H19 on 2018/6/21 0021.
 */

public class 网页 {
    public static String get域名(String url){
        java.net.URL t = null;
        String t2 = null;
        if (url == null || url.substring(0,1).equals("/")) return null;
        try {
            t = new java.net.URL(url);
            t2 = t.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            t2 = null;
        }
        return t2;
    }
}
