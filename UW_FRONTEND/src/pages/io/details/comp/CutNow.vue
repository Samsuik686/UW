<!--
  截料操作
-->

<template>
  <div class="cut-panel">
    <div class="box">
      <div class="content">
        <video controls="controls" id="sAudio" hidden>
          <source src="./../../../../assets/005-System05.ogg" type="video/ogg">
        </video>
        <video controls="controls" id="fAudio" hidden>
          <source src="./../../../../assets/141-Burst01.ogg" type="video/ogg">
        </video>

        <global-tips :message="tipsComponentMsg" v-if="isTipsShow"/>
        <input type="text" title="scanner" id="cut-check" v-model="scanText"
               @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">

        <div class="btn-step">
          <button class="btn" :class="{'btn-primary':!option}" @click="option = false">呼叫叉车</button>
          <div id="vertical-divider" class="ml-3 mr-3"></div>
          <button class="btn" :class="{'btn-primary':option}" @click="option = true">物料返库</button>
        </div>
        <div class="dropdown-divider"></div>
        <div class="option-step">
          <div class="mt-1 mb-3" v-if="!option">
            <p class="d-block text-center mt-5">{{tipsMessage}}</p>
          </div>
          <div class="mt-1 mb-3" v-else-if="option === true && messageTip !== ''">
            <p class="d-block text-center mt-5">{{messageTip}}</p>
          </div>
          <div v-else>
            <div class="row m-3 align-content-start">
              <div class="card bg-light col-12 col-lg-6 col-xl-7 m-2">
                <div class="card-body row">
                  <span class="col-form-label">任务: </span>
                  <p class="card-text form-control">{{taskNowItems.fileName}}</p>
                  <span class="col-form-label">料号: </span>
                  <p class="card-text form-control">{{taskNowItems.materialNo}}</p>
                  <span class="col-form-label">类型: </span>
                  <p class="card-text form-control">{{taskNowItems.type}}</p>
                </div>
                <div class="card-body row">
                  <div class="col pl-0">
                    <span class="col-form-label">计划: </span>
                    <p class="card-text form-control">{{taskNowItems.planQuantity}}</p>
                  </div>
                  <div class="col pr-0">
                    <span class="col-form-label">实际: </span>
                    <p class="card-text form-control">{{taskNowItems.actualQuantity}}</p>
                  </div>
                </div>
                <div class="card-body row">
                  <div class="col pr-0 pl-0">
                    <span class="col-form-label">欠入数量/超入数量:</span>
                    <p class="card-text form-control">{{overQuantity(taskNowItems.planQuantity,
                      taskNowItems.actualQuantity)}}</p>
                  </div>
                </div>
              </div>
              <div class="card bg-light col-12 col-lg-5 col-xl-4 m-2">
                <div class="border-light row ml-auto mr-auto mt-4">
                  <img src="static/img/finishedQRCode.png" alt="finished" class="img-style">
                </div>
                <span class="card-text text-center mt-auto">* 扫描此二维码或点击按钮以完成操作</span>
                <button class="btn btn-primary mb-4 mt-auto" @click="robotBack(taskNowItems.id)">操作完毕</button>
              </div>
            </div>
            <div class="row m-3">
              <div class="card bg-light col-12 col-xl-11 ml-2">
                <div class="row card-body mb-0 pb-1">
                  <div class="col">
                    <span class="text-center col-form-label">料盘: </span>
                  </div>
                  <div class="col">
                    <span class="card-text text-center">数量: </span>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <div class="row card-body" v-for="item in taskNowItems.details">
                  <div class="col pl-4">
                    <p class="card-text">{{item.materialId}}</p>
                  </div>
                  <div class="col pl-4">
                    <p class="card-text">{{item.quantity}}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div id="cancel-btn" class="ml-4" @click="closePanel">
      <icon name="cancel" scale="4" style="color: #fff;"></icon>
    </div>
  </div>
</template>

