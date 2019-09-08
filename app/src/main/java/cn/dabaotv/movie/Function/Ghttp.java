package cn.dabaotv.movie.Function;

import android.util.Log;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by H19 on 2018/6/16 0016.
 */

public class Ghttp {
    public static String getHttp(String url) {
        if (url == null || url.isEmpty()) return "";

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        /*
        ### post
        FormBody.Builder b1 = new FormBody.Builder();
                for (ItemName itemName : rule.postList) {
                    b1.add(itemName.name, itemName.msg);
                }
        builder.post(b1.build());
        */
        try {
            Request request = builder.build();
            Response response = okHttpClient.newCall(request).execute();
            String cid = response.body().string();
            Log.d("Ghttp","getHttp !" + cid);
            return cid;
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.e("Ghttp","getHttp eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee !");
        }
        return "";
    }
    public static String getHttp(Cache cache, final String url){
        if (url == null || url.isEmpty()) return null;
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newBuilder().cache(cache);
        return okhttpp(okHttpClient,url);
    }
    public static String getHttp(String url,String ua,String cookies){
        if (url == null || url.isEmpty()) return null;
        if (url.length() > 4 && !url.substring(0,4).equals("http")) url = "http://" + url;

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();

        if (ua !=null) builder.addHeader("User-agent",ua);
        if (cookies != null) builder.addHeader("Cookie",cookies);
        builder.url(url);
        builder.get();
        try {
            Request request = builder.build();
            Response response = okHttpClient.newCall(request).execute();
            String cid = response.body().string();
            return cid;
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.i("eee-ghttp",exception.toString());
        }
        return "";
    }
    public static String okhttpp(OkHttpClient okHttpClient,String url){
        Request request = new Request.Builder().url(url).get().build();
        String t_code = "";
        try {
            Response response = okHttpClient.newCall(request).execute();
            t_code = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t_code;
    }

    // jsoup 方法读取
    public static String getHttpx(String url){
        return getHttpx(url,null,System.getProperty("http.agent"),null);
    }
    public static String getHttpx(String url,String ua){
        return getHttpx(url,null,ua,null);
    }
    public static String getHttpx(String url,String ua,String cookies){
        return getHttpx(url,null,ua,cookies);
    }
    public static String getHttpx(String url,String post,String ua,String cookies){
        Document doc;
        //if (M.getLinkType(url) == null) return null;
        Connection con = null;
        try {
            con = Jsoup.connect(url).timeout(10000);
        }catch (Exception e){
            return null;
        }

        if (ua !=null && !ua.isEmpty()) con.header("User-agent",ua);
        if (post != null && !post.isEmpty()) con.data(post);
        //if (cookies !=null && !cookies.isEmpty()) con.cookie("",cookies);
        try {
            if (post == null || post.isEmpty()){
                doc = con.get();
            }else {
                doc = con.post();
            }

            return doc.html();
        }catch (Exception ex){
            //M.log(TAG,ex.toString());
        }
        return null;
    }
    private static String TAG = "GHTTP:" ;




}
