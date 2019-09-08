package cn.dabaotv.movie.DB;

import org.litepal.crud.DataSupport;

/**
 *
 * 存广告
 */

public class DBAd extends DataSupport {
    private int id;
    private String name;
    private String type;
    public DBAd(String name){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
