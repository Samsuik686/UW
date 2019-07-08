<template>
  <div class="cut-item">
    <div class="tip" v-if="!isShow">
      <div class="operation-img">
        <img src="static/img/cutQRCode.png" alt="finished" class="img-style">
      </div>
      <span class="operation-text">* 扫描此二维码或点击按钮进行截料操作</span>
      <el-button  @click="isShow = true" type="primary">截料入库</el-button>
    </div>
    <div v-else>
      <div class="item">
        <el-form  inline label-position="left" class="demo-table-expand" size="medium">
          <el-form-item label="类型">
            <span>截料入库</span>
          </el-form-item>
          <el-form-item label="任务">
            <span>{{taskItem.fileName}}</span>
          </el-form-item>
          <el-form-item label="料号">
            <span>{{taskItem.materialNo}}</span>
          </el-form-item>
          <el-form-item label="供应商">
            <span>{{taskItem.supplierName}}</span>
          </el-form-item>
          <el-form-item label="计划">
            <span>{{taskItem.planQuantity}}</span>
          </el-form-item>
          <el-form-item label="实际">
            <span>{{taskItem.actualQuantity}}</span>
          </el-form-item>
          <el-form-item label="库存">
            <span>{{taskItem.uwStoreQuantity}}</span>
          </el-form-item>
          <el-form-item label="本次缺发数量/超发数量">
            <span>{{overQuantity(taskItem.planQuantity,taskItem.actualQuantity)}}</span>
          </el-form-item>
        </el-form>
        <div class="item-operation">
          <div class="operation-img">
            <img src="static/img/finishedQRCode.png" alt="finished" class="img-style">
          </div>
          <span class="operation-text">* 扫描此二维码或点击按钮以完成操作</span>
          <el-button
            size="small"
            @click="changeState"
            :type="state === 2?'info':'primary'">{{stateText}}</el-button>
          <el-button
            size="small"
            type="primary"
            @click="robotBack">操作完毕</el-button>
        </div>
      </div>
      <el-divider></el-divider>
      <el-table
        border
        :data="taskItem.details"
        style="width:100%">
        <el-table-column
          prop="materialId"
          label="料盘">
        </el-table-column>
        <el-table-column
          prop="quantity"
          label="出库数量">
        </el-table-column>
        <el-table-column
          prop="remainderQuantity"
          label="剩余数量">
        </el-table-column>
        <el-table-column
          prop="productionTime"
          label="生产日期">
        </el-table-column>
        <el-table-column
          prop="isInBoxString"
          label="是否在盒内">
        </el-table-column>
        <el-table-column
          label="操作">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="printBarcode(scope.row)">打印</el-button>
          </template>
        </el-table-column>
      </el-table>
      <finish-tip :task-item="taskItem" :state="state"></finish-tip>
      <edit-material-id></edit-material-id>
    </div>
  </div>
</template>

<script>
  import {mapGetters,mapActions} from 'vuex'
  import FinishTip from './subscomp/FinishTip'
  import {robotBackUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import EditMaterialId from "./subscomp/EditMaterialId";
  export default {
    name: "CutItemDetails",
    props:{
      taskItem:Object
    },
    components:{
      EditMaterialId,
      FinishTip
    },
    computed:{
      ...mapGetters(['scanFinishBoxId','scanCutBoxId','configData','user'])
    },
    watch: {
      state:function(val){
        if(val === 2){
          this.stateText = '料盒未满'
        }else{
          this.stateText = "料盒已满"
        }
      },
      scanFinishBoxId:function(val){
        if(val === this.taskItem.boxId){
          this.robotBack();
          this.setScanFinishBoxId('');
        }
      },
      scanCutBoxId:function(val){
        if(val === this.taskItem.boxId){
          this.isShow = true;
          this.setScanCutBoxId('');
        }
      }
    },
    data() {
      return {
        state:1,
        stateText:'料盒已满',
        isShow:false
      }
    },
    methods:{
      ...mapActions(['setScanFinishBoxId','setScanCutBoxId']),
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
      changeState:function () {
        if(this.state === 0){
          this.state = 1;
        }else{
          this.state = 0;
        }
      },
      robotBack: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: robotBackUrl,
            data: {
              id: this.taskItem.id,
              isLater:false,
              state:this.state
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess("操作成功");
            } else {
              this.$alertWarning(res.data.data)
            }
            this.isPending = false;
            this.state = 1;
          }).catch(err => {
            this.$alertDanger(err);
            this.isPending = false;
            this.state = 1;
          })
        }
      },
      printBarcode: function (item) {
        let arr = item.productionTime.split(' ');
        if (this.configData.printerIP === "") {
          this.$alertWarning("请在设置界面填写打印机IP");
          return;
        }
        let options = {
          url: window.g.PRINTER_URL,
          data: {
            printerIP: this.configData.printerIP,
            materialId: item.materialId,
            materialNo: this.taskItem.materialNo,
            remainingQuantity:item.remainderQuantity,
            productDate:arr[0],
            user: this.user.uid,
            supplier: this.taskItem.supplierName
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
          this.$alertDanger(err);
        })
      },
    }
  }
</script>

<style scoped>
  .cut-item{
    width:100%;
    min-height:500px;
    box-sizing: border-box;
    border: 1px solid #ccc;
    border-radius: 3px;
    padding:20px 30px;
    background: rgba(226, 176, 35, 0.05);
  }
  .item{
    display:flex;
  }
  .demo-table-expand{
    flex:2;
    font-size: 0;
    margin-bottom:10px;
  }
  .demo-table-expand .el-form-item .el-form-item__label {
    width:90px;
    color: #99a9bf;
  }
  .demo-table-expand .el-form-item {
    margin-right: 0;
    margin-bottom: 0;
    width: 50%;
  }
  .item-operation{
    flex:1;
    margin-top:5px;
    text-align:center;
  }
  .operation-img{
    width:50%;
    margin:0 auto 10px;
  }
  .operation-text{
    display:block;
    margin-bottom:10px;
  }
  .img-style {
    width:150px;
    height:auto;
    text-align:center;
  }
  .tip{
    margin-top:100px;
    text-align: center;
  }
</style>

