<template>
  <div class="iologs-panel">
    <div class="form-row justify-content-center ">
      <div class="iologs-panel-container">
        <div class="form-row">
          <div class="form-group mb-0 mr-5">
            <h3>出入库记录：</h3>
          </div>
          <div class="form-group destination" style="margin-right:20px;">
            <label for="type" class="mb-0 destination-label">出入库类型：</label>
            <select id="type" v-model="type" class="custom-select">
              <option value="" disabled>请选择</option>
              <option label="出库" value="0"></option>
              <option label="入库" value="1"></option>
            </select>
          </div>
          <div class="form-group destination">
            <label for="destination" class="mb-0 destination-label">发料目的地：</label>
            <select id="destination" v-model="destination" class="custom-select" :disabled="type === '1'">
              <option value="" disabled>请选择</option>
              <option :value="item.id" v-for="item in destinations">{{item.name}}</option>
            </select>
          </div>
          <datatable v-bind="$data"/>
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
  import {mapActions, mapGetters} from 'vuex'
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import {destinationSelectUrl, getMaterialRecordsUrl} from "../../../../../config/globalUrl";

  export default {
    name: "IODetails",
    props: ['detailsID'],
    data() {
      return {
        destination:'',
        type:'0',
        destinations:[],
        fixHeaderAndSetBodyMaxHeight: 450,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed',
          'white-space': 'pre-wrap'
        },
        HeaderSettings: false,
        pageSizeOptions: [20, 40, 80],
        data: [],
        columns: [
          {field: 'fileName', title: '任务名', colStyle: {'width': '90px'}},
          {field: 'taskType', title: '类型', colStyle: {'width': '70px'}},
          {field: 'planQuantity', title: '计划数量', colStyle: {'width': '90px'}},
          {field: 'actualQuantity', title: '实际数量', colStyle: {'width': '90px'}},
          {field: 'superIssuedQuantity', title: '超发数量', colStyle: {'width': '90px'}},
          {field: 'operator', title: '操作员', colStyle: {'width': '90px'}},
          {field: 'ioTime', title: '出入库时间', colStyle: {'width': '130px'}},
        ],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
        thisDetailsID: ''
      }
    },
    watch: {
      query: {
        handler(val) {
          this.setLoading(true);
          this.dataFilter(val);
        },
        deep: true
      },
      destination:{
        handler(val){
          this.query.limit = 20;
          this.query.offset = 0;
          this.getMaterialRecords();
        },
        deep:true
      },
      type:{
        handler(val){
          this.query.limit = 20;
          this.query.offset = 0;
          this.getMaterialRecords();
        },
        deep:true
      }
    },
    mounted() {
      this.thisDetailsID = this.detailsID;
      this.selectDestination();
    },
    methods: {
      ...mapActions(['setLoading']),
      getMaterialRecords: function () {
        this.setLoading(true);
        this.init();
        let options = {
          url: getMaterialRecordsUrl,
          data: {
            materialTypeId: this.thisDetailsID,
            type:this.type,
            destination:this.destination,
            pageNo: 1,
            pageSize: 20
          }
        };
        this.fetchData(options);
      },
      init: function () {
        this.data = [];
        this.total = 0;
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;

          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.data = response.data.data.list;
              this.total = response.data.data.totalRow;
              this.setLoading(false)
            } else {
              this.setLoading(false);
              errHandler(response.data)
            }
          })
            .catch(err => {
              if (JSON.stringify(err)) {
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试');
                this.setLoading(false)
              }
            })
        } else {
          this.setLoading(false)
        }
      },
      dataFilter: function () {
        this.setLoading(true);
        let val = this.thisDetailsID;
        let options = {
          url: getMaterialRecordsUrl,
          data: {
            type: val,
            destination:this.destination
          }
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        this.fetchData(options);
      },
      closePanel: function () {
        this.init();
        this.thisDetailsID = '';
        eventBus.$emit('closeIODetailsPanel');
      },
      selectDestination: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: destinationSelectUrl,
            data: {}
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.destinations = response.data.data.list;
              this.destination = this.destinations[0].id + '';
              this.getMaterialRecords();
            } else {
              errHandler(response.data.result)
            }
          })
            .catch(err => {
              if (JSON.stringify(err) !== '{}') {
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试');
              }
            })
        }
      },
    }
  }
</script>

<style scoped>
  .iologs-panel {
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
    white-space: normal;
  }

  .iologs-panel-container {
    background: #ffffff;
    min-height: 300px;
    max-width: 90%;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
  .destination{
    display:flex;
    width:250px
  }
  .destination-label{
    width:180px;
    line-height:38px
  }
  #cancel-btn {
    height: 100%;
    cursor: pointer;
  }
</style>
