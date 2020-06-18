<template>
    <el-dialog
            title="添加物料类型"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="50%">
        <el-form :inline="true" :model="addInfo" class="add-form" label-position="top">
            <el-form-item label="料号">
                <el-input v-model.trim="addInfo.no" placeholder="料号"></el-input>
            </el-form-item>
            <el-form-item label="规格">
                <el-input v-model.trim="addInfo.specification" placeholder="规格"></el-input>
            </el-form-item>
            <el-form-item label="客户">
                <el-select v-model.trim="addInfo.supplierId" placeholder="客户" value="">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="厚度">
                <el-input v-model.trim="addInfo.thickness" placeholder="厚度"></el-input>
            </el-form-item>
            <el-form-item label="直径">
                <el-input v-model.trim="addInfo.radius" placeholder="直径"></el-input>
            </el-form-item>
            <el-form-item label="是否可超发">
                <el-checkbox v-model.trim="addInfo.isSuperable" placeholder="是否可超发"></el-checkbox>
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
    import {addRegularMaterialTypeUrl, materialAddUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "AddMaterial",
        props:{
            suppliers:Array,
            isAdding:Boolean
        },
        data(){
            return{
                addInfo:{
                    type:0,
                    no:'',
                    specification: '',
                    supplierId:'',
                    thickness:'',
                    radius:'',
                    isSuperable: false,
                },
                isPending: false,
                isCloseOnModal: false,
            }
        },
        methods:{
            cancel:function(){
                this.addInfo.radius = '';
                this.addInfo.thickness = '';
                this.addInfo.specification = '';
                this.addInfo.no = '';
                this.addInfo.supplierId = '';
                this.addInfo.isSuperable = false;
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                let result = handleUwMaterial(this.addInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: addRegularMaterialTypeUrl,
                        data:this.addInfo
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('添加成功');
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
            }
        }
    }
</script>

<style scoped lang="scss">
    .add-form{
        .el-form-item{
            width:30%;
        }
    }
</style>