<template>
    <el-dialog
            title="正在审核"
            :visible.sync="dialogVisible"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="90%">
        <el-table
                v-loading="isLoading"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="料号"
                    prop="materialNo"
                    min-width="120"
            >
            </el-table-column>
            <el-table-column
                    label="计划数量"
                    prop="planQuantity">
            </el-table-column>
            <el-table-column
                    label="UW库存"
                    prop="uwStoreNum">
            </el-table-column>
            <el-table-column
                    label="物料仓库存"
                    prop="whStoreNum">
            </el-table-column>
            <el-table-column
                    label="实际数量"
                    prop="actualQuantity">
            </el-table-column>
            <el-table-column
                    label="状态">
                <template slot-scope="scope">
                    <high-light :row="scope.row"></high-light>
                </template>
            </el-table-column>
            <el-table-column
                    label="完成时间"
                    prop="finishTime"
                    min-width="160"
            >
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
        <span slot="footer" class="dialog-footer">
            <el-button type="primary" size="mini" @click="taskPass">提交</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import Bus from './../../../utils/bus'
    import {taskCheckUrl, taskUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import ShowStatus from "./subscomp/ShowStatus";
    import HighLight from "./subscomp/HighLight";
    import bus from "../../../utils/bus";
    export default {
        name: "CheckTaskDetails",
        components: {HighLight, ShowStatus},
        data(){
            return{
                isCloseOnModal:false,
                dialogVisible:false,
                id:'',
                type:'',
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false,
                isLoading:false
            }
        },
        beforeDestroy(){
            Bus.$off('showCheckTaskDetails');
        },
        mounted(){
            Bus.$on('showCheckTaskDetails',(row) => {
                this.type = row.type;
                this.id = row.id;
                this.select();
                this.dialogVisible = true;
            })
        },
        methods:{
            select:function () {
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url: taskCheckUrl,
                        data: {
                            type:this.type,
                            id:this.id,
                            pageNo: this.pageNo,
                            pageSize: this.pageSize
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            if (res.data.data.list !== null) {
                                this.tableData = res.data.data.list;
                                this.totallyData = res.data.data.totalRow;
                            } else {
                                this.tableData = [];
                                this.totallyData = 0;
                            }
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
            taskPass:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: taskUrl + '/pass',
                        data: {
                            id:this.id
                        }
                    };
                    axiosPost(options).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess('设置成功');
                            Bus.$emit('refreshTask',true);
                            this.dialogVisible = false;
                        } else {
                            errHandler(res.data)
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            }
        }
    }
</script>

<style scoped lang="scss">
    .block {
        margin-top: 15px;
    }
</style>