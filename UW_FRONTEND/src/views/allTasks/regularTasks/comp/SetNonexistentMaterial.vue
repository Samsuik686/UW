<template>
    <el-dialog
            @close="close"
            :title="name"
            :visible.sync="dialogVisible"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            width="90%">
        <p class="tip">*生成物料类型表时需填写规格、厚度、直径</p>
        <el-table
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="序号"
                    prop="serialNumber"
                    min-width="70"
            >
            </el-table-column>
            <el-table-column
                    label="料号"
                    prop="no">
            </el-table-column>
            <el-table-column
                    label="数量"
                    prop="quantity">
            </el-table-column>
            <el-table-column
                    label="规格"
                    prop="specification">
            </el-table-column>
            <el-table-column
                    label="厚度"
                    prop="thickness">
            </el-table-column>
            <el-table-column
                    label="直径"
                    prop="radius"
            >
            </el-table-column>
            <el-table-column
                    label="是否可超发"
                    prop="isSuperable"
                    :formatter="function(row, column, cellValue, index) {
                        return !!cellValue ? '是' : '否';
                    }"
            >
            </el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <el-button
                        type="primary"
                        :disabled="scope.row.type === 0"
                        size="small"
                        icon="el-icon-coke-edit"
                        @click="handleEdit(scope.$index,scope.row)"
                    >
                        生成物料类型
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="handleCreate">创建任务单</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import axios from '../../../../plugins/http'
    import Bus from '../../../../utils/bus.js'
    import {taskCreateRegularIOTaskUrl} from "../../../../plugins/globalUrl";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "SetNonexistentMaterial",
        data(){
            return{
                supplierName:'',
                dialogVisible:false,
                tableData: [],
                task:{},
                name:'',
                isPending:false
            }
        },
        mounted(){
            Bus.$on('setNonexistentMaterial',(fileName,suppliers,task,data) => {
                this.name = fileName + ' - 物料类型不存在清单';
                this.task = task;
                data.map((item,index) =>{
                    item['specification'] = '';
                    item['thickness'] = '';
                    item['radius'] = '';
                });
                this.tableData = data;
                this.dialogVisible = true;
                for(let i = 0 ;i<suppliers.length;i++){
                    if(suppliers[i].id === task.supplier){
                        this.supplierName = suppliers[i].name;
                        break;
                    }
                }
            });
            Bus.$on('closeMaterialDialog',(index,editInfo) => {
                Object.assign(this.tableData[index],editInfo);
            })
        },
        methods:{
            close:function(){
                this.name = '';
                this.task = {};
                this.tableData = [];
                this.supplierName = '';
                this.dialogVisible = false;
            },
            handleEdit:function(index,row){
                let obj = JSON.parse(JSON.stringify(row));
                obj['supplierId'] = this.task.supplier;
                obj['supplierName'] = this.supplierName;
                Bus.$emit('setMaterialDialog',index,obj);
            },
            submit:function(){
                let formData = new FormData();
                for(let i in this.task){
                    formData.append(i,this.task[i]);
                }
                formData.append('isForced',true);
                this.isPending = true;
                axios.post(taskCreateRegularIOTaskUrl, formData).then(res => {
                    if (res.data.result === 200) {
                        this.$alertSuccess('创建成功');
                        Bus.$emit('refreshTask',true);
                        this.close();
                    }else if(res.data.result === 413){
                        let data = res.data.data;
                        data.map((item,index) =>{
                            item['specification'] = '';
                            item['thickness'] = '';
                            item['radius'] = '';
                        });
                        this.tableData = data;
                    }else{
                        errHandler(res.data);
                    }
                }).catch(err => {
                    console.log(err);
                }).finally(() => {
                    this.isPending = false;
                })
            },
            handleCreate:function(){
                if(this.name === ''){
                    return;
                }
                this.$confirm('当前操作会忽略未生成物料类型的物料直接创建任务单，请确定是否继续操作', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    showClose:false,
                    center: false
                }).then((action) => {
                    if(action === 'confirm'){
                        this.submit();
                    }
                }).catch(() => {
                    this.$alertInfo("已取消");
                });
            }
        }
    }
</script>

<style scoped lang="scss">
 .tip{
     color:red;
 }
</style>