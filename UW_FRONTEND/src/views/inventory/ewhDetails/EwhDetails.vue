<template>
    <div class="ewh-details" v-loading="isLoading">
        <el-form :inline="true" class="uw-details-form">
            <el-form-item label="供应商">
                <el-select v-model.trim="supplier" placeholder="供应商" value="">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="盘点任务">
                <el-select v-model.trim="taskId" placeholder="盘点任务" value="">
                    <el-option  v-for="item in tasks" :label="item.file_name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="料号">
                <el-input v-model.trim="no" placeholder="料号"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="select">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
            </el-form-item>
        </el-form>
        <el-button type="primary" @click="isImport = true" size="small">导入物料仓盘点数据</el-button>
        <el-table
                @expand-change="toggleRowExpansion"
                :data="tableData"
                style="width:100%">
            <el-table-column type="expand">
                <template slot-scope="props">
                    <ewh-details-item :row="props.row"></ewh-details-item>
                </template>
            </el-table-column>
            <el-table-column
                    label="序号"
                    prop="showId"
                    width="70">
            </el-table-column>
            <el-table-column
                    label="料号"
                    min-width="120">
                <template slot-scope="scope">
                    <span :class="{highLight:unInventoryData.includes(scope.row.material_type_id)}">{{scope.row.no}}</span>
                </template>
            </el-table-column>
            <el-table-column
                    label="供应商"
                    prop="supplier_name">
            </el-table-column>
            <el-table-column
                    label="盘前库存"
                    prop="before_num">
            </el-table-column>
            <el-table-column
                    label="盘点数量"
                    prop="actural_num">
            </el-table-column>
            <el-table-column
                    label="盘盈/盘亏"
                    prop="different_num">
            </el-table-column>
            <el-table-column
                    label="盘点人"
                    prop="inventory_operatior">
            </el-table-column>
            <el-table-column
                    label="盘点开始时间"
                    min-width="160"
                    prop="start_time">
            </el-table-column>
            <el-table-column
                    label="盘点结束时间"
                    min-width="160"
                    prop="end_time">
            </el-table-column>
            <el-table-column
                    label="审核人"
                    prop="check_operatior">
            </el-table-column>
            <el-table-column
                    label="审核时间"
                    min-width="160"
                    prop="check_time">
            </el-table-column>
        </el-table>
        <div style="width:100%;text-align:right;margin-top:8px;">
            <el-button type="primary" size="small" @click="checkInventoryData">审核盘点数据</el-button>
            <el-button type="primary" size="small" @click="coverEwhMaterial">一键平仓</el-button>
            <el-button type="primary" size="small" @click="exportInventoryReport">导出盘点报表</el-button>
        </div>
        <import-record :is-import.sync="isImport" :task-id="String(taskId)"></import-record>
    </div>
</template>

