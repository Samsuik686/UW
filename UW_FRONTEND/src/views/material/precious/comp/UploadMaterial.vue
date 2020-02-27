<template>
    <el-dialog
            title="导入物料类型表"
            :visible.sync="isUploading"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="客户">
                <el-select v-model.trim="supplierId" placeholder="客户" value="" style="width:100%">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
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
    import {mapGetters} from 'vuex'
    import {importPreciousMaterialTypeFileUrl} from "../../../../plugins/globalUrl";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "UploadMaterial",
        props:{
            isUploading:Boolean,
            suppliers:Array
        },
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                supplierId:'',
                fileName:'',
                thisFile:''
            }
        },
        computed:{
            ...mapGetters(['token'])
        },
        methods:{
            cancel:function(){
                this.supplierId = '';
                this.fileName = '';
                this.thisFile = '';
                this.$emit("update:isUploading",false);
            },
            submit:function(){
                if(this.supplierId === ''){
                    this.$alertWarning('客户不能为空');
                    return;
                }
                if(this.thisFile === ''){
                    this.$alertWarning('文件不能为空');
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let formData = new FormData();
                    formData.append('file', this.thisFile);
                    formData.append('supplierId', this.supplierId);
                    formData.append('#TOKEN#',this.token);
                    axios.post(importPreciousMaterialTypeFileUrl, formData).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess('导入成功');
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