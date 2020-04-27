package com.jimilab.uwclient.view.impl;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.presenter.BasePresenter;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;
import com.jimilab.uwclient.view.IBaseView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-01
 * @描述 :
 */
public abstract class BaseFragment<T extends BasePresenter, V extends IBaseView> extends Fragment implements IBaseView {

    //持有 presenter 层
    protected T presenter;
    protected UwClientApplication uwClientApplication;
    protected Context mContext;
    protected View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uwClientApplication = (UwClientApplication) getActivity().getApplicationContext();
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d("BaseFragment", "onCreateView: ");

        presenter = createPresenter();
        presenter.attachView((V) this);

        int layoutId = setLayoutId();

        view = inflater.inflate(layoutId, container, false);

        init(getActivity().getIntent().getExtras());

        return view;
    }

    //子类在初始化时具体实现
    protected abstract void init(Bundle bundle);

    //由实现类来决定使用哪个 presenter
    protected abstract T createPresenter();

    protected abstract int setLayoutId();

    //子类在销毁时，具体实现
    protected abstract void destroy();

    @Override
    public void showToast(String msg) {
        Tool.showToast(mContext, msg);
    }

    @Override
    public void showMsgDialog(String title, String msg) {
        Tool.showMsgDialog(mContext, title, msg);
    }

    @Override
    public void showLoading(String msg) {
        Tool.showLoading(mContext, msg);
    }

    @Override
    public void dismissLoading() {
        Tool.dismissLoading();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
        presenter.detachView();
    }
}
