<template>
  <div class="check-item-details">
    <el-button
      :disabled="disabledMaterialId !== ''"
      @click="backInventoryBox"
      type="primary">叉车回库
    </el-button>
    <el-table
      :data="checkTaskItem.list"
      style="width:100%">
      <el-table-column
        prop="materialId"
        label="料盘号">
      </el-table-column>
      <el-table-column
        prop="no"
        label="料号">
      </el-table-column>
      <el-table-column
        prop="specification"
        label="规格">
      </el-table-column>
      <el-table-column
        prop="supplier"
        label="供应商">
      </el-table-column>
      <el-table-column
        prop="storeNum"
        label="库存数量">
      </el-table-column>
      <el-table-column
        label="盘点数量">
        <template slot-scope="scope">
          <set-check-quantity
            :row="scope.row"
            :activeMaterialId = "activeMaterialId"
            :activeQuantity = "activeQuantity"
            :activeName="activeName"
            :boxId = "boxId"
            :taskId = "taskId"
            :isScanner="isScanner"
          ></set-check-quantity>
        </template>
      </el-table-column>
      <el-table-column
        label="操作">
        <template slot-scope="scope">
          <el-button
            :disabled="disabledMaterialId === scope.row.materialId"
            @click="setCheckQuantity(scope.row)"
            :type="printMaterialIdArr.includes(scope.row.materialId)?'warning':'primary'">确定
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  import SetCheckQuantity from "./subscomp/SetCheckQuantity";
  import eventBus from "../../../../utils/eventBus";
  import {backInventoryBoxUrl, inventoryMaterialUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {mapGetters,mapActions} from 'vuex'
  export default {
    name: "CheckTaskDetails",
    components: {SetCheckQuantity},
    props: {
      checkTaskItem: Object,
      activeName:String,
      activeMaterialId:String,
      disabledMaterialId:String,
      activeQuantity:Number,
      boxId:Number,
      taskId:Number,
      windowId:Number,
      isScanner:Boolean
    },
    data() {
      return {
        isPending:false
      }
    },
    computed: {
      ...mapGetters([
        'user', 'configData','printMaterialIdArr','isLoading'
      ]),
    },
    methods:{
      ...mapActions(['setPrintMaterialIdArr','setLoading']),
      setCheckQuantity:function(row){
        let actualNum = row.actualNum;
        if (actualNum === '' || actualNum === null) {
          this.$alertWarning('盘点数量不能为空');
          return;
        }
        if(!this.isNumber(actualNum)){
          this.$alertWarning('盘点数量必须为非负整数');
          return;
        }
        if (Number(row.storeNum) !== Number(actualNum)) {
          if (this.configData.printerIP === "") {
            this.$alertWarning("请在设置界面填写打印机IP");
            return;
          }
        }
        if (Number(row.storeNum) !== Number(actualNum)) {
          this.printBarcode(row,actualNum);
        }
      },
      checkQuantity:function(row,actualNum){
        if (!this.isPending) {
          this.isPending = true;
          this.setLoading(true);
          let options = {
            url: inventoryMaterialUrl,
            data: {
              materialId: row.materialId,
              boxId: this.boxId,
              taskId: this.taskId,
              acturalNum: actualNum
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            this.setLoading(false);
            if (response.data.result === 200) {
              eventBus.$emit('refreshCheckTask',true);
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            this.$alertDanger('连接超时，请刷新重试');
            console.log(err);
            this.isPending = false;
            this.setLoading(false);
          })
        }
      },
      printBarcode: function (row,actualNum) {
        let date = row.productionTime.split(' ');
        let options = {
          url: window.g.PRINTER_URL,
          data: {
            printerIP: this.configData.printerIP,
            materialId: row.materialId,
            materialNo: row.no,
            remainingQuantity: actualNum,
            productDate:date[0],
            user: this.user.uid,
            supplier:row.supplier
          }
        };
        axiosPost(options).then(response => {
          if (response.data.code === 200) {
            this.$alertSuccess('打印成功');
            this.handlePrintMaterialArr(row.materialId);
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
      isNumber: function (num) {
        let val = num;
        let reg = /^\+?(0|[1-9][0-9]*)$/;
        if (val !== "") {
          return reg.test(val);
        }
      },
      backInventoryBox:function(){
        if(this.checkTaskItem.taskId === null || this.checkTaskItem.boxId === null){
          this.$alertWarning('当前货位没有料盒');
          return;
        }
        for(let i=0;i<this.checkTaskItem.list.length;i++){
          let item = this.checkTaskItem.list[i];
          if(item.actualNum === '' || item.actualNum === null){
            this.$alertWarning('存在料盘未盘点');
            return;
          }
        }
        for(let i = 0;i<this.checkTaskItem.list.length;i++){
          let item = this.checkTaskItem.list[i];
          let isExit = false;
          if(item.actualNum !== item.storeNum){
            for(let j=0;j<this.printMaterialIdArr.length;j++){
              if(item.materialId === this.printMaterialIdArr[j]){
                isExit = true;
                break;
              }
            }
          }else{
            isExit = true;
          }
          if(isExit === false){
            this.$alertWarning('存在料盘未点击确定，打印新条码');
            return;
          }
        }
        if (!this.isPending) {
          this.isPending = true;
          this.setLoading(true);
          let options = {
            url: backInventoryBoxUrl,
            data: {
              taskId: this.taskId,
              boxId: this.boxId,
              windowId: this.windowId,
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            this.setLoading(false);
            if (response.data.result === 200) {
              this.$alertSuccess('叉车回库成功');
              this.cancelPrintArr();
              eventBus.$emit('refreshCheckTask',true);
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            this.isPending = false;
            this.setLoading(false);
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
          })
        }
      },
      handlePrintMaterialArr:function(materialId){
        let arr = JSON.parse(JSON.stringify(this.printMaterialIdArr));
        arr.push(materialId);
        this.setPrintMaterialIdArr(arr);
      },
      cancelPrintArr:function () {
        let arr = [];
        for(let i=0;i<this.printMaterialIdArr.length;i++){
          let materialId = this.printMaterialIdArr[i];
          let isExit = false;
          for(let i=0;i<this.checkTaskItem.list.length;i++){
            let item = this.checkTaskItem.list[i];
            if(materialId === item.materialId){
              isExit = true;
              break;
            }
          }
          if(isExit === false){
            arr.push(materialId);
          }
        }
        this.setPrintMaterialIdArr(arr);
      }
    }
  }
</script>

<style scoped>
</style>
