package com.jimilab.uwclient.presenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.BaseResult;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.bean.InMaterial;
import com.jimilab.uwclient.bean.TaskId;
import com.jimilab.uwclient.dao.GreenDaoUtil;
import com.jimilab.uwclient.dao.MaterialDao;
import com.jimilab.uwclient.dao.TaskDao;
import com.jimilab.uwclient.model.IUrgentModel;
import com.jimilab.uwclient.model.UrgentModel;
import com.jimilab.uwclient.utils.FileUtils;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;
import com.jimilab.uwclient.view.IUrgentView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-31
 * @描述 :
 */
public class UrgentPresenter<T extends IUrgentView> extends BasePresenter<T> {

    private static final String TAG = UrgentPresenter.class.getSimpleName();
    //持有model
    private UrgentModel iUrgentModel;
    private UwClientApplication mApplication;
    private Context mContext;
    private int selectIndex = -1;

    public UrgentPresenter(UwClientApplication clientApplication, Context context) {
        this.mApplication = clientApplication;
        this.mContext = context;
        iUrgentModel = new UrgentModel(clientApplication);
    }

    //加载缓存的任务单列表 "taskNames"
    public void fetchTaskNames(String key, int condition, List<InMaterial> curAllMaterials, String curTaskName) {
        iUrgentModel.getMemoryTaskNames(key, new IUrgentModel.OnGetMemoryTaskNames() {
            @Override
            public void onComplete(HashSet<String> taskNames) {
                // TODO: 2019-11-04  taskNames

                if (!taskNames.isEmpty()) {
                    List<String> nameList = new ArrayList<>(taskNames);
                    if (curTaskName != null) {
                        nameList.remove(curTaskName);
                    }
                    int len = nameList.size();
                    String[] names = new String[len];
                    for (int i = 0; i < len; i++) {
                        names[i] = nameList.get(i);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setCancelable(false);
                    builder.setTitle("以下为未完成的任务,请选择:");

                    builder.setSingleChoiceItems(names, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectIndex = which;
                        }
                    });

                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (selectIndex < 0) {
                                selectIndex = 0;
                            }

                            String selectName = names[selectIndex];
                            List<InMaterial> list = iUrgentModel.getInMaterialList(selectName);
                            if (list != null && list.size() > 0) {
                                tWeakReference.get().showLoading("正在加载...");
                                //切换任务，缓存之前的任务
                                if (condition == Constant.CHANGE) {
                                    iUrgentModel.cacheTaskMaterial(curAllMaterials, curTaskName);
                                }
                                tWeakReference.get().showMemoryTaskName(selectName, list);
                            } else {
                                tWeakReference.get().showToast("加载缓存失败");
                            }
                        }
                    });

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tWeakReference.get().showToast("取消");
                        }
                    });

                    builder.show();

                } else {
                    tWeakReference.get().showToast("无缓存任务");
                }
            }
        });
    }

    /**
     * 导入文件
     *
     * @param requestCode     请求码，初次加载、切换任务
     * @param curTaskName     当前任务名
     * @param curAllMaterials
     * @param uri             选中文件的uri
     */
    public void fetchLoadTaskFile(int requestCode, String curTaskName, List<InMaterial> curAllMaterials, Uri uri) {
        if (uri != null) {
            Log.d(TAG, "fetchLoadTaskFile: " + uri);
            //content://com.speedsoftware.rootexplorer.content/mnt/sdcard/test1_PDA专用任务单_1569395368363.xlsx
            ///storage/emulated/0/test1_PDA专用任务单_1569395368363.xlsx
            String taskPath;
            //获取选中文件所在绝对路径
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                //若使用第三方应用打开（定制的UI系统均为第三方）
                taskPath = uri.getPath();
            } else {
                //原生应用打开
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    //4.4以后的系统
                    taskPath = FileUtils.getAboveKITKATPath(mContext, uri);
                } else {
                    taskPath = FileUtils.getBelowKITKATPath(mContext, uri);
                }
            }
            Log.d(TAG, "file - taskPath = " + taskPath);

            if (taskPath != null) {
                File file = new File(taskPath);
                if (file.exists()) {
                    Log.d(TAG, "fetchLoadTaskFile: " + file.length());
                    //获取选中的文件名
                    String loadName = taskPath.substring(taskPath.lastIndexOf("/") + 1);
                    if (!loadName.equalsIgnoreCase(curTaskName)) {

                        iUrgentModel.loadTaskFile(requestCode, curTaskName, taskPath, curAllMaterials, new IUrgentModel.OnLoadTaskFileListener() {
                            @Override
                            public void onLoaded(boolean daoExitName, List<InMaterial> loadList) {

                                // TODO: 2019-11-05  回调，传入 UI

                                tWeakReference.get().onLoadTaskFile(daoExitName, loadName, loadList);

                            }

                            @Override
                            public void onError(Throwable throwable) {
                                //tWeakReference.get().showToast("任务单存在异常，请导入正确的任务单!");
                                tWeakReference.get().showToast(throwable.getMessage());
                            }
                        });

                    } else {
                        tWeakReference.get().showToast("任务重复,该任务正在操作");
                    }
                }
            }

        } else {
            tWeakReference.get().showToast("未选择文件!");
        }
    }

    /**
     * 获取当前任务列表进行到哪
     *
     * @param list
     * @return int
     */
    public int getCurMaterialOpIndex(List<InMaterial> list) {
        for (int i = 0, len = list.size(); i < len; i++) {
            InMaterial m = list.get(i);
            if (m.getOperateType() != UrgentModel.OPERATED) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 用于判断当前应该扫描料盘还是料盒
     *
     * @param beanList
     * @return -1，扫料盘，大于-1，扫料盒
     */
    public int getInMaterialBeanOpIndex(List<InMaterial.InMaterialBean> beanList) {
        for (int i = 0, size = beanList.size(); i < size; i++) {
            InMaterial.InMaterialBean bean = beanList.get(i);
            if (bean.getOperateType() == Constant.IN_OPERATE) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 判断是否全部出库完成
     *
     * @param outMaterials     出库任务
     * @param curMaterialIndex 当前操作位置
     * @return -1,没有任务，0，未完成，1，已完成, 2,未完成，但已操作到最后一盘
     */
    public int isOutWareComplete(List<InMaterial> outMaterials, int curMaterialIndex) {
        if (outMaterials.size() <= 0) {
            return -1;
        }
        for (int i = 0, len = outMaterials.size(); i < len; i++) {
            InMaterial m = outMaterials.get(i);
            int operateType = m.getOperateType();
            if (operateType != 2) {//0,未操作 1，已扫料盘，未扫料盒  2，扫料盒并完成
                if (curMaterialIndex != len - 1) {
                    return 0;//false
                }
                return 2;
            }
        }
        return 1;
    }

    /**
     * 保存任务单切换的目的地
     *
     * @param taskName         任务名
     * @param supplier         供应商
     * @param destination      目的地
     * @param destinationIndex 目的地选项
     */
    public void saveDestination(String taskName, String supplier, String destination, int destinationIndex) {
        //保存到数据库
        Log.d(TAG, "target_spinner - 保存到数据库");
        TaskDao taskDao = new TaskDao();
        taskDao.setTaskName(taskName);
        taskDao.setTaskSupplier(supplier);
        taskDao.setDestination(destination);
        taskDao.setDestinationIndex(String.valueOf(destinationIndex));

        GreenDaoUtil.getGreenDaoUtil(mContext).insertTask(taskDao);
        GreenDaoUtil.getGreenDaoUtil(mContext).updateMaterial(taskDao);

        tWeakReference.get().showToast("已保存目的地！");
    }

    /**
     * 扫描料盘
     *
     * @param scanValue        扫描的内容
     * @param curAllMaterials  当前任务的所有数据
     * @param curMaterialIndex 当前任务操作的位置
     * @param curItemMaterial  当前任务操作的item
     */
    public int doScanMaterial(String scanValue, List<InMaterial> curAllMaterials, int curMaterialIndex,
                              List<InMaterial> curItemMaterial) {
        int materialBeanDex = -1;
        int outRes = isOutWareComplete(curAllMaterials, curMaterialIndex);
        if (outRes == 0 || outRes == 2) {
            String[] materialValues = Tool.parserMaterialNo(scanValue);
            if (materialValues != null && materialValues.length >= 8) {
                InMaterial inMaterial = curItemMaterial.get(0);
                if (materialValues[0].equalsIgnoreCase(inMaterial.getMaterialNo())) {
                    materialBeanDex = checkScanMaterial(materialValues, inMaterial);
                    if (materialBeanDex == -1) {
                        tWeakReference.get().showToast("该料盘不在库存,请扫描正确料盘");
                        tWeakReference.get().setMaterialRequestFocus();
                    } else {
                        //加入列表
                        inMaterial.getAlreadyInList().add(inMaterial.getBeanList().get(materialBeanDex));
                        tWeakReference.get().setAdapterNotifyDataChanged();
                        tWeakReference.get().showToast("料盘正确,请扫描料盒");
                        tWeakReference.get().setBoxRequestFocus();
                    }
                } else {
                    tWeakReference.get().showToast("不存在该料号,请扫描正确料盘");
                    tWeakReference.get().setMaterialRequestFocus();
                }
            } else {
                tWeakReference.get().showToast("料号二维码不正确，重新扫描!");
                tWeakReference.get().setMaterialRequestFocus();
            }
        } else if (outRes == -1) {
            tWeakReference.get().showMsgDialog("提示", "未创建出库任务⚠️");
            tWeakReference.get().setMaterialRequestFocus();
        } else {
            tWeakReference.get().showMsgDialog("提示", "出库已全部完成✅");
            tWeakReference.get().setMaterialRequestFocus();
        }

        return materialBeanDex;
    }

    private int checkScanMaterial(String[] materialValues, InMaterial inMaterial) {
        int materialBeanDex = -1;
        List<InMaterial.InMaterialBean> list = inMaterial.getBeanList();
        for (int i = 0, size = list.size(); i < size; i++) {
            InMaterial.InMaterialBean bean = list.get(i);
            //料号@数量@时间戳(唯一码)@打印人员@供应商@站位@序号@生产日期@描述@
            if (materialValues[2].equals(bean.getMaterialTimeStamp())) {
                inMaterial.setOperateType(Constant.InMaterial_OPERATING);
                if (bean.getOperateType() != Constant.OPERATE_COMPLETE) {
                    materialBeanDex = i;

                    bean.setCount(Integer.valueOf(materialValues[1]));
                    bean.setMaterialTimeStamp((materialValues[2]));
                    bean.setSupplierName(materialValues[4]);
                    bean.setProductionTime(materialValues[7]);

                    bean.setOperateType(Constant.IN_OPERATE);
                }
                break;
            }
        }

        return materialBeanDex;
    }

    /**
     * 扫描料盒
     *
     * @param scanValue                扫描的内容
     * @param taskName                 当前任务名
     * @param curAllMaterials          当前任务的所有数据
     * @param curMaterialIndex         当前任务操作的位置
     * @param curItemMaterial          当前任务操作的item
     * @param curItemMaterialOpBeanDex 当前操作任务item中的 InMaterialBean 位置
     * @param operator                 操作员
     * @param destination              目的地
     * @return 返回int[] curMaterialIndex,curItemMaterialOpBeanDex
     */
    public int[] doScanBox(String scanValue, String taskName, List<InMaterial> curAllMaterials, int curMaterialIndex,
                           List<InMaterial> curItemMaterial, int curItemMaterialOpBeanDex, String operator, String destination) {
        int[] curMaterialIndexAndOpBeanDex = new int[]{curMaterialIndex, curItemMaterialOpBeanDex};
        //料盒号@区域@行@列@高@供应商
        String[] box = Tool.parserBox(scanValue);
        if (box != null && box.length >= 6) {
            InMaterial material = curItemMaterial.get(0);
            InMaterial.InMaterialBean inMaterialBean = material.getBeanList().get(curItemMaterialOpBeanDex);
            if (box[0].equalsIgnoreCase(inMaterialBean.getBoxNo())) {
                inMaterialBean.setOperateType(Constant.OPERATE_COMPLETE);
                //实际出库数据增加
                material.setTotalCount(material.getTotalCount() + inMaterialBean.getCount());
                if (material.getTotalCount() >= material.getDemand()) {
                    material.setOperateType(Constant.InMaterial_OPERATED);
                    //判断是否完成出库
                    curMaterialIndexAndOpBeanDex[0] = checkOutComplete(taskName, curAllMaterials, curMaterialIndex, curItemMaterial);
                } else {
                    tWeakReference.get().showToast("出库成功,该料未完成出库!");
                }
                //添加到数据库
                MaterialDao materialDao = MaterialDao.getMaterialDao(material, inMaterialBean, 1,
                        operator, taskName, destination);
                GreenDaoUtil.getGreenDaoUtil(mContext).insertMaterial(materialDao);
                //刷新页面
                tWeakReference.get().setAdapterNotifyDataChanged();
                //重置
                curMaterialIndexAndOpBeanDex[1] = -1;
                tWeakReference.get().setMaterialRequestFocus();
            } else {
                tWeakReference.get().showToast("料盒不正确，重新扫描!");
                tWeakReference.get().setBoxRequestFocus();
            }
        } else {
            tWeakReference.get().showToast("料盒二维码不正确，重新扫描!");
            tWeakReference.get().setBoxRequestFocus();
        }

        return curMaterialIndexAndOpBeanDex;

    }

    //判断是否完成出库
    private int checkOutComplete(String taskName, List<InMaterial> curAllMaterials, int curMaterialIndex,
                                 List<InMaterial> curItemMaterial) {
        int outRes = isOutWareComplete(curAllMaterials, curMaterialIndex);
        switch (outRes) {
            case 1:
                tWeakReference.get().showMsgDialog("提示", "出库已全部完成✅");
                //清除缓存
                iUrgentModel.removeDiskCache(taskName);
                break;

            case 2:
                tWeakReference.get().showMsgDialog("提示", "当前已是最后一种料,但出库未完成!");
                break;

            default:
                //进行下一个料
                tWeakReference.get().showToast("该料出库完成✅,出库下一个料!");
                curMaterialIndex++;
                curItemMaterial.clear();
                curItemMaterial.add(curAllMaterials.get(curMaterialIndex));
                break;
        }

        return curMaterialIndex;
    }

    /**
     * UW系统正常，判断任务上传状态
     *
     * @param taskUpLoadState 判断任务上传状态
     */
    public void checkUpLoadTaskState(int taskUpLoadState) {
        String toastMsg = "";
        switch (taskUpLoadState) {
            case Constant.TASK_NO_CREATE:
                toastMsg = "UW系统已正常运行,结束当前任务再退出!";
                break;

            case Constant.TASK_IN_CREATE:
                toastMsg = "正在上传数据...";
                break;

            case Constant.TASK_COMPLETE:
                toastMsg = "已完成上传数据";
                break;
        }
        if (!toastMsg.isEmpty()) {
            tWeakReference.get().showToast(toastMsg);
        }
        tWeakReference.get().setMaterialRequestFocus();
    }

    /**
     * 结束当前任务
     *
     * @param taskName         任务名
     * @param destinationIndex 任务目的地
     */
    public void fetchFinishTask(String taskName, int destinationIndex) {
        if (!taskName.isEmpty()) {
            if (destinationIndex <= 0) {
                tWeakReference.get().showToast("请选择该任务目的地！");
                return;
            }
        } else {
            tWeakReference.get().showToast("未导入任务单！");
            return;
        }
        //清除缓存
        iUrgentModel.removeDiskCache(taskName);
        //清除页面
        tWeakReference.get().initDisplay();
        tWeakReference.get().showToast("结束该任务！");
    }

    /**
     * 跳到下一个料
     *
     * @param taskName                 任务名
     * @param destinationIndex         目的地
     * @param curAllMaterials          当前任务所有料
     * @param curMaterialIndex         当前任务操作的料号位置
     * @param curItemMaterial          当前操作的料号
     * @param curItemMaterialOpBeanDex 当前操作料号的谋一盘
     */
    public int[] fetchSetNextItem(String taskName, int destinationIndex, List<InMaterial> curAllMaterials, int curMaterialIndex,
                                  List<InMaterial> curItemMaterial, int curItemMaterialOpBeanDex) {
        int[] curMaterialIndexAndOpBeanDex = new int[]{curMaterialIndex, curItemMaterialOpBeanDex};
        if (!taskName.isEmpty()) {
            if (destinationIndex <= 0) {
                tWeakReference.get().showToast("请选择该任务目的地！");
                return curMaterialIndexAndOpBeanDex;
            }
        } else {
            tWeakReference.get().showToast("未导入任务单！");
            return curMaterialIndexAndOpBeanDex;
        }
        //判断当前是否是最后一个料
        if (curMaterialIndex == (curAllMaterials.size() - 1)) {
            tWeakReference.get().showToast("当前已经是最后一个料啦！");
        } else {
            curMaterialIndex++;
            curItemMaterial.clear();
            curItemMaterial.add(curAllMaterials.get(curMaterialIndex));
            tWeakReference.get().setAdapterNotifyDataChanged();
        }

        //判断该料是否出库完成
        if (curItemMaterial.get(0).getOperateType() == Constant.InMaterial_OPERATED) {
            tWeakReference.get().showToast("该料已出库！");
            tWeakReference.get().requestDisallowScan();
        } else {
            //-1，扫料盘，大于-1，扫料盒
            curItemMaterialOpBeanDex = getInMaterialBeanOpIndex(curItemMaterial.get(0).getBeanList());
            if (curItemMaterialOpBeanDex == -1) {
                tWeakReference.get().showToast("请扫料号！");
                tWeakReference.get().setMaterialRequestFocus();
            } else {
                tWeakReference.get().showToast("请扫料盒！");
                tWeakReference.get().setBoxRequestFocus();
            }
        }


        /*
        int res = isOutWareComplete(curAllMaterials, curMaterialIndex);
        switch (res) {

            case -1:
                tWeakReference.get().showToast("未导入任务单！");
                break;

            case 0://未完成，进行下一个料
                tWeakReference.get().showToast("出库下一个料！");
                curMaterialIndex++;
                curItemMaterial.clear();
                curItemMaterial.add(curAllMaterials.get(curMaterialIndex));
                tWeakReference.get().setAdapterNotifyDataChanged();
                //-1，扫料盘，大于-1，扫料盒
                curItemMaterialOpBeanDex = getInMaterialBeanOpIndex(curItemMaterial.get(0).getBeanList());
                if (curItemMaterialOpBeanDex == -1) {
                    tWeakReference.get().showToast("请扫料号！");
                    tWeakReference.get().setMaterialRequestFocus();
                } else {
                    tWeakReference.get().showToast("请扫料盒！");
                    tWeakReference.get().setBoxRequestFocus();
                }
                curMaterialIndexAndOpBeanDex[0] = curMaterialIndex;
                curMaterialIndexAndOpBeanDex[1] = curItemMaterialOpBeanDex;
                break;

            case 1://已完成
                tWeakReference.get().showMsgDialog("提示", "出库已全部完成✅");
                //清除缓存
                iUrgentModel.removeDiskCache(taskName);
                break;

            case 2://未完成，但已操作到最后一盘
                tWeakReference.get().showMsgDialog("提示", "当前已是最后一种料,但出库未完成!");
                break;
        }
        */

        curMaterialIndexAndOpBeanDex[0] = curMaterialIndex;
        curMaterialIndexAndOpBeanDex[1] = curItemMaterialOpBeanDex;

        return curMaterialIndexAndOpBeanDex;
    }

    /**
     * 跳到上一个料
     *
     * @param taskName                 当前任务名
     * @param destinationIndex         目的地
     * @param curAllMaterials          当前所有任务
     * @param curMaterialIndex         当前操作的料的位置
     * @param curItemMaterial          当前操作的料
     * @param curItemMaterialOpBeanDex 当前操作料的料盘
     * @return
     */
    public int[] fetchSetLastItem(String taskName, int destinationIndex, List<InMaterial> curAllMaterials, int curMaterialIndex,
                                  List<InMaterial> curItemMaterial, int curItemMaterialOpBeanDex) {
        int[] curMaterialIndexAndOpBeanDex = new int[]{curMaterialIndex, curItemMaterialOpBeanDex};

        if (!taskName.isEmpty()) {
            if (destinationIndex <= 0) {
                tWeakReference.get().showToast("请选择该任务目的地！");
                return curMaterialIndexAndOpBeanDex;
            }
        } else {
            tWeakReference.get().showToast("未导入任务单！");
            return curMaterialIndexAndOpBeanDex;
        }

        //判断是否是第一个料
        if (curMaterialIndex == 0) {
            tWeakReference.get().showToast("当前已经是第一个料啦！");
        } else {
            curMaterialIndex--;
            curItemMaterial.clear();
            curItemMaterial.add(curAllMaterials.get(curMaterialIndex));
            tWeakReference.get().setAdapterNotifyDataChanged();
        }

        //判断该料是否出库完成
        if (curItemMaterial.get(0).getOperateType() == Constant.InMaterial_OPERATED) {
            tWeakReference.get().showToast("该料已出库！");
            tWeakReference.get().requestDisallowScan();
        } else {
            //-1，扫料盘，大于-1，扫料盒
            curItemMaterialOpBeanDex = getInMaterialBeanOpIndex(curItemMaterial.get(0).getBeanList());
            if (curItemMaterialOpBeanDex == -1) {
                tWeakReference.get().showToast("请扫料号！");
                tWeakReference.get().setMaterialRequestFocus();
            } else {
                tWeakReference.get().showToast("请扫料盒！");
                tWeakReference.get().setBoxRequestFocus();
            }
        }

        curMaterialIndexAndOpBeanDex[0] = curMaterialIndex;
        curMaterialIndexAndOpBeanDex[1] = curItemMaterialOpBeanDex;

        return curMaterialIndexAndOpBeanDex;
    }


    /**
     * UW后台状态活跃，并且当前处于为创建任务
     *
     * @param taskDao  当前任务数据
     * @param taskName 任务名
     */
    public void createTopTaskDao(TaskDao taskDao, String taskName) {
        if (taskDao != null) {
            Log.d(TAG, "getNotNullData: " + taskDao.taskDaoToString());
            List<MaterialDao> list = GreenDaoUtil.getGreenDaoUtil(mContext).queryMaterial(taskDao.getTaskName(),
                    taskDao.getTaskSupplier(), taskDao.getDestination(), Constant.OUT_WARE);
            if (list.isEmpty()) {
                //删除数据库数据
                GreenDaoUtil.getGreenDaoUtil(mContext).deleteTask(taskDao);
                //清除缓存
                iUrgentModel.removeDiskCache(taskDao.getTaskName());
                //获取下一个任务名
                taskDao = GreenDaoUtil.getGreenDaoUtil(mContext).getTopTask();
                createTopTaskDao(taskDao, taskName);
            } else {
                //创建任务
                createTask(taskDao, list, taskName);
            }
        } else {
            tWeakReference.get().initDisplay();
            tWeakReference.get().dismissLoading();
            tWeakReference.get().showToast("UW系统已正常使用，可退出此软件!!!");
        }

    }

    /**
     * 创建任务
     *
     * @param taskDao  任务的数据
     * @param list     任务数据
     * @param taskName 任务名
     */
    private void createTask(TaskDao taskDao, List<MaterialDao> list, String taskName) {

        tWeakReference.get().showLoading("上传数据中...");

        Tool.postTaskState(Constant.TASK_IN_CREATE);

        //mHttpUtils.createTask(taskDao, String.valueOf(Constants.OUT_WARE), list);

        mApplication.getNetApi()
                .createTask(taskDao.getTaskSupplier(), String.valueOf(Constant.OUT_WARE), taskDao.getDestination())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JsonObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        //TaskId
                        if (jsonObject != null) {
                            Log.d(TAG, "createTask - onNext: " + jsonObject.toString());

                            JsonElement jsonElement = jsonObject.get("result");
                            int resultCode = jsonElement.getAsInt();
                            Gson gson = new Gson();

                            if (resultCode == 200) {
                                //上传数据
                                TaskId baseResult = gson.fromJson(jsonObject, TaskId.class);
                                TaskId.DataBean dataBean = baseResult.getData();
                                int id = (int) dataBean.getTaskId();
                                String msgJson = MaterialDao.materialToJson(list, id);
                                Log.d(TAG, "createTask - onNext: " + " - " + id + "\n" + msgJson);
                                try {
                                    uploadRecord(taskDao, msgJson, taskName);
                                } catch (Exception e) {
                                    //java.lang.IllegalArgumentException: @Part annotation must supply a name or use MultipartBody.Part parameter type. (parameter #1)
                                    Log.d(TAG, "onNext: uploadRecord - " + e.toString());
                                }
                            } else {
                                BaseResult baseResult = gson.fromJson(jsonObject, BaseResult.class);
                                String dataStr = String.valueOf(baseResult.getData());
                                Tool.postTaskState(Constant.TASK_NO_CREATE);
                                tWeakReference.get().dismissLoading();
                                tWeakReference.get().showToast(dataStr);
                            }
                        } else {
                            tWeakReference.get().dismissLoading();
                            tWeakReference.get().showToast("创建任务失败!!!");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Log.d(TAG, "createTask - onError: " + e.toString());

                        Tool.postTaskState(Constant.TASK_NO_CREATE);
                        tWeakReference.get().showToast("创建任务失败!!!");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 上传任务数据
     *
     * @param taskDao  任务数据
     * @param msgJson  上传内容
     * @param taskName 任务名
     */
    private void uploadRecord(TaskDao taskDao, String msgJson, String taskName) throws Exception {
        Log.d(TAG, "uploadRecord: " + " - " + taskName + " - " + taskDao.taskDaoToString() + "\n" + msgJson);
        RequestBody body = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), msgJson);
        mApplication.getNetApi()
                .uploadTask(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResult<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BaseResult<String> stringBaseResult) {
                        if (stringBaseResult != null) {
                            Log.d(TAG, "uploadTask - onNext: " + stringBaseResult.toString());
                            if (stringBaseResult.getResultCode() == 200) {
                                String data = stringBaseResult.getData();
                                if (data != null && data.length() > 6) {
                                    tWeakReference.get().showDialog("提示", data, -1);
                                }
                                //删除本地数据库数据
                                GreenDaoUtil.getGreenDaoUtil(mContext).deleteMaterialRecord(taskDao, Constant.OUT_WARE);
                                GreenDaoUtil.getGreenDaoUtil(mContext).deleteTask(taskDao);
                                //如果上传的是当前操作的任务，清空页面
                                if (taskDao.getTaskName().equalsIgnoreCase(taskName)) {
                                    tWeakReference.get().initDisplay();
                                }
                                //清除缓存
                                iUrgentModel.removeDiskCache(taskDao.getTaskName());

                                //查询数据库是否还有任务单
                                TaskDao restDao = GreenDaoUtil.getGreenDaoUtil(mContext).getTopTask();
                                if (restDao != null) {
                                    Log.d(TAG, "仍有任务单未上传 - " + restDao.getTaskName());

                                    //继续上传数据
                                    createTopTaskDao(restDao, taskName);

                                } else {
                                    Log.d(TAG, "任务单上传完成 - taskdao - null");
                                    iUrgentModel.removeAllDiskCache("taskNames");
                                    Tool.postTaskState(Constant.TASK_COMPLETE);
                                    tWeakReference.get().dismissLoading();
                                    tWeakReference.get().showToast("上传任务完成✅");
                                }

                            } else {
                                Tool.postTaskState(Constant.TASK_NO_CREATE);
                                tWeakReference.get().dismissLoading();
                                tWeakReference.get().showToast("上传任务失败!!!");
                            }
                        } else {
                            tWeakReference.get().dismissLoading();
                            tWeakReference.get().showToast("上传任务失败!!!");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "uploadTask - onError: " + e.toString());

                        Tool.postTaskState(Constant.TASK_NO_CREATE);
                        tWeakReference.get().showToast("上传任务失败!!!");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
