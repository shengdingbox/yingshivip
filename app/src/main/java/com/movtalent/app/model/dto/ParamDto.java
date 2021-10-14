package com.movtalent.app.model.dto;

import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * @author huangyong
 * createTime 2019-10-16
 */
public class ParamDto{


    /**
     * Code : 200
     * Data : {"content":"公告内容公告内容公告内容公告内容公告内容公告内容公告内容公告内容公告内容公告内容","title":"公告标题","show":true}
     * Msg :
     */

    private int Code;
    private Param Data;
    private String Msg;

    public int getCode() {
        return Code;
    }

    public void setCode(int Code) {
        this.Code = Code;
    }


    public String getMsg() {
        return Msg;
    }

    public void setMsg(String Msg) {
        this.Msg = Msg;
    }


    public Param getData() {
        return Data;
    }

    public void setData(Param data) {
        Data = data;
    }
}
