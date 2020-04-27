package com.jimilab.uwclient.view.impl.urgent;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.jimilab.uwclient.R;
import com.jimilab.uwclient.adapter.InWareAdapter;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.bean.InMaterial;
import com.jimilab.uwclient.bean.UrgentBean;
import com.jimilab.uwclient.dao.GreenDaoUtil;
import com.jimilab.uwclient.dao.TaskDao;
import com.jimilab.uwclient.presenter.UrgentPresenter;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;
import com.jimilab.uwclient.view.IBaseView;
import com.jimilab.uwclient.view.IUrgentView;
import com.jimilab.uwclient.view.impl.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-31
 * @描述 :
 */
public class OutFragment extends BaseFragment<UrgentPresenter<IUrgentView>, IUrgentView> implements IUrgentView, IBaseView.onDialogListener {

    private static final String TAG = OutFragment.class.getSimpleName();
    @BindView(R.id.tv_operator)
    TextView tvOperator;
    @BindView(R.id.et_material)
    EditText etMaterial;
    @BindView(R.id.et_box)
    EditText etBox;
    @BindView(R.id.tv_operate_time)
    TextView tvOperateTime;
    @BindView(R.id.tv_task_dir)
    TextView tvTaskDir;
    @BindView(R.id.iv_set_task)
    ImageView ivSetTask;
    @BindView(R.id.tv_supplier)
    TextView tvSupplier;
    @BindView(R.id.btn_lock_target)
    Button btnLockTarget;
    @BindView(R.id.target_spinner)
    Spinner targetSpinner;
    @BindView(R.id.tv_scanner)
    TextView tvScanner;
    @BindView(R.id.lv_in_task)
    ListView lvInTask;
    @BindView(R.id.fab_next)
    FloatingActionButton fabNext;
    @BindView(R.id.fab_finish)
    FloatingActionButton fabFinish;
    @BindView(R.id.fla_menu)
    FloatingActionsMenu flaMenu;
    @BindView(R.id.fab_last)
    FloatingActionButton fabLast;
    //测试
    @BindView(R.id.tv_operate)
    TextView tvOperate;
    @BindView(R.id.ib_show_tasks)
    ImageButton ibShowTasks;

    //0，未创建任务上传数据；1，正在创建任务上传数据；2，已完成创建任务上传数据
    private int taskUpLoadState = Constant.TASK_NO_CREATE;
    private int uwLiveState = Constant.UW_DOWNTIME;
    //private ArrayList<InMaterial> outMaterials = new ArrayList<>();//出库任务
    private boolean taskSaved = false;//任务单保存过了
    private List<InMaterial> curAllMaterials = new ArrayList<>();//当前操作数据的列表
    private List<InMaterial> curItemMaterial = new ArrayList<>();//当前操作的一项数据
    private int curMaterialIndex = -1;//当前操作位置 与 curItemMaterial 相对应
    private int curItemMaterialOpBeanDex = -1;// curItemMaterial 中当前操作的料盘 InMaterialBean
    private int destinationIndex = -1;//选中的目的地
    private boolean destinationChanged = false;//目的地改变了

    private InWareAdapter mAdapter;
    private InputMethodManager inputMethodManager;
    private UrgentBean urgentBean;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");

        postUrgentBean(false);

