package cn.m.mslide;

/**
 * Created by H19 on 2018/6/25 0025.
 */

public class SlideItem {
    public String name;
    public String imgUrl;
    public int imgId;
    public String url;
    public SlideItem(String name,int imgId){
        this.name = name;
        this.imgId = imgId;
    }
    public SlideItem(String name,String url,String imgUrl){
        this.name = name;
        this.url = url;
        this.imgUrl = imgUrl;
    }

}
