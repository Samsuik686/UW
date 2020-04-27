package com.jimilab.uwclient.presenter;


import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.EmergencyTask;
import com.jimilab.uwclient.model.ComeAreaModel;
import com.jimilab.uwclient.model.InComeAreaModel;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;
import com.jimilab.uwclient.view.InComeAreaView;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-26
 * @描述 :
 */
public class ComeAreaPresenter<T extends InComeAreaView> extends BasePresenter<T> {
    private static final String TAG = ComeAreaPresenter.class.getSimpleName();
    private UwClientApplication mApplication;
    private ComeAreaModel comeAreaModel;

    public ComeAreaPresenter(UwClientApplication application) {
        this.mApplication = application;
        comeAreaModel = new ComeAreaModel(mApplication);
    }

    /**
     * 获取任务
     *
     * @param token
     */
    public void fetchGetTasks(String token) {
        tWeakReference.get().showLoading("正在加载...");
        comeAreaModel.loadTaskList(token, new InComeAreaModel.onLoadListener() {
            @Override
            public void onComplete(EmergencyTask emergencyTask) {
                //返回UI
                tWeakReference.get().showTaskList(token, emergencyTask);
                tWeakReference.get().dismissLoading();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "onError: " + throwable.toString());
                tWeakReference.get().dismissLoading();
                tWeakReference.get().showToast("获取任务失败,请点击刷新");
            }
        });

    }

    /**
     * 扫描
     *
     * @param id
     * @param scanValue
     */
    public void doScanMaterial(int id, String scanValue) {
        // 料号@数量@时间戳(唯一码)@打印人员@供应商@位置@物料类型（标准/非标准）@生产日期@型号@产商@周期@打印日期@
        String[] materialValues = Tool.parserMaterialNo(scanValue);
        if (materialValues.length < 12) {
            tWeakReference.get().showToast("二维码格式不正确");
            tWeakReference.get().setScanMaterialRequestFocus();
            return;
        }
        tWeakReference.get().showLoading("正在上传...");
        comeAreaModel.uploadMaterialItem(String.valueOf(id), materialValues, new InComeAreaModel.onItemLoadListener() {
            @Override
            public void onComplete(String[] item) {
                tWeakReference.get().showMaterial(item);
                tWeakReference.get().dismissLoading();
                tWeakReference.get().showResult(200, "上传成功!");
                tWeakReference.get().setScanMaterialRequestFocus();
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "onError: " + throwable.toString());
                tWeakReference.get().dismissLoading();
                tWeakReference.get().showResult(400, throwable.getMessage());
                tWeakReference.get().showToast("上传失败，重新扫描");
                tWeakReference.get().clearMaterial();
            }
        });
    }
}
