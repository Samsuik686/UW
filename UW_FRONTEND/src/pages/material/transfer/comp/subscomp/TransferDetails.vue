<template>
  <div class="add-panel">
    <div class="form-row justify-content-end  add-panel-box">
      <div class="add-panel-container">
        <div class="form-row">
          <div class="form-group mb-0">
            <h3>详情：</h3>
          </div>
          <datatable v-bind="$data" style="width:100%"/>
        </div>
      </div>
      <div id="cancel-btn" class="ml-2 mt-1" @click="closePanel">
        <icon name="cancel" scale="4" style="color: #fff;"></icon>
      </div>
    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus'
  import {mapActions} from 'vuex';
  import {axiosPost} from "../../../../../utils/fetchData";
  import {externalWhSelectEWhMaterialDetailsUrl} from "../../../../../config/globalUrl";
  import {errHandler} from "../../../../../utils/errorHandler";
  import DeleteDetailsItem from './DeleteDetailsItem'
  export default {
    name: "TransferDetails",
    data() {
      return {
        fixHeaderAndSetBodyMaxHeight: 450,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed',
          'white-space': 'pre-wrap'
        },
        HeaderSettings: false,
        pageSizeOptions: [20, 40, 80],
        data: [],
        columns: [],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
      }
    },
    props:{
      row:Object
    },
    components:{
      DeleteDetailsItem
    },
    watch: {
      query: {
        handler(query) {
          this.setLoading(true);
          this.dataFilter(query);
        },
        deep: true
      }
    },
    mounted() {
      this.init();
      let options = {
        url:externalWhSelectEWhMaterialDetailsUrl,
        data: {
          pageNo: 1,
          pageSize: 20,
          materialTypeId:this.row.materialTypeId,
          whId:this.row.whId
        }
      };
      this.fetchData(options);
      eventBus.$on('refresh',() => {
        this.dataFilter();
      });
      eventBus.$on('remarkRefresh',() => {
        this.dataFilter();
      });
    },
    methods: {
      ...mapActions(['setLoading']),
      init: function () {
        this.data = [];
        this.total = 0;
        this.columns = [
          {title: '序号', field: 'showId', colStyle: {width: '60px'}},
          {title: '任务', field: 'taskName', colStyle: {width: '140px'}},
          {title: '类型', field: 'taskTypeString', colStyle: {width: '80px'}},
          {title: '来源地', field: 'sourceWhName', colStyle: {width: '100px'}},
          {title: '目的地', field: 'destinationName', colStyle: {width: '100px'}},
          {title: '数量', field: 'quantity', colStyle: {width: '70px'}},
          {title: '调拨多出数量', field: 'returnNum', colStyle: {width: '70px'}},
          {title: '日期', field: 'time', colStyle: {width: '120px'}},
          {title: '备注', field: 'remarks', colStyle: {width: '100px'}},
          {title: '操作', tdComp: 'DeleteDetailsItem', colStyle: {'width': '80px'}}
        ]
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              if (response.data.data.list !== null) {
                this.data = response.data.data.list;
                this.data.map((item, index) => {
                  item.showId = index + 1 + this.query.offset;
                  if(item.taskType === 1 && item.quantity > 0){
                    item.taskTypeString = "出库超发";
                  }
                  if(item.taskType === 1 && item.quantity < 0){
                    item.taskTypeString = "出库抵扣";
                  }
                });
                this.total = response.data.data.totalRow;
              } else {
                this.init()
              }
            } else {
              errHandler(response.data)
            }
            this.setLoading(false)
          })
            .catch(err => {
              if (JSON.stringify(err) !== '{}'){
                this.isPending = false;
                this.$alertDanger('请求超时，请刷新重试');
                this.setLoading(false)
              }
            })
        } else {
          this.setLoading(false)
        }
      },
      closePanel: function () {
        eventBus.$emit('closeTransferDetailsPanel',true);
      },
      dataFilter: function () {
        let options = {
          url:externalWhSelectEWhMaterialDetailsUrl,
          data: {
            materialTypeId:this.row.materialTypeId,
            whId:this.row.whId
          }
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        this.fetchData(options);
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
    height:100%;
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
    width:95%;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
  #cancel-btn {
    height:100%;
    cursor: pointer;
  }
</style>

