<template>
    <el-dialog
            title="添加料盒"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="50%">
        <el-form :inline="true" :model="addInfo" class="add-form" label-position="top">
            <el-form-item label="区域">
                <el-select v-model.trim="addInfo.area" placeholder="请选择" style="width:100%" value="">
                    <el-option label="A" value='A'></el-option>
                    <el-option label="B" value='B'></el-option>
                    <el-option label="C" value='C'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="料盒类型">
                <el-select v-model.trim="isStandard" placeholder="请选择" style="width:100%" value="">
                    <el-option label="标准" value='1'></el-option>
                    <el-option label="非标准" value='0'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="客户">
                <el-select v-model.trim="addInfo.supplierId" placeholder="客户" value="">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="行">
                <el-input v-model.trim="addInfo.row" placeholder="行"></el-input>
            </el-form-item>
            <el-form-item label="列">
                <el-input v-model.trim="addInfo.col" placeholder="列"></el-input>
            </el-form-item>
            <el-form-item label="高度">
                <el-input v-model.trim="addInfo.height" placeholder="高度"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {axiosPost} from "../../../../utils/fetchData";
    import {addBoxUrl} from "../../../../plugins/globalUrl";
    import {errHandler} from "../../../../utils/errorHandler";
    import {addBox} from "../../../../utils/formValidate";

    export default {
        name: "AddBox",
        props:{
            isAdding:Boolean,
            suppliers:Array
        },
        data(){
            return{
                isPending: false,
                isCloseOnModal: false,
                addInfo:{
                    area:'',//区域
                    row:'',//行
                    col:'',//列
                    height:'',//高度
                    isStandard:'',//是否为标准料盒
                    supplierId:'',//客户Id
                },
                isStandard:''
            }
        },
        watch:{
            isStandard:function(val){
                if(val !== ''){
                    this.addInfo.isStandard = val === '1';
                }else{
                    this.addInfo.isStandard = '';
                }
            }
        },
        methods:{
            cancel:function(){
                this.addInfo.isStandard = '';
                this.isStandard = '';
                this.addInfo.supplierId = '';
                this.addInfo.area = '';
                this.addInfo.col = '';
                this.addInfo.row = '';
                this.addInfo.height = '';
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                let result = addBox(this.addInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: addBoxUrl,
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