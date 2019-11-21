<template>
  <div class="task-item">
    <div class="item">
      <el-form  inline label-position="left" class="item-form" size="medium">
        <el-form-item label="任务">
          <span>{{taskItem.fileName}}</span>
        </el-form-item>
        <el-form-item label="料号">
          <span>{{taskItem.materialNo}}</span>
        </el-form-item>
        <el-form-item label="客户">
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
        <el-form-item label="已扫料盘">
          <span>{{taskItem.details.length}}</span>
        </el-form-item>
        <el-form-item label="目的仓口库存">
          <span>{{taskItem.eWhStoreQuantity}}</span>
        </el-form-item>
        <el-form-item label="料盘数">
          <span>{{taskItem.reelNum}}</span>
        </el-form-item>
        <el-form-item label="规格" style="width:100%">
          <span>{{taskItem.specification}}</span>
        </el-form-item>
      </el-form>
      <div class="item-operation">
        <div class="operation-img">
          <img src="./../../../../assets/finishedQRCode.png" alt="finished" class="img-style">
        </div>
        <span class="operation-text">* 扫描此二维码或点击按钮以完成操作</span>
        <div style="display:flex;justify-content:center">
          <el-button
            size="small"
            @click="changeState"
            :type="state === 0?'info':'primary'">{{stateText}}</el-button>
          <el-button
            size="small"
            type="primary"
            @click="checkOverQuantity">操作完毕</el-button>
        </div>
      </div>
      <div class="item-box">
        <material-box
          :id="taskItem.boxId"
          :materials="taskItem.materials">
        </material-box>
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
        prop="remainderQuantity"
        label="原始数量">
      </el-table-column>
      <el-table-column
        prop="quantity"
        label="出库数量">
      </el-table-column>
      <el-table-column
        prop="productionTime"
        label="打印日期">
      </el-table-column>
      <el-table-column
        label="操作">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="primary"
            @click="handleEdit(scope.row)">修改</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <edit-material-id
      :isEditMaterial.sync="isEditMaterial"
      :packListItemId="taskItem.id"
      :row="activeRow">
    </edit-material-id>
    <finish-tip :task-item="taskItem" :state="state" :isFinishTip.sync="isFinishTip"></finish-tip>
    <show-position :is-show-position.sync="isShowPosition" :col="col" :row="row"></show-position>
  </div>
</template>

<script>
  import {mapGetters,mapActions} from 'vuex'
  import FinishTip from './subscomp/FinishTip'
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import EditMaterialId from "./subscomp/EditMaterialId";
  import MaterialBox from "./subscomp/MaterialBox";
  import ShowPosition from "./subscomp/ShowPosition";
  import {
    robotBackUrl,
    taskDeleteMaterialRecordUrl,
    taskDeleteRegularMaterialRecordUrl
  } from "../../../../plugins/globalUrl";
  export default {
    name: "TaskItemDetails",
    props:{
        taskItem:Object
    },
    components:{
        ShowPosition,
        MaterialBox,
        EditMaterialId,
        FinishTip
    },
    computed:{
        ...mapGetters(['scanFinishBoxId'])
    },
    watch: {
        state:function(val){
        if(val === 0){
          this.stateText = '料盒未空'
        }else{
          this.stateText = "料盒已空"
        }
      },
        scanFinishBoxId:function(val){
        if(val === this.taskItem.boxId){
          this.checkOverQuantity();
          this.setScanFinishBoxId('');
        }
      },
        isEditMaterial:function (val) {
            if(val === false){
                this.$emit('refreshData',true);
            }
        }
    },
    data() {
      return {
        state:1,
        stateText:'料盒已空',
        isFinishTip:false,
        isEditMaterial:false,
        activeRow:{},
        isPending:false,
        col:-1,
        row:-1,
        isShowPosition:false
      }
    },
    methods:{
      ...mapActions(['setScanFinishBoxId','setIsBlur']),
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
      handleEdit:function(row){
        this.activeRow = row;
        this.setIsBlur(true);
        this.isEditMaterial = true;
      },
      handleDelete:function (row) {
        this.$confirm('你正在删除料盘唯一码为'+row.materialId+ '的扫描记录，请确认是否删除?', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          center: false
        }).then((action) => {
          if (action === "confirm") {
            this.deleteMaterialRecord(row.materialId);
          }
        }).catch(() => {
          this.$alertInfo("已取消删除");
        });
      },
      checkOverQuantity:function(){
        this.isFinishTip = true;
      },
      changeState:function () {
        if(this.state === 0){
          this.state = 1;
        }else{
          this.state = 0;
        }
      },
      setBack: function (isLater) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: robotBackUrl,
            data: {
              id: this.taskItem.id,
              state:this.state,
              isLater:isLater
            }
          };
          axiosPost(options).then(response => {
            if (response.data.result === 200) {
              this.$alertSuccess('操作成功');
            } else {
              errHandler(response.data);
            }
            this.state = 1;
          }).catch(err => {
            console.log(err);
          }).finally(() =>{
              this.isPending = false;
          })
        }
      },
      deleteMaterialRecord:function(materialId){
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: taskDeleteRegularMaterialRecordUrl,
            data: {
              packingListItemId: this.taskItem.id,
              materialId: materialId
            }
          };
          axiosPost(options).then(response => {
            if (response.data.result === 200) {
              this.$alertSuccess("删除成功");
              let col = response.data.data.col;
              let row = response.data.data.row;
              if(col !== -1 && row !== -1){
                  this.col = col;
                  this.row = row;
                  this.isShowPosition = true;
              }
              this.$emit('refreshData',true);
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            console.log(err);
          }).finally(() => {
              this.isPending = false;
          })
        }
      }
    }
  }
</script>

<style scoped lang="scss">
  .task-item{
    width:100%;
    min-height:500px;
    box-sizing: border-box;
    border: 1px solid #ccc;
    border-radius: 3px;
    padding:20px 30px;
    .item{
      display:flex;
      .item-form{
        flex:2;
        font-size: 0;
        margin-bottom:10px;
        .el-form-item{
          width:45%;
          margin-right:20px;
          .el-form-item__label {
            width:90px;
            color: #99a9bf;
          }
        }
      }
      .item-operation{
        flex:1;
        margin-top:5px;
        text-align:center;
        .operation-img{
          width:50%;
          margin:0 auto 10px;
          .img-style {
            width:150px;
            height:auto;
            text-align:center;
          }
        }
        .operation-text{
          display:block;
          margin-bottom:10px;
        }
      }
    }
  }
</style>
