package cn.m.bdplayer;

/**
 * Created by H19 on 2018/4/28 0028.
 */

public interface Locode {
    int 下载_完毕 = 5;
    int 下载_等待 = 0;
    int 下载_进行中 = 1;
    int 下载_暂停 = 2;
    int 下载_失败 = 4;

    int 下载_单文件 = 0;
    int 下载_M3U8 = 1;
    int 下载_分段多文件 = 2;


    int 会员_状态_正常 = 1;
    int 会员_状态_已过期 = -2;
    int 会员_状态_未登录 = 0;


    int DISPLAYMODE_单集 = 1001;
    int DISPLAYMODE_多集 = 1002;
    int DISPLAYMODE_本地 = 1003;
    int DISPLAYMODE_直播 = 1004;

    int 触屏_双击 = 102;
    int 触屏_单击 = 101;

    int 播放状态_等待 = 0;
    int 播放状态_播放中 = 1;
    int 播放状态_暂停 = 2;

    int 样式_选中 = 1;
    int 样式_未选中 = 0;

    int 样式_全屏 = 2;
    int 样式_竖屏 = 1;


}
