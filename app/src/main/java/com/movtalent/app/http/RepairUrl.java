package com.movtalent.app.http;

public class RepairUrl {

    /**
     * 远端地址，这个不要改，涉及到主流程
     */
    public static final String BASE_URL = "https://app.dabaotv.cn/";


    /**
     * 检查根服务器
     */
    static final String checkRepair = BASE_URL+"public/?service=App.Mov.CheckRepair";
}
