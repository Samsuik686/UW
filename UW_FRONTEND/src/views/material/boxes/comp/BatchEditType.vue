<template>
    <el-dialog
            title="批量修改料盒类型"
            :visible.sync="isBatchEditType"
            :show-close="false"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            width="30%">
        <el-form>
            <el-form-item label="类型">
                <el-select v-model.trim="materialBoxTypeId" placeholder="料盒类型" value="">
                    <el-option label="标准" value='1'></el-option>
                    <el-option label="非标准" value='2'></el-option>
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
    import {editBoxOfTypeUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "BatchEditType",
        data(){
            return{
                materialBoxTypeId:'',
                isPending:false
            }
        },
        props:{
            isBatchEditType:Boolean,
            selection:Array
        },
        methods:{
            cancel:function(){
                this.materialBoxTypeId = '';
                this.$emit('update:isBatchEditType',false);
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
                        url: editBoxOfTypeUrl,
                        data: {
                            ids:ids.toString(),
                            materialBoxTypeId:this.materialBoxTypeId
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