<template>
    <el-dialog
            title="详细"
            @close="no = ''"
            :visible.sync="dialogVisible"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="90%">
        <el-button icon="el-icon-refresh" circle style="float: right; margin-right: 20px" @click="select"></el-button>
        <el-table
                v-loading="isLoading"
                :data="tableData"
                stripe
                style="width:100%">
            <el-table-column
                    label="条目id"
                    prop="id"
                    width="80">
            </el-table-column>
            <el-table-column
                    label="绑定次条目id"
                    prop="bindId"
                    width="80">
            </el-table-column>
            <el-table-column
                    label="状态"
                    prop="statusString"
                    width="80">
            </el-table-column>
            <el-table-column
                    label="绑定AGV编号"
                    width="80"
                    prop="robotId">
            </el-table-column>
            <el-table-column
                    label="初始行"
                    prop="x1">
            </el-table-column>
            <el-table-column
                    label="初始列"
                    prop="y1">
            </el-table-column>
            <el-table-column
                    label="初始高"
                    prop="z1">
            </el-table-column>
            <el-table-column
                    label="目标行"
                    prop="x2">
            </el-table-column>
            <el-table-column
                    label="目标列"
                    prop="y2">
            </el-table-column>
            <el-table-column
                    label="目标高"
                    prop="z2">
            </el-table-column>
        </el-table>
    </el-dialog>
</template>

<script>
    import Bus from '../../../../utils/bus'
    import {taskCarryBoxesTaskSelectDetailsUrl} from "../../../../plugins/globalUrl";
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
                isLoading:false,
                no:''
            }
        },
        beforeDestroy(){
            Bus.$off('showTaskDetails');
        },
        mounted(){
            Bus.$on('showTaskDetails',(row) => {
                this.id = row.id;
                this.select();
                this.dialogVisible = true;
                refresh();
            });
            let refresh = () => {
                setTimeout(() => {
                    this.select();
                }, 1000);
                if (this.dialogVisible) {
                    refresh();
                }
            }
        },
        methods:{
            select:function () {
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url: taskCarryBoxesTaskSelectDetailsUrl,
                        data: {
                            taskId:this.id,
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            if (res.data.data !== null) {
                                this.tableData = res.data.data;
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