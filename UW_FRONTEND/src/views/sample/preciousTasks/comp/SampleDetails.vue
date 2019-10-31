<template>
    <el-dialog
            title="详细"
            :visible.sync="dialogVisible"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="90%">
        <el-table
                :data="tableData"
                style="width:100%;">
            <el-table-column type="expand">
                <template slot-scope="props">
                    <el-table
                            border
                            :data="props.row.list"
                            style="width:100%">
                        <el-table-column
                                prop="materialId"
                                label="料盘号">
                        </el-table-column>
                        <el-table-column
                                prop="quantity"
                                label="数量">
                        </el-table-column>
                        <el-table-column
                                prop="outTypeString"
                                label="出库类型">
                        </el-table-column>
                        <el-table-column
                                prop="operator"
                                label="操作员">
                        </el-table-column>
                        <el-table-column
                                prop="time"
                                label="出库时间">
                        </el-table-column>
                    </el-table>
                </template>
            </el-table-column>
            <el-table-column
                    prop="no"
                    label="料号">
            </el-table-column>
            <el-table-column
                    prop="storeQuantity"
                    label="库存数">
            </el-table-column>
            <el-table-column
                    prop="scanQuantity"
                    label="扫描总数">
            </el-table-column>
            <el-table-column
                    prop="singularOutQuantity"
                    label="异常出库数">
            </el-table-column>
            <el-table-column
                    prop="regularOutQuantity"
                    label="抽检出库数">
            </el-table-column>
            <el-table-column
                    prop="lostOutQuantity"
                    label="料盘丢失数">
            </el-table-column>
        </el-table>
    </el-dialog>
</template>

<script>
    import Bus from './../../../../utils/bus'
    import {getSampleTaskDetailsUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "TaskDetails",
        data(){
            return{
                isCloseOnModal:false,
                dialogVisible:false,
                taskId:'',
                tableData: [],
                isPending:false,
                isLoading:false
            }
        },
        beforeDestroy(){
            Bus.$off('showSampleDetails');
        },
        mounted(){
            Bus.$on('showSampleDetails',(row) => {
                this.taskId = row.id;
                this.tableData = [];
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
                        url: getSampleTaskDetailsUrl,
                        data: {
                            taskId:this.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            if (res.data.data.length>0) {
                                this.tableData = res.data.data;
                            } else {
                                this.tableData = [];
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
            }
        }
    }
</script>

<style scoped>

</style>