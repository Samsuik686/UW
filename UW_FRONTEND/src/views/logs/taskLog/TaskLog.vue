<template>
    <div class="task-log" v-loading="isLoading">
        <el-form :inline="true" :model="taskLogInfo" class="task-log-form">
            <el-form-item label="任务ID">
                <el-input v-model.trim="taskLogInfo.packing_list_item_id" placeholder="任务ID"></el-input>
            </el-form-item>
            <el-form-item label="操作员ID">
                <el-input v-model.trim="taskLogInfo.operator" placeholder="操作员ID"></el-input>
            </el-form-item>
            <el-form-item label="料盘唯一码">
                <el-input v-model.trim="taskLogInfo.material_id" placeholder="料盘唯一码"></el-input>
            </el-form-item>
            <el-form-item label="料号">
                <el-input v-model.trim="taskLogInfo.no" placeholder="料号"></el-input>
            </el-form-item>
            <el-form-item label="时间">
                <el-date-picker
                        :clearable="isClear"
                        v-model="times"
                        type="datetimerange"
                        align="right"
                        value-format="yyyy-MM-dd HH:mm:ss"
                        :picker-options="pickerOptions"
                        range-separator="-"
                        :default-time="['00:00:00','23:59:59']"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期">
                </el-date-picker>
            </el-form-item>
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
            </el-form-item>
        </el-form>
        <el-table
                @sort-change="sortChange"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="序号"
                    prop="showId"
                    width="70">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="任务ID"
                    prop="packingListItemId"
            >
            </el-table-column>
            <el-table-column
                    label="任务类型"
                    prop="taskType">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    min-width="120"
                    label="料盘唯一码"
                    prop="materialId">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="料号"
                    min-width="140"
                    prop="materialNo">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="数量"
                    prop="quantity">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="操作员ID"
                    prop="operator">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="操作员"
                    prop="operatorName">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="是否自动"
                    prop="auto">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="时间"
                    min-width="160"
                    prop="time">
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
    </div>
</template>

<script>
    import {logsUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";

    export default {
        name: "TaskLog",
        data(){
            return{
                taskLogInfo:{
                    packing_list_item_id:'',//任务ID
                    operator:'',//操作员
                    material_id:'',//料盘唯一码
                    no:'',//料号
                    time:''//时间
                },
                isClear:false,
                times:[],
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false,
                isLoading:false,
                filter:'',
                pickerOptions: {
                    shortcuts: [{
                        text: '最近一周',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                            picker.$emit('pick', [start, end]);
                        }
                    }, {
                        text: '最近一个月',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
                            picker.$emit('pick', [start, end]);
                        }
                    }, {
                        text: '最近三个月',
                        onClick(picker) {
                            const end = new Date();
                            const start = new Date();
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
                            picker.$emit('pick', [start, end]);
                        }
                    }]
                },
                ascBy:'',
                descBy:'time'
            }
        },
        mounted(){
            this.setFilter();
        },
        methods:{
            initForm:function(){
                this.taskLogInfo.no = '';
                this.taskLogInfo.material_id = '';
                this.taskLogInfo.operator = '';
                this.taskLogInfo.packing_list_item_id = '';
                this.taskLogInfo.time = '';
                this.times = [];
                this.filter = '';
            },
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:logsUrl,
                        data:{
                            table: 'task_log',
                            pageNo:this.pageNo,
                            pageSize: this.pageSize,
                            filter:this.filter,
                            ascBy:this.ascBy,
                            descBy:this.descBy,
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
                    this.taskLogInfo.time = 'time' + '>=' + this.times[0] + '#&#' + 'time' + '<=' + this.times[1];
                }else{
                    this.taskLogInfo.time = '';
                }
                let copyTaskLogsInfo = {};
                for(let i in this.taskLogInfo){
                    if(this.taskLogInfo[i] !== ''){
                        copyTaskLogsInfo[i] = this.taskLogInfo[i];
                    }
                }
                let isFirst = true;
                let filter = '';
                for(let i in copyTaskLogsInfo){
                    if(isFirst === true){
                         if(i === 'time'){
                            filter = filter + copyTaskLogsInfo[i];
                        }else{
                            filter = filter + (i + "like" +  copyTaskLogsInfo[i]);
                        }
                        isFirst = false;
                    }else{
                        if(i === 'time'){
                            filter = filter + "#&#" + copyTaskLogsInfo[i];
                        }else{
                            filter = filter + ("#&#" + i + "like" +  copyTaskLogsInfo[i]);
                        }
                    }
                }
                this.filter = filter;
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            },
            sortChange:function(data){
                let prop = data.prop;
                switch (data.prop) {
                    case "packingListItemId":
                        prop = "packing_list_item_id";
                        break;
                    case "materialId":
                        prop = "material_id";
                        break;
                    case "materialNo":
                        prop = "no";
                        break;
                    case "quantity":
                        prop = "task_log.quantity";
                        break;
                    case "operatorName":
                        prop = "operator";
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
                    this.descBy = 'time';
                    this.ascBy = '';
                }
                this.pageNo = 1;
                this.select();
            }
        },
    }
</script>

<style scoped lang="scss">
    .task-log{
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