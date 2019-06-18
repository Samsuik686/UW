<template>
  <div class="options-area">
    <div class="form-row">
      <div class="row no-gutters pl-3 pr-3">
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/preview' || $route.path === '/io/call'">
          <label for="type-list">出入库类型:</label>
          <select id="type-list" v-model="windowType" class="custom-select" @change="setPreset">
            <option value="1">入库</option>
            <option value="2" v-if="$route.path === '/io/preview'">出库</option>
            <option value="2" v-if="$route.path === '/io/call'">截料入库</option>
            <option value="3">调拨入库</option>
          </select>
        </div>
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/outnow'">
          <label for="out">出入库类型:</label>
          <input type="text" class="form-control" id="out" disabled placeholder="出库">
        </div>
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/innow'">
          <label for="in">出入库类型:</label>
          <input type="text" class="form-control" id="in" disabled placeholder="入库">
        </div>
        <div class="form-group col pr-3 pl-1" v-if="$route.path === '/io/return'">
          <label for="return">出入库类型:</label>
          <input type="text" class="form-control" id="return" disabled placeholder="调拨入库">
        </div>
        <div class="form-group col pr-3 pl-1">
          <label for="window-list">选择仓口:</label>
          <select v-model="thisWindow" id="window-list" class="custom-select"
                  @change="setWindow" :disabled="windowsList.length === 0">
            <option value="">{{windowsList.length === 0 ? '无非空闲仓口' : '请选择'}}</option>
            <option v-for="item in windowsList" :value="item.id">{{item.id}}</option>
          </select>
        </div>
        <div class="form-group col pr-3 pl-1">
          <label for="robot-select">该仓口已选叉车:</label>
          <input type="text" class="form-control" id="robot-select" disabled v-model="robotsShow"
                 style="min-width:300px"/>
        </div>
        <div class="form-group row align-items-end">
          <a href="#" class="btn btn-primary ml-3 mr-4" @click="isSelectRobot = true">选择叉车</a>
        </div>
        <div class="form-group row align-items-end" v-if="isCutShow">
          <a href="#" class="btn btn-primary ml-3 mr-4" @click="initCutPanel">截料后重新入库</a>
        </div>
      </div>
    </div>

    <div v-if="isSelectRobot" id="delete-window">
      <div class="delete-panel">
        <div class="delete-panel-container form-row flex-column justify-content-between">
          <div class="form-row">
            <div class="form-group mb-0">
              <h3>选择叉车：</h3>
            </div>
          </div>
          <div class="form-row w-100">
            <div>
              <span v-for="(item,index) in robots" :key="index" class="span-robot">
                <input type="checkbox" id="robot" :value="item.id" v-model="checkBoxRobots">
                <label for="robot">{{item.id}}</label>
              </span>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div class="form-row justify-content-around">
            <a class="btn btn-secondary col mr-1 text-white" @click="isSelectRobot = false">取消</a>
            <a class="btn btn-danger col ml-1 text-white" @click="setWindowRobots">确定</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import {mapGetters, mapActions} from 'vuex';
  import {getWindowRobotsUrl, robotSelectUrl, setWindowRobotsUrl, taskWindowsUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import eventBus from "../../../../utils/eventBus";
  import {errHandler} from "../../../../utils/errorHandler";

  export default {
    name: "Options",
    components: {},
    data() {
      return {
        windowsList: [],
        thisWindow: '',
        windowType: '',
        isForceFinish: '',
        isCutShow: false,
        robots: [],
        selectRobots: [],
        checkBoxRobots: [],
        isPending: false,
        isSelectRobot: false,
        robotsShow: ''
      }
    },
    mounted: function () {
      this.getRobots();
      eventBus.$on('getIsForceFinish', (isForceFinish) => {
        this.isForceFinish = isForceFinish
      });
    },
    created() {
      /*组件创建时加载仓口数据*/
      switch (this.$route.path) {
        case '/io/innow':
          this.windowType = 1;
          break;
        case '/io/outnow':
          this.isCutShow = true;
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
      },
      currentWindowId: function (val) {
        if (val !== '') {
          this.getWindowRobots();
        } else {
          this.selectRobots = [];
          this.checkBoxRobots = [];
          this.isSelectRobot = false;
          this.robotsShow = '';
        }
      },
      isSelectRobot:function(val){
        if(val === true){
          this.getWindowRobots();
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
      initCutPanel: function () {
        if (this.isForceFinish === true) {
          eventBus.$emit('initCutPanel', true);
        } else if (this.isForceFinish === false) {
          this.$alertWarning("叉车第一次将托盘运到仓口，不是截料后重新入库，不能进入截料后返库界面");
        } else {
          this.$alertWarning("叉车未到站，当前无拣料数据");
        }
      },
      getRobots: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: robotSelectUrl,
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if (res.data.result === 200) {
              this.robots = res.data.data;
              if (this.currentWindowId !== '') {
                this.getWindowRobots();
              }
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.isPending = false;
            this.$alertDanger('连接超时，请刷新重试');
          })
        }
      },
      //仓口设置叉车
      setWindowRobots: function () {
        if (this.currentWindowId === '') {
          this.$alertWarning('请先选择仓口');
          return;
        }
        let robots = '';
        this.checkBoxRobots.map((item, index) => {
          if (index === 0) {
            robots = robots + item;
          } else {
            robots = robots + ',' + item;
          }
        });
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: setWindowRobotsUrl,
            data: {
              windowId: this.currentWindowId,
              robots: robots
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if (res.data.result === 200) {
              this.$alertSuccess(res.data.data);
              this.isSelectRobot = false;
              this.getWindowRobots();
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.isPending = false;
          })
        }
      },
      //查询当前仓口叉车
      getWindowRobots: function () {
        if (this.currentWindowId === '') {
          this.$alertWarning('当前无空闲仓口');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: getWindowRobotsUrl,
            data: {
              windowId: this.currentWindowId,
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if (res.data.result === 200) {
              this.robotsShow = res.data.data;
              if(this.robotsShow !== undefined && this.robotsShow !== null){
                this.checkBoxRobots = this.robotsShow.split(',');
                this.selectRobots = this.checkBoxRobots;
              }else{
                this.checkBoxRobots = [];
                this.selectRobots = [];
              }
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.isPending = false;
          })
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

  .delete-panel {
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

  .delete-panel-container {
    background: #ffffff;
    min-height: 220px;
    width: 500px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }

  .span-robot {
    font-size: 18px;
    margin-right: 30px;
  }

  .span-robot input {
    width: 20px;
    height: 20px;
    background: #fff;
  }
</style>
