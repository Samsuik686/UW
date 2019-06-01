<template>
  <div id="delete-window">
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
          <a class="btn btn-secondary col mr-1 text-white" @click="closeSelectRobotsPanel">取消</a>
          <a class="btn btn-danger col ml-1 text-white" @click="setRobots">确定</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {
    getInventoryWindowRobotsUrl,
    robotSelectUrl,
    setInventoryTaskRobotsUrl
  } from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "SelectRobots",
    props: {
      editData: Object
    },
    data() {
      return {
        robots: [],
        selectRobots: [],
        checkBoxRobots: [],
        isPending:false
      }
    },
    mounted(){
      this.getRobots();
    },
    methods:{
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
              this.getTaskRobots();
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
      closeSelectRobotsPanel:function(){
        eventBus.$emit('closeSelectRobotsPanel');
      },
      setRobots:function(){
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
            url: setInventoryTaskRobotsUrl,
            data: {
              taskId:this.editData.taskId,
              robots: robots
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if (res.data.result === 200) {
              this.$alertSuccess('设置成功');
              this.getTaskRobots();
              this.closeSelectRobotsPanel();
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.isPending = false;
          })
        }
      },
      getTaskRobots:function(){
        this.checkBoxRobots = [];
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: getInventoryWindowRobotsUrl,
            data: {
              taskId:this.editData.taskId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if (res.data.result === 200) {
              this.selectRobots = res.data.data;
              this.checkBoxRobots = [];
              this.selectRobots.map((item) => {
                this.checkBoxRobots.push(item.id);
              });
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
