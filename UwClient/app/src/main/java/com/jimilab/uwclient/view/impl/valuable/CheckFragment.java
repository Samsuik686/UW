package com.jimilab.uwclient.view.impl.valuable;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.jimilab.uwclient.R;
import com.jimilab.uwclient.adapter.TaskSpinnerAdapter;
import com.jimilab.uwclient.bean.BaseResult;
import com.jimilab.uwclient.bean.CheckTask;
import com.jimilab.uwclient.presenter.ValuablePresenter;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;
import com.jimilab.uwclient.view.IValuableView;
import com.jimilab.uwclient.view.impl.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckFragment extends BaseFragment<ValuablePresenter<IValuableView>, IValuableView> implements IValuableView {
    private static final String TAG = CheckFragment.class.getSimpleName();
    @BindView(R.id.check_spinner)
    Spinner checkSpinner;
    @BindView(R.id.tv_scan_check)
    TextView tvScanCheck;
    @BindView(R.id.tv_check_material_unique)
    TextView tvCheckMaterialUnique;
    @BindView(R.id.tv_check_num)
    TextView tvCheckNum;
    @BindView(R.id.tv_check_time)
    TextView tvCheckTime;
    @BindView(R.id.btn_fresh_check)
    Button btnFreshCheck;
    @BindView(R.id.et_check_material)
    EditText etCheckMaterial;

    private CheckTask.DataBean.ListBean topBean;
    private List<CheckTask.DataBean.ListBean> beanList = new ArrayList<>();
    private TaskSpinnerAdapter<CheckTask.DataBean.ListBean> spinnerAdapter;
    private String mTaskId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void init(Bundle bundle) {
        //必须调用
        ButterKnife.bind(this, view);
        initViewEvent();
        topBean = new CheckTask.DataBean.ListBean();
        topBean.setSupplierName("");
        topBean.setTaskName("请选择盘点任务");
        beanList.add(0, topBean);
        spinnerAdapter = new TaskSpinnerAdapter<>(beanList, uwClientApplication);
        checkSpinner.setAdapter(spinnerAdapter);
        checkSpinner.setSelection(0);
        presenter.fetchGetTaskList(this, uwClientApplication.getTOKEN());
    }

    private void initViewEvent() {
        //任务选择监听
        checkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);

                mTaskId = "";
                if (position > 0) {
                    mTaskId = String.valueOf(beanList.get(position).getTaskId());
                    scanNext();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //扫描输入监听
        etCheckMaterial.setOnEditorActionListener(mEditorActionListener);
    }

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                if (!mTaskId.isEmpty()) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        String scanValue = String.valueOf(v.getText()).replace("\r", "");
                        Log.d(TAG, "scanValue - " + scanValue);

                        tvScanCheck.setText(scanValue);

                        //料号@数量@时间戳(唯一码)@打印人员@供应商@站位@序号@生产日期@描述@
                        String[] materialValues = Tool.parserMaterialNo(scanValue);
                        if (materialValues.length >= 9) {
                            tvCheckNum.setText(materialValues[1]);
                            tvCheckMaterialUnique.setText(materialValues[2]);
                            tvCheckTime.setText(Tool.getTime());

                            //上传数据
                            presenter.fetchUploadMaterial(CheckFragment.this, uwClientApplication.getTOKEN(), mTaskId,
                                    tvCheckMaterialUnique.getText().toString().trim(), tvCheckNum.getText().toString().trim());

                        } else {
                            showToast("料号不正确,请重新扫描!");
                            scanNext();
                        }
                    }
                    return true;

                } else {
                    showToast("请选择盘点任务!");
                }

            }
            return false;
        }
    };

    @Override
    public void scanNext() {
        tvScanCheck.setText("");
        tvCheckMaterialUnique.setText("");
        tvCheckNum.setText("");
        tvCheckTime.setText("");
        setScanMaterialRequestFocus();
    }

    @Override
    protected ValuablePresenter<IValuableView> createPresenter() {
        return new ValuablePresenter<>(uwClientApplication);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_check;
    }

    @Override
    protected void destroy() {

    }

    @Override
    public void showTaskList(String token, BaseResult taskList) {
        //显示盘点任务列表
        if (taskList != null) {
            Log.d(TAG, "showTaskList: " + ((CheckTask) taskList).getData().getList().size());
            beanList.clear();
            beanList.add(0, topBean);
            beanList.addAll(1, ((CheckTask) taskList).getData().getList());
            spinnerAdapter.notifyDataSetChanged();
            checkSpinner.setSelection(0);
        }
    }

    @Override
    public void setScanMaterialRequestFocus() {
        etCheckMaterial.setText("");
        etCheckMaterial.requestFocus();
    }


    @OnClick(R.id.btn_fresh_check)
    void onViewClicked(View view) {
        presenter.fetchGetTaskList(this, uwClientApplication.getTOKEN());
    }
}