<script>
  import eventBus from "../../../../utils/eventBus";
  import GlobalTips from "./GlobalTips";
  import Options from "../../../logs/details/comp/QueryOptions";
  import {robotBackUrl, robotCallUrl, taskBackAfterCuttingUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";

  export default {
    name: "CutNow",
    props: ['item', 'currentWindowId', "messageTip"],
    data() {
      return {
        isPending: false,
        tipsComponentMsg: '',
        isTipsShow: false,
        scanText: '',
        tipsMessage: '请扫描料盘呼叫叉车',
        isSetFocus: false,
        option: false,
      }
    },
    components: {
      Options,
      GlobalTips
    },
    mounted() {
      this.isSetFocus = true;
      this.setFocus();
    },
    computed: {
      taskNowItems: function () {
        return this.item;
      }
    },
    methods: {
      closePanel: function () {
        this.isSetFocus = false;
        eventBus.$emit("closeCutPanel", true);
      },
      setFocus: function () {
        if (this.isSetFocus === true) {
          document.getElementById('cut-check').focus();
        }
      },
      scannerHandler: function () {
        /*sample: 03.01.0001@1000@1531817296428@A008@范例表@A-1@9@2018-07-17@*/
        let tempArray = this.scanText.split("@");
        if (!this.option) {
          this.robotCall(tempArray[0]);
        } else {
          if (this.scanText === "###finished###") {
            this.robotBack();
          } else {
            console.log(tempArray[0],this.taskNowItems.materialNo);
            console.log(tempArray[0] === this.taskNowItems.materialNo);
            if (tempArray[0] !== this.taskNowItems.materialNo) {
              this.failAudioPlay();
              this.isTipsShow = true;
              this.tipsComponentMsg = false;
              setTimeout(() => {
                this.isTipsShow = false;
              }, 3000);
              this.scanText = "";
              return;
            } else {
              this.backAfterCutting(tempArray[2]);
            }
          }
        }
      },
      overQuantity: function (plan, actual) {
        let overQty = plan - actual;
        if (plan > actual) {
          return ("欠入库数量: " + Math.abs(overQty)).toString();
        } else if (plan < actual) {
          return ("超入库数量: " + Math.abs(overQty)).toString();
        } else {
          return "--"
        }
      },
      robotCall: function (materialNo) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: robotCallUrl,
            data: {
              id: this.currentWindowId,
              no: materialNo
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess("调用成功");
            } else {
              this.$alertWarning(res.data.data)
            }
            this.isPending = false;
            this.scanText = "";
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.isPending = false;
              this.scanText = "";
              this.$alertDanger(JSON.stringify(err))
            }
          })
        }
      },
      backAfterCutting: function (materialId) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: taskBackAfterCuttingUrl,
            data: {
              packingListItemId: this.taskNowItems.id,
              materialId: materialId
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.successAudioPlay();
              this.$alertSuccess("操作成功");
              this.isTipsShow = true;
              this.tipsComponentMsg = true;
              setTimeout(() => {
                this.isTipsShow = false;
              }, 3000)
            } else {
              this.failAudioPlay();
              this.$alertWarning(res.data.data);
              this.isTipsShow = true;
              this.tipsComponentMsg = false;
              setTimeout(() => {
                this.isTipsShow = false;
              }, 3000)
            }
            this.isPending = false;
            this.scanText = "";
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.isPending = false;
              this.scanText = "";
              this.$alertDanger(JSON.stringify(err))
            }
          })
        }
      },
      robotBack: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: robotBackUrl,
            data: {
              id: this.taskNowItems.id
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess("操作成功");
            } else {
              this.$alertWarning(res.data.data)
            }
            this.isPending = false;
            this.scanText = "";
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.isPending = false;
              this.scanText = "";
              this.$alertDanger(JSON.stringify(err))
            }
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
      },
    },
  }
</script>

<style scoped>
  .cut-panel {
    position: fixed;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 101;
  }

  .box {
    display: flex;
    width: 70%;
    height: 90%;
  }

  .content {
    width: 100%;
    height: 100%;
    border-radius: 10px;
    background: #fff;
    z-index: 102;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
    overflow-y: auto;
  }

  #cancel-btn {
    height: 90%;
    cursor: pointer;
  }

  .btn-step {
    display: flex;
    margin: 5px;
  }

  #vertical-divider {
    width: 2px;
    border-left: 1px solid #e9ecef;
  }

 #cut-check {
    position: fixed;
    opacity: 0;
    height: 0;
    line-height: 0;
    border: none;
    padding: 0;
  }

  .option-step {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
    min-height: 300px;
  }

  .img-style {
    width: 100%;
    height: 100%;
  }
</style>
