package com.jimilab.uwclient.dao;

import android.content.Context;

import com.jimilab.uwclient.gen.MaterialDaoDao;
import com.jimilab.uwclient.gen.TaskDaoDao;
import com.jimilab.uwclient.utils.Log;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class GreenDaoUtil {

    private static String TAG = "GreenDaoUtil";
    private static GreenDaoUtil greenDaoUtil = null;

    private Context mContext;

    private GreenDaoUtil(Context context) {
        this.mContext = context;
    }

    public static GreenDaoUtil getGreenDaoUtil(Context context) {
        if (greenDaoUtil == null) {
            synchronized (GreenDaoUtil.class) {
                if (greenDaoUtil == null) {
                    greenDaoUtil = new GreenDaoUtil(context);
                }
            }
        }
        return greenDaoUtil;
    }

    /**
     * 插入操作数据
     *
     * @param materialDao
     */
    public void insertMaterial(MaterialDao materialDao) {
        Log.d(TAG, "===insertMaterial===");
        try {
            getMaterialDaoDao().insertOrReplace(materialDao);
        } catch (Exception e) {
            Log.d(TAG, "insertMaterial - " + e.toString());
        }
    }

    /**
     * 根据任务 供应商、操作类型 获取本地数据
     *
     * @param taskSupplier 供应商
     * @param operateType  操作类型
     * @return List<MaterialDao>
     */
    public List<MaterialDao> queryMaterial(String taskSupplier, int operateType) {
        QueryBuilder<MaterialDao> builder = getMaterialDaoDao().queryBuilder();
        if (null != taskSupplier) {
            return builder.where(MaterialDaoDao.Properties.TaskSupplier.eq(taskSupplier),
                    MaterialDaoDao.Properties.OperateType.eq(operateType)).list();
        } else {
            return builder.where(MaterialDaoDao.Properties.OperateType.eq(operateType)).list();
        }

    }

    /**
     * 查询操作数据
     *
     * @param taskName     任务名
     * @param taskSupplier 供应商
     * @param destination  目的地
     * @param operateType  操作类型  1、出库
     * @return
     */
    public List<MaterialDao> queryMaterial(String taskName, String taskSupplier, String
            destination, int operateType) {
        Log.d(TAG, "===queryMaterial===");
        QueryBuilder<MaterialDao> builder = getMaterialDaoDao().queryBuilder();
        return builder.where(MaterialDaoDao.Properties.TaskSupplier.eq(taskSupplier),
                MaterialDaoDao.Properties.TaskName.eq(taskName),
                MaterialDaoDao.Properties.Destination.eq(destination),
                MaterialDaoDao.Properties.OperateType.eq(operateType)).list();
    }


    /**
     * 删除记录
     *
     * @param taskDao
     */
    public void deleteMaterialRecord(TaskDao taskDao, int operateType) {
        Log.d(TAG, "deleteRecord - " + taskDao.taskDaoToString());
        try {
            List<MaterialDao> list = getMaterialDaoDao().queryBuilder().
                    where(MaterialDaoDao.Properties.TaskSupplier.eq(taskDao.getTaskSupplier()),
                            MaterialDaoDao.Properties.TaskName.eq(taskDao.getTaskName()),
                            MaterialDaoDao.Properties.Destination.eq(taskDao.getDestination()),
                            MaterialDaoDao.Properties.OperateType.eq(operateType)).list();
            if (null != list) {
                getMaterialDaoDao().deleteInTx(list);
                Log.d(TAG, "deleteRecord - delete");
            }
        } catch (Exception e) {
            Log.d(TAG, "deleteRecord - " + e.toString());
        }
    }


    /**
     * 插入任务单
     *
     * @param taskDao
     */
    public void insertTask(TaskDao taskDao) {
        Log.d(TAG, "===insertTask===");
        try {
            QueryBuilder<TaskDao> builder = getTaskDaoDao()
                    .queryBuilder()
                    .where(TaskDaoDao.Properties.TaskName.eq(taskDao.getTaskName()),
                            TaskDaoDao.Properties.TaskSupplier.eq(taskDao.getTaskSupplier()));
            List<TaskDao> list = builder.list();
            if (list.isEmpty()) {
                getTaskDaoDao().insertOrReplace(taskDao);
            } else {
                for (TaskDao dao : list) {
                    dao.setDestination(taskDao.getDestination());
                    dao.setDestinationIndex(taskDao.getDestinationIndex());
                    getTaskDaoDao().update(dao);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "insertTask - " + e.toString());
        }
    }

    /**
     * 根据任务查询任务下操作的数据
     *
     * @param taskDao
     */
    public void updateMaterial(TaskDao taskDao) {
        Log.d(TAG, "updateMaterial: " + taskDao.taskDaoToString());
        try {
            QueryBuilder<MaterialDao> builder = getMaterialDaoDao().queryBuilder()
                    .where(MaterialDaoDao.Properties.TaskName.eq(taskDao.getTaskName()))
                    .where(MaterialDaoDao.Properties.TaskSupplier.eq(taskDao.getTaskSupplier()));
            List<MaterialDao> materialDaos = builder.list();
            if (materialDaos.isEmpty()) {
                return;
            }
            for (MaterialDao dao : materialDaos) {
                dao.setDestination(taskDao.getDestination());
                getMaterialDaoDao().update(dao);
            }
        } catch (Exception e) {
            Log.d(TAG, "updateMaterial: " + e.toString());
        }

    }


    /**
     * 查询所有任务单
     *
     * @return
     */
    public List<TaskDao> queryTask() {
        Log.d(TAG, "===queryTask===");
        QueryBuilder<TaskDao> builder = getTaskDaoDao().queryBuilder();
        return builder.list();
    }

    /**
     * 查询所有任务单名称
     *
     * @return
     */
    public List<String> queryTaskName() {
        Log.d(TAG, "===queryTaskName===");
        List<String> names = new ArrayList<>();
        List<TaskDao> tasks = queryTask();
        for (TaskDao dao : tasks) {
            names.add(dao.getTaskName());
        }
        return names;
    }

    /**
     * 获取一个任务单
     *
     * @return
     */
    public TaskDao getTopTask() {
        Log.d(TAG, "===getTopTask===");
        QueryBuilder<TaskDao> builder = getTaskDaoDao().queryBuilder();
        List<TaskDao> taskDaoList = builder.limit(1).list();
        if (taskDaoList.size() > 0) {
            return taskDaoList.get(0);
        }
        return null;
    }

    /**
     * 获取一个任务单
     *
     * @return
     */
    public TaskDao getTopTask(String taskName, String supplier) {
        Log.d(TAG, "===getTopTask===");
        QueryBuilder<TaskDao> builder = getTaskDaoDao().queryBuilder();
        return builder.where(TaskDaoDao.Properties.TaskName.eq(taskName), TaskDaoDao.Properties.TaskSupplier.eq(supplier))
                .limit(1).list().get(0);
    }

    /**
     * 删除某个任务单
     *
     * @param taskDao
     */
    public void deleteTask(TaskDao taskDao) {
        Log.d(TAG, "deleteTask - " + taskDao.taskDaoToString());
        try {
            TaskDao dao = getTaskDaoDao().queryBuilder().where(TaskDaoDao.Properties.TaskName.eq(taskDao.getTaskName()),
                    TaskDaoDao.Properties.TaskSupplier.eq(taskDao.getTaskSupplier()),
                    TaskDaoDao.Properties.Destination.eq(taskDao.getDestination()),
                    TaskDaoDao.Properties.DestinationIndex.eq(taskDao.getDestinationIndex())).build().unique();
            if (null != dao) {
                getTaskDaoDao().deleteInTx(dao);
                Log.d(TAG, "deleteTask - delete");
            }
        } catch (Exception e) {
            Log.d(TAG, "deleteTask - " + e.toString());
        }
    }


    private MaterialDaoDao getMaterialDaoDao() {
        return GreenDaoManager.getInstance(mContext).getmDaoSession().getMaterialDaoDao();
    }

    private TaskDaoDao getTaskDaoDao() {
        return GreenDaoManager.getInstance(mContext).getmDaoSession().getTaskDaoDao();
    }

}
