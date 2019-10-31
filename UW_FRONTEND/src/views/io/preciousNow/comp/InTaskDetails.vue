<template>
    <div>
        <el-dialog
                @close="setClose"
                :close-on-click-modal="isCloseOnModal"
                :close-on-press-escape="isCloseOnModal"
                :visible="isInShow"
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
                        <el-form-item label="本次欠入数量/超入数量">
                            <span>{{taskItem.lackQuantity}}</span>
                        </el-form-item>
                        <el-form-item label="已扫料盘">
                            <span>{{taskItem.scanNum}}</span>
                        </el-form-item>
                        <el-form-item label="规格" style="width:100%">
                            <span>{{taskItem.specification}}</span>
                        </el-form-item>
                    </el-form>
                    <div class="item-operation">
                        <div class="operation-img">
                            <img src="./../../../../assets/finishedQRCode.png" alt="finished" class="img-style">
                        </div>
                        <span class="operation-text">* 扫描此二维码或点击按钮以完成操作</span>
                        <div style="display:flex;justify-content:center">
                            <el-button
                                    @click="finish"
                                    size="small"
                                    type="primary">操作完毕</el-button>
                        </div>
                    </div>
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
                            prop="materialQuantity"
                            label="原始数量">
                    </el-table-column>
                    <el-table-column
                            prop="taskLogQuantity"
                            label="出库数量">
                    </el-table-column>
                    <el-table-column
                            prop="productionTime"
                            label="生产日期">
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
        finishPreciousTaskItemUrl,
        taskDeletePreciousMaterialRecordUrl
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "InTaskDetails",
        data(){
            return{
                isCloseOnModal:false
            }
        },
        props:{
            taskItem:Object,
            isInShow:Boolean,
            finishType:String
        },
        watch:{
            finishType:function(val){
                if(val === '入库'){
                    this.finish();
                    this.$emit("update:finishType",'');
                }
            }
        },
        methods:{
            setClose:function () {
                this.$emit("update:isInShow",false);
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
                        url: taskDeletePreciousMaterialRecordUrl,
                        data: {
                            packingListItemId: this.taskItem.packingListItemId,
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
            },
            finish:function(){
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: finishPreciousTaskItemUrl,
                        data: {
                            packingListItemId: this.taskItem.packingListItemId,
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess("操作成功");
                            this.setClose();
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
                flex:2;
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
            .item-operation{
                flex:1;
                margin-top:5px;
                text-align:center;
                .operation-img{
                    width:50%;
                    margin:0 auto 10px;
                    .img-style {
                        width:150px;
                        height:auto;
                        text-align:center;
                    }
                }
                .operation-text{
                    display:block;
                    margin-bottom:10px;
                }
            }
        }
    }
</style>