package com.jimilab.uwclient.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jimilab.uwclient.R;

import androidx.annotation.Nullable;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-25
 * @描述 :
 */
public class ItemView extends LinearLayout {
    private static final String TAG = "ItemView";
    private View childLayout;
    private TextView item_title;
    private ImageView item_icon;
    private String title = "JIMI";
    private boolean isSelected = false;

    public ItemView(Context context) {
        super(context);
        init();
    }

    public ItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        initCustomAttrs(context, attrs);
    }

    public ItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initCustomAttrs(context, attrs);
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ItemView);
        title = typedArray.getString(R.styleable.ItemView_title);
        item_title.setText(title);
    }

    private void init() {
        //覆盖子类控件直接获取焦点
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        childLayout = LayoutInflater.from(this.getContext()).inflate(R.layout.item_layout, null, false);
        this.addView(childLayout);
        item_title = childLayout.findViewById(R.id.item_title);
        item_icon = childLayout.findViewById(R.id.item_icon);
    }

    private void setClickStyle() {
        if (!isSelected) {
            childLayout.setBackgroundResource(R.drawable.item_select_shape);
            item_title.setTextColor(getResources().getColor(R.color.myGreen));
            item_icon.setImageResource(R.mipmap.arrow_green);
            isSelected = true;
        }
    }

    public final void reset() {
        if (isSelected) {
            childLayout.setBackgroundResource(R.drawable.item_not_select_shape);
            item_title.setTextColor(getResources().getColor(R.color.myGray));
            item_icon.setImageResource(R.mipmap.arrow_gray);
            isSelected = false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "dispatchTouchEvent: " + ev.getAction());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent: " + ev.getAction());
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_UP) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setClickStyle();
                break;
            case MotionEvent.ACTION_UP:
                performClick();
                break;
        }
        //return super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean performClick() {
        Log.d(TAG, "performClick: ");
        return super.performClick();
    }
}
