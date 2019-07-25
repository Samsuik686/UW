<template>
  <div class="check">
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
        <el-select v-model="thisWindow" placeholder="仓口" :disabled="windowsList.length === 0">
          <el-option value="" :label="windowsList.length === 0 ? '无非空闲仓口':'请选择'"></el-option>
          <el-option v-for="item in windowsList" :value="item.id" :label="item.id" :key="item.id"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <el-collapse v-model="activeName" accordion class="in-collapse" v-if="checkTasks.length > 0">
      <el-collapse-item v-for="(item,index) in checkTasks"
                        :key="index"
                        :title='item.boxId === null?item.goodsLocationName+" "+"/"+" "+"无数据":item.goodsLocationName+" "+"/"+" "+item.boxId'
                        :name="item.boxId === null?String(item.goodsLocationId):String(item.boxId)">
        <check-task-details :checkTaskItem="item"
                            :boxId = "item.boxId"
                            :taskId ="item.taskId"
                            :windowId = "item.windowId"
                            :activeMaterialId = "activeMaterialId"
                            :activeQuantity = "activeQuantity"
                            :disabledMaterialId = "disabledMaterialId"
                            :isScanner = "isScanner"
                            :activeName="String(activeName)"></check-task-details>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
  import eventBus from "../../../utils/eventBus";
  import {mapGetters, mapActions} from 'vuex'
  import {getPackingInventoryUrl, taskWindowsUrl} from "../../../config/globalUrl";
  import {axiosPost} from "../../../utils/fetchData";
  import {errHandler} from "../../../utils/errorHandler";
  import CheckTaskDetails from "./comp/CheckTaskItemDetails";
  import {handleScanText} from "../../../utils/scan";

  export default {
    name: "OperationModule",
    components: {CheckTaskDetails},
    data() {
      return {
        scanText: '',
        windowsList: [],
        thisWindow: '',
        windowType: '4',
        checkTasks: [],
        myTimeOut: '',
        isTimeOut: false,
        activeName: '',
        isInputFocus:false,
        activeMaterialId:'',
        activeQuantity:-1,
        isPending:false,
        disabledMaterialId:'',
        isScanner:false
      }
    },
    beforeDestroy() {
      this.clearCheckTimeOut();
    },
    mounted() {
      this.setPreset();
      this.setFocus();
      if (this.thisWindow !== '') {
        this.setLoading(true);
        this.fetchData(this.thisWindow);
      }
      eventBus.$on('setIsInputFocus',val => {
        this.isInputFocus = val
      });
      eventBus.$on('refreshCheckTask',() => {
        if(this.thisWindow === ''){
          return;
        }
        this.fetchData(this.thisWindow);
      });
      eventBus.$on('setDisabledMaterialId',materialId => {
        this.disabledMaterialId = materialId;
      });
      eventBus.$on('setActiveMaterialId',materialId => {
        this.activeMaterialId = '';
        this.$nextTick(function(){
          this.isScanner = false;
          this.activeMaterialId = materialId;
        })
      });
      eventBus.$on('setActiveQuantity',() => {
        this.activeQuantity = -1;
      });
    },
    computed: {
      ...mapGetters(['checkWindowData'])
    },
    watch: {
      thisWindow: function (val) {
        if (val !== '') {
          this.activeName = '';
          this.setLoading(true);
          this.setCheckTimeOut();
          setTimeout(function(){
            eventBus.$emit('changeInputVal',true);
          },2000);
        }
      },
      isInputFocus:function(val){
        if(val === false){
          if(document.getElementById('scanner-check')){
            document.getElementById('scanner-check').focus();
          }
        }
      }
    },
    methods: {
      ...mapActions(['setLoading', 'setCheckWindowData']),
      //获取仓口列表
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
          this.$alertDanger('连接超时，请刷新重试');
        })
      },
      fetchData: function (id) {
        if (!id) {
          this.$alertWarning('请选择仓口');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: getPackingInventoryUrl,
            data: {
              windowId: id
            }
          };
          axiosPost(options).then(response => {
            this.setLoading(false);
            this.isPending = false;
            if (response.data.result === 200) {
              if (response.data.data === null) {
                this.checkTasks = [];
                this.setCheckTimeOut();
                return;
              }
              this.checkTasks = response.data.data;
              this.disabledMaterialId = '';
              //this.clearCheckTimeOut();
            } else {
              this.clearCheckTimeOut();
              if (response.data.result === 412) {
                let tempUrl = this.$route.fullPath;
                this.$router.push('_empty');
                this.$router.replace(tempUrl);
              } else {
                errHandler(response.data);
              }
            }
          })
            .catch(err => {
              this.isPending = false;
              console.log(err);
              this.clearCheckTimeOut();
              this.$alertDanger('请求超时，请刷新重试');
              this.setLoading(false)
            })
        }
      },
      setCheckTimeOut: function () {
        if (this.isTimeOut === true) {
          this.clearCheckTimeOut();
        }
        this.isTimeOut = true;
        let that = this;
        this.myTimeOut = setInterval(function () {
          that.fetchData(that.thisWindow);
        }, 1000);
      },
      clearCheckTimeOut: function () {
        this.isTimeOut = false;
        clearTimeout(this.myTimeOut);
        this.myTimeOut = null;
      },
      setFocus: function () {
        if(this.isInputFocus === false){
          if(document.getElementById('scanner-check')){
            document.getElementById('scanner-check').focus();
          }
        }
      },
      scannerHandler: function () {
        let scanText = this.scanText;
        this.scanText = '';
        let boxArr = scanText.split("@");
        for (let i = 0; i < this.checkTasks.length; i++) {
          if (Number(boxArr[0]) === this.checkTasks[i].boxId) {
            this.successAudioPlay();
            this.activeName = String(this.checkTasks[i].boxId);
            return;
          }
        }

        if(this.activeName === ''){
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
        /*对比料号是否一致*/
        let tempArray = scanText.split("@");
        let materialId = tempArray[2];
        let quantity = tempArray[1];
        let activeItem = [];
        this.checkTasks.map((item) => {
          if(Number(this.activeName) === Number(item.boxId)){
            activeItem = item.list;
          }
        });
        if(activeItem.length === 0){
          return;
        }
        let isMaterialExit = false;
        for(let i=0;i<activeItem.length;i++){
          if(materialId === activeItem[i].materialId){
            this.successAudioPlay();
            this.activeMaterialId = '';
            this.activeQuantity = -1;
            this.$nextTick(function(){
              this.activeMaterialId = materialId;
              this.activeQuantity = Number(quantity);
              this.isScanner = true;
            });
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
      }
    }
  }
</script>

<style scoped>
  .check {
    background: #fff;
    width: 100%;
    box-sizing: border-box;
    border: 1px solid #ebebeb;
    border-radius: 3px;
    transition: .2s;
    padding: 30px 30px;
    min-height: 500px;
  }

  #scanner-check {
    position: fixed;
    opacity: 0;
    height: 0;
    line-height: 0;
    border: none;
    padding: 0;
  }
</style>
