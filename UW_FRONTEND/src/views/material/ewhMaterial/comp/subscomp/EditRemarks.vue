<template>
    <el-dialog
            title="修改备注"
            :visible.sync="isEditing"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="备注">
                <el-input type="textarea" v-model.trim="remarks"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {editTaskRemarksUrl} from "../../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../../utils/fetchData";
    import {errHandler} from "../../../../../utils/errorHandler";

    export default {
        name: "EditRemarks",
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                remarks:''
            }
        },
        props:{
            isEditing:Boolean,
            editData:Object
        },
        watch:{
            isEditing:function (val) {
                if(val === true){
                    this.remarks = this.editData.remarks;
                }else{
                    this.remarks = '';
                }
            }
        },
        methods:{
            cancel:function () {
                this.remarks = '';
                this.$emit("update:isEditing",false);
            },
            submit:function () {
                if(this.remarks === ''){
                    this.$alertWarning('备注不能为空');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:editTaskRemarksUrl,
                        data:{
                            taskId:this.editData.taskId,
                            remarks:this.remarks
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('修改成功');
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