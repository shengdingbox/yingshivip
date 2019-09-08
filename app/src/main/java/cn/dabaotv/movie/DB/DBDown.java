package cn.dabaotv.movie.DB;

import org.litepal.crud.DataSupport;

/**
 * Created by H19 on 2018/4/19 0019.
 */

public class DBDown extends DataSupport {
    private int id;
    private String url; // 该剧的在飞飞的地址ID
    private String name; // 也是文件名
    private String img;
    private String msg;
    private int state;

    // 当前下载的信息
    private int index; // 当前下载的索引
    private String codeNama; // 对应的源名称
    private String codeUrl; // 视频url 未解析的地址

    private String downPath; // 保存本地路径
    private String downName; // 保存本地路径
    private String downUrl;

    private int downProgress; // 下载地址
    private int downType; //  下载类型
    private long downLength; // 文件长度

    private int downId; // 对应下载器中的下载ID

    public boolean select;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDownName() {
        return downName;
    }

    public void setDownName(String downName) {
        this.downName = downName;
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

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getCodeNama() {
        return codeNama;
    }

    public void setCodeNama(String codeNama) {
        this.codeNama = codeNama;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getDownPath() {
        return downPath;
    }

    public void setDownPath(String downPath) {
        this.downPath = downPath;
    }

    public String getDownUrl() {
        return downUrl;
    }


    public int getDownId() {
        return downId;
    }

    public void setDownId(int downId) {
        this.downId = downId;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public int getDownProgress() {
        return downProgress;
    }

    public void setDownProgress(int downProgress) {
        this.downProgress = downProgress;
    }

    public int getDownType() {
        return downType;
    }

    public void setDownType(int downType) {
        this.downType = downType;
    }

    public long getDownLength() {
        return downLength;
    }

    public void setDownLength(long downLength) {
        this.downLength = downLength;
    }
}