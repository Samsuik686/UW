<template>
    <el-dialog
            title="更换仓口"
            :visible.sync="isChange"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="仓口选择">
                <el-select v-model.trim="windowArr" placeholder="仓口选择,不选择时默认解绑仓口" style="width:100%" multiple value="">
                    <el-option v-for="item in window" :value="item.id" :label="item.id" :key="item.id"></el-option>
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

    import {getTaskWindowUrl, setTaskWindowUrl, taskWindowsUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "ChangeWindow",
        data(){
            return{
                isCloseOnModal:false,
                isPending:false,
                window: [],
                windowArr:[]
            }
        },
        watch:{
            isChange:function(val){
                if(val === true){
                    this.getWindows();
                    this.getTaskWindow();
                }
            }
        },
        props:{
            isChange:false,
            editData:Object
        },
        methods:{
            getWindows:function(){
                let options = {
                    url: taskWindowsUrl,
                    data: {
                        type:0
                    }
                };
                axiosPost(options).then(res => {
                    if(res.data.result === 200){
                        this.window = res.data.data
                    }else{
                        errHandler(res.data);
                    }
                })
            },
            getTaskWindow:function(){
                let options = {
                    url: getTaskWindowUrl,
                    data: {
                        taskId:this.editData.id
                    }
                };
                axiosPost(options).then(res => {
                    if(res.data.result === 200){
                        let data = res.data.data;
                        data.map((item) =>{
                            this.windowArr.push(item.id);
                        })
                    }else{
                        errHandler(res.data);
                    }
                })
            },
            cancel:function(){
                this.windowArr = [];
                this.$emit("update:isChange",false);
            },
            submit:function(){
                if(!this.isPending) {
                    this.isPending = true;
                    let windows = this.windowArr.toString();
                    let options = {
                        url:setTaskWindowUrl,
                        data:{
                            taskId:this.editData.id,
                            windowIds:windows
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('更换成功');
                            this.cancel();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err =>{
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