package com.jimilab.uwclient.view.impl.urgent;

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

import com.jimilab.uwclient.R;
import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.BaseResult;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.view.impl.BaseActivity;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UrgentLoginActivity extends BaseActivity {

    private static final String TAG = UrgentLoginActivity.class.getSimpleName();
    @BindView(R.id.iv_back)
    RelativeLayout ivUrgentBack;
    @BindView(R.id.et_usr_scan)
    EditText etUsrScan;
    @BindView(R.id.et_usr)
    TextView etUsr;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_cancel)
    Button btnCancel;
    @BindView(R.id.iv_urgent_name)
    ImageView ivUrgentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urgent_login);
        ButterKnife.bind(this);
        etUsrScan.requestFocus();
    }

    @OnClick({R.id.iv_back, R.id.btn_login, R.id.btn_cancel, R.id.iv_urgent_name})
    public void onViewClick(View view) {

        switch (view.getId()) {
            case R.id.iv_back:
                exit();
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_cancel:
            case R.id.iv_urgent_name:
                clearInput();
                break;
        }
    }

    private void clearInput() {
        if (!TextUtils.isEmpty(etUsrScan.getText())) {
            etUsr.setText("");
            etUsrScan.setText("");
            etUsrScan.requestFocus();
        }
    }

    // TODO: 2019-11-18
    private void login() {

        if (TextUtils.isEmpty(etUsr.getText().toString().trim())) {
            showToast("用户名不能为空!");
        } else {
            showLoading("正在加载...");
            ((UwClientApplication) mContext).getNetApi().ping()
                    .timeout(8000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BaseResult<String>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(BaseResult<String> stringBaseResult) {

                            dismissLoading();
                            if (stringBaseResult != null && stringBaseResult.getResultCode() == 200) {
                                showToast("UW系统运行正常,不必使用本软件!");
                            } else {
                                //UW系统运行异常，进入操作页面
                                Intent intent = new Intent(mContext, UrgentImplActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("usrNo", etUsr.getText().toString().trim());
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            dismissLoading();
                            //UW系统运行异常，进入操作页面
                            Intent intent = new Intent(mContext, UrgentImplActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("usrNo", etUsr.getText().toString().trim());
                            intent.putExtras(bundle);
                            startActivity(intent);

                            Log.d(TAG, "onError: " + e.toString());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }


    @OnEditorAction(R.id.et_usr_scan)
    public boolean onEditorAction(TextView v, int actionId, KeyEvent key) {
        if ((actionId == EditorInfo.IME_ACTION_SEND) || (key != null && key.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            if (key.getAction() == KeyEvent.ACTION_UP) {
                String scanValue = String.valueOf(etUsrScan.getText()).replace("\r", "");
                Log.d(TAG, "scanValue - " + scanValue);
                etUsr.setText(scanValue);
            }
            return true;
        }
        return false;
    }

    @OnTextChanged(R.id.et_usr)
    public void onTextChanged(CharSequence text) {

        ivUrgentName.setVisibility(text.length() > 0 ? View.VISIBLE : View.GONE);
    }
}
