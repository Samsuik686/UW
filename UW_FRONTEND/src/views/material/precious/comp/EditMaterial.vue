<template>
    <el-dialog
            title="更新物料类型"
            :visible.sync="isEditing"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="65%">
        <el-form :inline="true" :model="editInfo" class="edit-form">
            <el-form-item label="料号">
                <el-input v-model.trim="editInfo.no" placeholder="料号" disabled></el-input>
            </el-form-item>
            <el-form-item label="客户">
                <el-input v-model.trim="editInfo.supplierName" placeholder="客户" disabled></el-input>
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
            <el-form-item label="位号">
                <el-input placeholder="位号" v-model.trim="editInfo.designator"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {handleUwMaterial} from "../../../../utils/formValidate";
    import {materialUpdateUrl, updatePreciousMaterialTypeUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "EditMaterial",
        props:{
            isEditing:Boolean,
            editData:Object
        },
        data(){
            return{
                isPending: false,
                isCloseOnModal: false,
                editInfo:{
                    id: '',
                    specification: '',
                    no: '',
                    supplierName:'',
                    thickness: '',
                    radius: '',
                    designator:''
                }
            }
        },
        watch: {
            isEditing: function (val) {
                if (val === true) {
                    this.editInfo.id = this.editData.id;
                    this.editInfo.no = this.editData.no;
                    this.editInfo.specification = this.editData.specification;
                    this.editInfo.supplierName = this.editData.supplierName;
                    this.editInfo.thickness = this.editData.thickness;
                    this.editInfo.radius = this.editData.radius;
                    this.editInfo.designator = this.editData.designator;
                } else {
                    this.clearForm();
                }
            }
        },
        methods:{
            cancel:function () {
                this.$emit("update:isEditing",false);
            },
            submit:function(){
                let result = handleUwMaterial(this.editInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(this.editInfo.designator === ''){
                    this.$alertWarning('位号不能为空');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: updatePreciousMaterialTypeUrl,
                        data: {
                            id: this.editInfo.id,
                            enabled: 1,
                            thickness:this.editInfo.thickness,
                            radius:this.editInfo.radius,
                            designator:this.editInfo.designator,
                            specification:this.editInfo.specification
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('更新成功');
                            this.cancel();
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
                this.editInfo.id = '';
                this.editInfo.thickness = '';
                this.editInfo.supplierName = '';
                this.editInfo.specification = '';
                this.editInfo.no = '';
                this.editInfo.radius = '';
                this.editInfo.designator = '';
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