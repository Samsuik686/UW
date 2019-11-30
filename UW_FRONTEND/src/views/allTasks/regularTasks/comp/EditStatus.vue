<template>
    <el-dialog
            title="更新任务状态"
            :visible.sync="isEditStatus"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="状态更改" v-if="originState === '0' || originState === '1' || originState === '2'">
                <el-select v-model.trim="thisState" placeholder="状态更改" value="" style="width:100%">
                    <el-option  label="请选择" :value="originState" disabled></el-option>
                    <el-option  label="通过审核" value='1' v-if="originState === '0'"></el-option>
                    <el-option  label="开始任务" value='2' v-if="originState === '1'"></el-option>
                    <el-option  label="作废任务" value='3' v-if="(user.type === 1 || user.type === 3)"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="状态更改" v-else>
                <el-select v-model.trim="thisState" placeholder="状态更改" value="" style="width:100%" disabled>
                    <el-option  label="无法操作" :value='originState'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="仓口选择" v-if="windowShow === '2' && originState === '1' && editData.type !== 8">
                <el-select v-model.trim="windowVal" placeholder="仓口选择" value="" style="width:100%" :disabled="window.length === 0">
                    <el-option  :label="window.length > 0 ? '请选择' : '无可用仓口'" value=''></el-option>
                    <el-option v-for="item in window" :value="item.id" :label="item.id" :key="item.id"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit" :disabled="originState > 2">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {mapGetters} from 'vuex'
    import {taskUrl, taskWindowsUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "EditStatus",
        data(){
            return{
                isCloseOnModal:false,
                isPending:false,
                thisState:'',
                originState:'',
                windowShow: '',
                window: [],
                windowVal: ''
            }
        },
        watch:{
            isEditStatus:function(val){
              if(val === true){
                  this.getWindows();
                  this.originState = this.editData.state.toString();
                  this.thisState = this.editData.state.toString();
              }else{
                  this.originState = '';
                  this.thisState = '';
              }
          },
            thisState: function (newVal, oldVal) {
                this.windowShow = newVal;
                if(newVal === '1' && oldVal !== '' && this.editData.type === 1){
                    this.cancel();
                    this.$emit('showCheckTaskDetails',this.editData);
                }
            }
        },
        computed:{
            ...mapGetters([
                'user'
            ])
        },
        props:{
            isEditStatus:Boolean,
            editData:Object
        },
        methods:{
            getWindows:function(){
                let options = {
                    url: taskWindowsUrl,
                    data: {
                        type: 0
                    }
                };
                axiosPost(options).then(res => {
                    this.window = res.data.data
                })
            },
            cancel:function(){
                this.thisState = '';
                this.originState = '';
                this.windowShow = '';
                this.windowVal = '';
                this.$emit("update:isEditStatus",false);
            },
            submit:function(){
                if(!this.isPending) {
                    if (this.thisState > 0 && (this.thisState !== this.originState)) {
                        this.isPending = true;
                        let statusUrl;
                        switch (this.thisState) {
                            case '1':
                                statusUrl = '/passRegularIOTask';
                                break;
                            case '2':
                                statusUrl = '/startRegularIOTask';
                                break;
                            case '3':
                                statusUrl = '/cancelRegularIOTask';
                                break;
                        }
                        let options = {
                            url: taskUrl + statusUrl,
                            data: {
                                id: this.editData.id
                            }
                        };
                        if (this.thisState === '2' && this.editData.type !== 8) {
                            if (this.windowVal === "") {
                                this.$alertInfo("请选择仓口");
                                this.isPending = false;
                                return;
                            }
                            options.data.window = this.windowVal
                        }
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