package cn.dabaotv.movie.Q;

/**
 * Created by H19 on 2018/9/3 0003.
 */

public interface Qe {
    int INTENT_收藏 = 1;
    int INTENT_历史 = 2;
    int INTENT_下载 = 3;
    int INTENT_搜索 = 4;
    int INTENT_筛选 = 5;
    int INTENT_设置 = 6;

    int LISTTYPE_播放列表_竖屏 = 1;
    int LISTTYPE_播放列表_全屏 = 2;
    int LISTTYPE_缓存列表 = 3;
    int LISTTYPE_下载进行中 = 4;

    int PLAYTYPE_普通单条 = 1;
    int PLAYTYPE_直播 = 2;

    int 会员_状态_正常 = 1;
    int 会员_状态_已过期 = -2;
    int 会员_状态_未登录 = 0;

    String RECORDTYPE_历史 = "1";
    String RECORDTYPE_收藏 = "2";
    String RECORDTYPE_缓存 = "3"; // 全部
    String RECORDTYPE_正在缓存 = "4";

    //int DOWNSTATE_
    int DownState_Ing = 1; // 进行中
    int DownState_Cancel = 2; // 取消
    int DownState_Complete = 3; // 完成
    int DownState_Pause = 4; // 暂停
    int DownState_Error = 5;
    int DownState_Parse = 6; // 解析 读取真实地址
    int DownState_Error2 = 7; // 解析失败
    int DOWNSTATE_N = 0; // 等待



}
