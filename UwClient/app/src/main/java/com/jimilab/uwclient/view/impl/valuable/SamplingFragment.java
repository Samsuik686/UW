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
import com.jimilab.uwclient.bean.ChipTask;
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

public class SamplingFragment extends BaseFragment<ValuablePresenter<IValuableView>, IValuableView> implements IValuableView {

    private static final String TAG = SamplingFragment.class.getSimpleName();
    @BindView(R.id.sampling_spinner)
    Spinner samplingSpinner;
    @BindView(R.id.tv_scan)
    TextView tvScan;
    @BindView(R.id.tv_material_unique)
    TextView tvMaterialUnique;
    @BindView(R.id.tv_sampling_num)
    TextView tvSamplingNum;
    @BindView(R.id.tv_operate_time)
    TextView tvOperateTime;
    @BindView(R.id.btn_fresh_sampling)
    Button btnFreshSampling;
    @BindView(R.id.et_sampling_material)
    EditText etSamplingMaterial;

    private ChipTask.DataBean.ListBean topBean;
    private List<ChipTask.DataBean.ListBean> beanList = new ArrayList<>();
    private TaskSpinnerAdapter<ChipTask.DataBean.ListBean> spinnerAdapter;
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

        topBean = new ChipTask.DataBean.ListBean();
        topBean.setSupplierName("");
        topBean.setFileName("请选择抽检任务");
        beanList.add(0, topBean);
        spinnerAdapter = new TaskSpinnerAdapter<>(beanList, uwClientApplication);
        samplingSpinner.setAdapter(spinnerAdapter);
        samplingSpinner.setSelection(0);
        presenter.fetchGetTaskList(this, uwClientApplication.getTOKEN());
    }

    private void initViewEvent() {
        //任务选择监听
        samplingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);

                mTaskId = "";
                if (position > 0) {
                    mTaskId = String.valueOf(beanList.get(position).getId());
                    scanNext();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //扫描输入监听
        etSamplingMaterial.setOnEditorActionListener(mEditorActionListener);
    }

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                if (!mTaskId.isEmpty()) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        String scanValue = String.valueOf(v.getText()).replace("\r", "");
                        Log.d(TAG, "scanValue - " + scanValue);

                        tvScan.setText(scanValue);

                        //料号@数量@时间戳(唯一码)@打印人员@供应商@站位@序号@生产日期@描述@
                        String[] materialValues = Tool.parserMaterialNo(scanValue);
                        if (materialValues.length >= 9) {
                            tvSamplingNum.setText(materialValues[1]);
                            tvMaterialUnique.setText(materialValues[2]);
                            tvOperateTime.setText(Tool.getTime());

                            //上传数据
                            presenter.fetchUploadMaterial(SamplingFragment.this, uwClientApplication.getTOKEN(), mTaskId,
                                    tvMaterialUnique.getText().toString().trim(), tvSamplingNum.getText().toString().trim());

                        } else {
                            showToast("料号不正确,请重新扫描!");
                            scanNext();
                        }
                    }
                    return true;

                } else {
                    showToast("请选择抽检任务!");
                }

            }
            return false;
        }
    };

    @Override
    protected ValuablePresenter<IValuableView> createPresenter() {
        return new ValuablePresenter<>(uwClientApplication);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.fragment_sampling;
    }

    @Override
    protected void destroy() {

    }

    @Override
    public void showTaskList(String token, BaseResult taskList) {
        if (taskList != null) {
            Log.d(TAG, "showTaskList: " + ((ChipTask) taskList).getData().getList().size());
            beanList.clear();
            beanList.add(0, topBean);
            beanList.addAll(1, ((ChipTask) taskList).getData().getList());
            spinnerAdapter.notifyDataSetChanged();
            samplingSpinner.setSelection(0);
        }
    }

    @Override
    public void scanNext() {
        tvScan.setText("");
        tvMaterialUnique.setText("");
        tvSamplingNum.setText("");
        tvOperateTime.setText("");
        setScanMaterialRequestFocus();
    }

    @Override
    public void setScanMaterialRequestFocus() {
        etSamplingMaterial.setText("");
        etSamplingMaterial.requestFocus();
    }

    @OnClick(R.id.btn_fresh_sampling)
    public void onViewClicked() {
        presenter.fetchGetTaskList(this, uwClientApplication.getTOKEN());
    }
}
