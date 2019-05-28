<!--中转仓物料管理-->
<template>
  <div class="main-details mt-1 mb-3">
    <datatable
      ref="myTable"
      v-bind="$data"
    >
    </datatable>
    <div style="display:flex;align-items:center;justify-content:flex-end">
      前往
      <label>
        <input type="text"
               class="form-control"
               style="width:100px;margin:5px 10px 0 10px;"
               v-on:keyup.enter="jump"
               v-model.trim="turnPage">
      </label>
      页
    </div>
    <transition name="fade" v-if="isShow">
      <transfer-details :row="row"></transfer-details>
    </transition>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus'
  import {axiosPost} from "../../../../utils/fetchData";
  import {mapGetters, mapActions} from 'vuex'
  import {externalWhSelectExternalWhInfoUrl} from "../../../../config/globalUrl";
  import {errHandler} from "../../../../utils/errorHandler";
  import OperationOptions from "./subscomp/OperationOptions";
  import EntityDetails from '../../comp/EntityDetails'
  import TransferDetails from "./subscomp/TransferDetails";
  export default {
    name: "Details",
    components: {
      TransferDetails,
      OperationOptions,
      EntityDetails
    },
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
        thisRouter: '',
        turnPage:'',
        whId:'',
        supplierId:'',
        no:'',
        isShow:false,
        row:{}
      }
    },
    created() {
      this.init();
      let options = {
        url: externalWhSelectExternalWhInfoUrl,
        data: {
          pageNo: 1,
          pageSize: 20
        }
      };
      this.fetchData(options)
    },
    mounted(){
      eventBus.$on('showDetails',(row) => {
        this.row = row;
        this.isShow = true;
      });
      eventBus.$on('closeTransferDetailsPanel',() => {
        this.isShow = false;
        this.dataFilter();
      });
    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);
        let options = {
          url:externalWhSelectExternalWhInfoUrl,
          data: {
            pageNo: 1,
            pageSize: 20
          }
        };
        if (route.query.searchInfo) {
            this.no = route.query.searchInfo.no;
            this.whId = route.query.searchInfo.whId;
            this.supplierId = route.query.searchInfo.supplierId;
        } else {
          this.no = '';
          this.whId = '';
          this.supplierId = '';
        }
        options.data['no'] = this.no;
        options.data['whId'] = this.whId;
        options.data['supplierId'] = this.supplierId;
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
      ...mapActions(['setTableRouter', 'setLoading']),
      init: function () {
        this.data = [];
        this.columns = [
          {field: 'showId', title: '序号', colStyle: {'width': '70px'}},
          {field: 'wareHouse', title: '所在仓库', colStyle: {'width': '120px'}},
          {field: 'no', title: '料号', colStyle: {'width': '120px'}},
          {field: 'supplier', title: '供应商', colStyle: {'width': '100px'}},
          {field: 'specification', title: '规格', colStyle: {'width': '180px'}},
          {field: 'quantity', title: '数量', colStyle: {'width': '70px'}},
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
              this.turnPage = this.query.offset / this.query.limit + 1;
            } else if (response.data.result === 412) {
              this.$alertWarning(response.data.data);
            } else {
              errHandler(response.data)
            }
          })
            .catch(err => {
              if (JSON.stringify(err) !== '{}') {
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试');
                this.setLoading(false)
              }
            })
        }
      },
      dataFilter: function () {
        let options = {
          url: externalWhSelectExternalWhInfoUrl,
          data: {}
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        options.data['no'] = this.no;
        options.data['whId'] = this.whId;
        options.data['supplierId'] = this.supplierId;
        this.fetchData(options);
      },
      jump:function () {
        if(this.turnPage === ''){
          return;
        }
        let reg = /^[1-9]*[1-9][0-9]*$/;
        if(!reg.test(this.turnPage)){
          return;
        }
        let firstPage = 1;
        let lastPage = this.$refs.myTable.$children[2].totalPage;
        if(firstPage <= Number(this.turnPage) && Number(this.turnPage) <= lastPage ){
          this.$refs.myTable.$children[2].handleClick(this.turnPage);
        }
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
