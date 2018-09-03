<template>
  <div class="preview-details mt-1 mb-3">
    <datatable v-bind="$data.tableData"/>
  </div>
</template>

<script>
  import OperationOptions from './OperationOptions'
  import {mapGetters, mapActions} from 'vuex'
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {taskWindowTaskItems} from "../../../../config/globalUrl";

  export default {
    name: "PreviewDetails",
    components: {
      OperationOptions
    },
    data() {
      return {
        tableData: {
          fixHeaderAndSetBodyMaxHeight: 650,
          tblStyle: {
            'word-break': 'break-all',
            'table-layout': 'fixed'

          },
          HeaderSettings: false,
          pageSizeOptions: [20, 40, 80, 100],
          data: [],
          //srcData: [],
          columns: [],
          total: 0,
          query: {"limit": 20, "offset": 0},
        },
        isPending: false,
        thisRouter: '',
        filter: "",
      }
    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);
        let options = {
          url: taskWindowTaskItems,
          data: {
            id: this.currentWindowId
          }
        };
        this.fetchData(options);
      },
      query: {
        handler(query) {
          this.setLoading(true);
          this.dataFilter(query);
        },
        deep: true
      }
    },
    created() {

    },
    mounted() {
      this.init();
      this.setLoading(true);
      let options = {
        url: taskWindowTaskItems,
        data: {
          id: this.currentWindowId
        }
      };
      this.fetchData(options);
      window.g.PREVIEW_ITEMS_INTERVAL.push(setInterval(() => {
        if (this.currentWindowId !== '') {
          let options = {
            url: taskWindowTaskItems,
            data: {
              id: this.currentWindowId
            }
          };
          this.fetchData(options)
        } else {
          this.init()
        }
      }, 3000))
    },
    computed: {
      ...mapGetters(['currentWindowId'])
    },
    methods: {
      ...mapActions(['setLoading']),
      init: function () {
        this.tableData.data = [];
        this.tableData.columns = [
          {field: 'id', title: 'ID', colStyle: {'width': '60px'}},
          {field: 'fileName', title: '套料单名称', colStyle: {'width': '120px'}},
          {field: 'type', title: '操作类型', colStyle: {'width': '80px'}},
          {field: 'materialNo', title: '料号', colStyle: {'width': '120px'}},
          {field: 'planQuantity', title: '计划数量', colStyle: {'width': '90px'}},
          {field: 'actualQuantity', title: '实际数量', colStyle: {'width': '90px'}},
          {field: 'finishTime', title: '完成时间', colStyle: {'width': '120px'}},
          {field: 'operation', title: '操作', tdComp: 'OperationOptions', colStyle: {'width': '90px'}},
        ];
        this.tableData.total = 0;
        this.tableData.query = {"limit": 20, "offset": 0}
      },
      fetchData: function (options) {
        axiosPost(options).then(response => {
          this.isPending = false;
          if (response.data.result === 200) {
            if (response.data.data !== null) {
              this.tableData.data = response.data.data.list;
              this.tableData.total = response.data.data.totalRow;
            }
          } else {
            errHandler(response.data.result)
          }
          this.setLoading(false)
        })
          .catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试');
              this.setLoading(false)
            }
          })
      },
      dataFilter: function () {
        let options = {
          url: taskWindowTaskItems,
          data: {
            id: this.currentWindowId
          }
        };
        options.data.pageNo = this.tableData.query.offset / this.tableData.query.limit + 1;
        options.data.pageSize = this.tableData.query.limit;
        this.fetchData(options);
      },
      routerReload: function () {
        let tempPath = this.$route.path;
        this.$router.push('_empty');
        this.$router.push(tempPath)
      }
    }
  }
</script>

<style scoped>
  .preview-details {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
    min-height: 500px;
  }

</style>
