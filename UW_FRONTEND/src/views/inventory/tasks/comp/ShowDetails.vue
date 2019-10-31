<template>
    <el-dialog
            title="详细"
            :visible.sync="dialogVisible"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            width="95%">
        <el-table
                v-loading="isLoading"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="仓位"
                    prop="destinationName">
            </el-table-column>
            <el-table-column
                    label="审核人员"
                    prop="checkOperator">
            </el-table-column>
            <el-table-column
                    label="审核时间"
                    prop="checkTime">
            </el-table-column>
            <el-table-column
                    label="完成人员"
                    prop="finishOperator">
            </el-table-column>
            <el-table-column
                    label="完成时间"
                    prop="finishTime">
            </el-table-column>
        </el-table>
    </el-dialog>
</template>

<script>
    import Bus from './../../../../utils/bus'
    import {getInventoryTaskBaseInfoUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "ShowDetails",
        data(){
            return{
                dialogVisible:false,
                isPending:false,
                tableData:[],
                isLoading:false
            }
        },
        beforeDestroy(){
            Bus.$off('showTaskDetails');
        },
        mounted(){
            Bus.$on('showTaskDetails',(row) => {
                this.getInventoryTaskBaseInfo(row.taskId);
                this.dialogVisible = true;
            })
        },
        methods:{
            getInventoryTaskBaseInfo:function(taskId){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:getInventoryTaskBaseInfoUrl,
                        data:{
                            taskId:taskId,
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tableData = res.data.data;
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
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