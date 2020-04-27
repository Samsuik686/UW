package com.jimilab.uwclient.view.impl.urgent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jimilab.uwclient.R;
import com.jimilab.uwclient.adapter.InWareAdapter;
import com.jimilab.uwclient.bean.InMaterial;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-31
 * @描述 :
 */
public class InfoFragment extends Fragment {

    @BindView(R.id.lv_current_task)
    ListView lvCurrentTask;
    private View infoView;
    private List<InMaterial> curMaterialList = new ArrayList<>();
    private InWareAdapter inWareAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        infoView = inflater.inflate(R.layout.current_task, container, false);
        //必须调用
        ButterKnife.bind(this, infoView);
        inWareAdapter = new InWareAdapter(getContext(), curMaterialList);
        lvCurrentTask.setAdapter(inWareAdapter);
        registerLiveDataEvent();

        return infoView;
    }

    //订阅当前操作的数据
    private void registerLiveDataEvent() {
        Tool.getCurAllMaterial()
                .observeSticky(this, new Observer<ArrayList>() {
                    @Override
                    public void onChanged(ArrayList arrayList) {
                        if (arrayList != null && arrayList.size() > 0) {
                            Log.d("registerLiveDataEvent", "onChanged: " + arrayList.size());
                            curMaterialList.clear();
                            curMaterialList.addAll((ArrayList<InMaterial>) arrayList);
                            if (inWareAdapter != null) {
                                inWareAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (inWareAdapter != null) {
                inWareAdapter.notifyDataSetChanged();
            }

        }
    }
}
