package com.jimilab.uwclient.model;

import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.bean.InMaterial;
import com.jimilab.uwclient.bean.Material;
import com.jimilab.uwclient.dao.GreenDaoUtil;
import com.jimilab.uwclient.utils.CacheMemory;
import com.jimilab.uwclient.utils.FileUtils;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.NetApi;
import com.jimilab.uwclient.utils.Tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-01
 * @描述 :
 */
public class UrgentModel implements IUrgentModel {
    private static final String TAG = UrgentModel.class.getSimpleName();

    //操作状态 0,未操作 1，已扫料盘，未扫料盒  2，扫料盒并完成
    public static final int NOT_OPERATED = 0;
    public static final int OPERATING = 1;
    public static final int OPERATED = 2;

    //uw后台活跃状态
    public static final int UW_DOWNTIME = 0;
    public static final int UW_ALIVE = 1;

    private UwClientApplication mContext;
    private NetApi mNetApi;
    private CacheMemory mCacheMemory;

    public UrgentModel(UwClientApplication context) {
        this.mContext = context;
        this.mNetApi = mContext.getNetApi();
        mCacheMemory = CacheMemory.getmCacheMemory(mContext.getAppContext(), 8);
    }

    @Override
    public void getMemoryTaskNames(String key, OnGetMemoryTaskNames onGetMemoryTaskNames) {
        Log.d(TAG, "getMemoryTaskNames: " + key);
        //加载缓存的任务单列表，并弹出未完成的任务
        HashSet<String> taskNames = null;
        try {
            taskNames = mCacheMemory.getTaskNames(key);
            onGetMemoryTaskNames.onComplete(taskNames);
        } catch (Exception e) {
            Log.d(TAG, "mCacheMemory - " + e.toString());
        }
    }

    @Override
    public void loadTaskFile(int requestCode, String curTaskName, String loadAbsolutePath,
                             List<InMaterial> curAllMaterials, OnLoadTaskFileListener loadTaskFileListener) {

        Log.d(TAG, "loadTaskFile: " + " requestCode: " + requestCode + " curTaskName: " + curTaskName
                + " loadAbsolutePath: " + loadAbsolutePath + " curAllMaterials: " + curAllMaterials.size());
        //获取本地数据库保存的文件名
        String loadName = loadAbsolutePath.substring(loadAbsolutePath.lastIndexOf("/") + 1);
        List<String> names = GreenDaoUtil.getGreenDaoUtil(mContext).queryTaskName();
        boolean daoExitName = names.contains(loadName);
        List<InMaterial> loadList = null;

        try {
            if (daoExitName) { //数据库存在该文件名
                //获取缓存文件名
                HashSet<String> taskSet = mCacheMemory.getTaskNames("taskNames");
                if (taskSet.contains(loadName)) {
                    loadList = mCacheMemory.getList(loadName);
                }
            } else {
                //数据表中不存在该文件名
                loadList = analysisLoadFile(loadAbsolutePath);
            }

            //若是切换任务，缓存切换前的任务
            if (loadList != null && !loadList.isEmpty()) {
                if (requestCode == Constant.CHANGE) {
                    cacheTaskMaterial(curAllMaterials, curTaskName);
                }
            }

            //回调UI
            loadTaskFileListener.onLoaded(daoExitName, loadList);
        } catch (Throwable throwable) {
            Log.d(TAG, "loadTaskFile: " + throwable.toString());
            loadTaskFileListener.onError(throwable);
        }

    }

    /**
     * 缓存切换前的任务
     *
     * @param list
     * @param taskName
     */
    public void cacheTaskMaterial(List<InMaterial> list, String taskName) {
        if (list != null && !list.isEmpty()) {
            try {
                HashSet<String> taskNames = mCacheMemory.getTaskNames("taskNames");
                taskNames.add(taskName);
                mCacheMemory.putTaskNames("taskNames", taskNames);
                mCacheMemory.putList(taskName, list);
            } catch (Exception e) {
                Log.d(TAG, "mCacheMemory - " + e.toString());
            }
        }
    }


    /**
     * 解析本地Excel文件
     *
     * @param absolutePath 绝对地址
     */
    private List<InMaterial> analysisLoadFile(String absolutePath) throws Throwable {
        HashSet<String> materialSet = new HashSet<>();
        List<InMaterial> readInMaterials = new ArrayList<>();
        //解析文件
        List<Material> list = FileUtils.poiAnalyze(absolutePath, materialSet);
        List<String> materials = new ArrayList<>(materialSet);

        for (int i = 0, len = materials.size(); i < len; i++) {
            Log.d(TAG, "materialNo - " + materials.get(i));
            InMaterial material = new InMaterial();
            List<InMaterial.InMaterialBean> beanList = new ArrayList<>();
            int total = 0;
            material.setMaterialNo(materials.get(i));
            material.setOperateType(Constant.UN_OPERATE);
            for (int j = 0, size = list.size(); j < size; j++) {
                Material bean = list.get(j);
                if (bean.getMaterialNo().equalsIgnoreCase(material.getMaterialNo())) {
                    material.setSupplier(bean.getSupplier());
                    material.setDemand(bean.getDemand());
                    material.setMaterialType(bean.getMaterialType());

                    InMaterial.InMaterialBean materialBean = new InMaterial.InMaterialBean();
                    materialBean.setMaterialTimeStamp(bean.getMaterialTimestamp());
                    materialBean.setProductionTime(bean.getProductionDate());
                    materialBean.setCount(bean.getCount());
                    materialBean.setArea(bean.getArea());
                    materialBean.setBoxNo(bean.getBoxNo());
                    materialBean.setBoxX(bean.getBoxX());
                    materialBean.setBoxY(bean.getBoxY());
                    materialBean.setBoxZ(bean.getBoxZ());
                    materialBean.setCol(bean.getCol());
                    materialBean.setRow(bean.getRow());

                    beanList.add(materialBean);
                }
            }

            //按照生产日期升序
            Collections.sort(beanList, new Comparator<InMaterial.InMaterialBean>() {
                @Override
                public int compare(InMaterial.InMaterialBean o1, InMaterial.InMaterialBean o2) {

                    String format = "yyyy-MM-dd HH:mm:ss";
                    if (Tool.getTimeStampe(o1.getProductionTime(), format) > Tool.getTimeStampe(o2.getProductionTime(), format)) {
                        return 1;
                    }
                    if (Tool.getTimeStampe(o1.getProductionTime(), format) == Tool.getTimeStampe(o2.getProductionTime(), format)) {
                        return 0;
                    }
                    return -1;
                }
            });

            material.setBeanList(beanList);
            material.setTotalCount(total);
            material.setAlreadyInList(new ArrayList<InMaterial.InMaterialBean>());

            readInMaterials.add(material);
        }

        return readInMaterials;
    }

    public List<InMaterial> getInMaterialList(String key) {
        return mCacheMemory.getList(key);
    }


    public void removeDiskCache(String taskName) {
        mCacheMemory.removeDiskCache(taskName);
        HashSet<String> names = mCacheMemory.getTaskNames("taskNames");
        if (null != names && names.size() > 0) {
            names.remove(taskName);
            mCacheMemory.putTaskNames("taskNames", names);
        }
    }

    public void removeAllDiskCache(String key) {
        mCacheMemory.removeDiskCache(key);
    }

}
