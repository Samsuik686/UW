<template>
    <div class="precious-now">
        <video controls="controls" id="sAudio" hidden>
            <source src="./../../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
            <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
        </video>
        <input type="text" title="scanner" id="precious-check" v-model="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
        <el-form :inline="true" class="call-form" size="medium">
            <el-form-item label="出入库类型">
                <el-select v-model="windowType" placeholder="出入库类型" value="">
                    <el-option label="入库" value="0"></el-option>
                    <el-option label="出库" value="1"></el-option>
                    <el-option label="调拨入库" value="4"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="任务">
                <el-select v-model="taskId" placeholder="任务" value="">
                    <el-option v-for="item in tasks" :value="item.id" :label="item.fileName" :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="handleFinish" v-if="windowType === '1'">完成缺料条目</el-button>
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
                    :label="windowType === '1'?'本次欠发/超发数量':'本次欠入/超入数量'">
            </el-table-column>
            <el-table-column
                    prop="designator"
                    label="位号">
            </el-table-column>
            <el-table-column
                    prop="scanNum"
                    label="已扫料盘">
            </el-table-column>
            <el-table-column
                    v-if="windowType === '1'"
                    prop="oldestMaterialDate"
                    label="最旧料日期">
            </el-table-column>
        </el-table>
        <out-task-details :is-out-show.sync="isOutShow" :task-item="taskItem" :finishType.sync="finishType" @refreshData="refreshData"></out-task-details>
        <in-task-details :is-in-show.sync="isInShow" :task-item="taskItem" :finishType.sync="finishType" @refreshData="refreshData"></in-task-details>
    </div>
</template>

