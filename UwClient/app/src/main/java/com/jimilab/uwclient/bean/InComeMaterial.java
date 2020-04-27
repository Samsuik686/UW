package com.jimilab.uwclient.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-26
 * @描述 :
 */
public class InComeMaterial {

    private String materialNo;
    private int needCount;
    private Map<String, Integer> diskOutNoMap;
    private String remainCount;

    public InComeMaterial() {
        if (diskOutNoMap == null) {
            diskOutNoMap = new HashMap<>();
        }
    }

    public InComeMaterial(String materialNo, int needCount, Map<String, Integer> diskOutNoMap, String remainCount) {
        this.materialNo = materialNo;
        this.needCount = needCount;
        this.diskOutNoMap = diskOutNoMap;
        this.remainCount = remainCount;
    }

    public String getMaterialNo() {
        return materialNo;
    }

    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo;
    }

    public int getNeedCount() {
        return needCount;
    }

    public void setNeedCount(int needCount) {
        this.needCount = needCount;
    }

    public Map<String, Integer> getDiskOutNoMap() {
        return diskOutNoMap;
    }

    public void setDiskOutNoMap(Map<String, Integer> diskOutNoMap) {
        this.diskOutNoMap = diskOutNoMap;
    }

    //将料盘与发料数量写进去
    public void putDiskNo(String disk, int outNo) {
        this.diskOutNoMap.put(disk, outNo);
    }

    //读取某个料盘的发料数量
    public Integer getDiskNo(String disk) {
        return this.diskOutNoMap.get(disk);
    }

    //获取map的数量，即是料盘的数量
    public int getDiskMapCount() {
        return diskOutNoMap != null ? diskOutNoMap.size() : 0;
    }

    public String getRemainCount() {
        return remainCount;
    }

    public void setRemainCount(String remainCount) {
        this.remainCount = remainCount;
    }
}
