<template>
  <div class="details-panel">
    <div class="form-row justify-content-center">
      <div class="details-panel-container">
        <div class="form-row">
          <div class="form-group mb-0">
            <h3>任务详情：</h3>
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
  import {mapGetters, mapActions} from 'vuex';
  import store from '../../../../store'
  import {axiosPost} from "../../../../utils/fetchData";
  import {taskCheckUrl} from "../../../../config/globalUrl";
  import {errHandler} from "../../../../utils/errorHandler";
  import {getTaskDetailsConfig} from "../../../../config/taskDetailsConfig";
  import SubsOperation from './subscomp/SubsOperationOption'

  export default {
    name: "EntityDetails",
    components: {
      SubsOperation
    },
    data() {
      return {
        fixHeaderAndSetBodyMaxHeight: 550,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed'
        },
        HeaderSettings: false,
        pageSizeOptions: [20, 40, 80],
        data: [],
        columns: [],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false
      }
    },
    mounted() {
      let val = store.state.taskDetails;
      if (val.type === 0 || val.type === 1) {
        this.columns = getTaskDetailsConfig('io')
      }
      let options = {
        url: taskCheckUrl,
        data: {
          pageNo: 1,
          pageSize: 20,
          id: val.id,
          type: val.type
        }
      };
      this.fetchData(options)
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
    methods: {
      ...mapActions(['setTaskActiveState','setTaskData', 'setLoading']),
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
            } else {
              errHandler(response.data.result)
            }
            this.setLoading(false)
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
      closePanel: function () {
        this.setTaskActiveState(false);
        this.setTaskData('')
      },
      dataFilter: function () {
        let val = store.state.taskDetails;
        let options = {
          url: taskCheckUrl,
          data: {
            id: val.id,
            type: val.type
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
  .details-panel {
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

  .details-panel-container {
    background: #ffffff;
    min-height: 300px;
    max-width: 90%;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
  #cancel-btn{
    height: 100%;
    cursor: pointer;
  }
</style>
