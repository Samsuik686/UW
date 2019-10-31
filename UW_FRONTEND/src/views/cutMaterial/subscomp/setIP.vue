<template>
    <el-dialog
            title="设置IP"
            @close="close"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :visible.sync="dialogVisible"
            width="30%">
        <el-form>
            <el-form-item label="IP">
                <el-input type="text" v-model="ip"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="save">确认</el-button>
            </el-form-item>
        </el-form>
    </el-dialog>
</template>

<script>
    import {mapGetters,mapActions} from 'vuex'
    export default {
        name: "setIP",
        data(){
            return{
                dialogVisible:false,
                ip:''
            }
        },
        props:{
            isSet:Boolean
        },
        computed:{
            ...mapGetters([
                'configData'
            ]),
        },
        mounted(){
            this.dialogVisible = this.isSet;
            if(this.configData.cutLoginPrinterIP !== ''){
                this.ip = this.configData.cutLoginPrinterIP;
            }
        },
        watch:{
            isSet:function(){
                this.dialogVisible = this.isSet;
            }
        },
        methods:{
            ...mapActions([
                'setConfigData'
            ]),
            close:function(){
                this.$emit('update:isSet',false);
            },
            save:function(){
                if(this.ip === ''){
                    this.$alertWarning("打印机IP不能为空");
                    return;
                }
                let ipVerification = /^(?:(?:1[0-9][0-9]\.)|(?:2[0-4][0-9]\.)|(?:25[0-5]\.)|(?:[1-9][0-9]\.)|(?:[0-9]\.)){3}(?:(?:1[0-9][0-9])|(?:2[0-4][0-9])|(?:25[0-5])|(?:[1-9][0-9])|(?:[0-9]))$/;
                if(!ipVerification.test(this.ip)){
                    this.$alertWarning("IP格式不对");
                    return;
                }
                let configData = JSON.parse(JSON.stringify(this.configData));
                configData['cutLoginPrinterIP'] = this.ip;
                window.sessionStorage.setItem('configData', JSON.stringify(configData));
                this.setConfigData(configData);
                this.$alertSuccess("保存成功");
            }
        }
    }
</script>

<style scoped>

</style>