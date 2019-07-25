<template>
    <div class="out-now">
      <video controls="controls" id="sAudio" hidden>
        <source src="./../../../assets/005-System05.ogg" type="video/ogg">
      </video>
      <video controls="controls" id="fAudio" hidden>
        <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
      </video>
      <input type="text" title="scanner" id="out-check" v-model="scanText"
             @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
      <el-form :inline="true" class="demo-form-inline" size="medium">
        <el-form-item label="出入库类型">
          <el-input v-model="windowTypeString" placeholder="出库" disabled></el-input>
        </el-form-item>
        <el-form-item label="仓口">
          <el-select v-model="thisWindow" placeholder="仓口" :disabled="windowsList.length === 0">
            <el-option value="" :label="windowsList.length === 0 ? '无非空闲仓口':'请选择'"></el-option>
            <el-option v-for="item in windowsList" :value="item.id" :label="item.id" :key="item.id"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <el-collapse
        v-model="activeName"
        accordion
        class="in-collapse"
        v-if="tasks.length > 0">
        <el-collapse-item  v-for="(item,index) in tasks"
                           :key="index"
                           :title='item.materialNo === null?item.goodsLocationName+" "+"/"+" "+"无数据":item.goodsLocationName+" "+"/"+" "+item.boxId+" "+"/"+" "+item.materialNo'
                           :name="item.boxId === null?item.goodsLocationId:item.boxId">
          <div v-if="item.id !== null">
            <cut-item-details  :taskItem="item" v-if="item.isForceFinish === true" :x="x" :y="y"></cut-item-details>
            <task-item-details :taskItem="item" v-else></task-item-details>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </template>

  <script>
    import {mapActions} from 'vuex'
    import TaskItemDetails from './comp/TaskItemDetails';
    import {
      taskWindowParkingItems,
      taskWindowsUrl,
      taskOutUrl,
      taskBackAfterCuttingUrl
    } from "../../../config/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {handleScanText} from "../../../utils/scan";
    import eventBus from "../../../utils/eventBus";
    import CutItemDetails from "./comp/CutItemDetails";

    export default {
      name: "OutNow",
      data() {
        return {
          windowType: '2',
          windowTypeString: '出库',
          windowsList: [],
          thisWindow: '',
          activeName: '',
          tasks:[],
          myTimeOut: '',
          isTimeOut: false,
          scanText:'',
          tempArr:[],
          isBlurFocus:false,
          x:-1,
          y:-1
        }
      },
      components: {
        CutItemDetails,
        TaskItemDetails
      },
      beforeDestroy() {
        this.clearMyTimeOut();
      },
      mounted() {
        this.setPreset();
        this.setFocus();
        if (this.thisWindow !== '') {
          this.setMyTimeOut();
        }
        eventBus.$on('refreshTaskItem',() => {
          this.isBlurFocus = false;
          this.setFocus();
          this.fetchData(this.thisWindow);
        });
        eventBus.$on('blurFocus',(val) => {
          this.isBlurFocus = true;
          if(val === true){
            document.getElementById('out-check').blur();
          }else{
            document.getElementById('out-check').focus();
          }
        })
      },
      watch: {
        thisWindow: function (val) {
          if (val !== '') {
            this.activeName = '';
            this.setMyTimeOut();
          }
        },
        blurFocus:function(val){
          if(val === true){
            document.getElementById('out-check').blur();
          }else{
            document.getElementById('out-check').focus();
          }
        }
      },
      methods: {
        ...mapActions(['setScanFinishBoxId','setScanCutBoxId']),
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
        fetchData: function (id) {
          if (id === '') {
            return;
          }
          let options = {
            url: taskWindowParkingItems,
            data: {
              id: id
            }
          };
          axiosPost(options).then(response => {
            if (response.data.result === 200) {
              if (response.data.data) {
                this.tasks = response.data.data;
                this.tasks.map((item) => {
                  item.isCut = false;
                });
              } else {
                this.tasks = [];
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
          this.fetchData(this.thisWindow);
          this.myTimeOut = setInterval(function () {
            that.fetchData(that.thisWindow);
          }, 1000);
        },
        clearMyTimeOut: function () {
          this.isTimeOut = false;
          clearTimeout(this.myTimeOut);
          this.myTimeOut = null;
        },
        setFocus: function () {
          if(this.isBlurFocus === true){
            return;
          }
          if (this.$route.path === '/io/outnow') {
            document.getElementById('out-check').focus();
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
          //扫描截料入库码
          if (scanText === "###cut###") {
            this.setScanCutBoxId(this.activeName);
            return;
          }
          //扫描料盒码
          let boxArr = scanText.split("@");
          for (let i = 0; i < this.tasks.length; i++) {
            if (Number(boxArr[0]) === this.tasks[i].boxId) {
              this.activeName = this.tasks[i].boxId;
              return;
            }
          }

          if(this.activeName === ''){
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
          /*对比料号是否一致*/
          let tempArray = scanText.split("@");
          let text = tempArray[0].replace('\ufeff', '');
          let isExit = false;
          for (let i = 0; i < this.tasks.length; i++) {
            if (text === this.tasks[i].materialNo) {
              isExit = true;
              //判断是否为当前货位
              if(this.activeName !== this.tasks[i].boxId){
                this.$alertWarning('当前扫描的二维码不属于该料盒');
                return;
              }
              //判断是否为截料扫码
              if(this.tasks[i].isForceFinish === true){
                this.backAfterCutting(this.tasks[i],tempArray[2],tempArray[1],tempArray[4]);
                return;
              }
              let options = {
                url: taskOutUrl,
                data: {
                  packListItemId: this.tasks[i].id,
                  materialId: tempArray[2],
                  quantity: tempArray[1],
                  supplierName: tempArray[4]
                }
              };
              axiosPost(options).then(response => {
                if (response.data.result === 200) {
                  this.fetchData(this.thisWindow);
                  this.successAudioPlay();
                  this.$alertSuccess('操作成功');
                  setTimeout(() => {
                    this.textToSpeak('已扫'+this.tasks[i].details.length+'盘还剩'
                      +this.tasks[i].reelNum+'盘');
                  },1000);
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
        backAfterCutting: function (taskItem,materialId, quantity,supplierName) {
          if (!this.isPending) {
            this.isPending = true;
            let options = {
              url: taskBackAfterCuttingUrl,
              data: {
                packingListItemId:taskItem.id,
                materialId: materialId,
                quantity: quantity,
                supplierName:supplierName
              }
            };
            axiosPost(options).then(res => {
              if (res.data.result === 200) {
                this.successAudioPlay();
                this.x = res.data.data.col;
                this.y = res.data.data.row;
                this.$alertSuccess("操作成功，请将料盘放回料盒");
              } else {
                this.failAudioPlay();
                errHandler(res.data);
              }
              this.isPending = false;
            }).catch(err => {
              this.$alertDanger(err);
              this.isPending = false;
            })
          }
        },
        //文字转语音提示
        textToSpeak:function(text){
          let synth = window.speechSynthesis;
          let utterThis = new SpeechSynthesisUtterance(text);
          utterThis.volume = 1;
          utterThis.pitch = 2;
          synth.speak(utterThis);
        }
      }
    }
  </script>

  <style scoped>
    .out-now {
      background: #fff;
      width: 100%;
      box-sizing: border-box;
      border: 1px solid #ebebeb;
      border-radius: 3px;
      transition: .2s;
      padding: 30px 30px;
      min-height: 500px;
    }

    .out-now:hover {
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)
    }

    .el-collapse-item .el-collapse-item__header {
      font-size: 16px;
    }

    #out-check {
      position: fixed;
      opacity: 0;
      height: 0;
      line-height: 0;
      border: none;
      padding: 0;
    }
  </style>
