package cn.dabaotv.movie.DB;

import org.litepal.crud.DataSupport;

/**
 * Created by H19 on 2018/4/19 0019.
 */

public class DBRecord extends DataSupport {
    private int id;
    private String name;
    private String msg;
    private String img;
    private String url;
    private String type;
    private int drama;
    private long changetime;
    private int playindex;
    private int playCode;//播放源

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDrama() {
        return drama;
    }

    public void setDrama(int drama) {
        this.drama = drama;
    }

    public long getChangetime() {
        return changetime;
    }

    public void setChangetime(long changetime) {
        this.changetime = changetime;
    }

    public int getPlayindex() {
        return playindex;
    }

    public void setPlayindex(int playindex) {
        this.playindex = playindex;
    }

    public int getPlayCode() {
        return playCode;
    }

    public void setPlayCode(int playCode) {
        this.playCode = playCode;
    }

    public DBRecord() {}
    public DBRecord(String img, String name, String msg, String url){
        this.img = img;
        this.name = name;
        this.msg = msg;
        this.url = url;
    }



}