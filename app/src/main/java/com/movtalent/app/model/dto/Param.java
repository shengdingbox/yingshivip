package com.movtalent.app.model.dto;

import com.movtalent.app.util.DBjk;

import org.litepal.crud.LitePalSupport;

import java.util.List;

public class Param extends LitePalSupport {

    private List<DBjk> mJklist;
    private String qq;
    private boolean isAd;
    private int viplevel;

    public List<DBjk> getmJklist() {
        return mJklist;
    }

    public void setmJklist(List<DBjk> mJklist) {
        this.mJklist = mJklist;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public int getViplevel() {
        return viplevel;
    }

    public void setViplevel(int viplevel) {
        this.viplevel = viplevel;
    }
}
