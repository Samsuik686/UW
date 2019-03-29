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
        <h4 style="min-width:700px">
          截料入库：请扫描料盘二维码，并将剩余的物料放回料盒
        </h4>

        <div class="option-step">
          <div class="mt-1 mb-3" v-if="messageTip !== ''">
            <p class="d-block text-center mt-5">{{messageTip}}</p>
          </div>
          <div v-else>
            <div class="row m-3 align-content-start">
              <div class="card bg-light col-12 col-lg-6 col-xl-7 m-2">
                <div class="card-body row">
                  <div class="col pl-0">
                    <span class="col-form-label">任务: </span>
                    <p class="card-text form-control">{{taskNowItems.fileName}}</p>
                  </div>
                  <div class="col pr-0 pl-0">
                    <span class="col-form-label">料号: </span>
                    <p class="card-text form-control">{{taskNowItems.materialNo}}</p>
                  </div>
                </div>
                <div class="card-body row">
                  <span class="col-form-label">规格: </span>
                  <p class="card-text form-control">{{taskNowItems.specification}}</p>
                </div>
                <div class="card-body row">
                  <div class="col pl-0">
                    <span class="col-form-label">类型: </span>
                    <p class="card-text form-control">{{taskNowItems.type}}</p>
                  </div>
                  <div class="col pr-0 pl-0">
                    <span class="col-form-label">供应商: </span>
                    <p class="card-text form-control">{{taskNowItems.supplierName}}</p>
                  </div>
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
                  <div class="col pl-0">
                    <span class="col-form-label">库存: </span>
                    <p class="card-text form-control">{{taskNowItems.remainderQuantity}}</p>
                  </div>
                  <div class="col pr-0 pl-0">
                    <span class="col-form-label">本次缺发数量/超发数量:</span>
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
                    <span class="card-text text-center">出库数量: </span>
                  </div>
                  <div class="col">
                    <span class="card-text text-center">剩余数量: </span>
                  </div>
                  <div class="col">
                    <span class="card-text text-center">生产日期: </span>
                  </div>
                  <div class="col">
                    <span class="card-text text-center">是否在盒内: </span>
                  </div>
                </div>
                <div class="dropdown-divider"></div>
                <div class="row card-body" v-for="item in taskNowItems.details">
                  <div class="col">
                    <p class="card-text">{{item.materialId}}</p>
                  </div>
                  <div class="col">
                    <p class="card-text">{{item.quantity}}</p>
                  </div>
                  <div class="col">
                    <p class="card-text">{{item.remainderQuantity}}</p>
                  </div>
                  <div class="col">
                    <p class="card-text">{{item.productionTime}}</p>
                  </div>
                  <div class="col">
                    <p class="card-text">{{item.isInBoxString}}</p>
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
  import {mapGetters} from 'vuex'
  import eventBus from "../../../../utils/eventBus";
  import GlobalTips from "./GlobalTips";
  import Options from "../../../logs/details/comp/QueryOptions";
  import {robotBackUrl,taskBackAfterCuttingUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";

  export default {
    name: "CutNow",
    props: {
      item: Object,
      currentWindowId: Number,
      messageTip: String
    },
    data() {
      return {
        isPending: false,
        tipsComponentMsg: '',
        isTipsShow: false,
        scanText: '',
        isSetFocus: false,
        option: false,
        materialId:''
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
      },
      ...mapGetters([
         'user', 'configData'
      ]),
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
        let scanText = this.scanText;
        this.scanText = "";
        let tempArray = scanText.split("@");
        if (scanText === "###finished###") {
          this.robotBack();
          return;
        }
        let text = tempArray[0].replace('\ufeff','');
        if(text !== this.taskNowItems.materialNo){
          this.failAudioPlay();
          this.isTipsShow = true;
          this.tipsComponentMsg = false;
          setTimeout(() => {
            this.isTipsShow = false;
          }, 3000);
          return;
        }
        this.materialId = tempArray[2];
        this.backAfterCutting(tempArray[2],tempArray[1]);
      },
      overQuantity: function (plan, actual) {
        let overQty = plan - actual;
        if (plan > actual) {
          return ("缺发数量: " + Math.abs(overQty)).toString();
        } else if (plan < actual) {
          return ("超发数量: " + Math.abs(overQty)).toString();
        } else {
          return "--"
        }
      },
      backAfterCutting: function (materialId, quantity) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: taskBackAfterCuttingUrl,
            data: {
              packingListItemId: this.taskNowItems.id,
              materialId: materialId,
              quantity: quantity
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.successAudioPlay();
              this.$alertSuccess("操作成功，请将料盒放回料盒");
              this.printBarcode();
              this.isTipsShow = true;
              this.tipsComponentMsg = true;
              setTimeout(() => {
                this.isTipsShow = false;
              }, 3000)
            } else {
              this.failAudioPlay();
              errHandler(res.data);
              /*this.isTipsShow = true;
              this.tipsComponentMsg = false;
              setTimeout(() => {
                this.isTipsShow = false;
              }, 3000)*/
            }
            this.isPending = false;
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.$alertDanger(JSON.stringify(err));
              this.isPending = false;
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
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.$alertDanger(JSON.stringify(err));
              this.isPending = false;
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
      // 请求打印
      printBarcode: function () {
        if (this.configData.printerIP === "") {
          this.$alertWarning("请在设置界面填写打印机IP");
          return;
        }
        let remainingQuantity, productionTime;
        for(let i =0;i<this.taskNowItems.details.length;i++){
          let item = this.taskNowItems.details[i];
          if(this.materialId === item.materialId){
            remainingQuantity = item.remainingQuantity;
            productionTime = item.productionTime;
            break;
          }
        }
        let options = {
          url: window.g.PRINTER_URL,
          data: {
            printerIP: this.configData.printerIP,
            materialId: this.materialId,
            materialNo: this.taskNowItems.materialNo,
            remainingQuantity: remainingQuantity,
            productDate: productionTime,
            user: this.user,
            supplier: this.taskNowItems.supplierName
          }
        };
        axiosPost(options).then(response => {
          if (response.data.code === 200) {
            this.$alertSuccess(response.data.msg);
          } else if (response.data.code === 400) {
            this.$alertWarning(response.data.msg);
          } else {
            this.$alertDanger(response.data.msg);
          }
        }).catch(err => {
          if (JSON.stringify(err) !== '{}') {
            this.$alertDanger(JSON.stringify(err))
          }
        })
      },
    }
  }
</script>

<style scoped>
  .cut-panel {
    position:fixed;
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
    min-width:700px;
  }

  .img-style {
    width: 100%;
    height: 100%;
  }

  .card-text{
    height:auto;
    line-height:normal;
  }
  .col{
    align-self:center;
  }
</style>
