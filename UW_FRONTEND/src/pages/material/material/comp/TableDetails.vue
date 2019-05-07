<!--物料管理-->
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
      </label
      >页
    </div>
    <entity-details v-if="isDetailsActive"/>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus'
  import {axiosPost} from "../../../../utils/fetchData";
  import {mapGetters, mapActions} from 'vuex'
  import {deleteByIdsUrl, materialCountUrl} from "../../../../config/globalUrl";
  import {errHandler} from "../../../../utils/errorHandler";
  import OperationOptions from "./subscomp/OperationOptions";
  import EntityDetails from '../../comp/EntityDetails'
  export default {
    name: "Details",
    components: {
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
        //srcData: [],
        columns: [],
        total: 0,
        selection:[],
        query: {"limit": 20, "offset": 0},
        isPending: false,
        thisRouter: '',
        filter: '',
        turnPage:''
      }
    },
    created() {
      this.init();
      let options = {
        url: materialCountUrl,
        data: {
          pageNo: 1,
          pageSize: 20
        }
      };
      this.fetchData(options)
    },
    computed: {
      ...mapGetters([
        'isDetailsActive'
      ]),

    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);
        let options = {
          url: materialCountUrl,
          data: {
            pageNo: 1,
            pageSize: 20
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
      ...mapActions(['setTableRouter', 'setLoading']),
      init: function () {
        this.data = [];
        this.columns = [
          {field: 'showId', title: '序号', colStyle: {'width': '50px'}},
          {field: 'supplier', title: '客户专用码', colStyle: {'width': '70px'}},
          {field: 'id', title: '物料类型号', colStyle: {'width': '70px'}},
          {field: 'no', title: '料号', colStyle: {'width': '120px'}},
          {field: 'supplierName', title: '供应商', colStyle: {'width': '100px'}},
          {field: 'specification', title: '规格', colStyle: {'width': '180px'}},
          {field: 'thickness', title: '厚度', colStyle: {'width': '80px'}},
          {field: 'radius', title: '直径', colStyle: {'width': '80px'}},
          {field: 'enabled', title: '可用性', visible: false},
          {field: 'enabledString', title: '是否可用', colStyle: {'width': '70px'}, visible: false},
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
          url: materialCountUrl,
          data: {}
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        if (this.filter !== "") {
          options.data.filter = this.filter
        }
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
      },
      deleteByIds:function(){
        if(this.selection.length === 0){
          this.$alertWarning('请选择你要删除的物料');
          return;
        }
        let filter = '';
        this.selection.map((item,index) => {
          if(index !== this.selection.length - 1){
            filter = filter + item.id + ',';
          }else{
            filter = filter + item.id;
          }
        });
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:deleteByIdsUrl,
            data:{
              filter:filter
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess(res.data.data);
              let tempUrl = this.$route.path;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
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
  .main-details {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
    min-height: 500px;
  }
</style>
