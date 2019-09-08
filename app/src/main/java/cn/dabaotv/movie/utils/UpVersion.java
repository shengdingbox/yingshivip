package cn.dabaotv.movie.utils;

/**
 * Created by 奈蜇 on 2018-11-10.
 * 更新bean
 */
public class UpVersion {
    private String mode;
    private String up_info;
    private String up_time;
    private String up_url;
    private int up_version;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUp_info() {
        return up_info;
    }

    public void setUp_info(String up_info) {
        this.up_info = up_info;
    }

    public String getUp_time() {
        return up_time;
    }

    public void setUp_time(String up_time) {
        this.up_time = up_time;
    }

    public String getUp_url() {
        return up_url;
    }

    public void setUp_url(String up_url) {
        this.up_url = up_url;
    }

    public int getUp_version() {
        return up_version;
    }

    public void setUp_version(int up_version) {
        this.up_version = up_version;
    }
}
