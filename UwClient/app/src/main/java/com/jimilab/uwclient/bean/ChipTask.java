package com.jimilab.uwclient.bean;

import java.util.List;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-09-04
 * @描述 :
 */
public class ChipTask extends BaseResult<ChipTask.DataBean> {


    /**
     * result : 200
     * data : {"totalRow":161,"pageNumber":1,"lastPage":false,"firstPage":true,"totalPage":9,"pageSize":20,"list":[{"supplierName":"广东几米","fileName":"抽检_20190902183909","createTimeString":"2019-09-02 18:39:09","supplier":10,"typeString":"抽检","id":6809,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190902181601","createTimeString":"2019-09-02 18:16:02","supplier":10,"typeString":"抽检","id":6807,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"安定宝","fileName":"抽检_20190901095048","createTimeString":"2019-09-01 09:50:49","supplier":5,"typeString":"抽检","id":6772,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"测试","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190831030052","createTimeString":"2019-08-31 03:00:53","supplier":8,"typeString":"抽检","id":6689,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"00","status":false},{"supplierName":"乐行天下","fileName":"抽检_20190830173518","createTimeString":"2019-08-30 17:35:18","supplier":1,"typeString":"抽检","id":6668,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"0","status":false},{"supplierName":"新国都","fileName":"抽检_20190829063127","createTimeString":"2019-08-29 06:31:27","supplier":9,"typeString":"抽检","id":6545,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"测试","status":false},{"supplierName":"新国都","fileName":"抽检_20190829060955","createTimeString":"2019-08-29 06:09:56","supplier":9,"typeString":"抽检","id":6543,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"测试","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190828210456","createTimeString":"2019-08-28 21:04:56","supplier":8,"typeString":"抽检","id":6529,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"00","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827164920","createTimeString":"2019-08-27 16:49:21","supplier":10,"typeString":"抽检","id":6472,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827154700","createTimeString":"2019-08-27 15:47:00","supplier":10,"typeString":"抽检","id":6469,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827154520","createTimeString":"2019-08-27 15:45:21","supplier":10,"typeString":"抽检","id":6468,"state":1,"stateString":"未开始","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827100347","createTimeString":"2019-08-27 10:03:48","supplier":10,"typeString":"抽检","id":6443,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190826185944","createTimeString":"2019-08-26 18:59:45","supplier":8,"typeString":"抽检","id":6415,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"33","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190826183933","createTimeString":"2019-08-26 18:39:34","supplier":8,"typeString":"抽检","id":6413,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"0","status":false},{"supplierName":"新国都","fileName":"抽检_20190826092301","createTimeString":"2019-08-26 09:23:01","supplier":9,"typeString":"抽检","id":6377,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190825163244","createTimeString":"2019-08-25 16:32:45","supplier":8,"typeString":"抽检","id":6343,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"13","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190825123205","createTimeString":"2019-08-25 12:32:05","supplier":8,"typeString":"抽检","id":6331,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190824171643","createTimeString":"2019-08-24 17:16:43","supplier":10,"typeString":"抽检","id":6305,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190824144754","createTimeString":"2019-08-24 14:47:55","supplier":10,"typeString":"抽检","id":6300,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190824141342","createTimeString":"2019-08-24 14:13:42","supplier":10,"typeString":"抽检","id":6296,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false}]}
     */

