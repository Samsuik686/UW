<template>
    <div class="sample-operation" v-loading="isLoading">
        <video controls="controls" id="sAudio" hidden>
            <source src="./../../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
            <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
        </video>
        <input type="text" title="scanner" id="scanner-check" v-model="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
        <el-form :inline="true" class="demo-form-inline" size="medium">
            <el-form-item label="仓口">
                <el-select v-model="thisWindow" placeholder="仓口" :disabled="windowsList.length === 0" value="">
                    <el-option value="" :label="windowsList.length === 0 ? '无非空闲仓口':'请选择'"></el-option>
                    <el-option v-for="item in windowsList" :value="item.id" :label="item.id" :key="item.id"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <el-collapse v-model="activeName" accordion class="in-collapse" v-if="sampleData.length > 0">
            <el-collapse-item v-for="(item,index) in sampleData"
                              :key="index"
                              :title='item.boxId === null?item.goodsLocationName+" / "+"无数据":item.goodsLocationName+" / "+item.boxId'
                              :name="item.boxId === null?String(item.goodsLocationId):String(item.boxId)">
                <sample-item
                    :is-scan.sync="isScan"
                    :active-name="String(activeName)"
                    :active-material-id="activeMaterialId"
                    :sample-item="item"
                ></sample-item>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>
    import {
        getPackingSampleMaterialInfoUrl,
        sampleRegularUWMaterialUrl,
        scanMaterialUrl,
        taskWindowsUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {handleScanText} from "../../../utils/scan";
    import SampleItem from "./comp/SampleItem";
    import {activeName} from "../../../store/getters";

    export default {
        name: "Operation",
        components: {SampleItem},
        watch: {
            thisWindow: function (val) {
                if (val !== '') {
                    this.activeName = '';
                    this.isLoading = true;
                    this.setMyTimeOut();
                }else{
                    this.clearMyTimeOut();
                    this.activeName = '';
                    this.sampleData = [];
                }
            },
        },
        data() {
            return {
                scanText: '',
                windowsList: [],
                thisWindow: '',
                windowType: '5',
                myTimeOut: '',
                isTimeOut: false,
                isPending: false,
                sampleData: [],
                activeName:'',  //当前操作的料盒
                activeMaterialId:'', //当前扫描的料盘码
                isScan:false,  //当前是否扫描成功,
                isLoading:false
            }
        },
        beforeDestroy(){
            this.clearMyTimeOut();
        },
        mounted(){
            this.setPreset();
            this.setFocus();
        },
        methods:{
            //获取仓口
            setPreset: function () {
                let options = {
                    url: taskWindowsUrl,
                    data: {
                        type: this.windowType
                    }
                };
                axiosPost(options).then(response => {
                    if (response.data.result === 200) {
                        if (response.data.data !== null) {
                            this.windowsList = response.data.data;
                            if (this.windowsList.length > 0) {
                                this.thisWindow = this.windowsList[0].id;
                            }
                        } else {
                            this.thisWindow = '';
                        }
                    } else {
                        errHandler(response.data);
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            },
            //获取焦点
            setFocus:function(){
                if (this.$route.path === '/sample/operation') {
                    if(document.getElementById('scanner-check')){
                        document.getElementById('scanner-check').focus();
                    }
                }
            },
            //扫描处理
            scannerHandler: function () {
                let scanText = this.scanText;
                this.scanText = '';

                //判断是否为料盒码
                let boxArr = scanText.split("@");
                for(let i=0;i<boxArr.length;i++){
                    boxArr[i] = boxArr[i].replace(/\$AT\$/g, "@");
                }
                for (let i = 0; i < this.sampleData.length; i++) {
                    if (Number(boxArr[0]) === this.sampleData[i].boxId) {
                        this.successAudioPlay();
                        this.activeName = String(this.sampleData[i].boxId);
                        return;
                    }
                }
                //扫描料盘前需要先选料盒
                if (this.activeName === '') {
                    this.failAudioPlay();
                    this.$alertWarning('请扫料盒码或点开你当前要操作的货位');
                    return;
                }

                /*sample: 03.01.0001@1000@1531817296428@A008@范例表@A-1@9@2018-07-17@*/
                //判断扫描的条码格式
                let result = handleScanText(scanText);
                if (result !== '') {
                    this.failAudioPlay();
                    this.$alertWarning(result);
                    return;
                }

                let activeItem = [];  //当前操作料盒的料盘信息
                let groupId = '';     //当前操作料盒的groupId
                for(let i = 0;i<this.sampleData.length;i++){
                    let item = this.sampleData[i];
                    if(Number(this.activeName) === Number(item.boxId)){
                        activeItem = item.list;
                        groupId = item.groupId;
                    }
                }
                if(activeItem.length === 0){
                    return;
                }
                //判断扫描的二维码是否是该料盒的料盘码
                let tempArray = scanText.split("@");
                for(let i=0;i<tempArray.length;i++){
                    tempArray[i] = tempArray[i].replace(/\$AT\$/g, "@");
                }
                let materialId = tempArray[2];
                let isMaterialExit = false;
                for(let i=0;i<activeItem.length;i++){
                    if(materialId === activeItem[i].materialId){
                        this.scanMaterial(materialId,groupId);
                        isMaterialExit = true;
                        break;
                    }else{
                        isMaterialExit = false;
                    }
                }
                if(isMaterialExit === false){
                    this.failAudioPlay();
                    this.$alertWarning('当前扫描的料盘不属于该料盒');
                }
            },
            successAudioPlay: function () {
                let audio = document.getElementById('sAudio');
                if (audio !== null) {
                    if (audio.paused) {
                        audio.play();
                    }
                }
            },
            failAudioPlay: function () {
                let audio = document.getElementById('fAudio');
                if (audio !== null) {
                    if (audio.paused) {
                        audio.play();
                    }
                }
            },
            scanMaterial:function(materialId,groupId){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:sampleRegularUWMaterialUrl,
                        data:{
                            materialId:materialId,
                            groupId:groupId
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.successAudioPlay();
                            this.$alertSuccess('操作成功');
                            this.activeMaterialId = materialId;
                            this.isPending = false;
                            this.select(true);
                        }else{
                            this.failAudioPlay();
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            select:function(isScan){
                //仓口为空
                if (!this.thisWindow) {
                    return;
                }
                let options = {
                    url: getPackingSampleMaterialInfoUrl,
                    data: {
                        windowId: this.thisWindow
                    }
                };
                if (!this.isPending) {
                    this.isPending = true;
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            if (response.data.data === null) {
                                this.sampleData = [];
                                return;
                            }
                            this.sampleData = response.data.data;
                            if(isScan !== undefined){
                                this.isScan = true;
                            }
                        } else if(response.data.result === 412){
                            if(response.data.data === "仓口不存在任务"){
                                this.$alertWarning(response.data.data);
                                this.setPreset();
                            }
                        } else {
                            errHandler(response.data);
                            this.clearMyTimeOut();
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('请求超时，请刷新重试');
                        this.clearMyTimeOut();
                    }).finally(() => {
                        this.isPending = false;
                        this.isLoading = false;
                    })
                }
            },
            setMyTimeOut: function () {
                if (this.isTimeOut === true) {
                    this.clearMyTimeOut();
                }
                this.isTimeOut = true;
                let that = this;
                this.isPending = false;
                this.select();
                this.myTimeOut = setInterval(function () {
                    that.select();
                }, 1000);
            },
            clearMyTimeOut: function () {
                this.isTimeOut = false;
                clearTimeout(this.myTimeOut);
                this.myTimeOut = null;
                this.isPending = false;
            }
        }
    }
</script>

<style scoped lang="scss">
    .sample-operation{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        #scanner-check {
            position: fixed;
            opacity: 0;
            height: 0;
            line-height: 0;
            border: none;
            padding: 0;
        }
    }
</style>