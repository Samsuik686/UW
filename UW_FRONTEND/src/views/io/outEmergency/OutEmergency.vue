<template>
    <div class="out-emergency">
        <video controls="controls" id="sAudio" hidden>
            <source src="./../../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
            <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
        </video>
        <input type="text" title="scanner" id="emergency-check" v-model="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
        <el-form :inline="true" class="call-form" size="medium">
            <el-form-item label="任务">
                <el-select v-model="taskId" placeholder="任务" value="" @visible-change="selectTasks">
                    <el-option v-for="item in tasks" :value="item.id" :label="item.file_name" :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="finishTask">完成任务</el-button>
            </el-form-item>
        </el-form>
        <el-table
                @cell-dblclick="selectRow"
                :data="tableData"
                style="width:100%">
            <el-table-column
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
                    prop="planQuantity"
                    label="计划">
            </el-table-column>
            <el-table-column
                    prop="actuallyQuantity"
                    label="实际">
            </el-table-column>
            <el-table-column
                    prop="storeQuantity"
                    label="库存">
            </el-table-column>
            <el-table-column
                    prop="lackQuantity"
                    label="本次欠发/超发数量">
            </el-table-column>
            <el-table-column
                    prop="scanNum"
                    label="已扫料盘">
            </el-table-column>
        </el-table>
        <out-task-details :is-show.sync="isShow" :task-item="taskItem" @refreshData="refreshData"></out-task-details>
    </div>
</template>

<script>
    import {
        finishEmergencyRegularTaskUrl,
        getEmergencyRegularTasksUrl,
        outEmergencyRegularUrl,
        taskGetIOTaskInfosUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {handleScanText} from "../../../utils/scan";
    import OutTaskDetails from "./subscomp/OutTaskDetails";
    export default {
        name: "OutEmergency",
        components:{
            OutTaskDetails
        },
        data(){
            return{
                tableData:[],
                tasks:[],
                taskId:'',
                myTimeOut: '',
                isTimeOut: false,
                scanText:'',
                isPending:false,
                taskItem:{},
                isShow:false
            }
        },
        beforeDestroy(){
            this.clearMyTimeOut();
        },
        watch:{
            taskId:function(val){
                if(val !== ''){
                    this.setMyTimeOut();
                }else{
                    this.clearMyTimeOut();
                    this.tableData = [];
                }
            }
        },
        mounted(){
            this.setFocus();
            this.selectTasks();
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:taskGetIOTaskInfosUrl,
                        data:{
                            taskId:this.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tableData = res.data.data;
                            for(let i =0;i<this.tableData.length;i++){
                                if(this.taskItem.no === this.tableData[i].no){
                                    this.taskItem = this.tableData[i];
                                }
                            }
                        }else if(res.data.result === 412){
                            this.isPending = false;
                            this.clearMyTimeOut();
                            this.taskId = '';
                            this.selectTasks();
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
            selectTasks:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:getEmergencyRegularTasksUrl,
                        data:{}
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tasks = res.data.data;
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
                }, 1000);
            },
            clearMyTimeOut: function () {
                this.isTimeOut = false;
                clearTimeout(this.myTimeOut);
                this.myTimeOut = null;
            },
            setFocus:function(){
                if (this.$route.path === '/io/outEmergency') {
                    document.getElementById('emergency-check').focus();
                }
            },
            scannerHandler:function(){
                let scanText = this.scanText;
                this.scanText = '';

                //料号
                /*sample: 03.01.0001@1000@1531817296428@A008@范例表@A-1@9@2018-07-17@*/
                /*05.01.0506@1000@1562570647531@s444222@hcxx@A-6@13@2019-07-08@PCB:V10驱动光板,V10_Driver_v1.6@供应商@周期@*/
                //判断扫描的条码格式
                let result = handleScanText(scanText);
                if (result !== '') {
                    this.failAudioPlay();
                    this.$alertWarning(result);
                    return;
                }
                /*对比料号是否一致*/
                let tempArray = scanText.split("@");
                for(let i=0;i<tempArray.length;i++){
                    tempArray[i] = tempArray[i].replace(/\$AT\$/g, "@");
                }
                let text = tempArray[0].replace('\ufeff', '');
                let isExit = false;
                for (let i = 0; i < this.tableData.length; i++) {
                    if (text.toLocaleUpperCase() === this.tableData[i].no.toLocaleUpperCase()) {
                        isExit = true;
                        if(this.isShow === false){
                            this.isShow = true;
                            this.taskItem = this.tableData[i];
                            return;
                        }else{
                            if(JSON.parse(JSON.stringify(this.taskItem)) !== ''){
                                if(text !== this.taskItem.no){
                                    this.failAudioPlay();
                                    this.$alertWarning('二维码格式错误，料号不对应');
                                    return;
                                }
                            }
                            this.outM(tempArray,i);
                        }
                    }
                }
                if (isExit === false) {
                    this.failAudioPlay();
                    this.$alertWarning('二维码格式错误，料号不对应');
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
            selectRow:function(row,column,cell,event){
                this.taskItem = row;
                this.isShow = true;
            },
            refreshData:function(){
                this.isPending = false;
                this.select();
            },
            outM:function(tempArray){
                let options = {
                    url:outEmergencyRegularUrl,
                    data: {
                        taskId:this.taskId
                    }
                };
                if(tempArray[0])options.data['no'] = tempArray[0];
                if(tempArray[1])options.data['quantity'] = tempArray[1];
                if(tempArray[2])options.data['materialId'] = tempArray[2];
                if(tempArray[4])options.data['supplierName'] = tempArray[4];
                if(tempArray[7])options.data['productionTime'] = tempArray[7];
                if(tempArray[9])options.data['manufacturer'] = tempArray[9];
                if(tempArray[10])options.data['cycle'] = tempArray[10];
                if(tempArray[11])options.data['printTime'] = tempArray[11];
                axiosPost(options).then(response => {
                    if (response.data.result === 200) {
                        this.successAudioPlay();
                        this.$alertSuccess('操作成功');
                        this.isPending = false;
                        this.select();
                    } else {
                        this.failAudioPlay();
                        errHandler(response.data);
                    }
                });
            },
            finishTask:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:finishEmergencyRegularTaskUrl,
                        data:{
                            taskId:this.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('操作成功');
                            this.taskId = '';
                            this.tableData = [];
                            this.taskItem = {};
                            this.isPending = false;
                            this.selectTasks();
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
            }
        }
    }
</script>

<style scoped lang="scss">
    .out-emergency{
        position:relative;
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        #emergency-check {
            position: fixed;
            opacity: 0;
            height: 0;
            line-height: 0;
            border: none;
            padding: 0;
        }
    }
</style>