    public static class DataBean {
        /**
         * totalRow : 161
         * pageNumber : 1
         * lastPage : false
         * firstPage : true
         * totalPage : 9
         * pageSize : 20
         * list : [{"supplierName":"广东几米","fileName":"抽检_20190902183909","createTimeString":"2019-09-02 18:39:09","supplier":10,"typeString":"抽检","id":6809,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190902181601","createTimeString":"2019-09-02 18:16:02","supplier":10,"typeString":"抽检","id":6807,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"安定宝","fileName":"抽检_20190901095048","createTimeString":"2019-09-01 09:50:49","supplier":5,"typeString":"抽检","id":6772,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"测试","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190831030052","createTimeString":"2019-08-31 03:00:53","supplier":8,"typeString":"抽检","id":6689,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"00","status":false},{"supplierName":"乐行天下","fileName":"抽检_20190830173518","createTimeString":"2019-08-30 17:35:18","supplier":1,"typeString":"抽检","id":6668,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"0","status":false},{"supplierName":"新国都","fileName":"抽检_20190829063127","createTimeString":"2019-08-29 06:31:27","supplier":9,"typeString":"抽检","id":6545,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"测试","status":false},{"supplierName":"新国都","fileName":"抽检_20190829060955","createTimeString":"2019-08-29 06:09:56","supplier":9,"typeString":"抽检","id":6543,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"测试","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190828210456","createTimeString":"2019-08-28 21:04:56","supplier":8,"typeString":"抽检","id":6529,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"00","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827164920","createTimeString":"2019-08-27 16:49:21","supplier":10,"typeString":"抽检","id":6472,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827154700","createTimeString":"2019-08-27 15:47:00","supplier":10,"typeString":"抽检","id":6469,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827154520","createTimeString":"2019-08-27 15:45:21","supplier":10,"typeString":"抽检","id":6468,"state":1,"stateString":"未开始","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190827100347","createTimeString":"2019-08-27 10:03:48","supplier":10,"typeString":"抽检","id":6443,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190826185944","createTimeString":"2019-08-26 18:59:45","supplier":8,"typeString":"抽检","id":6415,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"33","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190826183933","createTimeString":"2019-08-26 18:39:34","supplier":8,"typeString":"抽检","id":6413,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"0","status":false},{"supplierName":"新国都","fileName":"抽检_20190826092301","createTimeString":"2019-08-26 09:23:01","supplier":9,"typeString":"抽检","id":6377,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190825163244","createTimeString":"2019-08-25 16:32:45","supplier":8,"typeString":"抽检","id":6343,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"13","status":false},{"supplierName":"康凯斯","fileName":"抽检_20190825123205","createTimeString":"2019-08-25 12:32:05","supplier":8,"typeString":"抽检","id":6331,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190824171643","createTimeString":"2019-08-24 17:16:43","supplier":10,"typeString":"抽检","id":6305,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190824144754","createTimeString":"2019-08-24 14:47:55","supplier":10,"typeString":"抽检","id":6300,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false},{"supplierName":"广东几米","fileName":"抽检_20190824141342","createTimeString":"2019-08-24 14:13:42","supplier":10,"typeString":"抽检","id":6296,"state":3,"stateString":"已完成","type":7,"priority":1,"remarks":"1","status":false}]
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
             * supplierName : 广东几米
             * fileName : 抽检_20190902183909
             * createTimeString : 2019-09-02 18:39:09
             * supplier : 10
             * typeString : 抽检
             * id : 6809
             * state : 3
             * stateString : 已完成
             * type : 7
             * priority : 1
             * remarks : 1
             * status : false
             */

            private String supplierName;
            private String fileName;
            private String createTimeString;
            private int supplier;
            private String typeString;
            private int id;
            private int state;
            private String stateString;
            private int type;
            private int priority;
            private String remarks;
            private boolean status;

            public String getSupplierName() {
                return supplierName;
            }

            public void setSupplierName(String supplierName) {
                this.supplierName = supplierName;
            }

            public String getFileName() {
                return fileName;
            }

            public void setFileName(String fileName) {
                this.fileName = fileName;
            }

            public String getCreateTimeString() {
                return createTimeString;
            }

            public void setCreateTimeString(String createTimeString) {
                this.createTimeString = createTimeString;
            }

            public int getSupplier() {
                return supplier;
            }

            public void setSupplier(int supplier) {
                this.supplier = supplier;
            }

            public String getTypeString() {
                return typeString;
            }

            public void setTypeString(String typeString) {
                this.typeString = typeString;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public String getStateString() {
                return stateString;
            }

            public void setStateString(String stateString) {
                this.stateString = stateString;
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

            public String getRemarks() {
                return remarks;
            }

            public void setRemarks(String remarks) {
                this.remarks = remarks;
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
