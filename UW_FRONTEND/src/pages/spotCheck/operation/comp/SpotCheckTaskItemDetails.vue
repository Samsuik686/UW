<template>
  <div class="spot-check-item-details">
    <div>
      <el-form :inline="true" class="demo-form-inline" size="medium">
        <el-form-item label="料盒总盘数">
          <el-input v-model="spotCheckItem.totalNum" disabled></el-input>
        </el-form-item>
        <el-form-item label="已扫盘数">
          <el-input v-model="spotCheckItem.scanNum" disabled></el-input>
        </el-form-item>
        <el-form-item label="已出库盘数">
          <el-input v-model="spotCheckItem.outNum" disabled></el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            @click="backInventoryBox"
            type="primary">叉车回库
          </el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="spotCheckItem.list"
        style="width:100%">
        <el-table-column
          prop="materialId"
          label="料盘号">
          <template slot-scope="scope">
            <high-light
              :spotCheckItem="spotCheckItem"
              :isScan = "isScan"
              :row="scope.row"
              :activeMaterialId="activeMaterialId"
            >
            </high-light>
          </template>
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
          label="数量">
        </el-table-column>
        <el-table-column
          width="200"
          label="操作">
          <template slot-scope="scope">
            <el-button
              size="small"
              :type="scope.row.isOuted === true?'info':'primary'"
              :disabled="scope.row.isOuted === true"
              @click="outSingular(scope.row.materialId)">异常出库
            </el-button>
            <el-button
              size="small"
              :type="scope.row.isOuted === true?'info':'primary'"
              :disabled="scope.row.isOuted === true"
              @click="outRegular(scope.row.materialId)">抽检出库
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="item-box" v-if="spotCheckItem.boxId !== null">
      <material-box
        :x="x"
        :y="y"
        :list="spotCheckItem.list"
        :id="spotCheckItem.boxId">
      </material-box>
    </div>
  </div>
</template>

<script>
  import eventBus from "../../../../utils/eventBus";
  import {backBoxSampleTaskUrl, outRegularSampleTaskUrl, outSingularSampleTaskUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import HighLight from "./subscomp/HighLight";
  import {mapActions} from 'vuex'
  import MaterialBox from './subscomp/MaterialBox'
  export default {
    name: "SpotCheckTaskItemDetails",
    components: {
      HighLight,
      MaterialBox
    },
    watch:{
      activeMaterialId:function (val) {
        if(val !== ''){
          for(let i =0;i<this.spotCheckItem.list.length;i++){
            let obj = this.spotCheckItem.list[i];
            if(obj.materialId === val){
              this.x = obj.col;
              this.y = obj.row;
              return;
            }
          }
        }else{
          this.x = -1;
          this.y = -1;
        }
      }
    },
    props:{
      spotCheckItem:Object,
      activeName:String,
      activeMaterialId:String,
      isScan:Boolean
    },
    data() {
      return {
        isPending:false,
        x:-1,
        y:-1
      }
    },
    methods: {
      ...mapActions(['setLoading']),
      backInventoryBox:function(){
        if (this.spotCheckItem.groupId === '') {
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          this.setLoading(true);
          let options = {
            url: backBoxSampleTaskUrl,
            data: {
              groupId: this.spotCheckItem.groupId
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            this.setLoading(false);
            if (response.data.result === 200) {
              this.$alertSuccess('叉车回库成功');
              eventBus.$emit('refreshSpotCheckTask',true);
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
      outRegular: function (materialId) {
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:outRegularSampleTaskUrl,
            data:{
              materialId:materialId,
              groupId:this.spotCheckItem.groupId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess(res.data.data);
              eventBus.$emit('refreshSpotCheckTask',true);
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
            console.log(err);
          })
        }
      },
      outSingular: function (materialId) {
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:outSingularSampleTaskUrl,
            data:{
              materialId:materialId,
              groupId:this.spotCheckItem.groupId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess(res.data.data);
              eventBus.$emit('refreshSpotCheckTask',true);
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
            console.log(err);
          })
        }
      }
    }
  }
</script>

<style scoped>
  .highLight{
    color:red
  }
</style>
