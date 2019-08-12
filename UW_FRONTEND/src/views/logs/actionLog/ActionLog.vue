<template>
    <div class="action-log" v-loading="isLoading">
        <el-form :inline="true" :model="actionLogInfo" class="action-log-form">
            <el-form-item label="IP地址">
                <el-input v-model.trim="actionLogInfo.ip" placeholder="IP地址"></el-input>
            </el-form-item>
            <el-form-item label="用户">
                <el-input v-model.trim="actionLogInfo.uid" placeholder="用户"></el-input>
            </el-form-item>
            <el-form-item label="时间">
                <el-date-picker
                        :clearable="isClear"
                        v-model="times"
                        :picker-options="pickerOptions"
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
                    label="IP地址"
                    prop="ip"
            >
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="用户"
                    prop="uid">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="操作"
                    prop="action"
                    min-width="250">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    prop="resultCode"
                    label="操作结果"
            >
                <template slot-scope="scope">
                    <high-light :row="scope.row"></high-light>
                </template>
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
    import HighLight from "./comp/HighLight";

    export default {
        name: "ActionLog",
        components: {HighLight},
        data(){
            return{
                actionLogInfo:{
                    ip:'',//IP地址
                    uid:'',//用户
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
                this.actionLogInfo.uid = '';
                this.actionLogInfo.ip = '';
                this.actionLogInfo.time = '';
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
                            table: 'action_log',
                            pageNo:this.pageNo,
                            pageSize: this.pageSize,
                            filter:this.filter,
                            ascBy:this.ascBy,
                            descBy:this.descBy
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
                    this.actionLogInfo.time = 'time' + '>=' + this.times[0] + '#&#' + 'time' + '<=' + this.times[1];
                }else{
                    this.actionLogInfo.time = '';
                }
                let copyActionLogsInfo = {};
                for(let i in this.actionLogInfo){
                    if(this.actionLogInfo[i] !== ''){
                        copyActionLogsInfo[i] = this.actionLogInfo[i];
                    }
                }
                let isFirst = true;
                let filter = '';
                for(let i in copyActionLogsInfo){
                    if(isFirst === true){
                        if(i === 'time'){
                            filter = filter + copyActionLogsInfo[i];
                        }else{
                            filter = filter + (i + "like" +  copyActionLogsInfo[i]);
                        }
                        isFirst = false;
                    }else{
                        if(i === 'time'){
                            filter = filter + "#&#" + copyActionLogsInfo[i];
                        }else{
                            filter = filter + ("#&#" + i + "like" +  copyActionLogsInfo[i]);
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
                if (data.prop === "resultCode") {
                    prop = "result_code";
                } else {
                    prop = data.prop;
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
                this.select();
            }
        },
    }
</script>

<style scoped lang="scss">
    .action-log{
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