<script>
    import {mapGetters,mapActions} from 'vuex'
    import {
        finishPreciousTaskLackItemUrl,
        taskBackPreciousAfterCuttingUrl,
        taskGetIOTaskInfosUrl,
        taskInPreciousUrl,
        taskOutPreciousUrl,
        taskSelectUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import OutTaskDetails from "./comp/OutTaskDetails";
    import InTaskDetails from "./comp/InTaskDetails";
    import {handleScanText} from "../../../utils/scan";
    export default {
        name: "PreciousNow",
        components: {InTaskDetails, OutTaskDetails},
        data(){
            return{
                tableData:[],
                windowType:'0',
                tasks:[],
                taskId:'',
                myTimeOut: '',
                isTimeOut: false,
                scanText:'',
                isOutShow:false,
                isInShow:false,
                isPending:false,
                taskItem:{},
                finishType:''
            }
        },
        beforeDestroy(){
            this.clearMyTimeOut();
        },
        watch:{
            windowType:function(val){
                if(val !== ''){
                    this.tasks = [];
                    this.taskId = '';
                    this.tableData = [];
                    this.selectTasks();
                }
            },
            taskId:function(val){
                if(val !== ''){
                    this.setMyTimeOut();
                }else{
                    this.clearMyTimeOut();
                    this.tableData = [];
                }
            },
            isPreciousBlur:function (val) {
                if(val === true){
                    document.getElementById('precious-check').blur();
                }else{
                    document.getElementById('precious-check').focus();
                }
            },
        },
        computed:{
            ...mapGetters(['isPreciousBlur','isPreciousCut'])
        },
        mounted(){
            this.setFocus();
            this.selectTasks();
        },
        methods:{
            ...mapActions(['setIsPreciousCut']),
            selectTasks:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:taskSelectUrl,
                        data:{
                            filter:'type='+this.windowType+'#&#warehouse_type=1#&#state=2'
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
            scannerHandler:function(){
                let scanText = this.scanText;
                this.scanText = '';
                //操作完成码
                if (scanText === "###finished###") {
                    if(this.isInShow === true){
                        this.finishType = '入库';
                    }
                    if(this.isOutShow === true){
                        this.finishType = '出库';
                    }
                    return;
                }
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
                let text = tempArray[0].replace('\ufeff', '');
                let isExit = false;
                for (let i = 0; i < this.tableData.length; i++) {
                    if (text.toLocaleUpperCase() === this.tableData[i].no.toLocaleUpperCase()) {
                        isExit = true;
                        if(this.isOutShow === false && this.isInShow === false){
                            if(this.windowType === '0'){
                                this.isInShow = true;
                                this.taskItem = this.tableData[i];
                                return;
                            }
                            if(this.windowType === '1'){
                                this.isOutShow = true;
                                this.taskItem = this.tableData[i];
                                return;
                            }
                        }else{
                            if(JSON.parse(JSON.stringify(this.taskItem)) !== ''){
                                if(text !== this.taskItem.no){
                                    this.$alertWarning('二维码格式错误，料号不对应');
                                    return;
                                }
                            }
                            let packingListItemId = this.tableData[i].packingListItemId;
                            //截料
                            if(this.isPreciousCut === true && this.isOutShow === true){
                                this.backAfterCutting(packingListItemId,tempArray,i);
                                return;
                            }
                            //出库
                            if(this.isPreciousCut === false && this.isOutShow === true){
                                this.outM(packingListItemId,tempArray,i);
                                return;
                            }
                            //入库
                            if(this.isInShow === true){
                                this.inM(packingListItemId,tempArray,i);
                                return;
                            }
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
            setFocus:function(){
                if(this.isPreciousBlur === true){
                    return;
                }
                if (this.$route.path === '/io/preciousNow') {
                    document.getElementById('precious-check').focus();
                }
            },
            select:function(index){
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
                            if(index !== undefined){
                                this.textToSpeak('已扫'+this.tableData[index].scanNum+'盘');
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
            selectRow:function(row,column,cell,event){
                if(this.windowType === ''){return;}
                this.taskItem = row;
                if(this.windowType === '1'){
                    this.isOutShow = true;
                }else{
                    this.isInShow = true;
                }
            },
            //出库
            outM:function(packingListItemId,tempArray,i){
                let options = {
                    url: taskOutPreciousUrl,
                    data: {
                        packingListItemId:packingListItemId,
                        materialId: tempArray[2],
                        quantity: tempArray[1],
                        supplierName: tempArray[4]
                    }
                };
                axiosPost(options).then(response => {
                    if (response.data.result === 200) {
                        this.successAudioPlay();
                        this.$alertSuccess('操作成功');
                        this.isPending = false;
                        this.select(i);
                    } else {
                        this.failAudioPlay();
                        errHandler(response.data);
                    }
                });
            },
            //入库
            inM:function(packingListItemId,tempArray,i){
                let options = {
                    url: taskInPreciousUrl,
                    data: {
                        packingListItemId:packingListItemId
                    }
                };
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
                        this.select(i);
                    } else {
                        this.failAudioPlay();
                        errHandler(response.data);
                    }
                });
            },
            //截料入库
            backAfterCutting: function (packingListItemId,tempArray,i) {
                let options = {
                    url: taskBackPreciousAfterCuttingUrl,
                    data: {
                        packingListItemId:packingListItemId,
                        materialId: tempArray[2],
                        quantity: tempArray[1],
                        supplierName: tempArray[4]
                    }
                };
                axiosPost(options).then(res => {
                    if (res.data.result === 200) {
                        this.successAudioPlay();
                        this.$alertSuccess('操作成功');
                        this.isPending = false;
                        this.select(i);
                        this.setIsPreciousCut(false);
                    } else {
                        this.failAudioPlay();
                        errHandler(res.data);
                    }
                }).catch(err => {
                    this.$alertError(err);
                })
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
            handleFinish:function(){
                if(this.taskId === ''){
                    return;
                }
                this.$confirm('该操作将会完成所有缺料条目，请确认是否继续进行?', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: false
                }).then((action) => {
                    if (action === "confirm") {
                        this.finishLackTask();
                    }
                }).catch(() => {
                    this.$alertInfo("已取消操作");
                });
            },
            finishLackTask:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:finishPreciousTaskLackItemUrl,
                        data:{
                            taskId:this.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('操作成功');
                            this.isPending = false;
                            this.select();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            refreshData:function(){
                this.isPending = false;
                this.select();
            },
            //文字转语音提示
            textToSpeak:function(text){
                let synth = window.speechSynthesis;
                let utterThis = new SpeechSynthesisUtterance(text);
                utterThis.volume = 1;
                utterThis.pitch = 2;
                synth.speak(utterThis);
            }
        }
    }
</script>

<style scoped lang="scss">
    .precious-now{
        position:relative;
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        #precious-check {
            position: fixed;
            opacity: 0;
            height: 0;
            line-height: 0;
            border: none;
            padding: 0;
        }
        .operation-img{
            position:absolute;
            right:0;
            top:0;
            .img-style {
                width:80px;
                height:auto;
                text-align:center;
            }
        }
    }
</style>