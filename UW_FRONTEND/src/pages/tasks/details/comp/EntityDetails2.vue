<template>
  <div class="details-panel2">
    <div class="form-row justify-content-center">
      <div class="details-panel-container">
        <div class="form-row">
          <div class="form-group mb-0">
            <h3>正在审核：</h3>
          </div>
          <datatable v-bind="$data"/>
          <div style="width:100%;text-align:right">
            <div class="btn btn-primary mr-2" @click="taskPass">提交</div>
            <div class="btn btn-secondary" @click="closePanel">取消</div>
          </div>
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
  import {taskCheckUrl,taskUrl} from "../../../../config/globalUrl";
  import {errHandler} from "../../../../utils/errorHandler";
  import {getTaskDetailsConfig} from "../../../../config/taskDetailsConfig";
  import HighLight from './subscomp/HighLight'

  export default {
    name: "EntityDetails",
    components: {
      HighLight
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
        columns: [],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
        val:''
      }
    },
    mounted() {
      let val = store.state.taskDetails;
      this.val = val;
      if (val.type === 0 || val.type === 1 || val.type === 4) {
        this.columns = getTaskDetailsConfig('io2');
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
      ...mapActions(['setTaskActiveState2', 'setTaskData', 'setLoading']),
      init: function () {
        this.data = [];
        this.total = 0;
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200 && response.data.data !== null) {
              this.data = response.data.data.list;
              this.total = response.data.data.totalRow;
            } else {
              errHandler(response.data)
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
        this.setTaskActiveState2(false);
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
      },
      taskPass:function(){
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url: taskUrl + '/pass',
            data: {
              id:this.val.id
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if (res.data.result === 200) {
              this.$alertSuccess('设置成功');
              this.closePanel();
              this.thisData = {};
              let tempUrl = this.$route.fullPath;
              this.$router.push('/_empty');
              this.$router.replace(tempUrl)
            } else {
              errHandler(res.data)
            }
          }).catch(err => {
            if (JSON.stringify(err) !== '{}'){
              this.$alertDanger(JSON.stringify(err))
            }
          })
        }
      }
    }

  }
</script>

<style scoped>
  .details-panel2 {
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
    overflow-y: auto;
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

  #cancel-btn {
    height: 100%;
    cursor: pointer;
  }
</style>
