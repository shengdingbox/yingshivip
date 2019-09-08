package cn.dabaotv.movie.Q;

/**
 * Created by H19 on 2018/9/3 0003.
 */

public interface QConfig {

    String ROOT = "http://feifei.dabaotv.cn/";
    String API = ROOT + "json.php";
    String UP = ROOT + "api/app_version.php";
    String API_LIVE = ROOT + "api2/ds.php";
    String API_VIDEOLIST = ROOT + "api2/list.php";
    String SavaPath = "/sdcard/dabaodown/";

    /*
        ### 保存到内存的全局设置 name ###

        player_parse_waiting_time   播放器 解析线路等待时间
        player_auto_next  播放器 自动切换 下一集


     */
}
