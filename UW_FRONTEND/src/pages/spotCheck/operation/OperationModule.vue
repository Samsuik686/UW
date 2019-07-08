<template>
  <div class="spot-check">
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
    <el-collapse v-model="activeName" accordion class="in-collapse" v-if="spotCheckData.length > 0">
      <el-collapse-item v-for="(item,index) in spotCheckData"
                        :key="index"
                        :title='item.boxId === null?item.goodsLocationName+" "+"/"+" "+"无数据":item.goodsLocationName+" "+"/"+" "+item.boxId'
                        :name="item.boxId === null?String(item.goodsLocationId):String(item.boxId)">
        <spot-check-task-item-details
          :isScan="isScan"
          :activeName="String(activeName)"
          :activeMaterialId = "activeMaterialId"
          :spotCheckItem="item" >
        </spot-check-task-item-details>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
  import eventBus from "../../../utils/eventBus";
  import {getPackingSampleMaterialInfoUrl, scanMaterialUrl, taskWindowsUrl} from "../../../config/globalUrl";
  import {axiosPost} from "../../../utils/fetchData";
  import {errHandler} from "../../../utils/errorHandler";
  import {handleScanText} from "../../../utils/scan";
  import SpotCheckTaskItemDetails from "./comp/SpotCheckTaskItemDetails";
  import {mapActions} from 'vuex'

  export default {
    name: "OperationModule",
    components: {SpotCheckTaskItemDetails},
    data() {
      return {
        scanText: '',
        windowsList: [],
        thisWindow: '',
        windowType: '5',
        myTimeOut: '',
        isTimeOut: false,
        isPending: false,
        spotCheckData: [],
        activeName:'',
        activeMaterialId:'',
        isScan:false
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
        this.setCheckTimeOut();
      }
      eventBus.$on('refreshSpotCheckTask',() => {
        this.setCheckTimeOut();
      });
      eventBus.$on('setIsScan',() => {
        this.isScan = false;
      });
    },
    watch: {
      thisWindow: function (val) {
        if (val !== '') {
          this.activeName = '';
          this.setLoading(true);
          this.setCheckTimeOut();
        }
      },
    },
    methods: {
      ...mapActions(['setLoading']),
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
      setFocus: function () {
        if (document.getElementById('scanner-check')) {
          document.getElementById('scanner-check').focus();
        }
      },
      scannerHandler: function () {
        let scanText = this.scanText;
        this.scanText = '';
        let boxArr = scanText.split("@");
        for (let i = 0; i < this.spotCheckData.length; i++) {
          if (Number(boxArr[0]) === this.spotCheckData[i].boxId) {
            this.successAudioPlay();
            this.activeName = String(this.spotCheckData[i].boxId);
            return;
          }
        }
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
        /*对比料号是否一致*/
        let tempArray = scanText.split("@");
        let materialId = tempArray[2];
        let activeItem = [];
        let groupId = '';
        this.spotCheckData.map((item) => {
          if(Number(this.activeName) === Number(item.boxId)){
            activeItem = item.list;
            groupId = item.groupId;
          }
        });
        if(activeItem.length === 0){
          return;
        }
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
      fetchData: function () {
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
            this.isPending = false;
            this.setLoading(false);
            if (response.data.result === 200) {
              if (response.data.data === null) {
                this.spotCheckData = [];
                return;
              }
              this.spotCheckData = response.data.data;
            } else {
              errHandler(response.data);
              this.clearCheckTimeOut();
            }
          })
            .catch(err => {
              console.log(err);
              this.isPending = false;
              this.$alertDanger('请求超时，请刷新重试');
              this.clearCheckTimeOut();
              this.setLoading(false);
            })
        }
      },
      setCheckTimeOut: function () {
        if (this.isTimeOut === true) {
          this.clearCheckTimeOut();
        }
        this.isTimeOut = true;
        let that = this;
        this.fetchData();
        this.myTimeOut = setInterval(function () {
          that.fetchData();
        }, 1000);
      },
      clearCheckTimeOut: function () {
        this.isTimeOut = false;
        clearTimeout(this.myTimeOut);
        this.myTimeOut = null;
      },
      scanMaterial:function(materialId,groupId){
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:scanMaterialUrl,
            data:{
              materialId:materialId,
              groupId:groupId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.successAudioPlay();
              this.$alertSuccess('操作成功');
              this.activeMaterialId = materialId;
              this.isScan = true;
              this.fetchData();
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.isPending = false;
            this.$alertDanger('连接超时，请刷新重试');
          })
        }
      }
    }
  }
</script>

<style scoped>
  .spot-check {
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
