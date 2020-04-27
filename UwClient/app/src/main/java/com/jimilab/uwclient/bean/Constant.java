package com.jimilab.uwclient.bean;

public class Constant {

    //http://10.10.11.90:8080/uw_server/manualTask/ping
    public static final String URL_CREATE = "manualTask/create";
    public static final String URL_UPLOAD = "manualTask/uploadRecord";
    public static final String URL_PING = "manualTask/ping";//  manage/user/login

    //InMaterialBean的操作状态
    public static final int UN_OPERATE = 0;//未操作
    public static final int IN_OPERATE = 1;//操作中
    public static final int OPERATE_COMPLETE = 2;//完成操作

    //InMaterial 操作状态 0,未操作 1，已扫料盘，未扫料盒  2，扫料盒并完成  3，库存为0
    public static final int InMaterial_NOT_OPERATED = 0;
    public static final int InMaterial_OPERATING = 1;
    public static final int InMaterial_OPERATED = 2;
    public static final int InMaterial_INVENTORY_ZERO = 3;

    //uw后台活跃状态
    public static final int UW_DOWNTIME = 0;
    public static final int UW_ALIVE = 1;

    //加载文件时状态
    public static final int FIRST_LOAD = 101;//首次加载
    public static final int CHANGE = 102;//切换任务

    //任务上传状态 0，未创建任务上传数据；1，正在创建任务上传数据；2，已完成创建任务上传数据
    public static final int TASK_NO_CREATE = 0;
    public static final int TASK_IN_CREATE = 1;
    public static final int TASK_COMPLETE = 2;

    //出入库类型
    public static final int IN_WARE = 0;
    public static final int OUT_WARE = 1;

    //测试 http://10.10.11.90:8080/uw_server/
    //4楼外网 183.236.111.164:6080
    public static final String IP = "http://183.236.111.164:6080/uw_server/";
}
