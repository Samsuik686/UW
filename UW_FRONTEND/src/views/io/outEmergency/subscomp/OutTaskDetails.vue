<template>
    <div>
        <el-dialog
                @close="setClose"
                :close-on-click-modal="false"
                :close-on-press-escape="false"
                :visible="isShow"
                width="90%">
            <div class="task-item">
                <div class="item">
                    <el-form  inline label-position="left" class="item-form" size="medium">
                        <el-form-item label="料号">
                            <span>{{taskItem.no}}</span>
                        </el-form-item>
                        <el-form-item label="客户">
                            <span>{{taskItem.supplier}}</span>
                        </el-form-item>
                        <el-form-item label="计划">
                            <span>{{taskItem.planQuantity}}</span>
                        </el-form-item>
                        <el-form-item label="实际">
                            <span>{{taskItem.actuallyQuantity}}</span>
                        </el-form-item>
                        <el-form-item label="库存">
                            <span>{{taskItem.storeQuantity}}</span>
                        </el-form-item>
                        <el-form-item label="本次缺发数量/超发数量">
                            <span>{{taskItem.lackQuantity}}</span>
                        </el-form-item>
                        <el-form-item label="已扫料盘">
                            <span>{{taskItem.scanNum}}</span>
                        </el-form-item>
                        <el-form-item label="规格" style="width:100%">
                            <span>{{taskItem.specification}}</span>
                        </el-form-item>
                    </el-form>
                </div>
                <el-divider></el-divider>
                <el-table
                        border
                        :data="taskItem.infos"
                        style="width:100%">
                    <el-table-column
                            prop="materialId"
                            label="料盘">
                    </el-table-column>
                    <el-table-column
                            prop="taskLogQuantity"
                            label="出库数量">
                    </el-table-column>
                    <el-table-column
                            prop="productionTime"
                            label="打印日期">
                    </el-table-column>
                    <el-table-column
                            label="操作">
                        <template slot-scope="scope">
                            <el-button
                                    @click="handleDelete(scope.row)"
                                    size="mini"
                                    type="danger">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import {
        deleteEmergencyRegularMaterialRecordUrl,
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "OutTaskDetails",
        data(){
            return{
                isCloseOnModal:false,
                isEdit:false,
                editData:{},
                cutData:[]
            }
        },
        props:{
            taskItem:Object,
            isShow:Boolean
        },
        methods:{
            setClose:function () {
                this.$emit("update:isShow",false);
            },
            handleDelete:function (row) {
                this.$confirm('你正在删除料盘唯一码为'+row.materialId+ '的扫描记录，请确认是否删除?', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: false
                }).then((action) => {
                    if (action === "confirm") {
                        this.deleteMaterialRecord(row.materialId);
                    }
                }).catch(() => {
                    this.$alertInfo("已取消删除");
                });
            },
            deleteMaterialRecord:function(materialId){
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: deleteEmergencyRegularMaterialRecordUrl,
                        data: {
                            taskLogId: this.taskItem.taskLogId,
                            materialId: materialId
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess("删除成功");
                            this.$emit('refreshData',true);
                        } else {
                            errHandler(response.data);
                        }
                    }).catch(err => {
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .task-item{
        width:100%;
        min-height:500px;
        box-sizing: border-box;
        border: 1px solid #ccc;
        border-radius: 3px;
        padding:20px 30px;
        .item{
            display:flex;
            .item-form{
                font-size: 0;
                margin-bottom:10px;
                .el-form-item{
                    width:45%;
                    margin-right:20px;
                    .el-form-item__label {
                        width:90px;
                        color: #99a9bf;
                    }
                }
            }
        }
    }
</style>