package com.jimilab.uwclient.dao;

import com.jimilab.uwclient.bean.InMaterial;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;


@Entity
public class MaterialDao {

    @Id(autoincrement = true)
    private Long id;
    private String taskName;//任务名
    private String destination;//目的地
    private String no;//料号
    private String taskSupplier;//任务供应商
    private int planQuantity;//计划数量
    private String materialId;
    private String materialSupplier;//料盘供应商
    private int quantity;//料数量
    private String productionTime;//生产时间
    private int boxId;//料盒号
    private int operateType;//出入库类型  1，出库 ，0 ，入库
    private String operator;//操作人

    @Generated(hash = 1862442835)
    public MaterialDao(Long id, String taskName, String destination, String no, String taskSupplier, int planQuantity, String materialId, String materialSupplier,
                       int quantity, String productionTime, int boxId, int operateType, String operator) {
        this.id = id;
        this.taskName = taskName;
        this.destination = destination;
        this.no = no;
        this.taskSupplier = taskSupplier;
        this.planQuantity = planQuantity;
        this.materialId = materialId;
        this.materialSupplier = materialSupplier;
        this.quantity = quantity;
        this.productionTime = productionTime;
        this.boxId = boxId;
        this.operateType = operateType;
        this.operator = operator;
    }


    @Generated(hash = 340132724)
    public MaterialDao() {
    }


    public static MaterialDao getMaterialDao(InMaterial material, InMaterial.InMaterialBean bean, int type, String operator, String taskName, String destination) {
        MaterialDao dao = new MaterialDao();
        dao.setNo(material.getMaterialNo());
        dao.setTaskSupplier(material.getSupplier());
        dao.setPlanQuantity(material.getDemand());

        dao.setOperateType(type);
        dao.setMaterialId(bean.getMaterialTimeStamp());
        dao.setMaterialSupplier(bean.getSupplierName());
        dao.setQuantity(bean.getCount());
        dao.setProductionTime(bean.getProductionTime());
        dao.setBoxId(Integer.valueOf(bean.getBoxNo()));
        dao.setOperator(operator);
        dao.setTaskName(taskName);
        dao.setDestination(destination);
        return dao;
    }

    //生成json
    public static String materialToJson(List<MaterialDao> list, int taskId) {
        JSONObject task = new JSONObject();
        String taskJson = null;
        try {
            task.put("taskId", taskId);
            JSONArray record = new JSONArray();

            HashSet<String> noList = new HashSet<>();
            for (MaterialDao d : list) {
                noList.add(d.getNo());
            }

            for (String no : noList) {
                JSONObject material = new JSONObject();
                String taskSupplier = null;
                String noStr = no;
                int planQuantity = 0;

                JSONArray materialReelArray = new JSONArray();

                for (int i = 0, len = list.size(); i < len; i++) {
                    MaterialDao dao = list.get(i);
                    if (no.equalsIgnoreCase(dao.getNo())) {
                        taskSupplier = dao.getTaskSupplier();
                        noStr = dao.getNo();
                        planQuantity = dao.getPlanQuantity();

                        JSONObject sub = new JSONObject();
                        sub.put("operator", dao.getOperator());
                        sub.put("materialId", dao.getMaterialId());
                        sub.put("supplierName", dao.getMaterialSupplier());
                        sub.put("quantity", dao.getQuantity());
                        sub.put("productionTime", dao.getProductionTime());
                        sub.put("boxId", dao.getBoxId());

                        materialReelArray.put(sub);
                    }
                }

                material.put("supplierName", taskSupplier);
                material.put("no", noStr);
                material.put("planQuantity", planQuantity);
                material.put("materialReels", materialReelArray);

                record.put(material);
            }

            task.put("records", record);


            taskJson = task.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return taskJson;
    }


    public String getNo() {
        return this.no;
    }


    public void setNo(String no) {
        this.no = no;
    }


    public String getTaskSupplier() {
        return this.taskSupplier;
    }


    public void setTaskSupplier(String taskSupplier) {
        this.taskSupplier = taskSupplier;
    }


    public int getPlanQuantity() {
        return this.planQuantity;
    }


    public void setPlanQuantity(int planQuantity) {
        this.planQuantity = planQuantity;
    }


    public String getMaterialId() {
        return this.materialId;
    }


    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }


    public String getMaterialSupplier() {
        return this.materialSupplier;
    }


    public void setMaterialSupplier(String materialSupplier) {
        this.materialSupplier = materialSupplier;
    }


    public int getQuantity() {
        return this.quantity;
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public String getProductionTime() {
        return this.productionTime;
    }


    public void setProductionTime(String productionTime) {
        this.productionTime = productionTime;
    }


    public int getBoxId() {
        return this.boxId;
    }


    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }


    public int getOperateType() {
        return this.operateType;
    }


    public void setOperateType(int operateType) {
        this.operateType = operateType;
    }


    public String getOperator() {
        return this.operator;
    }


    public void setOperator(String operator) {
        this.operator = operator;
    }


    public String getTaskName() {
        return this.taskName;
    }


    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }


    public String getDestination() {
        return this.destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }

}
