<template>
  <div class="add-panel">
    <div class="form-row justify-content-end add-panel-box">
      <div class="add-panel-container">
        <div class="form-row">
          <div class="form-group mb-0">
            <h3>详情：</h3>
          </div>
          <datatable v-bind="$data" style="width:100%"/>
          <div style="width:100%;text-align:right">
            <div class="btn btn-primary" @click="allClosePosition">一键平仓</div>
          </div>
        </div>
      </div>
      <div id="cancel-btn" class="ml-2 mt-1" @click="closePanel">
        <icon name="cancel" scale="4" style="color: #fff;"></icon>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapActions} from 'vuex';
  import {axiosPost} from "./../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import eventBus from "../../../../../utils/eventBus";
  import {
    coverUwMaterialByTaskIdUrl,
    getInventoryTaskDetailsUrl,
    getUwInventoryTaskDetailsUrl
  } from "../../../../../config/globalUrl";
  import {coverMaterialByTaskIdUrl} from "../../../../../config/globalUrl";
  import ClosePosition from './CloseUWPosition'

  export default {
    name: "EntityDetails",
    components:{
      ClosePosition
    },
    props:{
      row:Object
    },
    data() {
      return {
        fixHeaderAndSetBodyMaxHeight: 450,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed',
          'white-space': 'pre-wrap'
        },
        HeaderSettings: false,
        data: [],
        columns: [
          {title: '所在仓位', field: 'whName', colStyle: {width: '100px'}},
          {title: '料盘唯一码', field: 'materialId', colStyle: {width: '110px'}},
          {title: '盘前库存', field: 'beforeNum', colStyle: {width: '80px'}},
          {title: '盘点数量', field: 'atrualNum', colStyle: {width: '80px'}},
          {title: '盘盈/盘亏', field: 'differentNum', colStyle: {width: '80px'}},
          {title: '盘点人', field: 'inventoryOperatior', colStyle: {width: '90px'}},
          {title: '盘点时间', field: 'inventoryTime', colStyle: {width: '120px'}},
          {title: '平仓人', field: 'coverOperatior', colStyle: {width: '90px'}},
          {title: '平仓时间', field: 'coverTime', colStyle: {width: '120px'}},
          {title: '操作', tdComp: 'ClosePosition', colStyle: {'width': '90px'}}
        ],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
        Pagination:false,
      }
    },
    mounted(){
      /*if(this.row.checked_time === '' || this.row.checked_time === null){
        this.$alertWarning('当前盘点数据未审核');
      }*/
      this.getInventoryTaskDetails();
      eventBus.$on('checkDetailsRefresh',() => {
        this.getInventoryTaskDetails();
      });
    },
    methods: {
      ...mapActions([ 'setLoading']),
      init: function () {
        this.data = [];
        this.total = 0;
      },
      getInventoryTaskDetails:function(){
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:getUwInventoryTaskDetailsUrl,
            data:{
              taskId:this.row.task_id,
              materialTypeId:this.row.material_type_id
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.data = res.data.data;
              this.data.map((item) => {
                item.isChecked = this.row.checked_time !== null;
                item.isFinished = this.row.state === 3;
              })
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
          })
        }
      },
      closePanel: function () {
        eventBus.$emit('closeCheckPanel');
      },
      allClosePosition:function(){
        if(!this.isPending){
          this.isPending = false;
          let options = {
            url:coverUwMaterialByTaskIdUrl,
            data:{
              materialTypeId:this.row.material_type_id,
              taskId:this.row.task_id
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess('一键平仓成功');
              this.getInventoryTaskDetails();
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
          })
        }
      }
    }

  }
</script>

<style scoped>
  .add-panel {
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
  .add-panel-box{
    width:95%;
    display:flex;
  }
  .add-panel-container {
    background: #ffffff;
    min-height: 220px;
    width:90%;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
  #cancel-btn {
    height: 100%;
    cursor: pointer;
  }
</style>
