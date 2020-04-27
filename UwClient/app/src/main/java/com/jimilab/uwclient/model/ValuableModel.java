package com.jimilab.uwclient.model;

import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.CheckTask;
import com.jimilab.uwclient.bean.ChipTask;
import com.jimilab.uwclient.bean.ValuableBaseResult;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.view.IValuableView;
import com.jimilab.uwclient.view.impl.valuable.CheckFragment;
import com.jimilab.uwclient.view.impl.valuable.SamplingFragment;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-20
 * @描述 :
 */
public class ValuableModel implements IValuableModel {
    private static final String TAG = ValuableModel.class.getSimpleName();
    private UwClientApplication mApplication;

    public ValuableModel(UwClientApplication application) {
        this.mApplication = application;
    }

    public void getTaskList(IValuableView valuableView, String token, OnTaskLoadListener loadListener) {
        if (valuableView instanceof CheckFragment) {
            getCheckTaskList(token, loadListener);
        } else if (valuableView instanceof SamplingFragment) {
            getChipTaskList(token, loadListener);
        }
    }

    @Override
    public void uploadMaterial(IValuableView valuableView, String token, String mTaskId, String materialUnique, String count, OnUploadMaterialListener uploadMaterialListener) {
        if (valuableView instanceof CheckFragment) {
            uploadCheckMaterial(token, mTaskId, materialUnique, count, uploadMaterialListener);
        } else if (valuableView instanceof SamplingFragment) {
            uploadChipMaterial(token, mTaskId, materialUnique, count, uploadMaterialListener);
        }
    }

    //上传单个抽检数据
    private void uploadChipMaterial(String token, String mTaskId, String materialUnique, String count, OnUploadMaterialListener uploadMaterialListener) {
        Map<String, String> checkParam = new HashMap<>();
        checkParam.put("#TOKEN#", token);
        checkParam.put("taskId", mTaskId);
        checkParam.put("materialId", materialUnique);
        checkParam.put("acturalNum", count);

        Log.d(TAG, "uploadChipMaterial: " + checkParam);
        mApplication.getNetApi()
                .uploadChipMaterial(checkParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ValuableBaseResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ValuableBaseResult baseResult) {

                        if (baseResult != null) {
                            Log.d(TAG, "onNext: " + baseResult.toString());
                            uploadMaterialListener.onComplete(baseResult);
                        } else {
                            uploadMaterialListener.onError(new NullPointerException("ValuableModel - onNext : uploadChipMaterial return null"));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        uploadMaterialListener.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //上传单个盘点数据
    private void uploadCheckMaterial(String token, String mTaskId, String materialUnique, String count, OnUploadMaterialListener uploadMaterialListener) {
        Map<String, String> checkParam = new HashMap<>();
        checkParam.put("#TOKEN#", token);
        checkParam.put("taskId", mTaskId);
        checkParam.put("materialId", materialUnique);
        checkParam.put("acturalNum", count);

        Log.d(TAG, "uploadCheckMaterial: " + checkParam);
        mApplication.getNetApi()
                .uploadCheckMaterial(checkParam)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ValuableBaseResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ValuableBaseResult baseResult) {

                        if (baseResult != null) {
                            Log.d(TAG, "onNext: " + baseResult.toString());
                            uploadMaterialListener.onComplete(baseResult);
                        } else {
                            uploadMaterialListener.onError(new NullPointerException("ValuableModel - onNext : uploadCheckMaterial return null"));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        uploadMaterialListener.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //获取盘点的任务
    private void getCheckTaskList(String token, OnTaskLoadListener loadListener) {
        mApplication.getNetApi()
                .getCheckTask(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CheckTask>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CheckTask checkTask) {
                        if (checkTask != null) {
                            Log.d(TAG, "onNext: " + checkTask.toString());
                            loadListener.onComplete(checkTask);
                        } else {
                            loadListener.onError(new NullPointerException("ValuableModel - onNext : getCheckTaskList null"));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        loadListener.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //获取抽检的任务
    private void getChipTaskList(String token, OnTaskLoadListener loadListener) {
        mApplication.getNetApi()
                .getChipTask(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ChipTask>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ChipTask chipTask) {

                        if (chipTask != null) {
                            Log.d(TAG, "onNext: " + chipTask.toString());
                            loadListener.onComplete(chipTask);
                        } else {
                            loadListener.onError(new NullPointerException("ValuableModel - onNext : getChipTaskList null"));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        loadListener.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
