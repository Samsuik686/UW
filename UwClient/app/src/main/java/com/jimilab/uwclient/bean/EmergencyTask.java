package com.jimilab.uwclient.bean;

import java.util.List;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-29
 * @描述 :
 */
public class EmergencyTask extends BaseResult<List<EmergencyTask.DataBean>> {

    /**
     * result : 200
     * data : [{"warehouse_type":0,"create_time":"2019-11-05 14:25:09","file_name":"盘点_20191105142509","destination":1,"end_time":"mixed","type":2,"priority":1,"start_time":"2019-11-05 15:15:14","is_inventory_apply":false,"supplier":23,"id":3668,"window":"mixed","state":2,"inventory_task_id":"mixed","remarks":"mixed"},{"warehouse_type":0,"create_time":"2019-11-04 16:20:52","file_name":"盘点_20191104162051","destination":1,"end_time":"2019-11-04 17:41:22","type":2,"priority":1,"start_time":"2019-11-04 16:21:00","is_inventory_apply":false,"supplier":23,"id":3667,"window":"mixed","state":3,"inventory_task_id":"mixed","remarks":"mixed"}]
     */

    public static class DataBean {
        /**
         * warehouse_type : 0
         * create_time : 2019-11-05 14:25:09
         * file_name : 盘点_20191105142509
         * destination : 1
         * end_time : mixed
         * type : 2
         * priority : 1
         * start_time : 2019-11-05 15:15:14
         * is_inventory_apply : false
         * supplier : 23
         * id : 3668
         * window : mixed
         * state : 2
         * inventory_task_id : mixed
         * remarks : mixed
         */

        private int warehouse_type;
        private String create_time;
        private String file_name;
        private int destination;
        private String end_time;
        private int type;
        private int priority;
        private String start_time;
        private boolean is_inventory_apply;
        private int supplier;
        private int id;
        private String window;
        private int state;
        private String inventory_task_id;
        private String remarks;

        public int getWarehouse_type() {
            return warehouse_type;
        }

        public void setWarehouse_type(int warehouse_type) {
            this.warehouse_type = warehouse_type;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        public int getDestination() {
            return destination;
        }

        public void setDestination(int destination) {
            this.destination = destination;
        }

        public String getEnd_time() {
            return end_time;
        }

        public void setEnd_time(String end_time) {
            this.end_time = end_time;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getStart_time() {
            return start_time;
        }

        public void setStart_time(String start_time) {
            this.start_time = start_time;
        }

        public boolean isIs_inventory_apply() {
            return is_inventory_apply;
        }

        public void setIs_inventory_apply(boolean is_inventory_apply) {
            this.is_inventory_apply = is_inventory_apply;
        }

        public int getSupplier() {
            return supplier;
        }

        public void setSupplier(int supplier) {
            this.supplier = supplier;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getWindow() {
            return window;
        }

        public void setWindow(String window) {
            this.window = window;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getInventory_task_id() {
            return inventory_task_id;
        }

        public void setInventory_task_id(String inventory_task_id) {
            this.inventory_task_id = inventory_task_id;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}
