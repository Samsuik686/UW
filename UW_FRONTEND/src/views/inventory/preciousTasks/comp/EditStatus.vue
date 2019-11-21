<template>
    <el-dialog
            title="更新任务状态"
            :visible.sync="isEditStatus"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="状态更改" v-if="originState === '1' || originState === '2'">
                <el-select v-model.trim="thisState" placeholder="状态更改" value="" style="width:100%">
                    <el-option  label="请选择" :value="originState" disabled></el-option>
                    <el-option  label="开始任务" value='2' v-if="originState === '1'"></el-option>
                    <el-option  label="完成任务" value='3' v-if="originState === '2'"></el-option>
                    <el-option  label="作废任务" value='4' v-if="originState === '1'"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="状态更改" v-else>
                <el-select v-model.trim="thisState" placeholder="状态更改" value="" style="width:100%" disabled>
                    <el-option  label="无法操作" :value='originState'></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit" :disabled="originState !== '1' && originState !== '2'">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {mapGetters} from 'vuex'
    import {
        cancelPreciousInventoryTaskUrl,
        finishInventoryPreciousTaskUrl,
        startInventoryPreciousTaskUrl
    } from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "EditStatus",
        data(){
            return{
                isCloseOnModal:false,
                isPending:false,
                thisState:'',
                originState:''
            }
        },
        watch:{
            isEditStatus:function(val){
                if(val === true){
                    this.originState = this.editData.state.toString();
                    this.thisState = this.editData.state.toString();
                }else{
                    this.originState = '';
                    this.thisState = '';
                }
            }
        },
        props:{
            isEditStatus:Boolean,
            editData:Object
        },
        computed:{
            ...mapGetters([
                'user'
            ])
        },
        methods:{
            cancel:function(){
                this.thisState = '';
                this.originState = '';
                this.$emit("update:isEditStatus",false);
            },
            submit:function(){
                if(!this.isPending) {
                    if (this.thisState > 0 && (this.thisState !== this.originState)) {
                        this.isPending = true;
                        let statusUrl;
                        switch (this.thisState) {
                            case '2':
                                statusUrl =  startInventoryPreciousTaskUrl;
                                break;
                            case '3':
                                statusUrl =  finishInventoryPreciousTaskUrl;
                                break;
                            case '4':
                                statusUrl = cancelPreciousInventoryTaskUrl;
                                break;
                        }
                        let options = {
                            url: statusUrl,
                            data: {
                                taskId: this.editData.taskId
                            }
                        };
                        axiosPost(options).then(res => {
                            if(res.data.result === 200){
                                this.$alertSuccess('设置成功');
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
    }
</script>

<style scoped>

</style>