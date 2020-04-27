package com.jimilab.uwclient.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jimilab.uwclient.bean.InMaterial;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;


public class CacheMemory {

    private static final String TAG = "CacheMemory";

    private static CacheMemory mCacheMemory = null;
    private LruCache<String, List<InMaterial>> mLruCache;
    private LruCache<String, String> nameLruCache;
    private DiskLruCache mDiskLruCache;

    public CacheMemory(Context context, int cacheRatio) {

        /*
        int cacheSize = (int) (Runtime.getRuntime().maxMemory() / cacheRatio);
        mLruCache = new LruCache<String, List<InMaterial>>(cacheSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull List<InMaterial> value) {
                return super.sizeOf(key, value);
            }
        };

        nameLruCache = new LruCache<String, String>(cacheSize) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull String value) {
                return super.sizeOf(key, value);
            }
        };
        */


        try {
            File cacheDir = getDiskCacheDir(context, "material");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            Log.d(TAG, "mDiskLruCache - " + cacheDir.getAbsolutePath());

            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "mDiskLruCache - " + e.toString());
        }

    }

    public static CacheMemory getmCacheMemory(Context context, int ratio) {
        if (mCacheMemory == null) {
            mCacheMemory = new CacheMemory(context, ratio);
        }
        return mCacheMemory;
    }

    public void addListToMemory(String key, List<InMaterial> materials) {
        mLruCache.put(key, materials);
    }

    public List<InMaterial> getListFromMemory(String key) {
        return mLruCache.get(key);
    }

    public void removeListFromMemory(String key) {
        mLruCache.remove(key);
    }


    public void addNameToMemory(String key, String name) {
        nameLruCache.put(key, name);
    }

    public String getNameFromMemory(String key) {
        return nameLruCache.get(key);
    }

    public void removeNameFromMemory(String key) {
        nameLruCache.remove(key);
    }

    public void putList(String key, List<InMaterial> list) throws IOException {
        key = getMD5(key);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ObjectOutputStream obo = new ObjectOutputStream(bao);
        obo.writeObject(list);
        obo.flush();
        byte[] bytes = bao.toByteArray();
        obo.close();
        bao.close();
        OutputStream out = editor.newOutputStream(0);
        out.write(bytes);
        editor.commit();
        mDiskLruCache.flush();
    }

    public List<InMaterial> getList(String key) {
        key = getMD5(key);
        Log.d(TAG, "mDiskLruCache - getList - " + key);

        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            InputStream bai = snapshot.getInputStream(0);
            ObjectInputStream ois = new ObjectInputStream(bai);
            return (List<InMaterial>) ois.readObject();
        } catch (Exception e) {
            Log.d(TAG, "mDiskLruCache - getList - " + e.toString());
            return null;
        }

    }

    /**
     * 保存任务单名到缓存
     *
     * @param key
     * @param taskNames
     * @throws IOException
     */
    public void putTaskNames(String key, HashSet<String> taskNames) {
        key = getMD5(key);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream obo = new ObjectOutputStream(bao);
            obo.writeObject(taskNames);
            obo.flush();
            byte[] bytes = bao.toByteArray();
            obo.close();
            bao.close();
            OutputStream out = editor.newOutputStream(0);
            out.write(bytes);
            editor.commit();
            mDiskLruCache.flush();
        } catch (IOException e) {
            Log.d(TAG, "mDiskLruCache - putTaskNames - " + e.toString());
        }

    }

    /**
     * 获取任务单名
     *
     * @param key
     * @return
     */
    public HashSet<String> getTaskNames(String key) {
        key = getMD5(key);
        try {
            HashSet<String> nameSet;
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            InputStream bai = snapshot.getInputStream(0);
            ObjectInputStream ois = new ObjectInputStream(bai);
            nameSet = (HashSet<String>) ois.readObject();
            if (nameSet == null) nameSet = new HashSet<>();
            return nameSet;
        } catch (Exception e) {
            Log.d(TAG, "mDiskLruCache - getTaskNames - " + e.toString());
            return new HashSet<>();
        }
    }

    /**
     * 根据键值删除缓存
     *
     * @param key
     * @return
     */
    public boolean removeDiskCache(String key) {
        key = getMD5(key);
        try {
            return mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "getAppVersion - " + e.toString());
        }
        return 1;
    }

    /**
     * 传入字符串参数，返回MD5加密结果
     *
     * @return 加密结果
     */
    public String getMD5(String str) {
        MessageDigest messageDigest = null;

        try {
            //设置哪种算法
            messageDigest = MessageDigest.getInstance("MD5");
            //对算法进行重置以免影响下次计算
            messageDigest.reset();
            //使用指定的字段进行摘要
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "getMD5 - " + e.toString());
//            e.printStackTrace();
        }
        //完成Hash值的计算
        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return md5StrBuff.toString();
    }


}