        //Tool.postCurAllMaterial(curAllMaterials);
        //Tool.postCurMaterialIndex(curMaterialIndex);
        //Tool.postTaskSavedState(taskSaved);
        //Tool.postCurTaskName(tvTaskDir.getText().toString().trim());
        //Tool.postDestinationIndex(destinationIndex);

    }

    //发送消息
    private void postUrgentBean(boolean isExit) {

        urgentBean = new UrgentBean();
        urgentBean.setTaskName(tvTaskDir.getText().toString().trim());
        urgentBean.setCurAllMaterial(curAllMaterials);
        urgentBean.setCurMaterialIndex(curMaterialIndex);
        urgentBean.setCurTaskState(taskUpLoadState);
        urgentBean.setDestinationIndex(destinationIndex);
        urgentBean.setTaskSave(taskSaved);
        urgentBean.setExit(isExit);

        Log.d(TAG, "postUrgentBean: " + urgentBean.toString());

        Tool.postUrgentBean(urgentBean);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (!curItemMaterial.isEmpty()) {
                if (curItemMaterialOpBeanDex == -1) {
                    showToast("请扫描料盘");
                    setMaterialRequestFocus();
                    inputMethodManager.hideSoftInputFromWindow(etMaterial.getWindowToken(), 0);
                } else {
                    showToast("料盘正确,请扫描料盒");
                    setBoxRequestFocus();
                    inputMethodManager.hideSoftInputFromWindow(etBox.getWindowToken(), 0);
                }
            }

        } else {
            Tool.postCurAllMaterial((ArrayList<InMaterial>) curAllMaterials);
        }
    }

    @Override
    protected void init(Bundle bundle) {
        Log.d(TAG, "init: ");
        //必须调用
        ButterKnife.bind(this, view);
        //订阅消息
        registerEvent();
        //禁止扫描
        //requestDisallowScan();
        initViewEvent(bundle);

        mAdapter = new InWareAdapter(mContext, curItemMaterial);
        lvInTask.setAdapter(mAdapter);

        //获取缓存的任务列表
        presenter.fetchTaskNames("taskNames", Constant.FIRST_LOAD, null, null);
    }

    private void initViewEvent(Bundle bundle) {
        tvOperator.setText(bundle.getString("usrNo"));
        targetSpinner.setOnItemSelectedListener(onItemSelectedListener);

        etMaterial.setOnEditorActionListener(mEditorActionListener);
        etBox.setOnEditorActionListener(mEditorActionListener);

        inputMethodManager = (InputMethodManager) uwClientApplication.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(etMaterial.getWindowToken(), 1);
            inputMethodManager.hideSoftInputFromWindow(etBox.getWindowToken(), 1);
        }
    }

    @Override
    protected UrgentPresenter<IUrgentView> createPresenter() {
        return new UrgentPresenter<>(uwClientApplication, mContext);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_in_ware_house;
    }

    @Override
    protected void destroy() {

    }

    @Override
    public void initDisplay() {
        taskSaved = false;
        updateAllMaterials(null);
        curItemMaterial.clear();
        curMaterialIndex = -1;
        curItemMaterialOpBeanDex = -1;
        destinationIndex = -1;
        destinationChanged = false;

        targetSpinner.setSelection(0);
        targetSpinner.setEnabled(true);
        tvTaskDir.setText("");
        tvSupplier.setText("");
        tvScanner.setText("");
        tvOperateTime.setText("");
        btnLockTarget.setText("保存");

        setAdapterNotifyDataChanged();
    }

    @Override
    public void showMemoryTaskName(String taskName, List<InMaterial> list) {
        Log.d(TAG, "showMemoryTaskNames: " + taskName);
        if (list != null) {
            //获取选中的任务的进度
            curMaterialIndex = presenter.getCurMaterialOpIndex(list);
            // TODO: 2019-11-04
            if (curMaterialIndex >= 0) {
                updateAllMaterials(list);
                updateAdapter(curMaterialIndex, list);
                //加载缓存任务
                TaskDao taskDao = GreenDaoUtil.getGreenDaoUtil(mContext)
                        .getTopTask(taskName, curItemMaterial.get(0).getSupplier());
                //显示当前任务相关信息
                setTaskInfo(taskName, taskDao);
                //判断接下来扫描的内容
                checkScanMaterialOrBox(true);
            }
            dismissLoading();
        }

        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(etMaterial.getWindowToken(), 1);
            inputMethodManager.hideSoftInputFromWindow(etBox.getWindowToken(), 1);
        }
    }

    //显示当前任务相关信息
    private void setTaskInfo(String taskName, TaskDao taskDao) {
        tvTaskDir.setText(taskName);
        tvSupplier.setText(curItemMaterial.get(0).getSupplier());
        destinationIndex = Integer.parseInt(taskDao.getDestinationIndex());
        targetSpinner.setSelection(destinationIndex);
        targetSpinner.setEnabled(false);
        btnLockTarget.setText("解锁");
        taskSaved = true;
    }

    //更新当前操作的任务列表
    private void updateAllMaterials(List<InMaterial> updateList) {
        curAllMaterials.clear();
        if (updateList != null) {
            curAllMaterials.addAll(updateList);
        }
        //发送消息
        Tool.postCurAllMaterial((ArrayList<InMaterial>) curAllMaterials);
    }

    //更新当前操作的任务项
    private void updateAdapter(int index, List<InMaterial> originMaterials) {
        curItemMaterial.clear();
        curItemMaterial.add(originMaterials.get(index));
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 导入文件 回调
     *
     * @param daoExitName 数据库是否存在该导入的文件名
     * @param loadName    导入的文件名
     * @param loadList    导入文件解析得到的任务列表
     */
    @Override
    public void onLoadTaskFile(boolean daoExitName, String loadName, List<InMaterial> loadList) {
        // TODO: 2019-11-05 导入文件 回调
        Log.d(TAG, "onLoadTaskFile: " + "-" + daoExitName + "-" + loadName);

        /*
        int index = presenter.getCurMaterialOpIndex(loadList);
        if (index >= 0) {
            curAllMaterials.clear();
            curAllMaterials.addAll(loadList);
            updateAdapter(1, loadList);
        }
        */

        if (loadList != null && (!loadList.isEmpty())) {
            doLoadTaskFile(daoExitName, loadName, loadList);
        } else {
            if (daoExitName) {
                showToast("选择的任务已完成，请重新选择!");
            } else {
                showToast("任务单存在异常，请导入正确的任务单");
            }
        }

    }

    private void doLoadTaskFile(boolean daoExitName, String loadName, List<InMaterial> loadList) {
        curMaterialIndex = presenter.getCurMaterialOpIndex(loadList);
        updateAllMaterials(loadList);
        updateAdapter(curMaterialIndex, loadList);
        //设置页面
        tvSupplier.setText(curItemMaterial.get(0).getSupplier());
        tvTaskDir.setText(loadName);
        if (daoExitName) {
            showToast("该任务存在历史记录,正在恢复该任务！");
            //加载该文件缓存信息
            TaskDao taskDao = GreenDaoUtil.getGreenDaoUtil(mContext).getTopTask(loadName, curItemMaterial.get(0).getSupplier());
            //设置页面
            targetSpinner.setSelection(Integer.parseInt(taskDao.getDestinationIndex()));
            targetSpinner.setEnabled(false);
            btnLockTarget.setText("解锁");
            //设置任务状态
            taskSaved = true;
            //判断接下来扫描的内容
            checkScanMaterialOrBox(true);
        } else {
            showToast("请选择该任务目的地！");
            //设置页面
            targetSpinner.setSelection(0);
            targetSpinner.setEnabled(true);
            btnLockTarget.setText("保存");
            //设置任务状态
            taskSaved = false;
        }
    }

    //判断扫料盘还是扫料盒
    private void checkScanMaterialOrBox(boolean needShowToast) {
        curItemMaterialOpBeanDex = presenter.getInMaterialBeanOpIndex(curItemMaterial.get(0).getBeanList());
        if (curItemMaterialOpBeanDex > -1) {
            setBoxRequestFocus();
            if (needShowToast) {
                showToast("请扫料盒");
            }
        } else {
            setMaterialRequestFocus();
            if (needShowToast) {
                showToast("请扫料盘");
            }
        }
    }

    @OnClick({R.id.iv_set_task, R.id.btn_lock_target, R.id.fab_last, R.id.fab_next,
            R.id.fab_finish, R.id.tv_operate, R.id.ib_show_tasks})
    void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_set_task:
                //导入文件
                importFile(false);
                break;

            case R.id.btn_lock_target:
                //保存目的地
                doSaveTarget();
                break;

            case R.id.fab_last:
                //操作上一个
                if (taskSaved) {
                    setOpLastItem();
                } else {
                    showToast("请保存该任务目的地！");
                }
                flaMenu.collapse();
                break;

            case R.id.fab_next:
                //操作下一个
                if (taskSaved) {
                    setOpNextItem();
                } else {
                    showToast("请保存该任务目的地！");
                }
                flaMenu.collapse();
                break;

            case R.id.fab_finish:
                //结束当前任务
                if (taskSaved) {
                    showDialog("警告", "\n是否结束当前任务?", 2);
                } else {
                    showToast("请保存该任务目的地！");
                }
                flaMenu.collapse();
                break;

            case R.id.tv_operate://测试
                if (Constant.IP.equals(uwClientApplication.getIp())) {
                    uwClientApplication.setIp("http://10.10.11.90:8080/uw_server/");
                    showToast("已切换为（90）");
                } else {
                    uwClientApplication.setIp(Constant.IP);
                    showToast("已切换为（00）");
                }
                break;

            case R.id.ib_show_tasks:
                //加载缓存中未完成的任务单
                importFile(true);
                break;
        }
    }

    //结束当前任务
    private void finishTask() {
        String taskName = tvTaskDir.getText().toString().trim();
        presenter.fetchFinishTask(taskName, destinationIndex);
    }

    //操作下一个
    private void setOpNextItem() {
        String taskName = tvTaskDir.getText().toString().trim();
        int[] curMaterialIndexAndOpBeanDex = presenter.fetchSetNextItem(taskName, destinationIndex, curAllMaterials, curMaterialIndex,
                curItemMaterial, curItemMaterialOpBeanDex);
        curMaterialIndex = curMaterialIndexAndOpBeanDex[0];
        curItemMaterialOpBeanDex = curMaterialIndexAndOpBeanDex[1];
    }

    //操作上一个
    private void setOpLastItem() {
        String taskName = tvTaskDir.getText().toString().trim();
        int[] curMaterialIndexAndOpBeanDex = presenter.fetchSetLastItem(taskName, destinationIndex, curAllMaterials, curMaterialIndex,
                curItemMaterial, curItemMaterialOpBeanDex);
        curMaterialIndex = curMaterialIndexAndOpBeanDex[0];
        curItemMaterialOpBeanDex = curMaterialIndexAndOpBeanDex[1];
    }

    //保存目的地
    private void doSaveTarget() {
        if (taskSaved) {//解锁
            btnLockTarget.setText("保存");
            taskSaved = false;
            targetSpinner.setEnabled(true);
        } else {//保存
            if (!(TextUtils.isEmpty(tvTaskDir.getText().toString()))) {
                if (targetSpinner.getSelectedItemPosition() > 0) {
                    btnLockTarget.setText("解锁");
                    targetSpinner.setEnabled(false);
                    //保存目的地
                    saveDestination();
                    taskSaved = true;
                    //判断接下来扫描的内容
                    checkScanMaterialOrBox(true);
                } else {
                    showToast("请选择该任务目的地！");
                }
            } else {
                showToast("未导入任务单！");
            }
        }
    }

    //保存切换的目的地
    private void saveDestination() {
        Log.d(TAG, "saveDestination: " + destinationChanged);
        if (destinationChanged) {
            presenter.saveDestination(tvTaskDir.getText().toString().trim(),
                    tvSupplier.getText().toString().trim(),
                    String.valueOf(targetSpinner.getSelectedItem()),
                    targetSpinner.getSelectedItemPosition());
            destinationChanged = false;
        }
    }

    //导入文件
    private void importFile(boolean isGetCacheMemory) {
        //禁止扫描
        //requestDisallowScan();
        if (uwLiveState == Constant.UW_DOWNTIME) {//uw后台处于活跃状态
            int res = presenter.isOutWareComplete(curAllMaterials, curMaterialIndex);
            Log.d(TAG, "onClick: isOutWareComplete - " + res);
            if (res == 0 || res == 2) {
                int spinnerIndex = targetSpinner.getSelectedItemPosition();
                if (spinnerIndex > 0 && taskSaved) {
                    //未完成,提示
                    showDialog("警告", "当前任务未完成\n\n是否切换其他任务?", isGetCacheMemory ? 3 : 1);
                } else if (spinnerIndex < 0) {
                    showToast("请先为该任务选择目的地！");
                } else if (!taskSaved) {
                    showToast("请保存该任务目的地！");
                }
            } else {
                //未完成,提示
                if (!isGetCacheMemory) {
                    //不是加载缓存
                    choseTask(Constant.FIRST_LOAD);
                } else {
                    //加载缓存
                    // TODO: 2019-11-28
                    presenter.fetchTaskNames("taskNames", Constant.FIRST_LOAD, null, null);
                }
            }
        } else {
            showToast("UW系统已正常使用，可退出此软件!!!");
        }
    }

    /**
     * 选择本地文件
     *
     * @param requestCode 请求类型
     */
    private void choseTask(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            String curTaskName = tvTaskDir.getText().toString().trim();
            presenter.fetchLoadTaskFile(requestCode, curTaskName, curAllMaterials, uri);//执行完回调 onLoadTaskFile
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setMaterialRequestFocus() {
        etMaterial.setEnabled(true);
        etMaterial.setText("");
        etMaterial.requestFocus();
        inputMethodManager.hideSoftInputFromWindow(etMaterial.getWindowToken(), 1);
    }

    @Override
    public void setBoxRequestFocus() {
        etBox.setEnabled(true);
        etBox.setText("");
        etBox.requestFocus();
        inputMethodManager.hideSoftInputFromWindow(etBox.getWindowToken(), 1);
    }

    @Override
    public void requestDisallowScan() {
        etMaterial.setEnabled(false);
        etBox.setEnabled(false);
    }

    @Override
    public void setAdapterNotifyDataChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showDialog(String title, String msg, int condition) {
        Tool.showDialog(this, mContext, title, msg, condition);
    }

    //导入任务单，确定按钮
    @Override
    public void onPositiveClick(DialogInterface dialog, int which, int condition) {
        switch (condition) {
            case 1:
                //确定切换到别的任务单,则需缓存当前任务单
                choseTask(Constant.CHANGE);
                break;

            case 2:
                //结束当前任务
                finishTask();
                break;

            case 3:
                //加载缓存任务,先保存当前任务数据
                String curTaskName = tvTaskDir.getText().toString().trim();
                presenter.fetchTaskNames("taskNames", Constant.CHANGE, curAllMaterials, curTaskName);
                break;
        }
    }

    @Override
    public void onNegativeClick(DialogInterface dialog, int which, int condition) {

    }

    //目的地选择监听
    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position != destinationIndex) {
                destinationIndex = position;
                if (position > 0) {
                    destinationChanged = true;
                    showToast("选择成功,请点击\"保存\"");
                }
            } else {
                destinationChanged = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    //输入监听
    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (uwLiveState == Constant.UW_DOWNTIME) {
                    if (!curAllMaterials.isEmpty()) {
                        //uw系统不能正常运行
                        if (targetSpinner.getSelectedItemPosition() > 0 && taskSaved) {
                            if (event.getAction() == KeyEvent.ACTION_UP) {
                                String scanValue = String.valueOf(v.getText()).replace("\r", "");
                                Log.d(TAG, "scanValue - " + scanValue);
                                tvScanner.setText(scanValue);
                                //执行扫描的内容
                                switch (v.getId()) {
                                    case R.id.et_material:
                                        showLoading("正在加载...");
                                        Log.d(TAG, "onEditorAction: et_material - curMaterialIndex - " + curMaterialIndex);
                                        curItemMaterialOpBeanDex = presenter.doScanMaterial(scanValue, curAllMaterials, curMaterialIndex, curItemMaterial);
                                        dismissLoading();
                                        break;
                                    case R.id.et_box:
                                        showLoading("正在加载...");
                                        String taskName = tvTaskDir.getText().toString().trim();
                                        String operator = tvOperator.getText().toString().trim();
                                        String destination = String.valueOf(targetSpinner.getSelectedItem());
                                        int[] curMaterialIndexAndOpBeanDex = presenter.doScanBox(scanValue, taskName, curAllMaterials, curMaterialIndex,
                                                curItemMaterial, curItemMaterialOpBeanDex, operator, destination);
                                        curMaterialIndex = curMaterialIndexAndOpBeanDex[0];
                                        curItemMaterialOpBeanDex = curMaterialIndexAndOpBeanDex[1];
                                        tvOperateTime.setText(String.format("(%s)", "(" + Tool.getTime() + ")"));
                                        dismissLoading();
                                        Log.d(TAG, "onEditorAction: et_box - curMaterialIndex - " + curMaterialIndex);
                                        break;
                                }
                                return true;
                            }
                            return true;
                        } else if (targetSpinner.getSelectedItemPosition() <= 0) {
                            showToast("请选择该任务目的地！");
                            checkScanMaterialOrBox(false);
                        } else {
                            showToast("请保存该任务目的地！");
                            checkScanMaterialOrBox(false);
                        }
                    } else {
                        showToast("未导入任务单！");
                    }

                } else {
                    //uw系统正常
                    presenter.checkUpLoadTaskState(taskUpLoadState);
                }
            }
            return false;
        }
    };

    //注册消息监听
    private void registerEvent() {

        //订阅 UW后台 状态
        Tool.getUwLiveState()
                .observe(this, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        //Log.d(TAG, "UwLive - onChanged: " + integer);
                        //更新状态
                        uwLiveState = integer;
                        if (integer == Constant.UW_DOWNTIME) {
                            taskUpLoadState = Constant.TASK_NO_CREATE;
                            return;
                        }
                        if (taskUpLoadState != Constant.TASK_NO_CREATE) {
                            return;
                        }
                        //上传数据库中任务单列表中第一个
                        TaskDao taskDao = GreenDaoUtil.getGreenDaoUtil(mContext).getTopTask();
                        String taskName = tvTaskDir.getText().toString().trim();
                        presenter.createTopTaskDao(taskDao, taskName);
                    }
                });


        //订阅 上传数据状态
        Tool.getTaskState()
                .observe(this, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        Log.d(TAG, "taskUpLoadState - onChanged: " + integer);
                        //更新上传数据状态
                        taskUpLoadState = integer;
                    }
                });

        //订阅是否发送当前数据的消息
        Tool.getRequestUrgentBean()
                .observe(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            postUrgentBean(true);
                        }
                    }
                });

    }

}
