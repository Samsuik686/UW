<template>
  <div class="main-details mt-1 mb-3">
    <input type="text" title="scanner" id="scanner-check" v-model="scanText"
           @blur="confirmSetFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
    <datatable
      v-bind="$data"
    ></datatable>
    <div class="form-group row align-items-end return-btn">
      <div class="btn btn-primary ml-3 mr-4" @click="backInventoryBox">叉车回库</div>
    </div>
  </div>
</template>

<script>
  import {mapActions, mapGetters} from 'vuex'
  import OperationOptions from './subscomp/OperationOptions'
  import SetCheckQuantity from './subscomp/SetCheckQuantity'
  import eventBus from "../../../../utils/eventBus";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {backInventoryBoxUrl, getPackingInventoryUrl} from "../../../../config/globalUrl";
  import {handleScanText} from "../../../../utils/scan";

  export default {
    name: "TableDetails",
    components: {
      OperationOptions,
      SetCheckQuantity
    },
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
        scanText: '',
        isInput: false,
        boxId: '',
        taskId: '',
        //定时器
        myTimeOut:'',
        //是否启用定时器
        isTimeOut:false
      }
    },
    created() {
      this.init();
    },
    beforeDestroy(){
      this.clearCheckTimeOut();
    },
    watch: {
      checkWindowId: function (val) {
        if (val !== '') {
          this.clearCheckTimeOut();
          this.setIsRefresh(false);
          this.getInventoryBox(val);
        }
      }
    },
    mounted() {
      this.setFocus();
      if (this.checkWindowId !== '') {
        this.getInventoryBox(this.checkWindowId);
      }
      eventBus.$on('setIsInput', val => {
        this.isInput = val;
        if (val === false) {
          this.setFocus();
        }
      });
    },
    computed: {
      ...mapGetters(['checkWindowId', 'checkWindowData'])
    },
    methods: {
      ...mapActions(['setCheckWindowData', 'setLoading', 'setCheckOperationData', 'setIsRefresh','setIsScanner']),
      init: function () {
        this.columns = [
          {field: 'showId', title: '序号', colStyle: {'width': '70px'}},
          {field: 'materialId', title: '料盘号', colStyle: {'width': '100px'}},
          {field: 'no', title: '料号', colStyle: {'width': '100px'}},
          {field: 'specification', title: '规格', colStyle: {'width': '130px'}},
          {field: 'supplier', title: '供应商', colStyle: {'width': '100px'}},
          {field: 'storeNum', title: '库存数量', colStyle: {'width': '80px'}},
          {title: '盘点数量', tdComp: 'SetCheckQuantity', colStyle: {'width': '80px'}},
          {title: '操作', tdComp: 'OperationOptions', colStyle: {'width': '100px'}}
        ];
        this.total = 0;
        this.query = {"limit": 20, "offset": 0};
        this.data = [];
        this.taskId = '';
        this.boxId = '';
      },
      setFocus: function () {
        if (this.$route.path === '/check/operation') {
          document.getElementById('scanner-check').focus();
        }
      },
      confirmSetFocus: function () {
        if (this.isInput === false) {
          this.setFocus();
        }
      },
      scannerHandler: function () {
        let scanText = this.scanText;
        this.scanText = '';
        /*sample: 03.01.0001@1000@1531817296428@A008@范例表@A-1@9@2018-07-17@*/
        //判断扫描的条码格式
        let result = handleScanText(scanText);
        if (result !== '') {
          this.$alertWarning(result);
          return;
        }
        /*对比料号是否一致*/
        let tempArray = scanText.split("@");
        let materialId = tempArray[2];
        let quantity = tempArray[1];
        let checkData = JSON.parse(JSON.stringify(this.checkWindowData[this.checkWindowId]));
        //当前窗口是否存在对应的料号
        let isExit = false;
        for(let i=0;i<checkData.length;i++){
          let item = checkData[i];
          if(item.materialId === materialId){
            isExit = true;
            item.checkQuantity = quantity;
            item.isHighLight = true;
            item.isScanner = true;
            break;
          }
        }
        //确认扫描成功
        if(isExit === true){
          this.setCheckWindowData({
            id: this.checkWindowId,
            data: checkData
          });
          this.setIsScanner(true);
        }else{
          this.$alertWarning('找不到对应料盘');
        }
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;
          axiosPost(options).then(response => {
            this.setLoading(false);
            this.isPending = false;
            if (response.data.result === 200) {
              if (response.data.data === null) {
                this.data = [];
                this.taskId = '';
                this.boxId = '';
                this.setCheckTimeOut();
                return;
              }
              this.clearCheckTimeOut();
              this.boxId = response.data.data.boxId;
              this.taskId = response.data.data.taskId;
              this.setCheckOperationData({
                taskId: this.taskId,
                boxId: this.boxId
              });
              this.data = response.data.data.list;
              this.setIsRefresh(true);
              this.data.map((item, index) => {
                item.showId = index + 1 + this.query.offset;
              });
              this.setCheckWindowDataVuex();
            } else {
              this.clearCheckTimeOut();
              if(response.data.result === 412){
                let tempUrl = this.$route.fullPath;
                this.$router.push('_empty');
                this.$router.replace(tempUrl);
              }else{
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
      setCheckWindowDataVuex: function () {
        if (this.checkWindowData.hasOwnProperty(this.checkWindowId)) {
          if (this.checkWindowData[this.checkWindowId].length > 0) {
            return;
          }
        }
        let list = [];
        for (let i = 0; i < this.data.length; i++) {
          let obj = this.data[i];
          list.push({
            materialId: obj.materialId,
            checkQuantity: '',
            isHighLight: false,
            isConfirm: false,
            isScanner:false
          })
        }
        this.setCheckWindowData({
          id: this.checkWindowId,
          data: list
        })
      },
      backInventoryBox: function () {
        let list = this.checkWindowData[this.checkWindowId];
        for (let i = 0; i < list.length; i++) {
          if (list[i].checkQuantity === '') {
            this.$alertWarning('当前有料盘未进行盘点');
            return;
          }
        }
        for (let i = 0; i < list.length; i++) {
          let row = this.data[i];
          let obj = list[i];
          if(Number(row.storeNum) !== Number(obj.checkQuantity) && obj.isConfirm !== true){
            this.$alertWarning('当前有料盘未进行盘点');
            return;
          }
        }
        if(this.taskId === '' || this.boxId === ''){
          this.$alertWarning('当前仓口无任务');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: backInventoryBoxUrl,
            data: {
              taskId: this.taskId,
              boxId: this.boxId,
              windowId: this.checkWindowId,
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('叉车回库成功');
              this.init();
              this.setCheckWindowData({
                id: this.checkWindowId,
                data: []
              });
              this.getInventoryBox(this.checkWindowId);
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
      //获取该盘点任务料盘信息
      getInventoryBox: function (id) {
        if (!id) {
          this.$alertWarning('请选择仓口');
          return;
        }
        let options = {
          url: getPackingInventoryUrl,
          data: {
            windowId: id
          }
        };
        this.fetchData(options)
      },
      //设置定时器
      setCheckTimeOut:function(){
        if(this.isTimeOut === true){
          this.clearCheckTimeOut();
        }
        this.isTimeOut = true;
        let that = this;
        this.myTimeOut = setInterval(function () {
          that.getInventoryBox(that.checkWindowId);
        },1000);
      },
      //清除定时器
      clearCheckTimeOut:function(){
        this.isTimeOut = false;
        clearTimeout(this.myTimeOut);
        this.myTimeOut = null;
      },
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
    z-index:999;
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
