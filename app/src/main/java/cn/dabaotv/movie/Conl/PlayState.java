package cn.dabaotv.movie.Conl;

/**
 * Created by H19 on 2018/9/4 0004.
 */

public class PlayState {
    public static final int 无状态 = 0;
    public static final int 解析中 = 1;
    public static final int 等待播放 = 2; // 表示已有播放链接并进入缓冲状态
    public static final int 播放中 = 3;
    public static final int 播放暂停 = 4;
    public static final int 播放缓冲 = 5;
}
