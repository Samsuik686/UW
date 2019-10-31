<template>
    <el-dialog
            title="详细"
            :visible.sync="dialogVisible"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="90%">
        <el-table
                v-loading="isLoading"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    type="expand">
                <template slot-scope="props">
                    <div v-if="props.row.details.length === 0">
                        <p style="text-align:center">无数据</p>
                    </div>
                    <div v-else>
                        <div v-for="(item,index) in props.row.details" :key="index">
                            <div class="materialId-box">
                                <div class="box-item"><span>料盘唯一码</span><span>{{item.materialId}}</span></div>
                                <div class="box-item"><span>数量</span><span>{{item.quantity}}</span></div>
                            </div>
                        </div>
                    </div>
                </template>
            </el-table-column>
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
                    label="实际数量"
                    prop="actualQuantity">
            </el-table-column>
            <el-table-column
                    label="欠料数量"
                    prop="lackNum">
            </el-table-column>
            <el-table-column
                    label="完成时间"
                    prop="finishTime"
                    min-width="160"
            >
            </el-table-column>
            <el-table-column
                    label="状态">
                <template slot-scope="scope">
                    <show-status :row="scope.row"></show-status>
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
    </el-dialog>
</template>

<script>
    import Bus from '../../../../utils/bus'
    import {taskGetIOTaskDetailsUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    import ShowStatus from "./subscomp/ShowStatus";
    export default {
        name: "TaskDetails",
        components: {ShowStatus},
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
            Bus.$off('showTaskDetails');
        },
        mounted(){
            Bus.$on('showTaskDetails',(row) => {
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
                        url: taskGetIOTaskDetailsUrl,
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
                                this.tableData.map(item => {
                                    if(item.finishTime === 'no'){
                                        item.finishTime = '等待操作';
                                    }
                                });
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
    .materialId-box {
        display: flex;
    }
    .box-item{
        margin-right:100px;
    }
    .box-item span:first-of-type {
        display: inline-block;
        color: #99a9bf;
        margin-right: 20px;
    }
</style>