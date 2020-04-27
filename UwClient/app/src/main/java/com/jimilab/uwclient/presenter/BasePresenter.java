package com.jimilab.uwclient.presenter;

import com.jimilab.uwclient.view.IBaseView;

import java.lang.ref.WeakReference;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-01
 * @描述 :
 */
public class BasePresenter<T extends IBaseView> {
    //持有的view
    WeakReference<T> tWeakReference = null;

    public void attachView(T view) {
        tWeakReference = new WeakReference<>(view);
    }

    public void detachView() {
        if (tWeakReference != null) {
            tWeakReference.clear();
            tWeakReference = null;
        }
    }
}
