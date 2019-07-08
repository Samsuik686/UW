<template>
  <div class="io-preview">
    <el-form :inline="true" class="demo-form-inline" size="medium">
      <el-form-item label="出入库类型">
        <el-select v-model="windowType" placeholder="出入库类型">
          <el-option label="入库" value="1"></el-option>
          <el-option label="出库" value="2"></el-option>
          <el-option label="调拨入库" value="3"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="仓口">
        <el-select v-model="thisWindow" placeholder="仓口" :disabled="windowsList.length === 0">
          <el-option value="" :label="windowsList.length === 0 ? '无非空闲仓口':'请选择'"></el-option>
          <el-option v-for="item in windowsList" :value="item.id" :label="item.id" :key="item.id"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <el-table
      @expand-change="toggleRowExpansion"
      :data="tableData"
      style="width:100%">
      <el-table-column
        label="详情"
        type="expand">
        <template slot-scope="props">
          <div v-if="props.row.details.length === 0">
            <p style="text-align:center">无数据</p>
          </div>
          <div v-else>
            <div v-for="(item,index) in props.row.details" :key="index">
              <div class="materialId-box">
                <div class="box-item"><span>料盘唯一码</span><span>{{item.materialId}}</span></div>
                <div class="box-item"><span>数量</span><span>{{item.quantity}}</span></div>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        prop="id"
        label="ID"
        width="70">
      </el-table-column>
      <el-table-column
        prop="fileName"
        label="套料单名称">
      </el-table-column>
      <el-table-column
        prop="robotId"
        label="叉车">
      </el-table-column>
      <el-table-column
        prop="goodsLocationName"
        label="货位名">
      </el-table-column>
      <el-table-column
        prop="boxId"
        label="料盒号">
      </el-table-column>
      <el-table-column
        label="任务条目状态">
        <template slot-scope="scope">
          <span :class="{activeClass:scope.row.state === -3}">{{ scope.row.stateString}}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="materialNo"
        label="料号">
      </el-table-column>
      <el-table-column
        prop="planQuantity"
        label="计划数量">
      </el-table-column>
      <el-table-column
        prop="actualQuantity"
        label="实际数量">
      </el-table-column>
      <el-table-column
        prop="finishTime"
        label="完成时间">
      </el-table-column>
    </el-table>
    <div class="block">
      <el-pagination
        background
        :current-page.sync="pageNo"
        :page-size.sync="pageSize"
        :page-sizes="[20,40,80,100]"
        @size-change="select"
        @current-change="select"
        layout="total,sizes,prev,pager,next,jumper"
        :total="totallyData">
        >
      </el-pagination>
    </div>
  </div>
</template>

<script>
  import {mapActions} from 'vuex'
  import {taskWindowsUrl, taskWindowTaskItems} from "../../../config/globalUrl";
  import {axiosPost} from "../../../utils/fetchData";
  import {errHandler} from "../../../utils/errorHandler";

  export default {
    name: "IoPreview",
    data() {
      return {
        windowType: '1',
        windowsList: [],
        thisWindow: '',
        tableData: [],
        pageNo: 1,
        pageSize: 20,
        totallyData:0,
        isPending: false,
        //定时器
        myTimeOut: '',
        //是否启用定时器
        isTimeOut: false,
      }
    },
    mounted(){
      this.setPreset();
    },
    beforeDestroy(){
      this.clearMyTimeOut();
    },
    watch:{
      windowType:function(val){
        if(val !== ''){
          this.thisWindow = '';
          this.setPreset();
        }
      },
      thisWindow:function(val){
        if(val !== ''){
          this.setPreset();
          this.setMyTimeOut();
        }
      }
    },
    methods: {
      ...mapActions(['setLoading']),
      setPreset: function () {
        let options = {
          url: taskWindowsUrl,
          data: {
            type: this.windowType
          }
        };
        axiosPost(options).then(response => {
          if (response.data.result === 200) {
            this.windowsList = response.data.data;
            if (this.windowsList.length > 0) {
              if(this.thisWindow === ''){
                this.thisWindow = this.windowsList[0].id
              }
            }else{
              this.thisWindow = '';
            }
          }
        });
      },
      select: function () {
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:taskWindowTaskItems,
            data:{
              pageNo:this.pageNo,
              pageSize:this.pageSize,
              id:this.thisWindow
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              if (response.data.data !== null) {
                this.tableData = response.data.data.list;
                this.totallyData = response.data.data.totalRow;
              }else{
                this.tableData = [];
                this.totallyData = 0;
              }
            } else {
              errHandler(response.data);
            }
          });
        }
      },
      //设置定时器
      setMyTimeOut: function () {
        if (this.isTimeOut === true) {
          this.clearMyTimeOut();
        }
        this.isTimeOut = true;
        let that = this;
        this.select();
        this.myTimeOut = setInterval(function () {
          that.select();
        }, 1000);
      },
      //清除定时器
      clearMyTimeOut: function () {
        this.isTimeOut = false;
        clearTimeout(this.myTimeOut);
        this.myTimeOut = null;
      },
      toggleRowExpansion:function(row,expanded){
        if(expanded.length > 0){
          this.clearMyTimeOut();
        }else{
          this.setMyTimeOut();
        }
      }
    }
  }
</script>
<style scoped>
  .io-preview {
    background: #fff;
    width: 100%;
    box-sizing: border-box;
    border: 1px solid #ebebeb;
    border-radius: 3px;
    transition: .2s;
    padding:30px 30px;
    min-height:500px;
  }

  .io-preview:hover {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)
  }

  .activeClass {
    color: red;
    font-weight: bold;
  }

  .demo-form-inline .el-form-item {
    margin-bottom: 15px;
  }

  .materialId-box {
    display: flex;
  }
  .box-item{
    margin-right:100px;
  }
  .box-item span:first-of-type {
    display: inline-block;
    color: #99a9bf;
    margin-right: 20px;
  }

  .block {
    margin-top: 15px;
  }
</style>
