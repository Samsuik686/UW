<template>
    <div class="precious-operation">
        <video controls="controls" id="sAudio" hidden>
            <source src="./../../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
            <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
        </video>
        <input type="text" title="scanner" id="scanner-check" v-model="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
        <el-form :inline="true" class="precious-operation-form">
            <el-form-item label="任务">
                <el-select v-model.trim="taskId" placeholder="抽检任务" value="">
                    <el-option  v-for="item in tasks" :label="item.fileName" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="finish">完成任务</el-button>
            </el-form-item>
        </el-form>
        <div class="item-operation">
            <el-form :inline="true" size="medium">
                <el-form-item label="料盒总盘数">
                    <el-input v-model="preciousSampleData.totalNum" disabled></el-input>
                </el-form-item>
                <el-form-item label="已扫盘数">
                    <el-input v-model="preciousSampleData.scanNum" disabled></el-input>
                </el-form-item>
                <el-form-item label="已出库盘数">
                    <el-input v-model="preciousSampleData.outNum" disabled></el-input>
                </el-form-item>
            </el-form>
            <el-table
                    :data="preciousSampleData.list"
                    style="width:100%">
                <el-table-column
                        min-width="100"
                        prop="materialId"
                        label="料盘号">
                    <template slot-scope="scope">
                        <high-light
                                :activeMaterialId="activeMaterialId"
                                :row="scope.row"
                        ></high-light>
                    </template>
                </el-table-column>
                <el-table-column
                        min-width="100"
                        prop="no"
                        label="料号">
                </el-table-column>
                <el-table-column
                        prop="specification"
                        label="规格">
                </el-table-column>
                <el-table-column
                        prop="supplier"
                        label="客户">
                </el-table-column>
                <el-table-column
                        prop="storeNum"
                        label="数量">
                </el-table-column>
                <el-table-column
                        min-width="200"
                        label="操作">
                    <template slot-scope="scope">
                        <el-button
                                size="small"
                                :type="scope.row.isOuted === 1?'info':'primary'"
                                :disabled="scope.row.isOuted !== -1"
                                @click="outSingular(scope.row.materialId)">异常出库
                        </el-button>
                        <el-button
                                size="small"
                                :type="scope.row.isOuted === 0?'info':'primary'"
                                :disabled="scope.row.isOuted !== -1"
                                @click="outRegular(scope.row.materialId)">抽检出库
                        </el-button>
                        <el-button
                                size="small"
                                :type="scope.row.isOuted === 2?'info':'primary'"
                                :disabled="scope.row.isOuted !== -1"
                                @click="outLost(scope.row.materialId)">料盘丢失
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </div>
</template>

<script>
    import HighLight from "./comp/HighLight";
    import {
        finishPreciousTaskUrl,
        getSampleTaskMaterialInfoUrl, outPreciousTaskLostUrl, outPreciousTaskRegularUrl,
        outPreciousTaskSingularUrl,
        selectSampleTasksUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {handleScanText} from "../../../utils/scan";

    export default {
        name: "PreciousOperation",
        components:{
            HighLight
        },
        data(){
            return{
                taskId:'',
                tasks:[],
                isPending:false,
                preciousSampleData:{},
                activeMaterialId:'',
                scanText:'',
                myTimeOut: '',
                isTimeOut: false,
            }
        },
        created(){
            this.selectTasks();
        },
        watch:{
            taskId:function(val){
                if(val !== ''){
                    this.setMyTimeOut();
                }else{
                    this.clearMyTimeOut();
                }
            }
        },
        mounted(){
            this.setFocus();
        },
        methods:{
            selectTasks:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:selectSampleTasksUrl,
                        data:{
                            filter:'warehouse_type=1#&#state=2'
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tasks = res.data.data.list;
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: getSampleTaskMaterialInfoUrl,
                        data: {
                            taskId:this.taskId,
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.preciousSampleData = res.data.data;
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            outRegular: function (materialId) {
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:outPreciousTaskRegularUrl,
                        data:{
                            materialId:materialId,
                            taskId:this.preciousSampleData.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                            this.isPending = false;
                            this.select();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            outSingular: function (materialId) {
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:outPreciousTaskSingularUrl,
                        data:{
                            materialId:materialId,
                            taskId:this.preciousSampleData.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                            this.isPending = false;
                            this.select();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            outLost:function(materialId){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:outPreciousTaskLostUrl,
                        data:{
                            materialId:materialId,
                            taskId:this.preciousSampleData.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                            this.isPending = false;
                            this.select();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            successAudioPlay: function () {
                let audio = document.getElementById('sAudio');
                if (audio !== null) {
                    if (audio.paused) {
                        audio.play();
                    }
                }
            },
            failAudioPlay: function () {
                let audio = document.getElementById('fAudio');
                if (audio !== null) {
                    if (audio.paused) {
                        audio.play();
                    }
                }
            },
            //获取焦点
            setFocus:function(){
                if (this.$route.path === '/sample/preciousOperation') {
                    if(document.getElementById('scanner-check')){
                        document.getElementById('scanner-check').focus();
                    }
                }
            },
            //扫描处理
            scannerHandler: function () {
                let scanText = this.scanText;
                this.scanText = '';

                /*sample: 03.01.0001@1000@1531817296428@A008@范例表@A-1@9@2018-07-17@*/
                //判断扫描的条码格式
                let result = handleScanText(scanText);
                if (result !== '') {
                    this.failAudioPlay();
                    this.$alertWarning(result);
                    return;
                }

                //判断扫描的二维码是否是该料盒的料盘码
                let tempArray = scanText.split("@");
                for(let i=0;i<tempArray.length;i++){
                    tempArray[i] = tempArray[i].replace(/\$AT\$/g, "@");
                }
                let materialId = tempArray[2];
                let isMaterialExit = false;
                for(let i=0;i<this.preciousSampleData.list.length;i++){
                    let obj = this.preciousSampleData.list[i];
                    if(materialId === obj.materialId){
                        this.activeMaterialId = materialId;
                        isMaterialExit = true;
                        break;
                    }else{
                        isMaterialExit = false;
                    }
                }
                if(isMaterialExit === false){
                    this.failAudioPlay();
                    this.$alertWarning('当前扫描的料盘不属于该任务');
                }
            },
            setMyTimeOut: function () {
                if (this.isTimeOut === true) {
                    this.clearMyTimeOut();
                }
                this.isTimeOut = true;
                let that = this;
                this.isPending = false;
                this.select();
                this.myTimeOut = setInterval(function () {
                    that.select();
                }, 10000);
            },
            clearMyTimeOut: function () {
                this.isTimeOut = false;
                clearTimeout(this.myTimeOut);
                this.myTimeOut = null;
            },
            finish:function(){
                if(this.taskId === ''){
                    this.$alertWarning('抽检任务不能为空');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:finishPreciousTaskUrl,
                        data:{
                            taskId:this.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('操作成功');
                            this.isPending = false;
                            this.selectTasks();
                            this.taskId = '';
                            this.preciousSampleData = {};
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .precious-operation{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        #scanner-check {
            position: fixed;
            opacity: 0;
            height: 0;
            line-height: 0;
            border: none;
            padding: 0;
        }
    }
</style>