package com.jimilab.uwclient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jimilab.uwclient.R;
import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.CheckTask;
import com.jimilab.uwclient.bean.ChipTask;
import com.jimilab.uwclient.bean.EmergencyTask;

import java.util.List;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-20
 * @描述 : 任务适配器
 */
public class TaskSpinnerAdapter<T> extends BaseAdapter {
    private UwClientApplication mApplication;
    private List<T> mTaskList;

    public TaskSpinnerAdapter(List<T> taskList, UwClientApplication application) {
        this.mApplication = application;
        this.mTaskList = taskList;
    }

    @Override
    public int getCount() {
        return mTaskList == null ? 0 : mTaskList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTaskList == null ? null : mTaskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mApplication).inflate(R.layout.task_spinner_item, parent, false);
            viewHolder.tvSupplier = convertView.findViewById(R.id.tv_supplier_name);
            viewHolder.tvTaskName = convertView.findViewById(R.id.tv_task_name);
            viewHolder.centerView = convertView.findViewById(R.id.center_view);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        T bean = mTaskList.get(position);
        if (bean instanceof CheckTask.DataBean.ListBean) {
            if (((CheckTask.DataBean.ListBean) bean).getSupplierName().isEmpty()) {
                viewHolder.tvSupplier.setVisibility(View.GONE);
                viewHolder.centerView.setVisibility(View.GONE);
            } else {
                viewHolder.tvSupplier.setVisibility(View.VISIBLE);
                viewHolder.centerView.setVisibility(View.VISIBLE);
                viewHolder.tvSupplier.setText(((CheckTask.DataBean.ListBean) bean).getSupplierName());
            }
            viewHolder.tvTaskName.setText(((CheckTask.DataBean.ListBean) bean).getTaskName());

        } else if (bean instanceof ChipTask.DataBean.ListBean) {
            if (((ChipTask.DataBean.ListBean) bean).getSupplierName().isEmpty()) {
                viewHolder.tvSupplier.setVisibility(View.GONE);
                viewHolder.centerView.setVisibility(View.GONE);
            } else {
                viewHolder.tvSupplier.setVisibility(View.VISIBLE);
                viewHolder.centerView.setVisibility(View.VISIBLE);
                viewHolder.tvSupplier.setText(((ChipTask.DataBean.ListBean) bean).getSupplierName());
            }
            viewHolder.tvTaskName.setText(((ChipTask.DataBean.ListBean) bean).getFileName());
        } else if (bean instanceof EmergencyTask.DataBean) {
            if (((EmergencyTask.DataBean) bean).getId() == -1) {
                viewHolder.tvSupplier.setVisibility(View.GONE);
                viewHolder.centerView.setVisibility(View.GONE);
            } else {
                viewHolder.tvSupplier.setVisibility(View.VISIBLE);
                viewHolder.centerView.setVisibility(View.VISIBLE);
                viewHolder.tvSupplier.setText(String.valueOf(((EmergencyTask.DataBean) bean).getId()));
            }
            viewHolder.tvTaskName.setText(((EmergencyTask.DataBean) bean).getFile_name());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvSupplier;
        TextView tvTaskName;
        View centerView;
    }

}
