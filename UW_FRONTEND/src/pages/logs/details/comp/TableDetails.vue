<!--表单查看页面的表单详情 统一配置-->
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
  </div>
</template>

<script>
  import HighLight from './HighLight'
  import {axiosPost} from "../../../../utils/fetchData";
  import {mapGetters, mapActions} from 'vuex'
  import {logsUrl} from "../../../../config/globalUrl";
  import {errHandler} from "../../../../utils/errorHandler";
  import {getLogsConfig} from "../../../../config/logsApiConfig";

  export default {
    name: "Details",
    components: {
      HighLight
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
        columns: [
          {}
        ],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
        thisRouter: '',
        filter: '',
        turnPage:''
      }
    },
    created() {
      this.init();
      this.thisFetch(this.$route.query);
      this.thisRouter = this.$route.query.page;
    },
    computed: {
      ...mapGetters([
        'logsRouterApi'
      ]),

    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);
        if (route.query.data) {
          this.fetchData(route.query);
          this.filter = route.query.data.filter;
          this.thisRouter = route.query.data.table;

        } else {
          let options = {
            url: logsUrl,
            data: {
              table: route.query.page,
              pageNo: 1,
              pageSize: 20,
              descBy: 'time'
            }
          };
          this.fetchData(options);
          this.filter = "";
          this.thisRouter = route.query.page;
        }

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
      ...mapActions(['setLogsRouter', 'setLoading']),
      init: function () {
        this.data = [];
        //this.srcData = [];
        this.columns = [];
        this.total = 0;
        this.thisRouter = '';
        this.query = {"limit": 20, "offset": 0};
        this.setLogsRouter('default');

      },
      thisFetch: function (opt) {
        let options = {
          url: logsUrl,
          data: {
            table: opt.page,
            pageNo: 1,
            pageSize: 20,
            descBy: 'time'
          }
        };
        this.fetchData(options)
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;
          this.columns = getLogsConfig(options.data.table);
          this.setLogsRouter(options.data.table);
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.data = response.data.data.list;
              this.data.map((item, index) => {
                item.showId = index + 1 + this.query.offset;
              });
              this.total = response.data.data.totalRow;
              this.turnPage = this.query.offset / this.query.limit + 1;
              this.setLoading(false);
            } else if (response.data.result === 412) {
              this.$alertWarning(response.data.data);
              this.setLoading(false);
            } else {
              errHandler(response.data)
              this.setLoading(false);
            }
          })
            .catch(err => {
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试')
              this.setLoading(false);
            })
        }
      },
      dataFilter: function () {
        let options = {
          url: logsUrl,
          data: {
            table: this.thisRouter,
            descBy: 'time'
          }
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        if (this.filter !== "") {
          options.data.filter = this.filter
        }
        this.fetchData(options);
        //this.data = this.srcData.slice(this.query.offset, this.query.offset + this.query.limit);
        // this.data.map((item, index) => {
        //   item.showId = index + 1 + this.query.offset;
        // })
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
