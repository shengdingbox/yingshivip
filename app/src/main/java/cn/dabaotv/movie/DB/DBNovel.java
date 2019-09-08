package cn.dabaotv.movie.DB;

import org.litepal.crud.DataSupport;

/**
 * Created by H19 on 2018/5/4 0004.
 */

public class DBNovel extends DataSupport {
    private int id;
    private String img;
    private String name;
    private String url;
    private int index; // 当前阅读索引

    public String getImg() {
        return img;
    }
    public int getId(){return id;}

    public void setImg(String img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DBNovel(String img, String name, String url, int index){
        this.img = img;
        this.name = name;
        this.url = url;
        this.index = index;
    }
}
