package cn.dabaotv.movie.record;

/**
 * Created by H19 on 2018/4/14 0014.
 */

public class ItemC {
    public String name;
    public String subname;
    public String msg;
    public String url;
    public String img;
    public int imgId;
    public String id;
    public int index;
    public int Id;
    public boolean select;
    public ItemC(){}
    public ItemC(String name){this.name = name;}
    public ItemC(String name, String url){
        this.name = name;
        this.url = url;
    }
    public ItemC(String name, String msg, String url){
        this.name = name;
        this.url = url;
        this.msg = msg;
    }
    public ItemC(String name, String msg, int imgId){
        this.name = name;
        this.imgId = imgId;
        this.msg = msg;
    }

}
