<template>
    <el-dialog
            title="导出物料报表"
            :visible.sync="isExporting"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="30%">
        <el-form>
            <el-form-item label="供应商">
                <el-select v-model.trim="supplier" placeholder="供应商" value="" style="width:100%">
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
    import {downloadFile} from "../../../../utils/fetchData";
    import {exportReportUrl} from "../../../../plugins/globalUrl";

    export default {
        name: "ExportMaterial",
        props:{
            isExporting:Boolean,
            suppliers:Array
        },
        data(){
            return{
                isPending:false,
                isCloseOnModal:false,
                supplier:''
            }
        },
        methods:{
            cancel:function(){
                this.supplier = '';
                this.$emit("update:isExporting",false);
            },
            submit:function(){
                if(this.supplier === ''){
                    this.$alertWarning('请选择供应商');
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    let data = {
                        supplier:this.supplier,
                        '#TOKEN#': this.$store.state.token
                    };
                    downloadFile(exportReportUrl, data);
                    let count = 0;
                    let mark = setInterval(() => {
                        count++;
                        if (count > 9) {
                            count = 0;
                            clearInterval(mark);
                            this.isPending = false
                        }
                    }, 1000);
                    this.$alertSuccess('请求成功，请等待下载');
                    this.cancel();
                }
            }
        }
    }
</script>

<style scoped>

</style>