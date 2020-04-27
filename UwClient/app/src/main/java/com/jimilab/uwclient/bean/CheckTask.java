package com.jimilab.uwclient.bean;

import java.util.List;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-09-04
 * @描述 :
 */
public class CheckTask extends BaseResult<CheckTask.DataBean> {


    /**
     * result : 200
     * data : {"totalRow":50,"pageNumber":1,"lastPage":false,"firstPage":true,"totalPage":3,"pageSize":20,"list":[{"supplierName":"豪恩声学","supplierId":15,"stateString":"未开始","checkedOperatior":"mixed","checkedTime":"2019-08-31 16:59:03","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190831165903","startTime":"mixed","endTime":"mixed","state":1,"taskId":6747,"status":false},{"supplierName":"广东几米","supplierId":10,"stateString":"未开始","checkedOperatior":"mixed","checkedTime":"2019-08-31 07:05:55","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190831070555","startTime":"mixed","endTime":"mixed","state":1,"taskId":6696,"status":false},{"supplierName":"新国都","supplierId":9,"stateString":"未开始","checkedOperatior":"mixed","checkedTime":"2019-08-28 20:05:56","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190828200556","startTime":"mixed","endTime":"mixed","state":1,"taskId":6526,"status":false},{"supplierName":"安定宝","supplierId":5,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-28 00:35:59","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190828003559","startTime":"2019-08-28 02:33:40","endTime":"2019-08-28 02:48:33","state":3,"taskId":6486,"status":false},{"supplierName":"华元天地","supplierId":4,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-27 15:42:07","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190827154207","startTime":"2019-08-27 15:42:14","endTime":"2019-08-27 16:03:18","state":3,"taskId":6467,"status":false},{"supplierName":"康凯斯","supplierId":8,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-24 12:44:33","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190824124433","startTime":"2019-08-24 14:35:08","endTime":"2019-08-25 12:46:07","state":3,"taskId":6288,"status":false},{"supplierName":"锦弘霖","supplierId":13,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-14 10:22:13","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190814102213","startTime":"2019-08-15 05:24:20","endTime":"2019-08-15 09:29:37","state":3,"taskId":5758,"status":false},{"supplierName":"华元天地","supplierId":4,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-03 07:18:18","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190803071817","startTime":"2019-08-03 07:18:25","endTime":"2019-08-03 09:03:12","state":3,"taskId":5280,"status":false},{"supplierName":"安定宝","supplierId":5,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-03 01:39:07","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190803013907","startTime":"2019-08-03 01:39:12","endTime":"2019-08-03 01:57:29","state":3,"taskId":5268,"status":false},{"supplierName":"希科普","supplierId":3,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-03 00:44:28","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190803004428","startTime":"2019-08-03 00:44:34","endTime":"2019-08-03 01:33:47","state":3,"taskId":5263,"status":false},{"supplierName":"康凯斯","supplierId":8,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-02 19:30:41","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190802193040","startTime":"2019-08-08 12:45:25","endTime":"2019-08-10 04:40:48","state":3,"taskId":5251,"status":false},{"supplierName":"几米智造","supplierId":7,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-02 11:27:31","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190802112730","startTime":"2019-08-02 11:27:36","endTime":"2019-08-02 20:20:30","state":3,"taskId":5221,"status":false},{"supplierName":"广东几米","supplierId":10,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-01 20:21:44","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190801202143","startTime":"2019-08-01 20:21:52","endTime":"2019-08-02 15:23:02","state":3,"taskId":5196,"status":false},{"supplierName":"乐行天下","supplierId":1,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-01 15:36:48","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190801153648","startTime":"2019-08-01 15:37:01","endTime":"2019-08-02 01:38:53","state":3,"taskId":5179,"status":false},{"supplierName":"几米智造","supplierId":7,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-31 07:29:15","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190731072915","startTime":"2019-07-31 07:29:23","endTime":"2019-07-31 16:25:28","state":3,"taskId":5100,"status":false},{"supplierName":"几米智造","supplierId":7,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-31 05:53:19","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190731055318","startTime":"2019-07-31 05:54:55","endTime":"2019-07-31 07:29:01","state":3,"taskId":5098,"status":false},{"supplierName":"几米物联","supplierId":14,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 10:55:21","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730105521","startTime":"2019-07-30 11:34:50","endTime":"2019-07-30 15:32:01","state":3,"taskId":5050,"status":false},{"supplierName":"华元天地","supplierId":4,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 10:55:06","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730105506","startTime":"2019-07-30 13:03:27","endTime":"2019-07-30 15:30:00","state":3,"taskId":5049,"status":false},{"supplierName":"安定宝","supplierId":5,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 10:51:15","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730105114","startTime":"2019-07-30 14:13:47","endTime":"2019-07-30 15:32:40","state":3,"taskId":5048,"status":false},{"supplierName":"希科普","supplierId":3,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 08:26:25","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730082625","startTime":"2019-07-30 09:52:47","endTime":"2019-07-30 15:29:18","state":3,"taskId":5039,"status":false}]}
     */

