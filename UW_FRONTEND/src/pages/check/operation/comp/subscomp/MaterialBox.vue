<template>
    <canvas :id="id" :width="width*2" :height="height*20" class="myCanvas"></canvas>
</template>

<script>
    export default {
        name: "materialBox",
        data(){
          return{
            width:70,
            height:18
          }
        },
        props:{
          x:Number,
          y:Number,
          id:Number
        },
        mounted(){
          if(this.x !== '' && this.y !== ''){
            this.showPosition(this.x,this.y);
          }
        },
        watch:{
          x:function(val){
            if(val !== ''){
              this.showPosition(this.x,this.y);
            }
          },
          y:function(val){
          if(val !== ''){
            this.showPosition(this.x,this.y);
          }
        }
        },
        methods:{
          showPosition:function(x,y){
            let myCanvas = document.getElementById(this.id);
            myCanvas.style.left = 60 + (250 - this.width * 2)/2 + 'px';
            let ctx = myCanvas.getContext('2d');
            myCanvas.height = this.height * 20;
            for (let i=0;i<2;i++){
              for (let j=0;j<20;j++){
                ctx.strokeStyle = '#bababa';
                if(i === x && y === j){
                  ctx.fillStyle ='orange';
                  ctx.fillRect(i*this.width,j*this.height,this.width,this.height);
                  ctx.font = "18px serif";
                  ctx.strokeStyle = '#333';
                  ctx.strokeText(String(j + 1),i*this.width + this.width/2.5,j*this.height + this.height/1.2);
                }else{
                  ctx.strokeRect(i*this.width,j*this.height,this.width,this.height);
                }
              }
            }
          }
        }
    }
</script>

<style scoped>
.myCanvas{
  position:fixed;
  top:200px;
  border:5px solid #dddddd;
}
</style>
