<template>
    <div class="cut-material">
        <video controls="controls" id="sAudio" hidden>
            <source src="./../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
            <source src="./../../assets/141-Burst01.ogg" type="video/ogg">
        </video>
        <input type="text" title="scanner" id="check" v-model="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
        <div class="table">
            <h4>需截料、打印标签料盘</h4>
            <el-form :inline="true">
                <el-form-item label="任务">
                    <el-select v-model="taskId"  value="">
                        <el-option v-for="item in tasks" :value="item.id" :label="item.file_name" :key="item.id"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="IP">
                    <el-input v-model="configData.cutLoginPrinterIP" disabled></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="setIP">设置IP</el-button>
                </el-form-item>
                <el-form-item>
                    <el-button icon="el-icon-refresh" type="primary" @click="getCuttingMaterial"></el-button>
                </el-form-item>
            </el-form>
            <el-table
                    :data="tableData"
                    style="width:100%">
                <el-table-column
                        label="料号"
                        prop="no">
                </el-table-column>
                <el-table-column
                        label="料盘号"
                        prop="materialId">
                </el-table-column>
                <el-table-column
                        label="规格"
                        prop="specification">
                </el-table-column>
                <el-table-column
                        label="出库数量"
                        prop="outQuantity">
                </el-table-column>
                <el-table-column
                        label="剩余数量"
                        prop="materialQuantity">
                </el-table-column>
                <el-table-column
                        label="客户"
                        prop="supplierName">
                </el-table-column>
                <el-table-column
                        label="厂商"
                        prop="manufacturer">
                </el-table-column>
                <el-table-column
                        min-width="160"
                        label="生产日期"
                        prop="productionTime">
                </el-table-column>
                <el-table-column label="操作" min-width="100">
                    <template slot-scope="scope">
                        <el-button type="primary" @click="print(scope.row)">打印</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <div class="logout">
            <span class="name">{{user.name}}</span>
            <span class="icon-logout"><i class="el-icon-coke-power" title="退出" @click="logout"></i></span>
        </div>
        <set-i-p :is-set.sync="isSet"></set-i-p>
    </div>
</template>

<script>
    import {mapGetters,mapActions} from 'vuex'
    import {getCuttingMaterialUrl, getCuttingTaskUrl, logoutUrl, printCutUrl} from "../../plugins/globalUrl";
    import {axiosPost} from "../../utils/fetchData";
    import {errHandler} from "../../utils/errorHandler";
    import {handleScanText} from "../../utils/scan";
    import SetIP from "./subscomp/setIP";

    export default {
        name: "CutMaterial",
        components: {SetIP},
        data(){
            return{
                tableData:[],
                scanText:'',
                isPending:false,
                isSet:false,
                isFocus:false,
                tasks:[],
                taskId:''
            }
        },
        created(){
            this.getCuttingTask();
        },
        computed:{
            ...mapGetters(['user','configData'])
        },
        mounted(){
            this.setFocus();
        },
        watch:{
            isSet:function(val){
                this.isFocus = val;
            },
            isFocus:function(val){
                if(val === true){
                    document.getElementById('check').blur();
                }else{
                    document.getElementById('check').focus();
                }
            },
            taskId:function(val){
                if(val !== ''){
                    this.getCuttingMaterial();
                }
            }
        },
        methods:{
            ...mapActions(['setToken']),
            getCuttingMaterial:function(){
                if(this.taskId === ''){
                    this.$alertWarning('请先选择任务');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:getCuttingMaterialUrl
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tableData = res.data.data;
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
            getCuttingTask:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:getCuttingTaskUrl
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
            print:function(row){
                if(this.configData.cutLoginPrinterIP === ''){
                    this.$alertWarning("打印机IP不能为空");
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:printCutUrl,
                        data:{
                            ip:this.configData.cutLoginPrinterIP,
                            materialId:row.materialId,
                            packingListItemId:row.packingListItemId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                        }else{
                            this.$alertWarning(res.data.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            setFocus: function () {
                if(!this.isFocus){
                    if (this.$route.path === '/cut') {
                        document.getElementById('check').focus();
                    }
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
                /*对比料号是否一致*/
                let tempArray = scanText.split("@");
                for(let i = 0;i<this.tableData.length;i++){
                    if(tempArray[2] === this.tableData[i].materialId){
                        this.print(this.tableData[i]);
                    }
                }
            },
            logout: function () {
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:logoutUrl,
                        data:{}
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.setToken('');
                            window.sessionStorage.clear();
                            this.$router.replace('/cutLogin');
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err =>{
                        console.log(err);
                        this.$alertError('请求超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            setIP:function(){
                this.isSet = true;
            }
        }
    }
</script>

<style scoped lang="scss">
    .cut-material{
        position:relative;
        box-sizing:border-box;
        width:100%;
        height:100%;
        padding:20px 20px;
        .table{
            box-sizing:border-box;
            width:100%;
            background:#fff;
            border-radius:6px;
            padding:10px 30px 50px;
        }
        #check{
            position: fixed;
            opacity: 0;
            height: 0;
            line-height: 0;
            border: none;
            padding: 0;
        }
        .logout{
            position:absolute;
            top:30px;
            right:40px;
            display:flex;
            align-items:center;
            .name{
                font-weight:bold;
                font-size:18px;
                margin-right:10px;
            }
            .icon-logout{
                i{
                    font-size:20px;
                    cursor:pointer;
                }
            }
        }
    }
</style>