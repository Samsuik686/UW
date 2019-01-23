<template>
  <div class="options-area">
    <div class="form-row">
      <div class="row no-gutters pl-3 pr-3">
        <div class="form-group col pr-3 pl-1">
          <label for="window-list">选择仓口:</label>
          <select v-model="thisWindow" id="window-list" class="custom-select"
                  @change="setWindow" :disabled="windowsList.length === 0">
            <option value="">{{windowsList.length === 0 ? '无非空闲仓口' : '请选择'}}</option>
            <option v-for="item in windowsList" :value="item.id">{{item.id}}</option>
          </select>
        </div>
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/preview' || $route.path === '/io/call'">
          <label for="type-list">出入库类型:</label>
          <select id="type-list" v-model="windowType" class="custom-select" @change="setPreset">
            <option value="1">入库</option>
            <option value="2">出库</option>
            <option value="3">退料</option>
          </select>
        </div>
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/outnow'">
          <label for="out">出入库类型:</label>
          <input type="text" class="form-control" id="out" disabled  placeholder="出库">
        </div>
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/innow'">
          <label for="in">出入库类型:</label>
          <input type="text" class="form-control" id="in" disabled  placeholder="入库">
        </div>
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/return'">
          <label for="return">出入库类型:</label>
          <input type="text" class="form-control" id="return" disabled  placeholder="退料">
        </div>
        <div class="form-group row align-items-end" v-if="isShow">
          <div class="btn btn-primary ml-3 mr-4" @click="initCutPanel">截料后重新入库</div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import {mapGetters, mapActions} from 'vuex';
  import {taskWindowsUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {getLogsQuery} from "../../../../config/logsApiConfig";
  import _ from 'lodash'
  import eventBus from "../../../../utils/eventBus";

  export default {
    name: "Options",
    components: {},
    data() {
      return {
        windowsList: [],
        thisWindow: '',
        windowType: '',
        isForceFinish: '',
        isShow:false
      }
    },
    mounted: function () {
      eventBus.$on('getIsForceFinish',(isForceFinish) => {
        this.isForceFinish = isForceFinish
      })
    },
    created() {
      /*组件创建时加载仓口数据*/
      switch (this.$route.path) {
        case '/io/innow':
          this.windowType = 1;
          break;
        case '/io/outnow':
          this.isShow = true;
          this.windowType = 2;
          break;
        case '/io/return':
          this.windowType = 3;
          break;
        case '/io/call':
        case '/io/preview':
          if (this.currentOprType === '1') {
            this.windowType = 1
          } else if (this.currentOprType === '2') {
            this.windowType = 2
          } else if (this.currentOprType === '3') {
            this.windowType = 3
          } else {
            this.windowType = 1;
          }
          break;
      }
      this.setPreset();

    },
    computed: {
      ...mapGetters([
        'currentWindowId',
        'currentOprType'
      ]),
    },
    watch: {
      $route: function (route) {
        switch (route.path) {
          case '/io/innow':
            this.windowType = 1;
            this.setPreset();
            break;
          case '/io/outnow':
            this.isShow = true;
            this.windowType = 2;
            this.setPreset();
            break;
          case '/io/return':
            this.windowType = 3;
            this.setPreset();
            break;
          case '/io/call':
          case '/io/preview':
            if (this.currentOprType === '1') {
              this.windowType = 1
            } else if (this.currentOprType === '2') {
              this.windowType = 2
            } else if (this.currentOprType === '3') {
              this.windowType = 3
            } else {
              this.windowType = 1;
            }
            this.setPreset();
            break;
        }
      }
    },
    methods: {
      ...mapActions(['setLoading', 'setCurrentWindow']),

      setPreset: function () {
        let options = {
          url: taskWindowsUrl,
          data: {
            type: this.windowType
          }
        };
        axiosPost(options).then(response => {
          if (response.data.result === 200) {
            this.windowsList = response.data.data;

            if (this.windowsList.length > 0) {
              /*如果有缓存仓口id的话给select标签赋值*/
              if (this.currentWindowId !== "") {
                if (this.windowsList.indexOf(this.currentWindowId) !== -1) {
                  this.thisWindow = this.currentWindowId
                } else {
                  this.setCurrentWindow(this.windowsList[0].id);
                  this.thisWindow = this.windowsList[0].id
                }
              } else {
                this.setCurrentWindow(this.windowsList[0].id);
                this.thisWindow = this.windowsList[0].id
              }
            } else {
              this.thisWindow = '';
              this.setCurrentWindow('');
            }

          }
        });
      },

      /*设置仓口*/
      setWindow: function () {
        this.setCurrentWindow(this.thisWindow);
      },
      routerReload: function () {
        let tempPath = this.$route.path;
        this.$router.push('_empty');
        this.$router.push(tempPath)
      },
      initCutPanel:function(){
        if(this.isForceFinish === true){
          eventBus.$emit('initCutPanel',true);
        }else if(this.isForceFinish === false){
          this.$alertWarning("叉车第一次将托盘运到仓口，不是截料后重新入库，不能进入截料后返库界面");
        }else{
          this.$alertWarning("叉车未到站，当前无拣料数据");
        }
      }
    }
  }
</script>

<style scoped>
  .options-area {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
  }
</style>
