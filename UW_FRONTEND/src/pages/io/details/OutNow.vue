<!--
  仓口当前任务的操作
-->

<template>
  <div>
    <video controls="controls" id="sAudio" hidden>
      <source src="./../../../assets/005-System05.ogg" type="video/ogg">
    </video>
    <video controls="controls" id="fAudio" hidden>
      <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
    </video>

    <global-tips :message="tipsComponentMsg" v-if="isTipsShow"/>
    <options/>
    <input type="text" title="scanner" id="out-check" v-model="scanText"
           @blur="confirmSetFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">

    <div class="io-now mt-1 mb-3" v-if="tipsMessage !==''">
      <p class="d-block text-center mt-5">{{tipsMessage}}</p>
    </div>
    <div class="io-now mt-1 mb-3" v-else>
      <div class="row m-3 align-content-start">
        <div class="card bg-light col-12 col-lg-6 col-xl-4 m-2">
          <div class="card-body row">
            <span class="col-form-label">任务: </span>
            <p class="card-text form-control">{{taskNowItems.fileName}}</p>
            <span class="col-form-label">料号: </span>
            <p class="card-text form-control">{{taskNowItems.materialNo}}</p>
            <span class="col-form-label">类型: </span>
            <p class="card-text form-control">{{taskNowItems.type}}</p>
            <span class="col-form-label">供应商: </span>
            <p class="card-text form-control">{{taskNowItems.supplierName}}</p>
          </div>
          <div class="card-body row">
            <div class="col pl-0">
              <span class="col-form-label">计划: </span>
              <p class="card-text form-control">{{taskNowItems.planQuantity}}</p>
            </div>
            <div class="col pr-0">
              <span class="col-form-label">实际: </span>
              <p class="card-text form-control">{{actualQuantity}}</p>
            </div>
          </div>
          <div class="card-body row">
            <div class="col pl-0">
              <span class="col-form-label">库存: </span>
              <p class="card-text form-control">{{taskNowItems.remainderQuantity}}</p>
            </div>
            <div class="col pr-0">
              <span class="col-form-label">历史已超发: </span>
              <p class="card-text form-control">{{taskNowItems.superIssuedQuantity}}</p>
            </div>
          </div>
          <div class="card-body row">
            <div class="col pr-0 pl-0">
              <span class="col-form-label">本次缺发数量/超发数量: </span>
              <p class="card-text form-control">{{overQuantity(taskNowItems.planQuantity,
                actualQuantity)}}</p>
            </div>
          </div>
        </div>
        <div class="card bg-light col-12 col-lg-5 col-xl-3 m-2">
          <div class="border-light row ml-auto mr-auto mt-4">
            <img src="static/img/finishedQRCode.png" alt="finished" class="img-style">
          </div>
          <span class="card-text text-center mt-auto">* 扫描此二维码或点击按钮以完成操作</span>
          <button class="btn btn-primary mb-4 mt-auto" @click="checkOverQuantity">操作完毕</button>
        </div>
      </div>
      <div class="row m-3">
        <div class="card bg-light col-12 col-xl-9 ml-2">
          <div class="row card-body mb-0 pb-1">
            <div class="col">
              <span class="text-center col-form-label">料盘: </span>
            </div>
            <div class="col">
              <span class="card-text text-center">数量: </span>
            </div>
            <div class="col">
              <span class="card-text text-center">生产日期: </span>
            </div>
            <div class="col">
              <span class="card-text text-center">操作: </span>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div class="row card-body" v-for="item in materialOutRecords">
            <div class="col pl-4">
              <p class="card-text">{{item.materialId}}</p>
            </div>
            <div class="col pl-4">
              <p class="card-text">{{item.quantity}}</p>
            </div>
            <div class="col pl-4">
              <p class="card-text">{{item.productionTime}}</p>
            </div>
            <div class="col pl-4">
              <div class="btn pl-1 pr-1" title="修改出库数" @click="confirmEdit(item)">
                <icon name="edit" scale="1.8"></icon>
              </div>
              <div class="btn pl-1 pr-1" title="删除" @click="confirmDelete(item)">
                <icon name="cancel" scale="1.8"></icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="mention-box" v-if="isMentions">
      <div class="mention-panel">
        <div class="form-row flex-column justify-content-between">
          <div class="form-group mb-0">
            <h3>提示：</h3>
          </div>
        </div>
        <div class="card-body">
          <p>该任务计划出库数量: {{taskNowItems.planQuantity}}</p>
          <p>实际出库数量: {{actualQuantity}}</p>
          <p>{{overQuantity(taskNowItems.planQuantity, actualQuantity)}}</p>
          <div class="dropdown-divider"></div>
          <p v-if="taskNowItems.planQuantity - actualQuantity > 0">
            当前实际出库数少于计划数，如果要将该任务条目置为已完成，请点击“确认完成”按钮</p>
          <p v-else>请确定是否出库</p>
        </div>
        <div class="dropdown-divider"></div>
        <div class="form-row justify-content-around">
          <button class="btn btn-secondary col mr-1 text-white" @click="isMentions = false">取消</button>
          <button class="btn btn-delay col ml-1 text-white" @click="delay"
                  v-if="taskNowItems.planQuantity - actualQuantity > 0">稍候再见
          </button>
          <button class="btn btn-primary col ml-1 text-white" @click="submit">确认完成</button>
        </div>
      </div>
    </div>
    <div v-if="isEditing" id="edit-window">
      <EditMaterialId :editData="editData" @getEditData="getEditData"></EditMaterialId>
    </div>
    <div v-if="isDeleting" id="delete-window">
      <div class="delete-panel">
        <div class="delete-panel-container form-row flex-column justify-content-between">
          <div class="form-row">
            <div class="form-group mb-0">
              <h3>确认删除：</h3>
            </div>
          </div>
          <div class="form-row">
            <div class="form-row col pl-2 pr-2">
              你正在删除料盘唯一码为 "{{editData.materialId}}" 的扫描记录，请确认是否删除
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div class="form-row justify-content-around">
            <a class="btn btn-secondary col mr-1 text-white" @click="isDeleting = false">取消</a>
            <a class="btn btn-danger col ml-1 text-white" @click="deleteMaterialRecord">确定</a>
          </div>
        </div>
      </div>
    </div>
    <cut-now v-if="isCutting" :item="taskNowItems" :currentWindowId="currentWindowId"
             :messageTip="tipsMessage"></cut-now>
  </div>
