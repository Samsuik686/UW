<template>
    <el-dialog
            title="批量修改料盒客户"
            :visible.sync="isBatchEditSupplier"
            :show-close="false"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            width="30%">
        <el-form>
            <el-form-item label="客户">
                <el-select v-model.trim="supplierId" placeholder="请选择" style="width:100%" value="">
                    <el-option v-for="item in suppliers" :label="item.name" :value='item.id'></el-option>
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
    import {editBoxOfSupplierUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "BatchEditSupplier",
        data(){
            return{
                supplierId:'',
                isPending:false
            }
        },
        props:{
            isBatchEditSupplier:Boolean,
            selection:Array,
            suppliers:Array
        },
        methods:{
            cancel:function(){
                this.supplierId = '';
                this.$emit('update:isBatchEditSupplier',false);
            },
            submit:function(){
                if (this.selection.length === 0) {
                    this.$alertWarning('请选择你要修改的料盒');
                    return;
                }
                let ids = [];
                this.selection.map((item) => {
                    ids.push(item.id);
                });
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: editBoxOfSupplierUrl,
                        data: {
                            ids:ids.toString(),
                            supplierId:this.supplierId
                        }
                    };
                    axiosPost(options).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess('修改成功');
                            this.cancel();
                        } else {
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