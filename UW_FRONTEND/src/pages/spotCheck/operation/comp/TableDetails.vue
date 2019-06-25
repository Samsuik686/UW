<template>
  <div class="main-details mt-1 mb-3">
    <video controls="controls" id="sAudio" hidden>
      <source src="./../../../../assets/005-System05.ogg" type="video/ogg">
    </video>
    <video controls="controls" id="fAudio" hidden>
      <source src="./../../../../assets/141-Burst01.ogg" type="video/ogg">
    </video>
    <input type="text" title="scanner" id="scanner-check" v-model="scanText"
           @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
    <datatable
      v-bind="$data"
    ></datatable>
    <div class="form-group row align-items-end return-btn">
      <button class="btn btn-primary ml-3 mr-4" @click="backSampleBox" :disabled="groupId === ''">叉车回库</button>
    </div>
  </div>
</template>

<script>
  import eventBus from "../../../../utils/eventBus";
  import {mapActions, mapGetters} from "vuex";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import OperationOptions from './subcomp/OperationOptions';
  import MaterialHighLight from './subcomp/MaterialHighLight'
  import {backBoxSampleTaskUrl, getPackingSampleMaterialInfoUrl} from "../../../../config/globalUrl";
  import {handleScanText} from "../../../../utils/scan";

  export default {
    name: "TableDetails",
    data() {
      return {
        fixHeaderAndSetBodyMaxHeight: 650,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed',
          'white-space': 'pre-wrap'
        },
        HeaderSettings: false,
        data: [],
        columns: [],
        total: 0,
        query: {"limit": 20, "offset": 0},
        Pagination: false,
        isPending: false,
        groupId: '',
        //定时器
        myTimeOut: '',
        //是否启用定时器
        isTimeOut: false,
        scanText: '',
      }
    },
    components: {
      OperationOptions,
      MaterialHighLight
    },
    created() {
      this.init();
    },
    beforeDestroy() {
      this.clearCheckTimeOut();
    },
    mounted() {
      this.setFocus();
      if (this.spotWindowId !== '') {
        this.setCheckTimeOut();
      }
      eventBus.$on('refreshSampleInfo', () => {
        this.setCheckTimeOut();
      });
    },
    watch: {
      spotWindowId: function (val) {
        if (val !== '') {
          this.groupId = '';
          this.setCheckTimeOut();
        }
      }
    },
    computed: {
      ...mapGetters(['spotWindowId'])
    },
    methods: {
      ...mapActions(['setLoading', 'setSpotMaterialId']),
      init: function () {
        this.columns = [
          {field: 'showId', title: '序号', colStyle: {'width': '60px'}},
          {field: 'materialId', title: '料盘号', tdComp: 'MaterialHighLight', colStyle: {'width': '100px'}},
          {field: 'no', title: '料号', colStyle: {'width': '100px'}},
          {field: 'specification', title: '规格', colStyle: {'width': '130px'}},
          {field: 'supplier', title: '供应商', colStyle: {'width': '100px'}},
          {field: 'storeNum', title: '数量', colStyle: {'width': '80px'}},
          {title: '操作', tdComp: 'OperationOptions', colStyle: {'width': '180px'}}
        ];
        this.total = 0;
        this.query = {"limit": 20, "offset": 0};
        this.data = [];
        this.groupId = ''
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;
          axiosPost(options).then(response => {
            this.setLoading(false);
            this.isPending = false;
            this.setSpotMaterialId('');
            if (response.data.result === 200) {
              if (response.data.data !== null) {
                this.data = response.data.data.list;
                if (this.data.length !== 0) {
                  this.clearCheckTimeOut();
                }
                this.groupId = response.data.data.groupId;
                this.data.map((item, index) => {
                  item.showId = index + 1 + this.query.offset;
                  item.groupId = response.data.data.groupId;
                });
              } else {
                this.data = [];
              }
            } else {
              errHandler(response.data);
            }
          })
            .catch(err => {
              console.log(err);
              this.isPending = false;
              this.$alertDanger('请求超时，请刷新重试');
              this.setLoading(false)
            })
        }
      },
      //获取该盘点任务料盘信息
      getSampleBox: function (id) {
        if (!id) {
          return;
        }
        let options = {
          url: getPackingSampleMaterialInfoUrl,
          data: {
            windowId: id
          }
        };
        this.fetchData(options)
      },
      //设置定时器
      setCheckTimeOut: function () {
        if (this.isTimeOut === true) {
          this.clearCheckTimeOut();
        }
        this.isTimeOut = true;
        let that = this;
        this.getSampleBox(this.spotWindowId);
        this.myTimeOut = setInterval(function () {
          that.getSampleBox(that.spotWindowId);
        }, 1000);
      },
      //清除定时器
      clearCheckTimeOut: function () {
        this.isTimeOut = false;
        clearTimeout(this.myTimeOut);
        this.myTimeOut = null;
      },
      //叉车回库
      backSampleBox: function () {
        if (this.groupId === '') {
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: backBoxSampleTaskUrl,
            data: {
              groupId: this.groupId
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('叉车回库成功');
              this.init();
              this.setCheckTimeOut();
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            this.isPending = false;
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
          })
        }
      },
      setFocus: function () {
        if (this.$route.path === '/spot/operation') {
          document.getElementById('scanner-check').focus();
        }
      },
      scannerHandler: function () {
        let scanText = this.scanText;
        this.scanText = '';
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
        this.data.map((item) => {
          if (materialId === item.materialId) {
            this.setSpotMaterialId(materialId);
          }
        })
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

<style scoped>
  .main-details {
    position: relative;
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px 10px 80px 10px;
    min-height: 500px;
  }

  .return-btn {
    position: absolute;;
    right: 10px;
    bottom: 10px;
    z-index: 999;
  }

  #scanner-check {
    opacity: 0;
    height: 0;
    line-height: 0;
    border: none;
    padding: 0;
    position: fixed;
  }
</style>
