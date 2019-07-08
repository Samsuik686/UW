<template>
  <div class="main-details mt-1 mb-3">
    <datatable
      v-bind="$data"
    ></datatable>
    <entity-details v-if="isShow" :row="row" @closePanel="closePanel"></entity-details>
  </div>
</template>

<script>
  import StartPause from './subscomp/StartPause'
  import {mapActions} from 'vuex'
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {selectAllInventoryTaskUrl, selectSampleTasksUrl} from "../../../../config/globalUrl";
  import OperationOptions from './subscomp/OperationOptions'
  import EntityDetails from './subscomp/EntityDetails'
  import eventBus from "../../../../utils/eventBus";
  export default {
    name: "TableDetails",
    data() {
      return {
        fixHeaderAndSetBodyMaxHeight: 650,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed',
          'white-space': 'pre-wrap'
        },
        HeaderSettings: false,
        pageSizeOptions: [20, 40, 80, 100],
        data: [],
        columns: [],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
        isShow:false,
        row:{}
      }
    },
    mounted(){
      eventBus.$on('showSampleDetails',(row) => {
        this.row = row;
        this.isShow = true;
      });
      eventBus.$on('refreshTask',() => {
        this.dataFilter();
      });
    },
    components:{
      EntityDetails,
      OperationOptions,
      StartPause
    },
    created() {
      this.init();
      let options = {
        url:selectSampleTasksUrl,
        data: {
          descBy: 'create_time',
          pageNo: 1,
          pageSize: 20
        }
      };
      this.fetchData(options)
    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);
        let options = {
          url:selectSampleTasksUrl,
          data: {
            pageNo: 1,
            pageSize: 20,
            descBy: 'create_time'
          }
        };
        if (route.query.filter) {
          this.filter = route.query.filter;
          options.data.filter = this.filter
        } else {
          this.filter = "";
        }
        this.fetchData(options)
      },
      query: {
        handler(query) {
          this.setLoading(true);
          this.dataFilter(query);
        },
        deep: true
      }
    },
    methods: {
      ...mapActions(['setLoading']),
      init: function () {
        this.data = [];
        this.columns = [
          {title: '启动/暂停', tdComp: 'StartPause', colStyle: {'width':'80px'}},
          {field: 'showId', title: '序号', colStyle: {'width': '70px'}},
          {field: 'fileName', title: '任务名', colStyle: {'width': '120px'}},
          {field: 'stateString', title: '状态', colStyle: {'width': '80px'}},
          {field: 'supplierName', title: '供应商', colStyle: {'width': '80px'}},
          {field: 'createTimeString', title: '创建时间', colStyle: {'width': '120px'}},
          {field: 'remarks', title: '备注', colStyle: {'width': '100px'}},
          {title: '操作', tdComp: 'OperationOptions', colStyle: {'width': '80px'}}
        ];
        this.total = 0;
        this.query = {"limit": 20, "offset": 0}
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;
          axiosPost(options).then(response => {
            this.setLoading(false);
            this.isPending = false;
            if (response.data.result === 200) {
              this.data = response.data.data.list;
              this.data.map((item, index) => {
                item.showId = index + 1 + this.query.offset;
              });
              this.total = response.data.data.totalRow;
            } else {
              errHandler(response.data)
            }
          })
            .catch(err => {
              this.isPending = false;
              console.log(err);
              this.$alertDanger('请求超时，请刷新重试');
              this.setLoading(false)
            })
        }
      },
      dataFilter: function () {
        let options = {
          url: selectSampleTasksUrl,
          data: {
            descBy: 'create_time'
          }
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        if (this.filter !== "") {
          options.data.filter = this.filter
        }
        this.fetchData(options);
      },
      closePanel:function () {
        this.isShow = false;
      }
    }
  }
</script>

<style scoped>
  .main-details {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
    min-height: 500px;
  }
</style>
