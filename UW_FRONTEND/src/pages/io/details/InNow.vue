<!--
  仓口当前任务的操作
-->

<template>
  <div>
    <global-tips :message="tipsComponentMsg" v-if="isTipsShow"/>
    <options/>
    <input type="text" title="scanner" id="in-check" v-model="scanText"
           @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">

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
              <p class="card-text form-control">{{overQuantity(taskNowItems.planQuantity, taskNowItems.actualQuantity)}}</p>
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
    <div class="mention-box" v-if="isMentions">
      <div class="mention-panel">
        <div class="form-row flex-column justify-content-between">
          <div class="form-group mb-0">
            <h3>提示：</h3>
          </div>
        </div>
        <div class="card-body">
          <p>该任务计划入库数量: {{taskNowItems.planQuantity}}</p>
          <p>实际入库数量: {{taskNowItems.actualQuantity}}</p>
          <p>{{overQuantity(taskNowItems.planQuantity, taskNowItems.actualQuantity)}}</p>
          <p>请确认是否入库</p>
        </div>
        <div class="dropdown-divider"></div>
        <div class="form-row justify-content-around">
          <button class="btn btn-secondary col mr-1 text-white" @click="isMentions = false">取消</button>
          <button class="btn btn-primary col ml-1 text-white" @click="submit">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import Options from './comp/QueryOptions'
  import GlobalTips from './comp/GlobalTips'
  import {axiosPost} from "../../../utils/fetchData";
  import {mapGetters, mapActions} from 'vuex'
  import {robotBackUrl, taskWindowParkingItems, taskIOUrl} from "../../../config/globalUrl";

  export default {
    name: "InNow",
    components: {
      Options,
      GlobalTips
    },
    data() {

      return {
        /* --item sample--
          "data": {
          "id": 1,
          "fileName": "套料单1",
          "type": "出库",
          "materialNo": "KBG132123",
          "planQuantity": 10000,
          "actualQuantity": 10100,
          "details": [
                  {
                    "materialId": "29301282",
                    "quantity": 2000
                  }
               ]
           }
           --sample ends-- */
        taskNowItems: {},

        scanText: '',
        tipsMessage: '无数据',
        tipsComponentMsg: '',
        isTipsShow: false,
        isPending: false,
        isMentions: false,

        patchAutoFinishStack: 0,


      }
    },
    mounted() {
      this.initData();
      this.setFocus();
      this.fetchData(this.currentWindowId);
      this.setCurrentOprType('1');
      window.g.PARKING_ITEMS_INTERVAL_IN.push(setInterval(() => {
        if (this.currentWindowId !== '') {
          this.fetchData(this.currentWindowId)
          //this.autoFinish(); //patch !
        } else {
          this.initData();
        }
      }, 1000))
    },
    watch: {},
    computed: {
      ...mapGetters([
        'currentWindowId'
      ]),

    },
    methods: {
      /*patch! wait for delete*/
      autoFinish: function () {
        if (JSON.stringify(this.taskNowItems) !== '{}') {
          this.patchAutoFinishStack += 1;
        }
        if (this.patchAutoFinishStack > 10 && JSON.stringify(this.taskNowItems) !== '{}') {
          this.patchAutoFinishStack = 0;
          this.setBack();
        }

      },

      ...mapActions(['setCurrentOprType']),

      initData: function () {
        this.taskNowItems = {};
        this.scanText = '';
        this.tipsMessage = '无数据';
        this.tipsComponentMsg = '';
        this.isTipsShow = false;

      },

      fetchData: function (id) {
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
              this.tipsMessage = ""
            } else {
              this.taskNowItems = {};
              this.tipsMessage = "无数据"
            }
          } else if (response.data.result === 412) {
            this.taskNowItems = {};
            this.tipsMessage = response.data.data
          }
        })

      },
      /*设置输入框焦点*/
      setFocus: function () {
        if (this.$route.path === '/io/innow') {
          document.getElementById('in-check').focus();
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
            this.isTipsShow = true;
            this.tipsComponentMsg = false;
            setTimeout(() => {
              this.isTipsShow = false;
            }, 3000);
            this.scanText = "";
            return;
          } else {
            let options = {
              url: taskIOUrl,
              data: {
                packListItemId: this.taskNowItems.id,
                materialId: tempArray[2],
                quantity: tempArray[1]
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
              id: this.taskNowItems.id
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
          return ("欠入库数量: " + Math.abs(overQty)).toString();
        } else if (plan < actual) {
          return ("超入库数量: " + Math.abs(overQty)).toString();
        } else {
          return "--"
        }
      },
      submit: function () {
        this.isMentions = false;
        this.setBack();
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

  #in-check {
    opacity: 0;
    height: 0;
    line-height: 0;
    border: none;
    padding: 0;
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
</style>
