<template>
    <el-dialog
            title="更新客户名"
            :visible.sync="isEditing"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="客户名">
                <el-input v-model.trim="name" placeholder="客户名"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {supplierChangeNameUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    import {judgeCodeLen256, judgeCodeLen32} from "../../../../utils/formValidate";

    export default {
        name: "EditSupplier",
        props:{
            isEditing:Boolean,
            editData:Object
        },
        data(){
            return{
                isPending: false,
                isCloseOnModal: false,
                id:'',
                name:''
            }
        },
        watch:{
            isEditing:function (val) {
                if(val === true){
                    this.id = this.editData.id;
                    this.name = this.editData.name;
                }else{
                    this.id = '';
                    this.name = '';
                }
            }
        },
        methods:{
            cancel:function(){
                this.$emit("update:isEditing",false);
            },
            submit:function(){
                if(this.name === ''){
                    this.$alertWarning('客户名不能为空');
                    return;
                }
                if(!judgeCodeLen256(this.name)){
                    this.$alertWarning('客户名过长');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: supplierChangeNameUrl,
                        data: {
                            id:this.id,
                            name:this.name
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
            }
        }
    }
</script>

<style scoped>

</style>