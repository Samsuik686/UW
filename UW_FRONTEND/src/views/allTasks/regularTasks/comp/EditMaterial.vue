<template>
    <el-dialog
            title="生成物料类型"
            :visible.sync="dialogVisible"
            :show-close="false"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            width="65%">
        <el-form :inline="true" :model="editInfo" class="edit-form">
            <el-form-item label="料号">
                <el-input v-model.trim="editInfo.no" placeholder="料号" disabled></el-input>
            </el-form-item>
            <el-form-item label="客户">
                <el-input v-model.trim="supplierName" placeholder="客户" disabled></el-input>
            </el-form-item>
            <el-form-item label="规格">
                <el-input v-model.trim="editInfo.specification" placeholder="规格"></el-input>
            </el-form-item>
            <el-form-item label="厚度">
                <el-input v-model.trim="editInfo.thickness" placeholder="厚度"></el-input>
            </el-form-item>
            <el-form-item label="直径">
                <el-input v-model.trim="editInfo.radius" placeholder="直径"></el-input>
            </el-form-item>
            <el-form-item label="是否可超发">
                <el-checkbox v-model.trim="editInfo.isSuperable" placeholder="是否可超发"></el-checkbox>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import Bus from '../../../../utils/bus.js'
    import {handleUwMaterial} from "../../../../utils/formValidate";
    import {addRegularMaterialTypeUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "EditMaterial",
        data(){
            return{
                index:'',
                dialogVisible:false,
                supplierName:'',
                editInfo:{
                    type:0,
                    no:'',
                    specification: '',
                    supplierId:'',
                    thickness:'',
                    radius:'',
                    isSuperable: ''
                },
                isPending:false
            }
        },
        mounted(){
            Bus.$on('setMaterialDialog',(index,editData)=>{
                this.editInfo.no = editData.no;
                this.editInfo.specification = editData.specification;
                this.supplierName = editData.supplierName;
                this.editInfo.thickness = editData.thickness;
                this.editInfo.radius = editData.radius;
                this.editInfo.supplierId = editData.supplierId;
                this.editInfo.isSuperable = Boolean(editData.isSuperable);
                this.dialogVisible = true;
                this.index = index;
            });
        },
        methods:{
            cancel:function () {
                this.clearForm();
            },
            submit:function(){
                let result = handleUwMaterial(this.editInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: addRegularMaterialTypeUrl,
                        data:this.editInfo
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('添加成功');
                            Bus.$emit('closeMaterialDialog',this.index,this.editInfo);
                            this.clearForm();
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
            clearForm:function(){
                this.editInfo.thickness = '';
                this.supplierName = '';
                this.editInfo.specification = '';
                this.editInfo.no = '';
                this.editInfo.radius = '';
                this.editInfo.supplierId = '';
                this.editInfo.isSuperable = false;
                this.index = '';
                this.dialogVisible = false;
            }
        }
    }
</script>

<style scoped lang="scss">
    .edit-form{
        .el-form-item{
            width:30%;
        }
    }
</style>