    public static class DataBean {
        /**
         * totalRow : 50
         * pageNumber : 1
         * lastPage : false
         * firstPage : true
         * totalPage : 3
         * pageSize : 20
         * list : [{"supplierName":"豪恩声学","supplierId":15,"stateString":"未开始","checkedOperatior":"mixed","checkedTime":"2019-08-31 16:59:03","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190831165903","startTime":"mixed","endTime":"mixed","state":1,"taskId":6747,"status":false},{"supplierName":"广东几米","supplierId":10,"stateString":"未开始","checkedOperatior":"mixed","checkedTime":"2019-08-31 07:05:55","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190831070555","startTime":"mixed","endTime":"mixed","state":1,"taskId":6696,"status":false},{"supplierName":"新国都","supplierId":9,"stateString":"未开始","checkedOperatior":"mixed","checkedTime":"2019-08-28 20:05:56","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190828200556","startTime":"mixed","endTime":"mixed","state":1,"taskId":6526,"status":false},{"supplierName":"安定宝","supplierId":5,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-28 00:35:59","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190828003559","startTime":"2019-08-28 02:33:40","endTime":"2019-08-28 02:48:33","state":3,"taskId":6486,"status":false},{"supplierName":"华元天地","supplierId":4,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-27 15:42:07","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190827154207","startTime":"2019-08-27 15:42:14","endTime":"2019-08-27 16:03:18","state":3,"taskId":6467,"status":false},{"supplierName":"康凯斯","supplierId":8,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-24 12:44:33","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190824124433","startTime":"2019-08-24 14:35:08","endTime":"2019-08-25 12:46:07","state":3,"taskId":6288,"status":false},{"supplierName":"锦弘霖","supplierId":13,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-14 10:22:13","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190814102213","startTime":"2019-08-15 05:24:20","endTime":"2019-08-15 09:29:37","state":3,"taskId":5758,"status":false},{"supplierName":"华元天地","supplierId":4,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-03 07:18:18","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190803071817","startTime":"2019-08-03 07:18:25","endTime":"2019-08-03 09:03:12","state":3,"taskId":5280,"status":false},{"supplierName":"安定宝","supplierId":5,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-03 01:39:07","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190803013907","startTime":"2019-08-03 01:39:12","endTime":"2019-08-03 01:57:29","state":3,"taskId":5268,"status":false},{"supplierName":"希科普","supplierId":3,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-03 00:44:28","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190803004428","startTime":"2019-08-03 00:44:34","endTime":"2019-08-03 01:33:47","state":3,"taskId":5263,"status":false},{"supplierName":"康凯斯","supplierId":8,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-02 19:30:41","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190802193040","startTime":"2019-08-08 12:45:25","endTime":"2019-08-10 04:40:48","state":3,"taskId":5251,"status":false},{"supplierName":"几米智造","supplierId":7,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-02 11:27:31","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190802112730","startTime":"2019-08-02 11:27:36","endTime":"2019-08-02 20:20:30","state":3,"taskId":5221,"status":false},{"supplierName":"广东几米","supplierId":10,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-01 20:21:44","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190801202143","startTime":"2019-08-01 20:21:52","endTime":"2019-08-02 15:23:02","state":3,"taskId":5196,"status":false},{"supplierName":"乐行天下","supplierId":1,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-08-01 15:36:48","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190801153648","startTime":"2019-08-01 15:37:01","endTime":"2019-08-02 01:38:53","state":3,"taskId":5179,"status":false},{"supplierName":"几米智造","supplierId":7,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-31 07:29:15","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190731072915","startTime":"2019-07-31 07:29:23","endTime":"2019-07-31 16:25:28","state":3,"taskId":5100,"status":false},{"supplierName":"几米智造","supplierId":7,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-31 05:53:19","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190731055318","startTime":"2019-07-31 05:54:55","endTime":"2019-07-31 07:29:01","state":3,"taskId":5098,"status":false},{"supplierName":"几米物联","supplierId":14,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 10:55:21","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730105521","startTime":"2019-07-30 11:34:50","endTime":"2019-07-30 15:32:01","state":3,"taskId":5050,"status":false},{"supplierName":"华元天地","supplierId":4,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 10:55:06","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730105506","startTime":"2019-07-30 13:03:27","endTime":"2019-07-30 15:30:00","state":3,"taskId":5049,"status":false},{"supplierName":"安定宝","supplierId":5,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 10:51:15","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730105114","startTime":"2019-07-30 14:13:47","endTime":"2019-07-30 15:32:40","state":3,"taskId":5048,"status":false},{"supplierName":"希科普","supplierId":3,"stateString":"已完成","checkedOperatior":"mixed","checkedTime":"2019-07-30 08:26:25","type":2,"createTime":"mixed","typeString":"盘点","taskName":"盘点_20190730082625","startTime":"2019-07-30 09:52:47","endTime":"2019-07-30 15:29:18","state":3,"taskId":5039,"status":false}]
         */

