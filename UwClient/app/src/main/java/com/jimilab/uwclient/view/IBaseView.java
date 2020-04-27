package com.jimilab.uwclient.view;

import android.content.DialogInterface;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-25
 * @描述 :
 */
public interface IBaseView {

    void showToast(String msg);

    void showMsgDialog(String title, String msg);

    void showLoading(String msg);

    void dismissLoading();

    interface onDialogListener {
        void onPositiveClick(DialogInterface dialog, int which, int condition);

        void onNegativeClick(DialogInterface dialog, int which, int condition);
    }

}
