<template>
    <div class="precious-details-item">
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
                    label="操作"
                    min-width="200"
            >
                <template slot-scope="scope">
                    <el-button type="primary"
                               @click="print(scope.row)"
                               size="small">
                        打印</el-button>
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
        coverPreciousUWMaterialByTaskIdUrl,
        coverPreciousUWMaterialUrl,
        getUwInventoryTaskDetailsUrl, printUrl
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    import EditQuantity from "./subscomp/EditQuantity";
    import {mapGetters} from 'vuex'
    export default {
        name: "UwDetailsItem",
        components: {EditQuantity},
        props:{
            row:Object
        },
        data(){
            return{
                tableData: [],
                isPending:false,
                editData:{},
                isEditing:false
            }
        },
        computed:{
            ...mapGetters([
                'user','configData'
            ])
        },
        mounted(){
            this.select();
        },
        watch:{
            isEditing:function(val){
                if(val === false){
                    this.select();
                }
            }
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: getUwInventoryTaskDetailsUrl,
                        data: {
                            taskId:this.row.task_id,
                            materialTypeId:this.row.material_type_id
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
                        url:coverPreciousUWMaterialUrl,
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
                        url:coverPreciousUWMaterialByTaskIdUrl,
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
            },
            print:function(row){
                let options = {
                    url: printUrl,
                    data: {
                        ip: this.configData.printerIP,
                        materialId: row.materialId,
                        quantity: row.atrualNum
                    }
                };
                axiosPost(options).then(response => {
                    if (response.data.result === 200) {
                        this.$alertSuccess('打印成功');
                    } else{
                        errHandler(response.data);
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            }
        }
    }
</script>

<style scoped lang="scss">

</style>