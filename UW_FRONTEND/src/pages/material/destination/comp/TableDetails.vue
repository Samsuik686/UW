<!--发料目的地管理-->
<template>
  <div class="main-details mt-1 mb-3">
    <datatable
      v-bind="$data"
    ></datatable>
  </div>
</template>

<script>
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {destinationSelectUrl} from "../../../../config/globalUrl";
  import {mapActions} from 'vuex'
  import OperationOptions from "./subscomp/OperationOptions";
  export default {
    name: "TableDetails",
    data(){
      return{
        fixHeaderAndSetBodyMaxHeight: 650,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed'
        },
        HeaderSettings: false,
        pageSizeOptions: [20, 40, 80, 100],
        data: [],
        columns: [],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
        thisRouter: '',
        filter: '',
      }
    },
    created(){
      this.init();
      let options = {
        url:destinationSelectUrl,
        data: {
          pageNo: 1,
          pageSize: 20
        }
      };
      this.fetchData(options)
    },
    components:{
      OperationOptions
    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);
        let options = {
          url: destinationSelectUrl,
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
    methods:{
      ...mapActions(['setTableRouter', 'setLoading']),
      init: function () {
        this.data = [];
        this.columns = [
          {field: 'showId', title: '序号', colStyle: {'width': '70px'}},
          {field: 'id', title: '发料目的地ID', colStyle: {'width': '70px'}},
          {field: 'name', title: '发料目的地', colStyle: {'width': '150px'}},
          {title: '操作', tdComp: 'OperationOptions', colStyle: {'width': '80px'} }
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
              let data = response.data.data.list;
              data.map((item,index) => {
                if(item.id !== -1 && item.id !== 0){
                  this.data.push(item);
                }
              });
              this.data.map((item, index) => {
                item.showId = index + 1 + this.query.offset;
              });
              this.total = this.data.length;
            } else {
              errHandler(response.data);
            }
          })
            .catch(err => {
              if (JSON.stringify(err) !== '{}'){
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
          url: destinationSelectUrl,
          data: {
          }
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        if (this.filter !== "") {
          options.data.filter = this.filter
        }
        this.fetchData(options);
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
