package com.jimilab.uwclient.bean;

import java.util.List;

public class Material {

    private String supplier;
    private String materialNo;
    private int demand;
    private String materialType;
    private String materialTimestamp;
    private String productionDate;
    private int count;
    private String area;
    private String boxNo;
    private String boxX;
    private String boxY;
    private String boxZ;
    private String row;
    private String col;

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getMaterialNo() {
        return materialNo;
    }

    public void setMaterialNo(String materialNo) {
        this.materialNo = materialNo;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getMaterialTimestamp() {
        return materialTimestamp;
    }

    public void setMaterialTimestamp(String materialTimestamp) {
        this.materialTimestamp = materialTimestamp;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
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

    public static Material getMaterial(List<String> columns) {
        Material material = new Material();
        material.setSupplier(columns.get(0));
        material.setMaterialNo(columns.get(1));
        material.setDemand(Integer.valueOf(columns.get(2)));
        material.setMaterialType(columns.get(3));
        material.setMaterialTimestamp(columns.get(4));
        material.setProductionDate(columns.get(5));
        material.setCount(Integer.valueOf(columns.get(6)));
        material.setArea(columns.get(7));
        material.setBoxNo(columns.get(8));
        material.setBoxX(columns.get(9));
        material.setBoxY(columns.get(10));
        material.setBoxZ(columns.get(11));
        material.setRow(columns.get(12));
        material.setCol(columns.get(13));
        return material;
    }

    public static String materialToString(Material material) {
        return "[" + material.getSupplier() + ","
                + material.getMaterialNo() + ","
                + material.getDemand() + ","
                + material.getMaterialType() + ","
                + material.getMaterialTimestamp() + ","
                + material.getProductionDate() + ","
                + material.getCount() + ","
                + material.getArea() + ","
                + material.getBoxNo() + ","
                + material.getBoxX() + ","
                + material.getBoxY() + ","
                + material.getBoxZ() + ","
                + material.getRow() + ","
                + material.getCol() + "]";
    }

}
