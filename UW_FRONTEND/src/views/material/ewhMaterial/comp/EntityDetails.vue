<template>
    <div class="ewhMaterial-details">
        <el-table
                border
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="任务"
                    prop="taskName"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="类型"
                    prop="taskTypeString"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="来源地"
                    prop="sourceWhName"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="目的地"
                    prop="destinationName"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="数量"
                    prop="quantity"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="调拨多出数量"
                    prop="returnNum"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="日期"
                    prop="time"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="备注"
                    prop="remarks"
                    align="center">
            </el-table-column>
            <el-table-column
                    label="操作"
                    align="center">
                <template slot-scope="scope">
                    <span style="margin-right:10px;cursor: pointer;" title="修改备注" @click="handleEdit(scope.row)">
                        <i class="el-icon-coke-edit"></i>
                    </span>
                    <span style="cursor: pointer" title="删除" @click="handleDelete(scope.row)" v-if="scope.row.taskType === 6">
                        <i class="el-icon-coke-cancel"></i>
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
        <edit-remarks :is-editing.sync="isEditing" :edit-data="editData"></edit-remarks>
    </div>
</template>

<script>
    import {
        externalWhDeleteExternalWhLogUrl,
        externalWhSelectEWhMaterialDetailsUrl
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    import EditRemarks from "./subscomp/EditRemarks";

    export default {
        name: "EntityDetails",
        components: {EditRemarks},
        props:{
            row:Object
        },
        data(){
            return{
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false,
                isEditing:false,
                editData:{}
            }
        },
        watch: {
            isEditing: function (val) {
                if (val === false) {
                    this.select();
                }
            }
        },
        mounted(){
            this.select();
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: externalWhSelectEWhMaterialDetailsUrl,
                        data: {
                            materialTypeId:this.row.materialTypeId,
                            whId:this.row.whId,
                            pageNo:this.pageNo,
                            pageSize:this.pageSize
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tableData = res.data.data.list;
                            this.tableData.map(item => {
                                if(item.taskType === 1 && item.quantity > 0){
                                    item.taskTypeString = "出库超发";
                                }
                                if(item.taskType === 1 && item.quantity < 0){
                                    item.taskTypeString = "出库抵扣";
                                }
                            });
                            this.totallyData = res.data.data.totalRow;
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            handleEdit:function(row){
                this.editData = row;
                this.isEditing = true;
            },
            handleDelete: function (row) {
                this.$confirm('你正在删除任务为“'+row.taskName+'”的任务条目，请确认是否删除', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    showClose:false,
                    center: false
                }).then((action) => {
                    if (action === "confirm") {
                        this.submitDelete(row);
                    }
                }).catch(() => {
                    this.$alertInfo("已取消删除");
                });
            },
            submitDelete:function(row){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: externalWhDeleteExternalWhLogUrl,
                        data:{
                            logId:row.id
                        },
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('删除成功');
                            this.isPending = false;
                            this.select();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
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
    .block{
        margin-top:15px;
    }
</style>