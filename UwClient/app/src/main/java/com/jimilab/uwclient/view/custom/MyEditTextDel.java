package com.jimilab.uwclient.view.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

import com.jimilab.uwclient.R;

/**
 * 类名:MyEditTextDel
 * 创建人:Liang GuoChang
 * 创建时间:2018/4/11 17:11
 * 描述:
 * 版本号:
 * 修改记录:
 */

public class MyEditTextDel extends EditText implements TextWatcher {
    private String TAG = "MyEditTextDel";
    //位于控件内右边的清除EditText内容的图片
    private Drawable rightDrawable = null;
    private boolean pwdType = false;

    public MyEditTextDel(Context context) {
        super(context);
        initEditText();
    }

    public MyEditTextDel(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
        initEditText();
    }

    public MyEditTextDel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEditText();
    }

    //设置样式
    private void initEditText() {
        //设置背景
        setBackgroundResource(R.drawable.editext_background);
        setTextColor(Color.BLACK);

        //设置右边显示的按钮
        int inputType = getInputType();
        rightDrawable = getCompoundDrawables()[2];
        if (inputType == 129 || inputType == 145) {
            if (getInputType() == 129) {
                rightDrawable = getResources().getDrawable(R.mipmap.hide_icon, null);
                setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else {
                rightDrawable = getResources().getDrawable(R.mipmap.display_icon, null);
                setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            pwdType = true;
        } else {
            pwdType = false;
            rightDrawable = getResources().getDrawable(R.mipmap.clear, null);
        }


        setRightDrawable();
        addTextChangedListener(this);
    }

    private void setRightDrawable() {
        if (length() < 1) {
            setRightDrawableVisible(false);
        } else {
            setRightDrawableVisible(true);
        }
    }

    private void setRightDrawableVisible(boolean b) {
        if (rightDrawable != null) {
            rightDrawable.setBounds(0, 0,
                    (int) (rightDrawable.getIntrinsicWidth() * 0.9),
                    (int) (rightDrawable.getIntrinsicHeight() * 0.9));
            setCompoundDrawables(getCompoundDrawables()[0],
                    getCompoundDrawables()[1],
                    b ? rightDrawable : null,
                    getCompoundDrawables()[1]);
        }
    }

    //输入款内容发生变化时调用该方法
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        setRightDrawable();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        setRightDrawable();
    }

    //点击删除图片，清除内容
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //手势抬起时
        if (event.getAction() == MotionEvent.ACTION_UP) {
            boolean isTouchClear = (event.getX() > (getWidth() - getTotalPaddingRight()))
                    && (event.getX() < (getWidth() - getPaddingRight()));

            if (getCompoundDrawables()[2] != null) {
                if (isTouchClear) {
                    if (pwdType) {
                        if (getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                            rightDrawable = getResources().getDrawable(R.mipmap.display_icon, null);
                            setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        } else if (getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                            rightDrawable = getResources().getDrawable(R.mipmap.hide_icon, null);
                            setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                        setRightDrawable();
                    } else {
                        this.setText(getText().subSequence(0, length() - 1));
                        this.setFocusable(true);
                        this.setFocusableInTouchMode(true);
                        this.requestFocus();
                    }
                }
            }

        }
        return super.onTouchEvent(event);
    }

}
