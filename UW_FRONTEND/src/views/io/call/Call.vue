<template>
    <div class="call-robot">
        <el-form :inline="true" class="call-form" size="medium">
        <el-form-item label="出入库类型">
            <el-select v-model="windowType" placeholder="出入库类型" value="">
                <el-option label="入库" value="1"></el-option>
                <el-option label="截料入库" value="2"></el-option>
                <el-option label="调拨入库" value="3"></el-option>
            </el-select>
        </el-form-item>
        <el-form-item label="仓口">
            <el-select v-model="thisWindow" placeholder="仓口" :disabled="windowsList.length === 0" value="">
                <el-option value="" :label="windowsList.length === 0 ? '无非空闲仓口':'请选择'"></el-option>
                <el-option v-for="item in windowsList" :value="item.id" :label="item.id" :key="item.id"></el-option>
            </el-select>
        </el-form-item>
    </el-form>
        <video controls="controls" id="sAudio" hidden>
            <source src="./../../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
            <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
        </video>
        <input type="text" title="scanner" id="call-check" v-model.trim="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
    </div>
</template>

<script>
    import {axiosPost} from "../../../utils/fetchData";
    import {handleScanText} from "../../../utils/scan";
    import {errHandler} from "../../../utils/errorHandler";
    import {robotCallUrl, taskWindowsUrl} from "../../../plugins/globalUrl";

    export default {
        name: "CallRobot",
        data(){
            return{
                windowType: '1',
                windowsList: [],
                thisWindow: '',
                scanText:'',
                isPending:false
            }
        },
        mounted(){
            this.setPreset();
            this.setFocus();
        },
        watch:{
            windowType:function(val){
                if(val !== ''){
                    this.setPreset();
                }
            }
        },
        methods:{
            setPreset: function () {
                let options = {
                    url: taskWindowsUrl,
                    data: {
                        type: this.windowType
                    }
                };
                axiosPost(options).then(response => {
                    if (response.data.result === 200) {
                        this.windowsList = response.data.data;
                        if (this.windowsList.length > 0) {
                            this.thisWindow = this.windowsList[0].id
                        }else{
                            this.thisWindow = '';
                        }
                    }else{
                        this.$alertWarning(response.data);
                    }
                });
            },
            setFocus: function () {
                if (this.$route.path === '/io/call') {
                    document.getElementById('call-check').focus();
                }
            },
            scannerHandler: function () {
                let scanText = this.scanText;
                this.scanText = "";

                //判断扫描的条码格式
                let result = handleScanText(scanText);
                if(result !== ''){
                    this.$alertWarning(result);
                    this.failAudioPlay();
                    return;
                }

                //仓口为空
                if(this.thisWindow === ''){
                    this.$alertWarning('未选择仓口');
                    return;
                }

                //呼叫叉车
                if (!this.isPending) {
                    this.isPending = true;
                    let tempArray = scanText.split("@");
                    for(let i=0;i<tempArray.length;i++){
                        tempArray[i] = tempArray[i].replace(/\$AT\$/g, "@");
                    }
                    let options = {
                        url: robotCallUrl,
                        data: {
                            id: this.thisWindow,
                            no: tempArray[0],
                            supplierName:tempArray[4]
                        }
                    };
                    axiosPost(options).then(res => {
                        if (res.data.result === 200) {
                            this.successAudioPlay();
                            this.$alertSuccess("调用成功")
                        } else {
                            this.failAudioPlay();
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                        this.failAudioPlay();
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            // 扫描成功提示
            successAudioPlay: function () {
                let audio = document.getElementById('sAudio');
                if (audio !== null) {
                    if (audio.paused) {
                        audio.play();
                    }
                }
            },
            // 扫描失败提示
            failAudioPlay: function () {
                let audio = document.getElementById('fAudio');
                if (audio !== null) {
                    if (audio.paused) {
                        audio.play();
                    }
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .call-robot {
        background: #fff;
        width: 100%;
        box-sizing: border-box;
        border: 1px solid #ebebeb;
        transition: .2s;
        padding: 30px 30px 0 30px;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        #call-check {
            opacity: 0;
            height: 0;
            line-height: 0;
            border: none;
            padding: 0;
            position: fixed;
        }
    }
</style>