<template>
  <div class="form-group">
    <div class="btn btn-primary"  :class="{confirmBtn:isConfirm}" @click="setCheckQuantity">确定</div>
  </div>
</template>

<script>
  import {mapGetters,mapActions} from 'vuex'
  import {axiosPost} from "../../../../../utils/fetchData";
  import {inventoryMaterialUrl} from "../../../../../config/globalUrl";
  import {errHandler} from "../../../../../utils/errorHandler";
  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "OperationOptions",
    data() {
      return {
        isPending: false,
        isConfirm:false
      }
    },
    props: {
      row: Object
    },
    computed: {
      ...mapGetters([
        'user', 'configData', 'checkWindowId', 'checkWindowData', 'checkOperationData','isRefresh'
      ]),
    },
    watch:{
      isRefresh: function (val) {
        //仓口改变，重新初始化
        if (val === true) {
          let data = this.checkWindowData[this.checkWindowId];
          data.map((item) => {
            if (item.materialId === this.row.materialId) {
              this.isConfirm = item.isConfirm
            }
          });
        }
      },
    },
    mounted(){
      let data = this.checkWindowData[this.checkWindowId];
      data.map((item) => {
        if (item.materialId === this.row.materialId) {
          this.isConfirm = item.isConfirm
        }
      });
      eventBus.$on('refreshConfirm',() => {
        let data = this.checkWindowData[this.checkWindowId];
        data.map((item) => {
          if (item.materialId === this.row.materialId) {
            this.isConfirm = item.isConfirm
          }
        });
      })
    },
    methods: {
      ...mapActions(['setCheckWindowData']),
      // 请求打印
      printBarcode: function (acturalNum) {
        let checkQuantity = this.getCheckQuantity();
        if (checkQuantity === '') {
          console.log('获取数量失败');
          return;
        }
        let date = this.row.productionTime.split(' ');
        let options = {
          url: window.g.PRINTER_URL,
          data: {
            printerIP: this.configData.printerIP,
            materialId: this.row.materialId,
            materialNo: this.row.no,
            remainingQuantity: checkQuantity,
            productDate:date[0],
            user: this.user.uid,
            supplier: this.row.supplier
          }
        };
        axiosPost(options).then(response => {
          if (response.data.code === 200) {
            //this.$alertSuccess(response.data.msg);
            this.checkQuantity(acturalNum);
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
      //获取盘点的数量
      getCheckQuantity: function () {
        let checkQuantity = '';
        let data = this.checkWindowData[this.checkWindowId];
        data.map((item) => {
          if (item.materialId === this.row.materialId) {
            checkQuantity = item.checkQuantity;
          }
        });
        return checkQuantity;
      },
      //设置已确认
      setConfirm:function(){
        let data = this.checkWindowData[this.checkWindowId];
        data.map((item) => {
          if (item.materialId === this.row.materialId) {
            item.isConfirm = true;
          }
        });
        this.setCheckWindowData({
          id:this.checkWindowId,
          data:data
        });
      },
      isNumber: function (num) {
        let val = num;
        let reg = /^\+?(0|[1-9][0-9]*)$/;
        if (val !== "") {
          return reg.test(val);
        }
      },
      //修改库存
      setCheckQuantity: function () {
        let acturalNum = this.getCheckQuantity();
        if (acturalNum === '') {
          this.$alertWarning('盘点数量不能为空');
          return;
        }
        if(!this.isNumber(acturalNum)){
          this.$alertWarning('盘点数量必须为非负整数');
          return;
        }
        if (Number(this.row.storeNum) !== Number(acturalNum)) {
          if (this.configData.printerIP === "") {
            this.$alertWarning("请在设置界面填写打印机IP");
            return;
          }
        }
        if (Number(this.row.storeNum) !== Number(acturalNum)) {
          this.printBarcode(acturalNum);
        }else{
          this.checkQuantity(acturalNum);
        }
      },
      //修改库存
      checkQuantity:function(acturalNum){
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: inventoryMaterialUrl,
            data: {
              materialId: this.row.materialId,
              boxId: this.checkOperationData.boxId,
              taskId: this.checkOperationData.taskId,
              acturalNum: acturalNum
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('修改成功');
              this.setConfirm();
              this.isConfirm = true;
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            this.$alertDanger('连接超时，请刷新重试');
            console.log(err);
            this.isPending = false;
          })
        }
      }
    }
  }
</script>

<style scoped>
  .confirmBtn{
    background-color:orange;
    border-color:orange;
  }
</style>
