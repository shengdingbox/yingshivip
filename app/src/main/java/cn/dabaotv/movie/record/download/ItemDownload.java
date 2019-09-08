package cn.dabaotv.movie.record.download;

/**
 * Created by H19 on 2018/4/25 0025.
 */

public class ItemDownload {
    public int id;
    public String tid;
    public String name;
    public int state;  // 0 等待，1 下载中 、 2 暂停 、 3 下载完毕 、 4 下载失败
    public long length; // 文件长度
    public int progress; // 进度
    public String downloadUrl;
    public String savePath;
    public boolean select; // 记录是否选中

    public ItemDownload(){}
}
