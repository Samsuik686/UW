package com.jimilab.uwclient.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-18
 * @描述 :
 */
public class LoginResult extends BaseResult<LoginResult.DataBean> {

    public static class DataBean {
        @SerializedName("#TOKEN#")
        private String _$TOKEN219; // FIXME check this code
        private String uid;
        private String password;
        private String name;
        private int type;
        private boolean enabled;

        public String get_$TOKEN219() {
            return _$TOKEN219;
        }

        public void set_$TOKEN219(String _$TOKEN219) {
            this._$TOKEN219 = _$TOKEN219;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
