<template>
    <el-dialog
            title="更新料盒在架情况"
            :visible.sync="isEditing"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="料盒是否在架">
                <el-select v-model.trim="isOnShelf" placeholder="请选择" style="width:100%" value="">
                    <el-option label="是" value='1'></el-option>
                    <el-option label="否" value='0'></el-option>
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
    import {updateBoxUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "EditBox",
        props:{
            isEditing:Boolean,
            editData:Object
        },
        watch:{
            isEditing:function (val) {
                if(val === true){
                    this.id = this.editData.id;
                    this.isOnShelf =(this.editData.isOnShelfString === '是') ? '1' : '0';
                }else{
                    this.id = '';
                    this.isOnShelf = 0;
                }
            }
        },
        data(){
            return{
                isPending: false,
                isCloseOnModal: false,
                id:'',
                isOnShelf:0
            }
        },
        methods:{
            cancel:function(){
                this.$emit("update:isEditing",false);
            },
            submit:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: updateBoxUrl,
                        data: {
                            id:this.id,
                            isOnShelf:this.isOnShelf
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