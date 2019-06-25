<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>更新任务状态：</h3>
        </div>
      </div>
      <div class="form-row" v-if="originState === '1' || originState === '2'">
        <label for="examine-select" class="col-form-label">状态更改:</label>
        <select id="examine-select" class="custom-select"
                v-model="thisState">
          <option :value="originState" disabled>请选择</option>
          <option value="2" v-if="originState === '1'">开始任务</option>
          <option value="3" >作废任务</option>
        </select>
      </div>
      <div class="form-row" v-else>
        <label for="status-else-select" class="col-form-label">状态更改:</label>
        <select id="status-else-select" class="custom-select"
                v-model="thisState" disabled>
          <option :value="originState" disabled>无法操作</option>
        </select>
      </div>
      <div class="form-row" v-if="windowShow === '2' && originState === '1'">
        <label for="window-select" class="col-form-label">仓口选择:</label>
        <select id="window-select" class="custom-select" v-model="windowArr" multiple :disabled="window.length === 0">
          <option value="" disabled>{{window.length > 0 ? '请选择' : '无可用仓口'}}</option>
          <option v-for="item in window" :value="item.id">{{item.id}}</option>
        </select>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row">
        <button class="btn btn-secondary col mr-1 text-white" @click="closePanel">取消</button>
        <button class="btn btn-primary col ml-1 text-white" @click="submitUpdate" :disabled="originState !== '1' && originState !== '2'">提交</button>
      </div>
    </div>
  </div>
</template>

<script>
  import {
    cancelSampleTaskUrl,
    startSampleTaskUrl,
    taskWindowsUrl
  } from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import eventBus from "../../../../../utils/eventBus";
  import {errHandler} from "../../../../../utils/errorHandler";
  import {mapActions} from 'vuex'
  import Qs from 'qs'
  import {editData} from "../../../../../store/getters";
  export default {
    name: "EditStatus",
    props: {
      editData: Object
    },
    data() {
      return {
        originState: '',
        thisState: '',
        windowArr: [],
        window: [],
        windowShow: '',
        taskId: '',
        isPending: false
      }
    },
    watch: {
      thisState: function (val) {
        this.windowShow = val;
      }
    },
    mounted() {
      this.taskId = this.editData['id'];
      this.originState = this.editData['state'].toString();
      this.thisState = this.editData['state'].toString();
      this.selectWindows();
    },
    methods: {
      ...mapActions(['setLoading']),
      //获取仓口
      selectWindows: function () {
        let options = {
          url: taskWindowsUrl,
          data: {
            type: 0
          }
        };
        axiosPost(options).then(res => {
          this.window = res.data.data
        }).catch(err => {
          console.log(err);
        })
      },
      closePanel: function () {
        eventBus.$emit('closeSpotPanel');
      },
      submitUpdate: function () {
        if (!this.isPending) {
          this.isPending = true;
          this.setLoading(true);
          if (this.thisState > 0 && (this.thisState !== this.originState)) {
            let statusUrl;
            switch (this.thisState) {
              case '2':
                statusUrl = startSampleTaskUrl;
                break;
              case '3':
                statusUrl = cancelSampleTaskUrl;
                break;
            }
            let options = {
              url: statusUrl,
              data: {
                taskId: this.taskId
              }
            };
            if (this.thisState === '2') {
              if (this.windowArr.length === 0) {
                this.$alertInfo("请选择仓口");
                this.isPending = false;
                this.setLoading(false);
                return;
              }
              let windows = '';
              this.windowArr.map((item,index) => {
                if(index === 0){
                  windows = windows + item;
                }else{
                  windows = windows + ','+item;
                }
              });
              options.data.windows = windows;
              this.startTask(options);
            }else{
              axiosPost(options).then(res => {
                this.isPending = false;
                this.setLoading(false);
                if (res.data.result === 200) {
                  this.$alertSuccess(res.data.data);
                  this.windowShow = '';
                  this.taskId = '';
                  this.windowArr = [];
                  let tempUrl = this.$route.fullPath;
                  this.$router.push('/_empty');
                  this.$router.replace(tempUrl);
                } else {
                  errHandler(res.data)
                }
              }).catch(err => {
                this.isPending = false;
                this.setLoading(false);
                console.log(err);
                this.$alertDanger('连接超时，请刷新重试');
              })
            }
          }
        }
      },
      startTask:function(options){
        this.$axios({
          method: 'post',
          url:options.url,
          timeout: 600000,
          headers: {
            'Content-type': 'application/x-www-form-urlencoded; charset=UTF-8'
          },
          data: Qs.stringify(options.data)
        }).then(res => {
          this.isPending = false;
          this.setLoading(false);
          if (res.data.result === 200) {
            this.$alertSuccess(res.data.data);
            this.windowShow = '';
            this.taskId = '';
            this.windowArr = [];
            let tempUrl = this.$route.fullPath;
            this.$router.push('/_empty');
            this.$router.replace(tempUrl);
          } else {
            errHandler(res.data)
          }
        }).catch(err => {
          this.isPending = false;
          this.setLoading(false);
          console.log(err);
          this.$alertDanger('连接超时，请刷新重试');
        })
      }
    }
  }
</script>

<style scoped>
  .edit-panel {
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

  .edit-panel-container {
    background: #ffffff;
    min-height: 220px;
    max-width: 600px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
</style>
