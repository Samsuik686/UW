<template>
    <el-dialog
            title="设置优先级"
            :visible.sync="isSetting"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="优先级">
                <el-select v-model.trim="thisPriority" placeholder="优先级" value="" style="width:100%">
                    <el-option  label="紧急" value='0'></el-option>
                    <el-option  label="正常" value='1'></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {taskSetPriorityUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "SetPriority",
        props:{
            isSetting:Boolean,
            editData:Object
        },
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                thisPriority:''
            }
        },
        methods:{
            cancel:function(){
                this.thisPriority = '';
                this.$emit("update:isSetting",false);
            },
            submit:function(){
                if(this.thisPriority === ""){
                    this.$alertWarning("请选择优先级");
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url:taskSetPriorityUrl ,
                        data: {
                            id: this.editData.id,
                            priority:this.thisPriority
                        }
                    };
                    axiosPost(options).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess('设置成功');
                            this.cancel();
                        } else {
                            errHandler(res.data)
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试')
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