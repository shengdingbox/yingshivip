package cn.dabaotv.movie.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@SuppressWarnings("unused")
public class JsonUtils {

    private static Gson gson = new Gson();
    //model转json字符串
    public static <T> String toJson(T src){
        try {
            return gson.toJson(src);
        }catch (Exception e){
            return null;
        }
    }
    //jsonList转成List<T>
    public static <T> List<T> jsonStringToList(String string, Class<T> T) {
        try {
            List<T> lst = new ArrayList<>();
            JsonArray array = new JsonParser().parse(string).getAsJsonArray();
            for (final JsonElement element : array) {
                lst.add(gson.fromJson(element, T));
            }
            return lst;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //json字符串转成Bean对象
    public static <T> T jsonStringToBean (String string , Class<T> tClass){
        try {
            return gson.fromJson(string,tClass);
        } catch (Exception e) {
            return null;
        }

    }
    //记录坑---hashMap记得值的类型，string ,HashMap<String,String>转换没问题，string ,HashMap<String,int(或者long,..)>这里会直接给你转成double
    public static HashMap jsonStringToHashMap (String string){
        try {
            return gson.fromJson(string,HashMap.class);
        } catch (Exception e) {
            return null;
        }
    }

}