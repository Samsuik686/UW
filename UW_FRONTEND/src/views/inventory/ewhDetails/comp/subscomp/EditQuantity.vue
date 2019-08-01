<template>
    <el-dialog
            title="修改数量"
            :visible.sync="isEditing"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="40%">
        <el-form :inline="true" :model="editInfo" class="edit-form">
            <el-form-item label="盘点数量">
                <el-input v-model.trim="editInfo.atrualNum" placeholder="盘点数量"></el-input>
            </el-form-item>
            <el-form-item label="盘前盈亏">
                <el-input v-model.trim="editInfo.returnNum" placeholder="盘前盈亏"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {handleEditQuantity} from "../../../../../utils/formValidate";
    import {editEwhInventoryLogUrl} from "../../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../../utils/fetchData";
    import {errHandler} from "../../../../../utils/errorHandler";

    export default {
        name: "EditQuantity",
        props:{
            isEditing:Boolean,
            editData:Object
        },
        data(){
            return{
                isPending: false,
                isCloseOnModal: false,
                editInfo:{
                    atrualNum:'',
                    returnNum:''
                }
            }
        },
        watch: {
            isEditing: function (val) {
                if (val === true) {
                    this.editInfo.atrualNum = this.editData.atrualNum;
                    this.editInfo.returnNum = this.editData.materialreturnNum;
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
                let result = handleEditQuantity(this.editInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: editEwhInventoryLogUrl,
                        data: {
                            id:this.editData.id,
                            acturalNum:this.editInfo.atrualNum,
                            returnNum:this.editInfo.returnNum
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
                this.editInfo.returnNum = '';
                this.editInfo.atrualNum = '';
            }
        }
    }
</script>

<style scoped lang="scss">
    .edit-form{
        .el-form-item{
            width:45%;
        }
    }
</style>