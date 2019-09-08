package cn.dabaotv.movie.view.list;

import java.io.Serializable;
import java.util.List;


/**
 * Created by H19 on 2018/5/18 0018.
 */

public class ItemList implements Serializable {

    public int Id;
    public String id;
    public String name; // 名称
    public String subname;
    public String msg; // 相当于副名称
    public String url;
    public String img;
    public int imgId;
    public String score; //评分
    public String type; // 类
    public String info; //视频简介
    public String playcode; // 播放源
    public String playlist; // 播放列表
    public List<String> t;
    public int z; // 多用于状态

    public boolean select;

    public ItemList(){
    }
    public ItemList(String name){
        this.name = name;
    }
    public ItemList(String name,String url){
        this.name = name;
        this.url = url;
    }
    public ItemList(String name,int imgId){
        this.name = name;
        this.imgId = imgId;
    }

    public ItemList(String name,String msg,int imgId){
        this.name = name;
        this.msg = msg;
        this.imgId = imgId;
    }


}