<script>
    import {
        checkEwhInventoryTaskUrl,
        coverEwhMaterialOneKeyUrl,exportEWhReportInventoryUrl,
        finishInventoryTaskUrl, getEwhInventoryTaskInfoUrl,
        getInventoryTaskUrl, supplierSelectUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost, downloadFile} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {mapGetters,mapActions} from 'vuex'
    import ImportRecord from "./comp/ImportRecord";
    import EwhDetailsItem from "./comp/EwhDetailsItem";
    export default {
        name: "EwhDetails",
        components: {EwhDetailsItem, ImportRecord},
        data(){
            return{
                tableData: [],
                isPending:false,
                isLoading:false,
                suppliers:[],
                tasks:[],
                supplier:'',
                taskId:'',
                no:'',
                isImport:false
            }
        },
        created(){
            this.selectSupplier();
        },
        computed:{
            ...mapGetters(['unInventoryData'])
        },
        watch:{
            supplier:function (val) {
                if(val !== ''){
                    this.tasks = [];
                    this.taskId = '';
                    this.tableData = [];
                    this.getInventoryTask(val);
                }
            },
            taskId:function(val){
                if(val !== ''){
                    this.select();
                }
            }
        },
        methods:{
            ...mapActions(['setUnInventoryData']),
            selectSupplier:function(){
                let options = {
                    url: supplierSelectUrl,
                    data: {}
                };
                axiosPost(options).then(res => {
                    if(res.data.result === 200){
                        let data = res.data.data.list;
                        data.map((item) => {
                            if(item.enabled === true){
                                this.suppliers.push(item);
                            }
                        });
                    }else{
                        errHandler(res.data)
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            },
            getInventoryTask: function (supplierId) {
                if (!this.isPending) {
                    this.isPending = false;
                    let options = {
                        url: getInventoryTaskUrl,
                        data: {
                            supplierId: supplierId
                        }
                    };
                    axiosPost(options).then(response => {
                        this.tasks = response.data.data;
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            setStateString:function(state){
                let stateString = '';
                switch (state) {
                    case 0:
                        stateString = '未审核';
                        break;
                    case 1:
                        stateString = '未开始';
                        break;
                    case 2:
                        stateString = '进行中';
                        break;
                    case 3:
                        stateString = '已完成';
                        break;
                    case 4:
                        stateString = '已作废';
                        break;
                    case 5:
                        stateString = '存在缺料';
                        break;
                    default:
                        break;
                }
                return stateString;
            },
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url: getEwhInventoryTaskInfoUrl,
                        data: {
                            taskId:this.taskId,
                            no:this.no
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            let data = res.data.data;
                            data.map((item,index) => {
                                item.showId = index + 1;
                                item.stateString = this.setStateString(item.state);
                            });
                            this.tableData = data;
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                        this.isLoading = false;
                    })
                }
            },
            initForm:function(){
                this.no = '';
            },
            toggleRowExpansion:function(row,expanded){
                if(expanded.length === 0){
                    this.select();
                }
            },
            exportInventoryReport:function(){
                if(this.taskId === ''){
                    this.$alertWarning('盘点任务不能为空');
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let data = {
                        taskId:this.taskId,
                        no:this.no,
                        '#TOKEN#': this.$store.state.token
                    };
                    downloadFile(exportEWhReportInventoryUrl, data);
                    let count = 0;
                    let mark = setInterval(() => {
                        count++;
                        if (count > 9) {
                            count = 0;
                            clearInterval(mark);
                            this.isPending = false
                        }
                    }, 1000);
                    this.$alertSuccess('请求成功，请等待下载');
                } else {
                    this.$alertInfo('请稍后再试')
                }
            },
            checkInventoryData:function(){
                if(this.taskId === ''){
                    this.$alertWarning('请先选择盘点任务');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:checkEwhInventoryTaskUrl,
                        data:{
                            taskId:this.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            if(res.data.data === "操作成功"){
                                this.$alertSuccess('审核成功');
                                this.setUnInventoryData([]);
                            }else{
                                this.$alertWarning('存在物料未盘点');
                                let data = res.data.data;
                                let arr = data.split('[')[1].split(']')[0].split(',');
                                this.setUnInventoryData(arr);
                            }
                            this.isPending = false;
                            this.select();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                        this.isLoading = false;
                    })
                }
            },
            coverEwhMaterial:function(){
                if(this.taskId === ''){
                    this.$alertWarning('请先选择盘点任务');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:coverEwhMaterialOneKeyUrl,
                        data:{
                            taskId:this.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess(res.data.data);
                            this.isPending = false;
                            this.select();
                            this.confirmFinishTask();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                        this.isLoading = false;
                    })
                }
            },
            confirmFinishTask:function(){
                this.$confirm('请点击“完成任务”按钮完成盘点任务?', '提示', {
                    confirmButtonText: '完成任务',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.finishTask();
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: '已取消'
                    });
                });
            },
            finishTask:function () {
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: finishInventoryTaskUrl,
                        data: {
                            taskId: this.taskId
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
    .ewh-details{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        .highLight{
            color:red;
        }
    }
</style>