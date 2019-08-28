<template>
    <div class="sample-task" v-loading="isLoading">
        <el-form :inline="true" :model="tasksInfo" class="tasks-form">
            <el-form-item label="状态">
                <el-select v-model.trim="tasksInfo.state" placeholder="状态" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option label="未开始" value='1'></el-option>
                    <el-option label="进行中" value='2'></el-option>
                    <el-option label="已完成" value='3'></el-option>
                    <el-option label="已作废" value='4'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="供应商">
                <el-select v-model.trim="tasksInfo.supplier" placeholder="供应商" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="创建时间">
                <el-date-picker
                        :clearable="isClear"
                        v-model="times"
                        type="datetimerange"
                        align="right"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        range-separator="-"
                        :default-time="['00:00:00','23:59:59']"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期">
                </el-date-picker>
            </el-form-item>
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="isAdding = true">创建任务</el-button>
            </el-form-item>
        </el-form>
        <el-table
                @sort-change="sortChange"
                :data="tableData"
                style="width:100%">
            <el-table-column label="启动/暂停" width="60">
                <template slot-scope="scope">
                    <span title="启动/暂停" v-if="scope.row.state === 2" @click="startPause(scope.row)">
                        <i :class="scope.row.status === false?'el-icon-video-play':'el-icon-video-pause'"
                           style="font-size:22px;cursor:pointer"></i>
                    </span>
                </template>
            </el-table-column>
            <el-table-column
                    label="序号"
                    prop="showId"
                    width="70">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    min-width="120"
                    label="任务名"
                    prop="fileName">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="状态"
                    prop="stateString">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="供应商"
                    prop="supplierName">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="创建时间"
                    min-width="160"
                    prop="createTimeString">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="备注"
                    prop="remarks">
            </el-table-column>
            <el-table-column label="操作" min-width="120">
                <template slot-scope="scope">
                    <span style="margin-right:10px;cursor:pointer" title="详细" @click="showDetails(scope.row)">
                        <i class="el-icon-coke-list"></i>
                    </span>
                    <span style="margin-right:10px;cursor:pointer" title="修改状态" @click="handleEditStatus(scope.row)">
                        <i class="el-icon-coke-menu"></i>
                    </span>
                    <span style="margin-right:10px;cursor:pointer" title="更换仓口" @click="handleChangeWindow(scope.row)">
                        <i class="el-icon-coke-transfer"></i>
                    </span>
                    <span style="margin-right:10px;cursor:pointer" title="导出报表"  @click="downloadReport(scope.row)">
                        <i class="el-icon-coke-download"></i>
                    </span>
                </template>
            </el-table-column>
        </el-table>
        <div class="block">
            <el-pagination
                    background
                    :current-page.sync="pageNo"
                    :page-size.sync="pageSize"
                    :page-sizes="[20,40,80,100]"
                    @size-change="handlePageSize"
                    @current-change="select"
                    layout="total,sizes,prev,pager,next,jumper"
                    :total="totallyData">
                >
            </el-pagination>
        </div>
        <add-task :is-adding.sync="isAdding" :suppliers="suppliers"></add-task>
        <edit-status :is-edit-status.sync="isEditStatus" :edit-data="editData"></edit-status>
        <sample-details></sample-details>
        <change-window :is-change.sync="isChange" :edit-data="editData"></change-window>
    </div>
</template>

