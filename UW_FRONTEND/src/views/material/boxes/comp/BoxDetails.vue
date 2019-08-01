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
                    label="料盘唯一码"
                    width="140"
                    prop="id"
            >
            </el-table-column>
            <el-table-column
                    label="类型"
                    prop="type">
            </el-table-column>
            <el-table-column
                    label="料号"
                    width="160"
                    prop="materialNo">
            </el-table-column>
            <el-table-column
                    label="所在料盒"
                    prop="box">
            </el-table-column>
            <el-table-column
                    label="料盒所在区域"
                    prop="boxArea">
            </el-table-column>
            <el-table-column
                    label="盒内行号"
                    prop="row">
            </el-table-column>
            <el-table-column
                    label="盒内列号"
                    prop="col">
            </el-table-column>
            <el-table-column
                    label="剩余数量"
                    prop="remainderQuantity">
            </el-table-column>
            <el-table-column
                    label="入库日期"
                    width="160"
                    prop="store_time">
            </el-table-column>
        </el-table>
        <div class="block">
            <el-pagination
                    background
                    :current-page.sync="pageNo"
                    :page-size.sync="pageSize"
                    :page-sizes="[20,40,80,100]"
                    @size-change="select"
                    @current-change="select"
                    layout="total,sizes,prev,pager,next,jumper"
                    :total="totallyData"
            >
            </el-pagination>
        </div>
    </el-dialog>
</template>

<script>
    import Bus from './../../../../utils/bus'
    import {materialEntityUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "BoxDetails",
        data(){
            return{
                isCloseOnModal:false,
                dialogVisible:false,
                box:'',
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false,
                isLoading:false
            }
        },
        beforeDestroy(){
            Bus.$off('showBoxDetails');
        },
        mounted(){
            Bus.$on('showBoxDetails',(row) => {
                this.box = row.id;
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
                        url: materialEntityUrl,
                        data: {
                            box: this.box,
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
            }
        }
    }
</script>

<style scoped lang="scss">
    .block {
        margin-top: 15px;
    }
</style>