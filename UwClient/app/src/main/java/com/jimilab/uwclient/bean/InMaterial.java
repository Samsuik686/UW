package com.jimilab.uwclient.bean;

import java.io.Serializable;
import java.util.List;

public class InMaterial implements Serializable {

    private String materialNo;
    private String supplier;
    //计划需求数
    private int demand;
    //实际数
    private int totalCount;
    private String materialType;
    //操作状态 0,未操作 1，已扫料盘，未扫料盒  2，扫料盒并完成
    private int operateType = Constant.InMaterial_NOT_OPERATED;

    //原有库存
    private List<InMaterialBean> beanList;

    //当前已入库的
    private List<InMaterialBean> alreadyInList;

    public String getMaterialNo() {
        return materialNo;
    }

    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }


    public int getOperateType() {
        return operateType;
    }

    public void setOperateType(int operateType) {
        this.operateType = operateType;
    }

    public List<InMaterialBean> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<InMaterialBean> beanList) {
        this.beanList = beanList;
    }

    public List<InMaterialBean> getAlreadyInList() {
        return alreadyInList;
    }

    public void setAlreadyInList(List<InMaterialBean> alreadyInList) {
        this.alreadyInList = alreadyInList;
    }

    public static class InMaterialBean implements Serializable {
        private String supplierName;
        private String productionTime;
        private String materialTimeStamp;
        private int count;
        private String area;
        private String boxNo;
        private String boxX;
        private String boxY;
        private String boxZ;
        private String row;
        private String col;
        private int operateType = Constant.UN_OPERATE;

        public InMaterialBean() {
            this.area = "";
            this.boxNo = "";
            this.boxX = "";
            this.boxY = "";
            this.boxZ = "";
            this.row = "";
            this.col = "";
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public String getProductionTime() {
            return productionTime;
        }

        public void setProductionTime(String productionTime) {
            this.productionTime = productionTime;
        }

        public String getMaterialTimeStamp() {
            return materialTimeStamp;
        }

        public void setMaterialTimeStamp(String materialTimeStamp) {
            this.materialTimeStamp = materialTimeStamp;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getBoxNo() {
            return boxNo;
        }

        public void setBoxNo(String boxNo) {
            this.boxNo = boxNo;
        }

        public String getBoxX() {
            return boxX;
        }

        public void setBoxX(String boxX) {
            this.boxX = boxX;
        }

        public String getBoxY() {
            return boxY;
        }

        public void setBoxY(String boxY) {
            this.boxY = boxY;
        }

        public String getBoxZ() {
            return boxZ;
        }

        public void setBoxZ(String boxZ) {
            this.boxZ = boxZ;
        }

        public String getRow() {
            return row;
        }

        public void setRow(String row) {
            this.row = row;
        }

        public String getCol() {
            return col;
        }

        public void setCol(String col) {
            this.col = col;
        }

        public int getOperateType() {
            return operateType;
        }

        public void setOperateType(int operateType) {
            this.operateType = operateType;
        }
    }

}