</template>

<script>
  import Options from './comp/QueryOptions'
  import EditMaterialId from './comp/EditMaterialId'
  import GlobalTips from './comp/GlobalTips'
  import {axiosPost} from "../../../utils/fetchData";
  import {mapGetters, mapActions} from 'vuex'
  import {
    robotBackUrl,
    taskWindowParkingItems,
    taskOutUrl,
    taskFinishUrl,
    taskDeleteMaterialRecordUrl
  } from "../../../config/globalUrl";
  import {errHandler} from "../../../utils/errorHandler";
  import eventBus from '@/utils/eventBus';
  import CutNow from "./comp/CutNow";
  import {outMaterialOutRecords} from "../../../store/getters";

  export default {
    name: "OutNow",
    components: {
      CutNow,
      Options,
      GlobalTips,
      EditMaterialId
    },
    data() {

      return {
        /* --item sample--
        {
          "result": 200,
          "data": {
            "id": 1,
            "fileName": "套料单1",
            "type": "出库",
            "materialNo": "KBG132123",
            "supplierName": "智锐得",
            "remainderQuantity": 20000,
            "superIssuedQuantity": 1000,
            "planQuantity": 10000,
            "actualQuantity": 11000,
            "details": [
             {
                "materialId": "29301282",
                "quantity": 2000,
                "productionTime" : "2018-09-06 00:00:00"
             },
            {
                "materialId": "39301282",
                "quantity": 8000,
                "productionTime" : "2018-09-06 00:00:00"
             },
            {
                "materialId": "49301282",
                "quantity": 3000,
                "productionTime" : "2018-09-06 00:00:00"
            }
          ]
        }
        }
           --sample ends-- */
        taskNowItems: {},

        scanText: '',
        tipsMessage: '无数据',
        tipsComponentMsg: '',
        isTipsShow: false,
        isPending: false,
        isMentions: false,
        isDeleting: false,
        isEditing: false,
        isCutting: false,
        patchAutoFinishStack: 0,
        editData: {
          packListItemId: "",
          materialId: "",
          quantity: 0,
          initQuantity: 0,
          productionTime: '',
        },
        materialOutRecords: [],
        actualQuantity: 0
      }
    },
    mounted() {
      eventBus.$on('closeEditPanel', () => {
        this.isEditing = false;
        this.setFocus();
      });
      eventBus.$on('initCutPanel', () => {
        this.isCutting = true;
      });
      eventBus.$on('closeCutPanel', () => {
        this.isCutting = false;
        this.setFocus();
      });

      if (this.editMaterialOutRecords !== []) {
        this.materialOutRecords = this.editMaterialOutRecords;
        this.countActualQuantity();
      }
      this.initData();
      this.setFocus();
      this.fetchData(this.currentWindowId);
      this.setCurrentOprType('2');
      window.g.PARKING_ITEMS_INTERVAL_OUT.push(setInterval(() => {
        if (this.currentWindowId !== '') {
          this.fetchData(this.currentWindowId)
        } else {
          this.initData();
        }
      }, 1000))
    },
    watch: {},
    computed: {
      ...mapGetters([
        'currentWindowId', 'user', 'configData', 'editMaterialOutRecords'
      ]),
    },
    methods: {
      ...mapActions(['setCurrentOprType', 'setEditMaterialOutRecords']),

      initData: function () {
        this.taskNowItems = {};
        this.scanText = '';
        this.tipsMessage = '无数据';
        this.tipsComponentMsg = '';
        this.isTipsShow = false;

      },
      fetchData: function (id) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: taskWindowParkingItems,
            data: {
              id: id
            }
          };
          axiosPost(options).then(response => {
            if (response.data.result === 200) {
              if (response.data.data) {
                this.taskNowItems = response.data.data;
                if (this.compareArr(this.materialOutRecords, this.taskNowItems.details) === false) {
                  this.materialOutRecords = this.taskNowItems.details;
                  this.actualQuantity = this.taskNowItems.actualQuantity;
                  this.setEditMaterialOutRecords(this.materialOutRecords);
                }
                this.tipsMessage = "";
              } else {
                this.taskNowItems = {};
                this.tipsMessage = "无数据"
              }
            } else if (response.data.result === 412) {
              this.taskNowItems = {};
              this.tipsMessage = response.data.data
            }
            this.isPending = false;
          })
        }

      },
      /*设置输入框焦点*/
      setFocus: function () {
        if (this.$route.path === '/io/outnow') {
          document.getElementById('out-check').focus();
        }
      },
      checkOverQuantity: function () {
        if (this.taskNowItems.planQuantity - this.taskNowItems.actualQuantity !== 0) {
          this.isMentions = true
        } else {
          this.setBack()
        }
      },
      /*扫码集中处理*/
      scannerHandler: function () {
        /*若扫描结果为叉车返回的页面二维码，则调用叉车回库*/
        if (this.scanText === "###finished###") {
          this.checkOverQuantity()
        } else if (this.scanText === "###JUMPTOCALL###") {
          this.$router.push('/io/preview')
        } else {
          /*sample: 03.01.0001@1000@1531817296428@A008@范例表@A-1@9@2018-07-17@*/
          /*对比料号是否一致*/
          let tempArray = this.scanText.split("@");
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
            let options = {
              url: taskOutUrl,
              data: {
                packListItemId: this.taskNowItems.id,
                materialId: tempArray[2],
                quantity: tempArray[1]
              }
            };
            axiosPost(options).then(response => {
              if (response.data.result === 200) {
                this.successAudioPlay();
                this.isTipsShow = true;
                this.tipsComponentMsg = true;
                setTimeout(() => {
                  this.isTipsShow = false;
                }, 3000)
              } else {
                this.failAudioPlay();
                this.isTipsShow = true;
                this.tipsComponentMsg = false;
                setTimeout(() => {
                  this.isTipsShow = false;
                }, 3000)
              }
            })
          }

        }
        this.scanText = "";
      },
      setBack: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: robotBackUrl,
            data: {
              id: this.taskNowItems.id,
              materialOutputRecords: JSON.stringify(this.materialOutRecords)
            }
          };
          axiosPost(options).then(response => {
            if (response.data.result === 200) {
              this.isTipsShow = true;
              this.tipsComponentMsg = true;
              setTimeout(() => {
                this.isTipsShow = false;
              }, 3000)
            } else {
              this.isTipsShow = true;
              this.tipsComponentMsg = false;
              setTimeout(() => {
                this.isTipsShow = false;
              }, 3000)
            }
            this.isPending = false;
          })
        }
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
      submit: function () {
        this.isMentions = false;
        if (this.taskNowItems.planQuantity - this.taskNowItems.actualQuantity > 0) {
          this.setFinishItem(true, this.setBack)
        } else {
          this.setBack();
        }
      },
      delay: function () {
        this.isMentions = false;
        this.setFinishItem(false, this.setBack)
      },
      //调用finishItem接口，用于需要出入库实际数与计划数不同的地方
      setFinishItem: function (boolean, callback) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: taskFinishUrl,
            data: {
              packListItemId: this.taskNowItems.id,
              isFinish: boolean
            }
          };
          axiosPost(options).then(response => {
            if (response.data.result === 200) {
              this.isPending = false;
              callback();
            } else {
              errHandler(response.data.result)
            }
            this.isPending = false;
          })
        }

      },
      //确认编辑
      confirmEdit: function (item) {
        this.editData.packListItemId = this.taskNowItems.id;
        this.editData.materialId = item.materialId;
        this.editData.quantity = item.quantity;
        for (let i = 0; i < this.taskNowItems.details.length; i++) {
          let obj = this.taskNowItems.details[i];
          if (obj.materialId === this.editData.materialId) {
            this.editData.initQuantity = obj.quantity;
            break;
          }
        }
        this.editData.productionTime = item.productionTime;
        this.isEditing = true;
        this.$nextTick(function () {
          eventBus.$emit('replaceFocus', true);
        })
      },
      //确认删除
      confirmDelete: function (item) {
        this.isDeleting = true;
        this.editData.packListItemId = this.taskNowItems.id;
        this.editData.materialId = item.materialId;
        this.editData.quantity = item.quantity;
      },
      //删除
      deleteMaterialRecord: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: taskDeleteMaterialRecordUrl,
            data: {
              packListItemId: this.editData.packListItemId,
              materialId: this.editData.materialId
            }
          };
          axiosPost(options).then(response => {
            this.isDeleting = false;
            if (response.data.result === 200) {
              this.$alertSuccess("删除成功");
              for (let i = 0; i < this.materialOutRecords.length; i++) {
                if (this.editData.materialId === this.materialOutRecords[i].materialId) {
                  this.materialOutRecords.splice(i, 1);
                  break;
                }
              }
              this.countActualQuantity();
            } else {
              errHandler(response.data.result);
            }
            this.isPending = false;
          })
        }
      },
      //判断是否setFocus
      confirmSetFocus: function () {
        if (this.isEditing === false && this.isCutting === false) {
          this.setFocus();
        }
      },
      // 获取修改的数据
      getEditData: function (thisData) {
        this.isEditing = false;
        let remainingQuantity = thisData.initQuantity - thisData.quantity;
        for (let i = 0; i < this.materialOutRecords.length; i++) {
          let item = this.materialOutRecords[i];
          if (item.materialId === thisData.materialId) {
            item.quantity = thisData.quantity;
          }
        }
        this.$alertSuccess("修改成功");
        this.setEditMaterialOutRecords(this.materialOutRecords);
        this.countActualQuantity();
        this.setFocus();
        this.printBarcode(thisData.materialId, remainingQuantity, thisData.productionTime);
      },
      // 计算实际数量和统计超发数量
      countActualQuantity: function () {
        let sum = 0;
        for (let i = 0; i < this.materialOutRecords.length; i++) {
          let item = this.materialOutRecords[i];
          sum = sum + item.quantity;
        }
        this.actualQuantity = sum;
      },
      // 比较两个数组
      compareArr: function (materialOutRecords, details) {
        if (materialOutRecords.length !== details.length) {
          return false;
        } else {
          for (let i = 0; i < materialOutRecords.length; i++) {
            if (materialOutRecords[i].materialId !== details[i].materialId) {
              return false;
            }
          }
        }
        return true;
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
      printBarcode: function (materialId, remainingQuantity, productionTime) {
        if (this.configData.printerIP === "") {
          this.$alertWarning("请在设置界面填写打印机IP");
          return;
        }
        let options = {
          url: window.g.PRINTER_URL,
          data: {
            printerIP: this.configData.printerIP,
            materialId: materialId,
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
      }
    }
  }
</script>

<style scoped>
  .io-now {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
    min-height: 500px;
  }

  .img-style {
    width: 100%;
    height: 100%;
  }

  #out-check {
    opacity: 0;
    height: 0;
    line-height: 0;
    border: none;
    padding: 0;
    position: fixed;
  }

  .mention-box {
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 1001;
  }

  .mention-panel {
    background: #ffffff;
    min-height: 220px;
    max-width: 600px;
    z-index: 1002;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }

  .btn-delay {
    color: #fff;
    background-color: #ffb85b;
    border-color: #ffb85b;
  }

  .btn-delay:hover {
    color: #fff;
    background-color: #e6a654;
    border-color: #f5ad5c;
  }

  .btn-delay:focus, .btn-delay.focus {
    box-shadow: 0 0 0 0.2rem rgba(252, 180, 93, 0.5);
  }

  .btn-delay.disabled, .btn-delay:disabled {
    color: #fff;
    background-color: #ffb85b;
    border-color: #ffb85b;
  }

  .btn-delay:not(:disabled):not(.disabled):active, .btn-delay:not(:disabled):not(.disabled).active,
  .show > .btn-delay.dropdown-toggle {
    color: #fff;
    background-color: #f1aa5b;
    border-color: #fdb35f;
  }

  .btn-delay:not(:disabled):not(.disabled):active:focus, .btn-delay:not(:disabled):not(.disabled).active:focus,
  .show > .btn-delay.dropdown-toggle:focus {
    box-shadow: 0 0 0 0.2rem rgba(252, 180, 93, 0.5);
  }

  .delete-panel {
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 101;
  }

  .delete-panel-container {
    background: #ffffff;
    height: 220px;
    width: 400px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
