<template>
    <div class="config">
        <el-form :model="configInfo" class="config-form">
            <el-form-item label="打印机IP">
                <el-input v-model.trim="configInfo.printerIP" placeholder="打印机IP"></el-input>
            </el-form-item>
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-edit" @click="save">保存</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'
    export default {
        name: "Config",
        data(){
            return{
                configInfo:{
                    printerIP:''
                }
            }
        },
        mounted(){
            if(this.configData.printerIP !== ''){
                this.configInfo.printerIP = this.configData.printerIP;
            }
        },
        computed:{
            ...mapGetters([
                'configData'
            ]),
        },
        methods:{
            ...mapActions([
                'setConfigData'
            ]),
            initForm:function(){
                this.configInfo.printerIP = '';
            },
            save:function(){
                if(this.configInfo.printerIP === ''){
                    this.$alertWarning("打印机IP不能为空");
                    return;
                }
                let ipVerification = /^(?:(?:1[0-9][0-9]\.)|(?:2[0-4][0-9]\.)|(?:25[0-5]\.)|(?:[1-9][0-9]\.)|(?:[0-9]\.)){3}(?:(?:1[0-9][0-9])|(?:2[0-4][0-9])|(?:25[0-5])|(?:[1-9][0-9])|(?:[0-9]))$/;
                if(!ipVerification.test(this.configInfo.printerIP)){
                    this.$alertWarning("IP格式不对");
                    return;
                }
                window.sessionStorage.setItem('configData', JSON.stringify(this.configInfo));
                this.setConfigData(this.configInfo);
                this.$alertSuccess("保存成功");
            }
        }
    }
</script>

<style scoped lang="scss">
    .config{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        .config-form{
            width:500px;
            margin:40px auto 0;
        }
    }
</style>