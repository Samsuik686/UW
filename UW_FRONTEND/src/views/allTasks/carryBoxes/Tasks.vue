<template>
    <div class="tasks" v-loading="isLoading">
        <el-form :inline="true" :model="tasksInfo" class="tasks-form">
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-plus" @click="isAdding = true">创建任务</el-button>
            </el-form-item>
        </el-form>
        <el-table
                :data="tableData"
                style="width:100%">
            <el-table-column label="启动/暂停" width="60">
                <template slot-scope="scope">
                    <span title="启动/暂停" v-if="scope.row.state === 2" @click="startPause(scope.row)">
                        <i :class="scope.row.isRunning === false?'el-icon-video-play':'el-icon-video-pause'"
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
                    label="状态"
                    prop="stateString"
            >
            </el-table-column>
            <el-table-column
                    min-width="200"
                    label="任务名"
                    prop="name">
            </el-table-column>
            <el-table-column label="操作" min-width="80">
                <template slot-scope="scope">
                    <span style="margin-right:10px;cursor:pointer" title="详细" @click="showDetails(scope.row)">
                        <i class="el-icon-coke-list"></i>
                    </span>
                    <span style="margin-right:10px;cursor:pointer" title="修改状态" @click="handleEditStatus(scope.row)">
                        <i class="el-icon-coke-menu"></i>
                    </span>
                </template>
            </el-table-column>
        </el-table>
        <add-task :is-adding.sync="isAdding"></add-task>
        <edit-status :is-edit-status.sync="isEditStatus" :edit-data="editData" @showCheckTaskDetails="showCheckTaskDetails"></edit-status>
        <task-details></task-details>
    </div>
</template>

<script>
    import {mapGetters} from 'vuex'
    import Bus from '../../../utils/bus'
    import {
        exportUnfinishRegularTaskDetailsUrl, forceUnbundlingWindowUrl,
        supplierSelectUrl, taskCarryBoxesTaskSelectUrl,
        taskCarryBoxesTaskSwitchUrl,
        taskSelectUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost, downloadFile} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import AddTask from "./comp/AddTask";
    import SetPriority from "./comp/SetPriority";
    import EditRemarks from "./comp/EditRemarks";
    import TaskDetails from './comp/TaskDetails'
    import EditStatus from "./comp/EditStatus";
    import CheckTaskDetails from "./comp/CheckTaskDetails";
    import ChangeWindow from "./comp/ChangeWindow";
    import SetNonexistentMaterial from "./comp/SetNonexistentMaterial";
    import EditMaterial from "./comp/EditMaterial";
    export default {
        name: "Tasks",
        components: {
            EditMaterial,
            SetNonexistentMaterial,
            ChangeWindow, CheckTaskDetails, EditStatus, EditRemarks, SetPriority, AddTask,TaskDetails},
        data(){
            return{
                tasksInfo:{
                    state:'',
                    type:'',
                    file_name:'',
                    create_time:'',
                    supplier:'',
                    warehouse_type:0
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
                isSetting:false,
                isChange:false,
                isEditRemarks:false,
                isEditStatus:false,
                filter:'',
                isAdding:false,
                editData:{},
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
                descBy:'create_time'
            }
        },
        created(){
        },
        beforeDestroy(){
            Bus.$off('refreshTask');
        },
        mounted(){
            this.select();
            Bus.$on('refreshTask',() => {
                this.select();
            })
        },
        computed:{
            ...mapGetters([
                'user'
            ])
        },
        watch:{
            isSetting:function (val) {
                if(val === false){
                    this.select()
                }
            },
            isEditRemarks:function(val){
                if(val === false){
                    this.select();
                }
            },
            isEditStatus:function(val){
                if(val === false){
                    this.select();
                }
            },
            isAdding:function(val){
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
                        url:taskCarryBoxesTaskSelectUrl,
                        data:{
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            let data = res.data.data;
                            data.map((item,index) => {
                                item.showId = index + 1 + (this.pageNo - 1)*this.pageSize;
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
                this.tasksInfo.create_time = '';
                this.tasksInfo.file_name = '';
                this.tasksInfo.state = '';
                this.tasksInfo.supplier = '';
                this.tasksInfo.type = '';
                this.times = [];
                this.filter = '';
            },

            startPause:function(row){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:taskCarryBoxesTaskSwitchUrl,
                        data:{
                            taskId:row.id,
                            flag:!row.isRunning
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
            handleSet:function(row){
                if(row.stateString === "未开始" || row.stateString === "进行中"){
                    this.editData = row;
                    this.isSetting = true;
                }else{
                    this.$alertWarning("该状态不能设置优先级");
                }
            },
            handleEditRemark:function (row) {
                this.editData = row;
                this.isEditRemarks = true;
            },
            handleEditStatus:function (row) {
                this.editData = row;
                this.isEditStatus = true;
            },
            handleChangeWindow:function(row){
                this.editData = row;
                this.isChange = true;
            },

            showDetails:function(row){
                Bus.$emit('showTaskDetails',row);
            },
            showCheckTaskDetails:function(editData){
                Bus.$emit('showCheckTaskDetails',editData);
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            },
        }
    }
</script>

<style scoped lang="scss">
    .tasks{
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