<template>
    <div class="in-now">
        <video controls="controls" id="sAudio" hidden>
            <source src="./../../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
            <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
        </video>
        <input type="text" title="scanner" id="in-check" v-model="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
        <el-form :inline="true" class="in-form" size="medium">
            <el-form-item label="出入库类型">
                <el-input v-model="windowTypeString" disabled></el-input>
            </el-form-item>
            <el-form-item label="仓口">
                <el-select v-model="thisWindow" placeholder="仓口" :disabled="windowsList.length === 0" value="">
                    <el-option value="" :label="windowsList.length === 0 ? '无非空闲仓口':'请选择'"></el-option>
                    <el-option v-for="item in windowsList" :value="item.id" :label="item.id" :key="item.id"></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <el-collapse
                ref="collapse"
                v-model="activeName"
                @change="setClose"
                accordion
                v-if="tasks.length > 0">
            <el-collapse-item  v-for="(item,index) in tasks"
                               :key="index"
                               :title='item.materialNo === null?item.goodsLocationName+" / "+"无数据":item.goodsLocationName+" / "+item.boxId+" / "+item.materialNo'
                               :name="item.boxId === null?item.goodsLocationId:item.boxId">
                <task-item-details
                    :taskItem="item"
                    v-if="item.id !== null"
                    :id="index"
                    :col="col"
                    :row="row"
                    @refreshData="refreshData">
                 </task-item-details>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>
    import {mapActions} from 'vuex'
    import TaskItemDetails from './comp/TaskItemDetails';
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {handleScanText} from "../../../utils/scan";
    import {taskInUrl, taskWindowParkingItems, taskWindowsUrl} from "../../../plugins/globalUrl";

    export default {
        name: "InNow",
        data() {
            return {
                windowType: '1',
                windowTypeString: '入库',
                windowsList: [],
                thisWindow: '',
                activeName: '',
                tasks:[],
                myTimeOut: '',
                isTimeOut: false,
                scanText:'',
                col:-1,
                row:-1
            }
        },
        components: {
            TaskItemDetails
        },
        beforeDestroy() {
            this.clearMyTimeOut();
        },
        created(){
            if (this.$route.path === '/io/returnNow') {
                this.windowType = '3';
                this.windowTypeString = '调拨入库';
            } else {
                this.windowType = '1';
                this.windowTypeString = '入库';
            }
        },
        mounted() {
            this.setPreset();
            this.setFocus();
        },
        watch: {
            thisWindow: function (val) {
                if (val !== '') {
                    this.activeName = '';
                    this.setMyTimeOut();
                }else{
                    this.clearMyTimeOut();
                    this.tasks = [];
                    this.activeName = '';
                }
            },
            $route:function(){
                if (this.$route.path === '/io/returnNow') {
                    this.windowType = '3';
                    this.windowTypeString = '调拨入库';
                    this.initData();
                    this.setPreset();
                } else {
                    this.windowType = '1';
                    this.windowTypeString = '入库';
                    this.initData();
                    this.setPreset();
                }
            }
        },
        methods: {
            ...mapActions(['setScanFinishBoxId']),
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
                        this.windowsList = response.data.data;
                        if (this.windowsList.length > 0) {
                            this.thisWindow = this.windowsList[0].id
                        } else {
                            this.thisWindow = '';
                        }
                    }
                });
            },
            //获取各仓口数据
            select: function (i) {
                if (this.thisWindow === '') {
                    return;
                }
                let options = {
                    url: taskWindowParkingItems,
                    data: {
                        id: this.thisWindow
                    }
                };
                axiosPost(options).then(response => {
                    if (response.data.result === 200) {
                        if (response.data.data) {
                            this.tasks = response.data.data;
                            if(i !== undefined){
                                this.textToSpeak('已扫'+this.tasks[i].details.length+'盘');
                            }
                        } else {
                            this.tasks = [];
                        }
                    } else if(response.data.result === 412){
                        if(response.data.data === "仓口不存在任务"){
                            this.$alertWarning(response.data.data);
                            this.setPreset();
                        }
                    } else {
                        errHandler(response.data);
                    }
                }).catch(err => {
                    console.log(err);
                })
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
            },
            setFocus: function () {
                if (this.$route.path === '/io/inNow' || this.$route.path === '/io/returnNow') {
                    document.getElementById('in-check').focus();
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
            scannerHandler: function () {
                let scanText = this.scanText;
                this.scanText = '';

                //扫描操作完毕码
                if (scanText === "###finished###") {
                    this.setScanFinishBoxId(this.activeName);
                    return;
                }

                //判断码是否为料盒码
                let boxArr = scanText.split("@");
                for (let i = 0; i < this.tasks.length; i++) {
                    if (Number(boxArr[0]) === this.tasks[i].boxId) {
                        this.activeName = this.tasks[i].boxId;
                        return;
                    }
                }
                if(this.activeName === ''){
                    this.$alertWarning('请扫料盒码');
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

                /*对比料号是否一致*/
                let tempArray = scanText.split("@");
                let text = tempArray[0].replace('\ufeff', '');
                let isExit = false;
                for (let i = 0; i < this.tasks.length; i++) {
                    if (text === this.tasks[i].materialNo) {
                        isExit = true;
                        if(this.activeName !== this.tasks[i].boxId){
                            this.$alertWarning('当前扫描的二维码不属于该料盒');
                            return;
                        }
                        let options = {
                            url: taskInUrl,
                            data: {
                                packListItemId: this.tasks[i].id,
                                materialId: tempArray[2],
                                quantity: tempArray[1],
                                productionTime: tempArray[7],
                                supplierName: tempArray[4]
                            }
                        };
                        axiosPost(options).then(response => {
                            if (response.data.result === 200) {
                                this.col = response.data.data.col;
                                this.row = response.data.data.row;
                                this.successAudioPlay();
                                this.$alertSuccess('操作成功');
                                this.isPending = false;
                                this.select(i);
                            } else {
                                this.failAudioPlay();
                                errHandler(response.data);
                            }
                        });
                        return;
                    }
                }
                if (isExit === false) {
                    this.failAudioPlay();
                    this.$alertWarning('二维码格式错误，料号不对应');
                }
            },
            textToSpeak:function(text){
                let synth = window.speechSynthesis;
                let utterThis = new SpeechSynthesisUtterance(text);
                utterThis.volume = 1;
                utterThis.pitch = 2;
                synth.speak(utterThis);
            },
            setClose:function(){
                this.activeName = '';
                this.$refs.collapse.activeNames = [];
            },
            refreshData:function(){
                this.select();
            },
            initData:function(){
                this.windowsList = [];
                this.thisWindow = '';
                this.activeName = '';
                this.tasks = [];
                this.myTimeOut = '';
                this.isTimeOut = false;
                this.scanText = '';
                this.col = -1;
                this.row = -1;
            }
        }
    }
</script>

<style scoped lang="scss">
    .in-now {
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        #in-check {
            position: fixed;
            opacity: 0;
            height: 0;
            line-height: 0;
            border: none;
            padding: 0;
        }
    }
</style>
