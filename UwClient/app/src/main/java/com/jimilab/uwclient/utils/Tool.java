package com.jimilab.uwclient.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.jeremyliao.liveeventbus.core.Observable;
import com.jimilab.uwclient.bean.InMaterial;
import com.jimilab.uwclient.bean.UrgentBean;
import com.jimilab.uwclient.view.IBaseView;
import com.jimilab.uwclient.view.custom.LoadingDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Tool {

    @SuppressLint("StaticFieldLeak")
    private static LoadingDialog loadingDialog;

    private static Toast mToast = null;

    public static void showToast(Context context, String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showLoading(Context context, String msg) {
        dismissLoading();
        loadingDialog = new LoadingDialog(context, msg);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    public static void dismissLoading() {
        //取消弹出窗
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.cancel();
            loadingDialog.dismiss();
        }
    }

    public static void showDialog(final IBaseView.onDialogListener dialogListener, Context context, String title, String msg, final int condition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogListener.onPositiveClick(dialog, which, condition);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogListener.onNegativeClick(dialog, which, condition);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    //弹出消息框
    public static void showMsgDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 料号@数量@时间戳(唯一码)@打印人员@供应商@位置@物料类型（标准/非标准）@生产日期@型号@产商@周期@打印日期@
     *
     * @param values
     * @return String[]
     */
    public static String[] parserMaterialNo(String values) {
        String[] material;
        material = values.split("@");
        return material;
    }

    /**
     * 料盒号@区域@行@列@高@供应商
     *
     * @param values
     * @return
     */
    public static String[] parserBox(String values) {
        String[] box;
        box = values.split("@");
        return box;
    }

    //获取当前时间
    public static String getTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(date);
    }

    //字符串转化为时间戳
    public static long getTimeStampe(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date();
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 判断是否全部出库完成
     *
     * @param outMaterials     出库任务
     * @param curMaterialIndex 当前操作位置
     * @return -1,没有任务，0，未完成，1，已完成, 2,未完成，但已操作到最后一盘
     */
    public static int isOutWareComplete(List<InMaterial> outMaterials, int curMaterialIndex) {
        if (outMaterials.size() <= 0) {
            return -1;
        }
        for (int i = 0, len = outMaterials.size(); i < len; i++) {
            InMaterial m = outMaterials.get(i);
            if (m.getOperateType() != 2) {//0,未操作 1，已扫料盘，未扫料盒  2，扫料盒并完成
                if (curMaterialIndex != len - 1) {
                    return 0;//false
                }
                return 2;
            }
        }
        return 1;
    }

    /**
     * 发送uw后台状态
     *
     * @param uwLiveState
     */
    public static void postUWLiveState(int uwLiveState) {
        LiveEventBus.get("UwLive", Integer.class).post(uwLiveState);
    }

    /**
     * 订阅UW后台状态
     *
     * @return
     */
    public static Observable<Integer> getUwLiveState() {
        return LiveEventBus.get("UwLive", Integer.class);
    }

    /**
     * 发送当前操作的任务数据
     *
     * @param curAllMaterials
     */
    public static void postCurAllMaterial(ArrayList<InMaterial> curAllMaterials) {
        LiveEventBus.get("curAllMaterials").post(curAllMaterials);
    }

    /**
     * 订阅当前数据
     *
     * @return
     */
    public static Observable<ArrayList> getCurAllMaterial() {
        return LiveEventBus.get("curAllMaterials", ArrayList.class);
    }

    /**
     * 发送任务状态
     *
     * @param taskState
     */
    public static void postTaskState(int taskState) {
        LiveEventBus.get("taskUpLoadState", Integer.class).post(taskState);
    }

    /**
     * 订阅任务状态
     *
     * @return
     */
    public static Observable<Integer> getTaskState() {
        return LiveEventBus.get("taskUpLoadState", Integer.class);
    }

    /**
     * 发送任务保存状态
     *
     * @param taskSave
     */
    public static void postTaskSavedState(boolean taskSave) {
        LiveEventBus.get("taskSavedState", Boolean.class).post(taskSave);
    }

    /**
     * 订阅任务保存状态
     *
     * @return
     */
    public static Observable<Boolean> getTaskSavedState() {
        return LiveEventBus.get("taskSavedState", Boolean.class);
    }

    /**
     * 发送当前操作的项
     *
     * @param curMaterialIndex
     */
    public static void postCurMaterialIndex(int curMaterialIndex) {
        LiveEventBus.get("curMaterialIndex", Integer.class).post(curMaterialIndex);
    }

    /**
     * 订阅当前操作的项
     *
     * @return
     */
    public static Observable<Integer> getCurItemMaterialIndex() {
        return LiveEventBus.get("curMaterialIndex", Integer.class);
    }

    /**
     * 发送任务目的地
     *
     * @param destinationIndex
     */
    public static void postDestinationIndex(int destinationIndex) {
        LiveEventBus.get("destinationIndex", Integer.class).post(destinationIndex);
    }

    /**
     * 订阅任务目的地
     *
     * @return
     */
    public static Observable<Integer> getDestinationIndex() {
        return LiveEventBus.get("destinationIndex", Integer.class);
    }

    /**
     * 发送任务目的地
     *
     * @param taskName
     */
    public static void postCurTaskName(String taskName) {
        LiveEventBus.get("taskName", String.class).post(taskName);
    }

    /**
     * 订阅任务目的地
     *
     * @return
     */
    public static Observable<String> getCurTaskName() {
        return LiveEventBus.get("taskName", String.class);
    }

    /**
     * 发送消息
     *
     * @param urgentBean
     */
    public static void postUrgentBean(UrgentBean urgentBean) {
        LiveEventBus.get("urgentBean", UrgentBean.class).post(urgentBean);
    }

    /**
     * 订阅消息
     *
     * @return
     */
    public static Observable<UrgentBean> getUrgentBean() {
        return LiveEventBus.get("urgentBean", UrgentBean.class);
    }

    /**
     * 发送消息
     *
     * @param request
     */
    public static void postRequestUrgentBean(boolean request) {
        LiveEventBus.get("requestUrgentBean", Boolean.class).post(request);
    }

    /**
     * 订阅消息
     *
     * @return
     */
    public static Observable<Boolean> getRequestUrgentBean() {
        return LiveEventBus.get("requestUrgentBean", Boolean.class);
    }


}
