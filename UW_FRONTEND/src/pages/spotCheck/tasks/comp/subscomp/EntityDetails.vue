<template>
  <div class="add-panel">
    <div class="form-row justify-content-end add-panel-box">
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
  import {mapActions} from 'vuex';
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import {getSampleTaskDetailsUrl} from "../../../../../config/globalUrl";
  export default {
    name: "EntityDetails",
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
        pageSizeOptions: [20, 40, 80],
        data: [],
        columns: [
          {title: 'ID', field: 'id', colStyle: {width: '80px'}},
          {title: '料号', field: 'no', colStyle: {width: '120px'}},
          {title: '料盘号', field: 'materialId', colStyle: {width: '120px'}},
          {title: '数量', field: 'quantity', colStyle: {width: '90px'}},
          {title: '状态', field: 'isSingularString', colStyle: {width: '120px'}},
          {title: '操作员', field: 'operator', colStyle: {width: '90px'}},
          {title: '出库时间', field: 'time', colStyle: {width: '120px'}}
        ],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false
      }
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
      let options = {
        url: getSampleTaskDetailsUrl,
        data: {
          taskId:this.row.id,
          pageNo: 1,
          pageSize: 20
        }
      };
      this.fetchData(options);
    },
    methods: {
      ...mapActions(['setLoading']),
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
              if (response.data.data.list !== null) {
                this.data = response.data.data.list;
                this.total = response.data.data.totalRow;
              } else {
                this.init()
              }
            } else if (response.data.result === 501) {
              this.$alertWarning(response.data.data)
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
        this.$emit('closePanel',true);
      },
      dataFilter: function () {
        let options = {
          url: getSampleTaskDetailsUrl,
          data: {
            taskId:this.row.id
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
