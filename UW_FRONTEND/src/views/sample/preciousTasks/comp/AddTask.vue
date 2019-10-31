<template>
    <el-dialog
            title="创建任务"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="客户">
                <el-select v-model.trim="supplier" placeholder="客户" value="" style="width:100%">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item  label="选择文件">
                <input type="file" style="display:none;" id="fileUpload" @change="handleFileChange"/>
                <el-input id="uploadFile" size="large" @click.native="handleUpload" v-model="fileName"
                          placeholder="请选择"></el-input>
            </el-form-item>
            <el-form-item label="备注">
                <el-input type="textarea" v-model.trim="remarks"></el-input>
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
    import {createPreciousSampleTaskUrl} from "../../../../plugins/globalUrl";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "AddTask",
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                supplier:'',
                thisFile:'',
                remarks:'',
                fileName:''
            }
        },
        computed:{
            ...mapGetters(['token'])
        },
        props:{
            isAdding:Boolean,
            suppliers:Array,
        },
        methods:{
            cancel:function(){
                this.supplier = '';
                this.remarks = '';
                this.fileName = '';
                this.thisFile = '';
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                if(!this.isPending){
                    let formData = new FormData();
                    if(this.supplier === ''){
                        this.$alertWarning('客户不能为空');
                        return;
                    }
                    if(this.thisFile === ''){
                        this.$alertWarning('任务不能为空');
                        return;
                    }
                    if(this.remarks === ''){
                        this.$alertWarning('备注不能为空');
                        return;
                    }
                    formData.append('file', this.thisFile);
                    formData.append('supplierId', this.supplier);
                    formData.append('remarks',this.remarks);
                    formData.append('#TOKEN#', this.token);
                    this.isPending = true;
                    axios.post(createPreciousSampleTaskUrl, formData).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess('创建成功');
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