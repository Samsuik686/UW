package com.jimilab.uwclient.view.impl.valuable;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jimilab.uwclient.R;
import com.jimilab.uwclient.bean.BaseResult;
import com.jimilab.uwclient.bean.LoginResult;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.view.custom.MyEditTextDel;
import com.jimilab.uwclient.view.impl.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ValuableLoginActivity extends BaseActivity {

    private static final String TAG = ValuableLoginActivity.class.getSimpleName();
    @BindView(R.id.iv_back)
    RelativeLayout ivBack;
    @BindView(R.id.et_usr_scan)
    EditText etUsrScan;
    @BindView(R.id.iv_clear_name)
    ImageView ivClearName;
    @BindView(R.id.tv_usr)
    TextView tvUsr;
    @BindView(R.id.et_enter_pwd)
    MyEditTextDel etEnterPwd;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_cancel)
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valuable_login);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        etEnterPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (TextUtils.isEmpty(tvUsr.getText().toString().trim())) {
                        tvUsr.setText("");
                        etUsrScan.setText("");
                        etUsrScan.requestFocus();
                        showToast("请扫描工号");
                    }
                }
            }
        });

        etUsrScan.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        String scanValue = String.valueOf(v.getText()).replace("\r", "");
                        Log.d(TAG, "scanValue - " + scanValue);

                        tvUsr.setText(scanValue);

                        etEnterPwd.setText("");
                        etEnterPwd.requestFocus();
                        showToast("请输入密码");

                        return true;
                    }
                    return true;

                }
                return false;
            }
        });
    }

    @OnClick({R.id.iv_back, R.id.btn_login, R.id.btn_cancel, R.id.iv_clear_name})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                exit();
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_cancel:
                cancel();
                break;
            case R.id.iv_clear_name:
                clearName();
                break;
        }
    }

    @OnTextChanged(R.id.tv_usr)
    public void onTextChangedListener(CharSequence s) {
        ivClearName.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
        if (s.length() <= 0) {
            etUsrScan.setText("");
            etUsrScan.requestFocus();
            showToast("请扫描工号");
        }
    }

    //清除用户名
    private void clearName() {
        tvUsr.setText("");
        etUsrScan.setText("");
        etUsrScan.requestFocus();
        showToast("请扫描工号");
    }

    private void cancel() {
        etEnterPwd.setText("");
        tvUsr.setText("");
        etUsrScan.setText("");
        etUsrScan.requestFocus();
        showToast("请扫描工号");
    }

    private void login() {

        if (TextUtils.isEmpty(tvUsr.getText().toString().trim())
                || TextUtils.isEmpty(etEnterPwd.getText().toString().trim())) {
            showToast("用户名或密码不能为空");
        } else {
            //登录
            showLoading("正在加载...");
            StringBuilder msg = new StringBuilder();
            mApplication.getNetApi()
                    .login(tvUsr.getText().toString().trim(), etEnterPwd.getText().toString().trim())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(new Function<JsonObject, Integer>() {
                        @Override
                        public Integer apply(JsonObject loginResult) throws Exception {
                            if (loginResult != null) {
                                Log.d(TAG, "apply: " + loginResult.toString());
                                JsonElement jsonElement = loginResult.get("result");
                                int resultCode = jsonElement.getAsInt();
                                Gson gson = new Gson();
                                if (resultCode != 200) {
                                    BaseResult baseResult = gson.fromJson(loginResult, BaseResult.class);
                                    msg.append(baseResult.getData());
                                } else {
                                    LoginResult result = gson.fromJson(loginResult, LoginResult.class);
                                    //保存token
                                    mApplication.setTOKEN(result.getData().get_$TOKEN219());
                                }
                                return resultCode;
                            } else {
                                msg.append("网络异常，登录失败");
                            }
                            return 404;
                        }
                    })
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer integer) {

                            if (integer == 200) {
                                gotoOperate(tvUsr.getText().toString().trim());
                            } else {
                                showToast(msg.toString());
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError: " + e.toString());
                            showToast("网络异常,登录失败!");
                            dismissLoading();
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete: ");
                            dismissLoading();
                        }
                    });


        }

    }

    //跳转到操作页面
    private void gotoOperate(String trim) {
        Intent intent = new Intent(this, ValuableImplActivity.class);
        intent.putExtra("usrname", trim);
        startActivity(intent);
    }

}
