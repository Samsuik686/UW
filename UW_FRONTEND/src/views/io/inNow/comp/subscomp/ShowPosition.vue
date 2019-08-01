<template>
  <el-dialog
          title="提示"
          :show-close="isCloseOnModal"
          :close-on-click-modal="isCloseOnModal"
          :close-on-press-escape="isCloseOnModal"
          :visible.sync="isShowPosition"
          width="30%">
            <p>请把料盘从图示位置取出</p>
            <canvas id="myCanvas" :width="width*2" :height="height*20" class="myCanvas"></canvas>
        <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="cancel" size="small">确定</el-button>
        </span>
  </el-dialog>
</template>

<script>
    export default {
        name: "ShowPosition",
        data(){
          return{
              isCloseOnModal:false,
              width:60,
              height:15,
              myCanvas:''
          }
        },
        props:{
            col:Number,
            row:Number,
            isShowPosition:Boolean
        },
        watch:{
            isShowPosition:function (val) {
                if(val === true){
                    this.$nextTick(function(){
                        this.showPosition(this.col,this.row);
                    });
                }
            }
        },
        methods:{
            showPosition:function(col,row){
                let myCanvas = document.getElementById('myCanvas');
                if(myCanvas === null){
                    return;
                }
                let ctx = myCanvas.getContext('2d');
                myCanvas.height = this.height * 20;
                for (let i=0;i<2;i++){
                    for (let j=0;j<20;j++){
                        ctx.strokeStyle = '#bababa';
                        if(i === col && row === j){
                            ctx.fillStyle ='orange';
                            ctx.fillRect(i*this.width,j*this.height,this.width,this.height);
                            ctx.font = "16px serif";
                            ctx.strokeStyle = '#333';
                            let num = 0;
                            if(col === 0){
                                num = row + 1;
                            }else{
                                num = 21 + row;
                            }
                            ctx.strokeText(String(num),i*this.width + this.width/2.5,j*this.height + this.height/1.1);
                        }else{
                            ctx.strokeRect(i*this.width,j*this.height,this.width,this.height);
                        }
                    }
                }
            },
            cancel:function(){
                this.$emit('update:isShowPosition',false);
            }
        }
    }
</script>

<style scoped>
  .myCanvas{
    border:3px solid #dddddd;
  }
</style>
