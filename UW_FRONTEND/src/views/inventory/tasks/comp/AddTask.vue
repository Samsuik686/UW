<template>
    <el-dialog
            title="创建任务"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="供应商">
                <el-select v-model.trim="supplierId" placeholder="供应商" value="" style="width:100%">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
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
    import {errHandler} from "../../../../utils/errorHandler";
    import {createInventoryTaskUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";

    export default {
        name: "AddTask",
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                supplierId:''
            }
        },
        props:{
            isAdding:Boolean,
            suppliers:Array,
        },
        methods:{
            cancel:function(){
                this.supplierId = '';
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                if (this.supplierId === '') {
                    this.$alertWarning('供应商不能为空');
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: createInventoryTaskUrl,
                        data: {
                            supplierId: this.supplierId
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess('创建任务成功');
                            this.cancel();
                        } else {
                            errHandler(response.data);
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