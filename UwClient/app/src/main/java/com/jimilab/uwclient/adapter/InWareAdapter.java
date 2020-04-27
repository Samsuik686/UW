package com.jimilab.uwclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jimilab.uwclient.R;
import com.jimilab.uwclient.bean.InMaterial;
import com.jimilab.uwclient.utils.Log;

import java.util.List;


public class InWareAdapter extends BaseAdapter {

    private static final String TAG = "InWareAdapter";
    private Context mContext;
    private List<InMaterial> mList;
    private LayoutInflater inflater;
    private ViewHolder viewHolder;
//    private View beanViews[] = null;

    public InWareAdapter(Context context, List<InMaterial> list) {
        mContext = context;
        mList = list;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return null == mList ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView - " + position);

        InMaterial material = mList.get(position);
        int viewSize = 0;

        List<InMaterial.InMaterialBean> beanList = material.getBeanList();
        if (null != beanList && beanList.size() > 0) {
            viewSize = beanList.size();
        }
        convertView = inflater.inflate(R.layout.in_ware_list_item, null);
        //TextView tv_supplier = convertView.findViewById(R.id.tv_supplier);
        TextView tv_material_no = convertView.findViewById(R.id.tv_material_no);
        TextView tv_demand = convertView.findViewById(R.id.tv_demand);
        TextView tv_totalCount = convertView.findViewById(R.id.tv_totalCount);
        LinearLayout bean_layout = convertView.findViewById(R.id.bean_layout);


        //tv_supplier.setText(material.getSupplier());
        tv_material_no.setText(material.getMaterialNo());
        tv_demand.setText(String.valueOf(material.getDemand()));
        tv_totalCount.setText(String.valueOf(material.getTotalCount()));

        switch (material.getOperateType()) {
            case 0:
                break;
            case 1:
                tv_material_no.setBackgroundResource(R.mipmap.wait);
                break;
            case 2:
                tv_material_no.setBackgroundResource(R.mipmap.get);
                break;
        }


        if (null != beanList && viewSize > 0) {

            View[] beanViews = new View[viewSize];
            for (int i = 0; i < viewSize; i++) {
                InMaterial.InMaterialBean bean = beanList.get(i);
                beanViews[i] = inflater.inflate(R.layout.in_bean_layout, null);
                TextView tv_product_date = beanViews[i].findViewById(R.id.tv_product_date);
                TextView tv_material_time = beanViews[i].findViewById(R.id.tv_material_time);
                TextView tv_count = beanViews[i].findViewById(R.id.tv_count);
                TextView tv_box_area = beanViews[i].findViewById(R.id.tv_box_area);
                View view_bottom = beanViews[i].findViewById(R.id.view_bottom);

                if (viewSize <= 1) {
                    view_bottom.setVisibility(View.GONE);
                }

                tv_product_date.setText(bean.getProductionTime());
                tv_material_time.setText(String.valueOf(bean.getMaterialTimeStamp()));
                tv_count.setText(String.valueOf(bean.getCount()));
                tv_box_area.setText((bean.getBoxNo() + "\n" + bean.getArea() + " " + bean.getBoxX() + "-" + bean.getBoxY() + "-" + bean.getBoxZ()));

                switch (bean.getOperateType()) {
                    case 0:
                        break;
                    case 1:
                        tv_material_time.setBackgroundResource(R.mipmap.wait);
                        break;
                    case 2:
                        tv_material_time.setBackgroundResource(R.mipmap.get);
                        break;
                }

//                beanViews[i].setId(i);

//                Log.d(TAG, "i - " + i + " - " + beanViews[i].getId());

                bean_layout.addView(beanViews[i]);
            }
        }

        return convertView;
    }

    class ViewHolder {
        TextView tv_supplier;
        TextView tv_material_no;
        TextView tv_demand;
        TextView tv_totalCount;
        LinearLayout bean_layout;
        View beanViews[];
    }
}
