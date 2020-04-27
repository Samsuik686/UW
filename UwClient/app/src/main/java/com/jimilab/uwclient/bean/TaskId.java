package com.jimilab.uwclient.bean;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-12
 * @描述 :
 */
public class TaskId extends BaseResult<TaskId.DataBean> {

    /**
     * data : {"taskId":3706}
     * result : 200
     */

    public static class DataBean {
        /**
         * taskId : 3706.0
         */

        private double taskId;

        public double getTaskId() {
            return taskId;
        }

        public void setTaskId(double taskId) {
            this.taskId = taskId;
        }
    }
}