        private int totalRow;
        private int pageNumber;
        private boolean lastPage;
        private boolean firstPage;
        private int totalPage;
        private int pageSize;
        private List<ListBean> list;

        public int getTotalRow() {
            return totalRow;
        }

        public void setTotalRow(int totalRow) {
            this.totalRow = totalRow;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public boolean isLastPage() {
            return lastPage;
        }

        public void setLastPage(boolean lastPage) {
            this.lastPage = lastPage;
        }

        public boolean isFirstPage() {
            return firstPage;
        }

        public void setFirstPage(boolean firstPage) {
            this.firstPage = firstPage;
        }

        public int getTotalPage() {
            return totalPage;
        }

        public void setTotalPage(int totalPage) {
            this.totalPage = totalPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public List<ListBean> getList() {
            return list;
        }

        public void setList(List<ListBean> list) {
            this.list = list;
        }

        public static class ListBean {
            /**
             * supplierName : 豪恩声学
             * supplierId : 15
             * stateString : 未开始
             * checkedOperatior : mixed
             * checkedTime : 2019-08-31 16:59:03
             * type : 2
             * createTime : mixed
             * typeString : 盘点
             * taskName : 盘点_20190831165903
             * startTime : mixed
             * endTime : mixed
             * state : 1
             * taskId : 6747
             * status : false
             */

            private String supplierName;
            private int supplierId;
            private String stateString;
            private String checkedOperatior;
            private String checkedTime;
            private int type;
            private String createTime;
            private String typeString;
            private String taskName;
            private String startTime;
            private String endTime;
            private int state;
            private int taskId;
            private boolean status;

            public String getSupplierName() {
                return supplierName;
            }

            public void setSupplierName(String supplierName) {
                this.supplierName = supplierName;
            }

            public int getSupplierId() {
                return supplierId;
            }

            public void setSupplierId(int supplierId) {
                this.supplierId = supplierId;
            }

            public String getStateString() {
                return stateString;
            }

            public void setStateString(String stateString) {
                this.stateString = stateString;
            }

            public String getCheckedOperatior() {
                return checkedOperatior;
            }

            public void setCheckedOperatior(String checkedOperatior) {
                this.checkedOperatior = checkedOperatior;
            }

            public String getCheckedTime() {
                return checkedTime;
            }

            public void setCheckedTime(String checkedTime) {
                this.checkedTime = checkedTime;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }

            public String getCreateTime() {
                return createTime;
            }

            public void setCreateTime(String createTime) {
                this.createTime = createTime;
            }

            public String getTypeString() {
                return typeString;
            }

            public void setTypeString(String typeString) {
                this.typeString = typeString;
            }

            public String getTaskName() {
                return taskName;
            }

            public void setTaskName(String taskName) {
                this.taskName = taskName;
            }

            public String getStartTime() {
                return startTime;
            }

            public void setStartTime(String startTime) {
                this.startTime = startTime;
            }

            public String getEndTime() {
                return endTime;
            }

            public void setEndTime(String endTime) {
                this.endTime = endTime;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public int getTaskId() {
                return taskId;
            }

            public void setTaskId(int taskId) {
                this.taskId = taskId;
            }

            public boolean isStatus() {
                return status;
            }

            public void setStatus(boolean status) {
                this.status = status;
            }
        }
    }
}