<script>
    import {
        exportSampleTaskInfoUrl,
        selectSampleTasksUrl,
        supplierSelectUrl,
        switchTaskUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost, downloadFile} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import AddTask from "./comp/AddTask";
    import EditStatus from "./comp/EditStatus";
    import Bus from './../../../utils/bus'
    import SampleDetails from './comp/SampleDetails'
    import ChangeWindow from './comp/ChangeWindow'
    export default {
        name: "Tasks",
        components: {EditStatus, AddTask,SampleDetails,ChangeWindow},
        data(){
            return{
                tasksInfo:{
                    state:'',
                    create_time:'',
                    supplier:''
                },
                times:[],
                isClear:false,
                suppliers:[],
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false,
                isLoading:false,
                isAdding:false,
                isEditStatus:false,
                filter:'',
                editData:{},
                isChange:false,
                ascBy:'',
                descBy:'create_time'
            }
        },
        created(){
            this.selectSupplier();
        },
        mounted(){
            this.setFilter();
        },
        watch:{
            isAdding:function (val) {
                if(val === false){
                    this.select()
                }
            },
            isEditStatus:function(val){
                if(val === false){
                    this.select();
                }
            },
            isChange:function(val){
                if(val === false){
                    this.select();
                }
            }
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:selectSampleTasksUrl,
                        data:{
                            pageNo:this.pageNo,
                            pageSize: this.pageSize,
                            ascBy:this.ascBy,
                            descBy:this.descBy,
                            filter:this.filter
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            let data = res.data.data.list;
                            data.map((item,index) => {
                                item.showId = index + 1 + (this.pageNo - 1)*this.pageSize;
                            });
                            this.tableData = data;
                            this.totallyData = res.data.data.totalRow;
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
            setFilter:function(){
                if(this.times !== null && this.times.length > 0){
                    this.tasksInfo.create_time = 'create_time' + '>=' + this.times[0] + '#&#' + 'create_time' + '<=' + this.times[1];
                }else{
                    this.tasksInfo.create_time = '';
                }
                let copyTasksInfo = {};
                for(let i in this.tasksInfo){
                    if(this.tasksInfo[i] !== ''){
                        copyTasksInfo[i] = this.tasksInfo[i];
                    }
                }
                let isFirst = true;
                let filter = '';
                for(let i in copyTasksInfo){
                    if(isFirst === true){
                        if(i === 'create_time'){
                            filter = filter + copyTasksInfo[i];
                        }else{
                            filter = filter + (i + "=" +  copyTasksInfo[i]);
                        }
                        isFirst = false;
                    }else{
                        if(i === 'create_time'){
                            filter = filter + "#&#" + copyTasksInfo[i];
                        }else{
                            filter = filter + ("#&#" + i + "=" +  copyTasksInfo[i]);
                        }
                    }
                }
                this.filter = filter;
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            initForm:function(){
                this.tasksInfo.create_time = '';
                this.tasksInfo.supplier = '';
                this.tasksInfo.state = '';
                this.times = [];
                this.filter = '';
            },
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
            startPause:function(row){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:switchTaskUrl,
                        data:{
                            taskId:row.id,
                            flag:!row.status
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
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            downloadReport:function(row){
                if (!this.isPending) {
                    this.isPending = true;
                    let data = {
                        taskId:row.id,
                        type:row.type,
                        '#TOKEN#': this.$store.state.token
                    };
                    downloadFile(exportSampleTaskInfoUrl,data);
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
            handleEditStatus:function (row) {
                this.editData = row;
                this.isEditStatus = true;
            },
            showDetails:function(row){
                Bus.$emit('showSampleDetails',row);
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            },
            handleChangeWindow:function(row){
                this.editData = row;
                this.isChange = true;
            },
            sortChange:function(data){
                let prop = '';
                switch (data.prop) {
                    case "fileName":
                        prop = "file_name";
                        break;
                    case "stateString":
                        prop = "state";
                        break;
                    case "supplierName":
                        prop = "supplier";
                        break;
                    case "createTimeString":
                        prop = "create_time";
                        break;
                    default:
                        prop = data.prop;
                        break;
                }
                if(data.order === "ascending"){
                    this.ascBy = prop;
                    this.descBy = '';
                }else if(data.order === "descending"){
                    this.descBy = prop;
                    this.ascBy = '';
                }else{
                    this.descBy = 'create_time';
                    this.ascBy = '';
                }
                this.pageNo = 1;
                this.select();
            }
        }
    }
</script>

<style scoped lang="scss">
    .sample-task{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        .block {
            margin-top: 15px;
        }
    }
</style>