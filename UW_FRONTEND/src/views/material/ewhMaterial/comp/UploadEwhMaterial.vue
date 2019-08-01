<template>
    <el-dialog
            title="导入物料仓物料表"
            :visible.sync="isUploading"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="供应商">
                <el-select v-model.trim="uploadInfo.supplierId" placeholder="供应商" value="" style="width:100%">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="来源地">
                <el-select v-model.trim="uploadInfo.sourceWhId" placeholder="来源地" value="" style="width:100%">
                    <el-option  v-for="item in origins" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="目的地">
                <el-select v-model.trim="uploadInfo.destinationwhId" placeholder="目的地" value="" style="width:100%">
                    <el-option  v-for="item in destinations" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item  label="选择文件">
                <input type="file" style="display:none;" id="fileUpload" @change="handleFileChange"/>
                <el-input id="uploadFile" size="large" @click.native="handleUpload" v-model="fileName"
                          placeholder="请选择"></el-input>
            </el-form-item>
            <el-form-item label="备注">
                <el-input type="textarea" v-model.trim="uploadInfo.remarks"></el-input>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import {mapGetters} from 'vuex'
    import axios from './../../../../plugins/http'
    import {externalWhImportTaskUrl} from "../../../../plugins/globalUrl";
    import {handleUploadEwhMaterial} from "../../../../utils/formValidate";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "UploadEwhMaterial",
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                uploadInfo:{
                    supplierId:'',
                    thisFile:'',
                    sourceWhId:'',
                    destinationwhId:'',
                    remarks:''
                },
                fileName:''
            }
        },
        computed:{
            ...mapGetters(['token'])
        },
        props:{
            isUploading:Boolean,
            suppliers:Array,
            destinations:Array,
            origins:Array
        },
        methods:{
            cancel:function(){
                this.uploadInfo.supplierId = '';
                this.uploadInfo.sourceWhId = '';
                this.uploadInfo.destinationwhId = '';
                this.uploadInfo.remarks = '';
                this.fileName = '';
                this.uploadInfo.thisFile = '';
                this.$emit("update:isUploading",false);
            },
            submit:function(){
                let result = handleUploadEwhMaterial(this.uploadInfo);
                if(result !== ''){
                    this.$alertWarning(result);
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let formData = new FormData();
                    formData.append('file', this.uploadInfo.thisFile);
                    formData.append('supplierId', this.uploadInfo.supplierId);
                    formData.append('destinationwhId', this.uploadInfo.destinationwhId);
                    formData.append('sourceWhId', this.uploadInfo.sourceWhId);
                    formData.append('remarks',this.uploadInfo.remarks);
                    formData.append('#TOKEN#',this.token);
                    axios.post(externalWhImportTaskUrl, formData).then(res => {
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
                this.uploadInfo.thisFile = file;
                this.fileName = file.name;
            }
        }
    }
</script>

<style scoped>

</style>