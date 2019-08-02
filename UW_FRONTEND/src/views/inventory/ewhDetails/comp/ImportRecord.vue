<template>
    <el-dialog
            title="导入物料仓盘点数据"
            :visible.sync="isImport"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item  label="选择文件">
                <input type="file" style="display:none;" id="fileUpload" @change="handleFileChange"/>
                <el-input id="uploadFile" size="large" @click.native="handleUpload" v-model="fileName"
                          placeholder="请选择"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import axios from './../../../../plugins/http'
    import {mapGetters,mapActions} from 'vuex'
    import {importEWhInventoryRecordUrl} from "../../../../plugins/globalUrl";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "ImportRecord",
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                thisFile:'',
                fileName:''
            }
        },
        computed:{
            ...mapGetters(['token'])
        },
        props:{
            isImport:Boolean,
            taskId:String
        },
        methods:{
            ...mapActions(['setUnInventoryData']),
            cancel:function(){
                this.fileName = '';
                this.thisFile = '';
                this.$emit("update:isImport",false);
            },
            submit:function(){
                if(!this.isPending){
                    let formData = new FormData();
                    if(this.taskId === ''){
                        this.$alertWarning('请先选择盘点任务');
                        return;
                    }
                    if(this.thisFile === ''){
                        this.$alertWarning('文件不能为空');
                        return;
                    }
                    formData.append('taskId',this.taskId);
                    formData.append('file', this.thisFile);
                    formData.append('#TOKEN#', this.token);
                    this.isPending = true;
                    axios.post(importEWhInventoryRecordUrl, formData).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess('添加成功');
                            this.setUnInventoryData([]);
                            this.cancel();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            handleUpload: function () {
                let file = document.getElementById('fileUpload');
                file.value = null;
                file.click();
            },
            handleFileChange: function () {
                let files = document.getElementById('fileUpload');
                let file = files.files[0];
                this.thisFile = file;
                this.fileName = file.name;
            }
        }
    }
</script>

<style scoped>

</style>