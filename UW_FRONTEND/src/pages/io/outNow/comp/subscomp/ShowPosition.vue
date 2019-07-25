<template>
  <div class="delete-panel">
    <div class="delete-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>提示：</h3>
        </div>
      </div>
      <div class="form-row w-100">
        <div class="text-center">
          <p>请把料盘放置到图示位置</p>
          <canvas id="myCanvas" :width="width*2" :height="height*20" class="myCanvas"></canvas>
        </div>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <a class="btn btn-primary col mr-1 text-white" @click="closeShowPosition">确认</a>
      </div>
    </div>
  </div>
</template>

<script>
    export default {
        name: "ShowPosition",
        data(){
          return{
            width:60,
            height:15
          }
        },
        props:{
          x:Number,
          y:Number
        },
        mounted(){
          this.showPosition(this.x,this.y);
        },
        methods:{
          showPosition:function(x,y){
            let myCanvas = document.getElementById('myCanvas');
            let ctx = myCanvas.getContext('2d');
            myCanvas.height = this.height * 20;
            for (let i=0;i<2;i++){
              for (let j=0;j<20;j++){
                ctx.strokeStyle = '#bababa';
                if(i === x && y === j){
                  ctx.fillStyle ='orange';
                  ctx.fillRect(i*this.width,j*this.height,this.width,this.height);
                  ctx.font = "16px serif";
                  ctx.strokeStyle = '#333';
                  ctx.strokeText(String(j + 1),i*this.width + this.width/2.5,j*this.height + this.height/1.1);
                }else{
                  ctx.strokeRect(i*this.width,j*this.height,this.width,this.height);
                }
              }
            }
          },
          closeShowPosition:function(){
            this.$emit('closeShowPosition',true);
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
    width: 400px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
  .myCanvas{
    border:3px solid #dddddd;
  }
</style>
