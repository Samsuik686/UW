<template>
    <el-dialog
            title="填写损耗数量"
            :visible.sync="isEditing"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form :model="editInfo" class="edit-form">
            <el-form-item label="数量">
                <el-input v-model.trim="editInfo.quantity" placeholder="数量"></el-input>
            </el-form-item>
            <el-form-item label="备注">
                <el-input  type="textarea" v-model.trim="editInfo.remarks" placeholder="备注"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {externalWhAddWorstageLogUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    import {handleEditWastage} from "../../../../utils/formValidate";

    export default {
        name: "EditWastage",
        props:{
            isEditing:Boolean,
            editData:Object
        },
        data(){
            return{
                isPending: false,
                isCloseOnModal: false,
                editInfo:{
                    remarks: '',
                    quantity: ''
                }
            }
        },
        methods:{
            cancel:function () {
                this.editInfo.remarks = '';
                this.editInfo.quantity = '';
                this.$emit("update:isEditing",false);
            },
            submit:function(){
                let result = handleEditWastage(this.editInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: externalWhAddWorstageLogUrl,
                        data: {
                            materialTypeId:this.editData.materialTypeId,
                            whId:this.editData.whId,
                            quantity: this.editInfo.quantity,
                            remarks: this.editInfo.remarks
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
        }
    }
</script>

<style scoped>

</style>