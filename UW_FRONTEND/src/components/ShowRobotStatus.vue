<template>
    <div class="show-robot" v-if="isShow">
      <el-card class="box-card">
        <div slot="header" class="clearfix">
          <span style="font-size:18px;font-weight:bold;color:#444">叉车异常</span>
          <span style="float: right; padding: 3px 0;cursor:pointer;font-weight:bold" @click="isTip = true">
            <i class="el-icon-close" style="font-size:22px;"></i>
          </span>
        </div>
        <div v-for="item in robotStatus" :key="item.id" class="text item">
          {{'叉车 ' + item.id + ' : ' + item.str}}
        </div>
      </el-card>
      <div v-if="isTip" id="delete-window">
        <div class="delete-panel">
          <div class="delete-panel-container form-row flex-column justify-content-between">
            <div class="form-row">
              <div class="form-group mb-0">
                <h3>提示：</h3>
              </div>
            </div>
            <div class="form-row w-100">
              <div class="text-center">
                <p>请尽快解决叉车问题！</p>
              </div>
            </div>
            <div class="dropdown-divider"></div>
            <div class="form-row justify-content-around">
              <a class="btn btn-secondary col mr-1 text-white" @click="isTip = false">取消</a>
              <a class="btn btn-danger col ml-1 text-white" @click="setOperation">确定</a>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script>
    import {robotSelectUrl} from "../config/globalUrl";
    import {axiosPost} from "../utils/fetchData";
    import {errHandler} from "../utils/errorHandler";

    export default {
        name: "ShowRobotStatus",
        data(){
          return{
            isPending:false,
            isShow:false,
            robotStatus:[],
            myTimeOut: '',
            isTimeOut: false,
            isTip:false,
            isOperation:false
          }
        },
        beforeDestroy(){
          this.clearMyTimeOut();
        },
        mounted(){
          this.getRobots();
          this.setMyTimeOut();
        },
        methods:{
          getRobots:function(){
            if(!this.isPending){
              this.isPending = true;
              let options = {
                url: robotSelectUrl,
              };
              axiosPost(options).then(res => {
                this.isPending = false;
                if(res.data.result === 200){
                  let robotStatus = [];
                  let data = res.data.data;
                  data.map((item) => {
                    let str = '';
                    if(item.errorString !== ''){
                      str = item.errorString;
                    }else if(item.loadExceptionString !== ''){
                      str = item.loadExceptionString;
                    }else if(item.warnString !== ''){
                      str = item.warnString;
                    }else{
                      str = '';
                    }
                    if(str !== ''){
                      robotStatus.push({
                        id:item.id,
                        str:str
                      })
                    }
                  });
                  if(robotStatus.length > 0){
                    if(this.isOperation === false){
                      this.robotStatus = robotStatus;
                      this.isShow = true;
                    }else{
                      this.robotStatus = [];
                      this.isShow = false;
                    }
                  }else{
                    this.robotStatus = [];
                    this.isShow = false;
                    this.isOperation = false;
                  }
                }else{
                  errHandler(res.data);
                }
              }).catch(err => {
                console.log(err);
                this.$alertDanger('连接超时，请刷新重试');
                this.isPending = false;
              })
            }
          },
          setMyTimeOut: function () {
            if (this.isTimeOut === true) {
              this.clearMyTimeOut();
            }
            this.isTimeOut = true;
            let that = this;
            this.myTimeOut = setInterval(function () {
              that.getRobots();
            }, 5000);
          },
          clearMyTimeOut: function () {
            this.isTimeOut = false;
            clearTimeout(this.myTimeOut);
            this.myTimeOut = null;
          },
          setOperation:function(){
            this.isTip = false;
            this.isShow = false;
            this.isOperation = true;
          }
       }
    }
</script>

<style scoped>
  .show-robot{
    position:absolute;
    top:10px;
    right:10px;
    z-index:9999;
  }
  .text {
    font-size:16px;
  }

  .item {
    margin-bottom: 18px;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }
  .clearfix:after {
    clear: both
  }

  .box-card {
    width:320px;
    background: #ffd333;
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
    width: 400px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
</style>
