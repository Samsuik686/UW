package com.jimilab.uwclient.utils;

import com.google.gson.JsonObject;
import com.jimilab.uwclient.BuildConfig;
import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.BaseResult;
import com.jimilab.uwclient.bean.CheckTask;
import com.jimilab.uwclient.bean.ChipTask;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.bean.EmergencyTask;
import com.jimilab.uwclient.bean.ValuableBaseResult;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-29
 * @描述 :
 */
public interface NetApi {

    //=====================紧急出库=====================//

    /**
     * ping后台
     *
     * @return
     */
    @POST(Constant.URL_PING)
    Observable<BaseResult<String>> ping();

    /**
     * 创建任务
     *
     * @param supplierName
     * @param type
     * @param destinationName
     * @return
     */
    @POST(Constant.URL_CREATE)
    @FormUrlEncoded
    Observable<JsonObject> createTask(@Field("supplierName") String supplierName,
                                      @Field("type") String type,
                                      @Field("destinationName") String destinationName);

    /**
     * 上传数据
     *
     * @param taskJson
     * @return
     */
    @POST(Constant.URL_UPLOAD)
    Observable<BaseResult<String>> uploadTask(@Body RequestBody taskJson);


    //=====================贵重仓=====================//

    /**
     * 登录
     *
     * @param uid
     * @param password
     * @return
     */
    @POST("manage/user/login")
    @FormUrlEncoded
    Observable<JsonObject> login(@Field("uid") String uid, @Field("password") String password);

    /**
     * 获取盘点任务
     *
     * @param token
     * @return
     */
    @POST("task/inventory/getWorkingPreciousTask")
    @FormUrlEncoded
    Observable<CheckTask> getCheckTask(@Field("#TOKEN#") String token);

    /**
     * 获取抽检任务
     *
     * @param token
     * @return
     */
    @POST("task/sampleTask/getWorkingPreciousTask")
    @FormUrlEncoded
    Observable<ChipTask> getChipTask(@Field("#TOKEN#") String token);

    /**
     * 上传盘点任务
     *
     * @param params #TOKEN#   taskId    materialId     actualNum
     * @return
     */
    @POST("task/inventory/inventoryPreciousUWMaterial")
    @FormUrlEncoded
    Observable<ValuableBaseResult> uploadCheckMaterial(@FieldMap Map<String, String> params);

    /**
     * 上传抽检任务
     *
     * @param params #TOKEN#   taskId    materialId
     * @return
     */
    @POST("task/sampleTask/samplePreciousUWMaterial")
    @FormUrlEncoded
    Observable<ValuableBaseResult> uploadChipMaterial(@FieldMap Map<String, String> params);


    /**
     * 结束盘点
     *
     * @param token
     * @param taskId
     * @return
     */
    @POST("task/inventory/finishPreciousTask")
    @FormUrlEncoded
    Observable<ValuableBaseResult> finishCheck(@Field("#TOKEN#") String token, @Field("taskId") String taskId);


    /**
     * 结束抽检
     *
     * @param token
     * @param taskId
     * @return
     */
    @POST("task/sampleTask/finishPreciousTask")
    @FormUrlEncoded
    Observable<ValuableBaseResult> finishChip(@Field("#TOKEN#") String token, @Field("taskId") String taskId);

    //=====================来料区=====================//

    /**
     * 获取来料区紧急出库任务
     *
     * @param token
     * @return
     */
    @POST("task/getEmergencyRegularTasks")
    @FormUrlEncoded
    Observable<EmergencyTask> getEmergencyRegularTasks(@Field("#TOKEN#") String token);


    /**
     * 上传来料区出库料号
     * #TOKEN#
     * taskId 任务ID
     * no 料号
     * materialId 时间戳
     * quantity 数量
     * productionTime 生产日期
     * supplierName 供应商名
     * cycle 周期
     * manufacturer 产商
     * printTime 打印时间
     *
     * @param params
     * @return
     */
    @POST("task/outEmergencyRegular")
    @FormUrlEncoded
    Observable<BaseResult<String>> uploadMaterial(@FieldMap Map<String, String> params);

    class NetApiFactory {
        public static NetApi getNetApi(UwClientApplication context) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_URL)
                    //.baseUrl(context.getIp())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            return retrofit.create(NetApi.class);
        }

    }


}
