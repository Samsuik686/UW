<template>
    <div class="ewh-details-item">
        <el-table
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="所在仓位"
                    prop="whName">
            </el-table-column>
            <el-table-column
                    label="料盘唯一码"
                    min-width="120"
                    prop="materialId">
            </el-table-column>
            <el-table-column
                    label="盘前库存"
                    prop="beforeNum">
            </el-table-column>
            <el-table-column
                    label="盘点数量"
                    prop="atrualNum">
            </el-table-column>
            <el-table-column
                    label="盘盈/盘亏"
                    prop="differentNum">
            </el-table-column>
            <el-table-column
                    label="调拨多出数量"
                    prop="materialreturnNum">
            </el-table-column>
            <el-table-column
                    label="盘点人"
                    prop="inventoryOperatior">
            </el-table-column>
            <el-table-column
                    label="盘点时间"
                    min-width="160"
                    prop="inventoryTime">
            </el-table-column>
            <el-table-column
                    label="平仓人"
                    prop="coverOperatior">
            </el-table-column>
            <el-table-column
                    label="平仓时间"
                    min-width="160"
                    prop="coverTime">
            </el-table-column>
            <el-table-column
                    min-width="150"
                    label="操作"
            >
                <template slot-scope="scope">
                    <el-button type="primary"
                               @click="handleEdit(scope.row)"
                               size="small">
                        修改</el-button>
                    <el-button type="primary"
                               size="small"
                               @click="closePosition(scope.row)"
                               v-if="scope.row.isChecked && scope.row.coverTime === null && scope.row.isFinished === false">
                        平仓</el-button>
                </template>
            </el-table-column>
        </el-table>
        <div style="width:100%;text-align:right;margin-top:8px;">
            <el-button type="primary" @click="allClosePosition" size="small">一键平仓</el-button>
        </div>
        <edit-quantity :is-editing.sync="isEditing" :edit-data="editData"></edit-quantity>
    </div>
</template>

<script>
    import {
        coverRegularEwhMaterialByTaskIdUrl,
        coverRegularEWhMaterialUrl,
        getEwhInventoryTaskDetailsUrl,
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    import EditQuantity from "./subscomp/EditQuantity";

    export default {
        name: "EwhDetailsItem",
        components: {EditQuantity},
        props:{
            row:Object,
            whId:Number
        },
        data(){
            return{
                tableData: [],
                isPending:false,
                isEditing:false,
                editData:{}
            }
        },
        watch:{
            isEditing:function(val){
                if(val === false){
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
                        url: getEwhInventoryTaskDetailsUrl,
                        data: {
                            taskId:this.row.task_id,
                            materialTypeId:this.row.material_type_id,
                            whId:this.whId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tableData = res.data.data;
                            this.tableData.map((item) => {
                                item.isChecked = this.row.checked_time !== null;
                                item.isFinished = this.row.state === 3;
                            })
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
            closePosition:function(row){
                if(!this.isPending){
                    this.isPending = false;
                    let options = {
                        url:coverRegularEWhMaterialUrl,
                        data:{
                            id:row.id,
                            taskId:row.taskId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('平仓成功');
                            this.isPending = false;
                            this.select();
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
            allClosePosition:function(){
                if(!this.isPending){
                    this.isPending = false;
                    let options = {
                        url:coverRegularEwhMaterialByTaskIdUrl,
                        data:{
                            materialTypeId:this.row.material_type_id,
                            taskId:this.row.task_id
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('一键平仓成功');
                            this.isPending = false;
                            this.select();
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
            }
        }
    }
</script>

<style scoped lang="scss">

</style>