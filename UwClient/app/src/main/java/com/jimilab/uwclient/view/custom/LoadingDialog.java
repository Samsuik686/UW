package com.jimilab.uwclient.view.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jimilab.uwclient.R;

/**
 * 类名:LoadingDialog
 * 创建人:Liang GuoChang
 * 创建时间:2017/11/28 11:49
 * 描述:正在加载旋转进度框
 * 版本号:
 * 修改记录:
 */

public class LoadingDialog extends Dialog {

    private String mTitle = "";
    private TextView tv_title;
    private ImageView logo;
    private AnimatedVectorDrawable animatedVectorDrawable;

    public LoadingDialog(Context context, String title) {
        super(context, R.style.loadingDialogStyle);
        this.mTitle = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        tv_title = findViewById(R.id.tv);
        logo = findViewById(R.id.iv_log_loading);
        animatedVectorDrawable = (AnimatedVectorDrawable) logo.getDrawable();
        tv_title.setText(mTitle);
        LinearLayout linearLayout = this.findViewById(R.id.LinearLayout);
        linearLayout.getBackground().setAlpha(190);

        setCancelable(false);


        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (animatedVectorDrawable != null && !animatedVectorDrawable.isRunning()) {
                    animatedVectorDrawable.start();
                }
            }
        });


        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (animatedVectorDrawable != null && animatedVectorDrawable.isRunning()) {
                    animatedVectorDrawable.stop();
                }
            }
        });
    }

    public void setMsgWithSpeed(String msg) {
        tv_title.setText(msg);
    }
}
