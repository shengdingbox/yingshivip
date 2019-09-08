package cn.m.bdplayer;

/**
 * Created by H19 on 2018/5/11 0011.
 */

public interface player_in {
    void 回调(int v);
    boolean isFull();
    boolean hide全屏按钮();
    String getVideoUrl();
    int PLAYER_BT_PLAY = 1; // 播放
    int PLAYER_BT_NEXT = 2; // 下集
    int PLAYER_BT_DRAME = 3; // 选集
    int PLAYER_BT_FULLSCREEN = 4; // 全屏
    int PLAYER_BT_RETURN = 5;
    int PLAYER_BT_PUSH = 6; // 投屏
    int PLAYER_BT_DOWNLOAD = 7; // 下载
    int PLAYER_BT_LINE = 8; // 线路
    int 双击 = 9;
    int PLAYER_BT_JUMP = 10;
    int PLAYER_BT_SC = 11;
    int PLAYER_BT_ERROR = 10000;
    int PLAYER_BT_ERROR_CODE = 10001;



    // 1 播放 | 2 下集 | 3 选集 | 4 全屏 | 5 ic_head_return | 6 投屏 | 7 下载 | 8 线路


}
