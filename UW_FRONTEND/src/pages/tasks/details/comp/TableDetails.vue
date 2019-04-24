<!--物料管理-->
<template>
  <div class="main-details mt-1 mb-3">
    <datatable
      ref="myTable"
      v-bind="$data"
    ></datatable>
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
    <entity-details v-if="isTaskDetailsActive"/>
    <!--<div class="row">-->
    <!--<div class="form-group col pr-3">-->
    <!--<label :for="opt.id" class="col-form-label">{{opt.name}}：</label>-->
    <!--<select :id="opt.id" v-model="opt.model" class="custom-select">-->
    <!--<option value="" disabled>请选择</option>-->
    <!--<option :value="item.value"  v-for="item in opt.list">{{item.string}}</option>-->
    <!--</select>-->
    <!--</div>-->
    <!--</div>-->
  </div>
</template>

<script>
  import {axiosPost} from "../../../../utils/fetchData";
  import {mapGetters, mapActions} from 'vuex'
  import {taskSelectUrl} from "../../../../config/globalUrl";
  import {errHandler} from "../../../../utils/errorHandler";
  import OperationOptions from "./subscomp/OperationOptions";
  import EntityDetails from './EntityDetails'

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
        query: {"limit": 20, "offset": 0},
        isPending: false,
        thisRouter: '',
        filter: "",
        turnPage:''
      }
    },
    created() {
      this.init();
      let options = {
        url: taskSelectUrl,
        data: {
          pageNo: 1,
          pageSize: 20,
          descBy: 'create_time'
        }
      };
      this.fetchData(options)
    },
    computed: {
      ...mapGetters([
        'isTaskDetailsActive'
      ]),

    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);

        let options = {
          url: taskSelectUrl,
          data: {
            pageNo: 1,
            pageSize: 20,
            descBy: 'create_time',
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
    mounted: function () {
    },
    methods: {
      ...mapActions(['setTableRouter', 'setLoading']),
      init: function () {
        this.data = [];
        this.columns = [
          {field: 'showId', title: '序号', colStyle: {'width': '70px'}},
          {field: 'id', title: '序号', visible: false},
          {field: 'state', title: '状态', visible: false},
          {field: 'stateString', title: '状态', colStyle: {'width': '80px'}},
          {field: 'type', title: '类型', visible: false},
          {field: 'typeString', title: '类型', colStyle: {'width': '80px'}},
          {field: 'supplierName', title: '供应商', colStyle: {'width': '100px'}},
          {field: 'priority', title: '优先级', colStyle: {'width': '60px'}},
          {field: 'fileName', title: '文件名', colStyle: {'width': '120px'}},
          {field: 'createTimeString', title: '创建时间', colStyle: {'width': '70px'}},
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
              if (JSON.stringify(err)) {
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试');
                this.setLoading(false);
              }
            })
        }
      },
      dataFilter: function () {
        let options = {
          url: taskSelectUrl,
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
