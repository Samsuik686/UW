<template>
  <canvas :id="id" :width="width*2" :height="height*20" class="myCanvas"></canvas>
</template>

<script>
  export default {
    name: "materialBox",
    data(){
      return{
        width:60,
        height:15
      }
    },
    props:{
      x:Number,
      y:Number,
      id:Number,
      list:Array
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
      },
      list:function () {
        this.showPosition(this.x,this.y);
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
            ctx.strokeRect(i*this.width,j*this.height,this.width,this.height);
          }
        }
        for(let  i = 0;i<this.list.length;i++){
          let obj = this.list[i];
          if(obj.row !== -1 || obj.col !== -1){
            if(obj.row === y && obj.col === x){
              ctx.fillStyle ='red';
            }else{
              ctx.fillStyle ='orange';
            }
            ctx.fillRect(obj.col*this.width,obj.row*this.height,this.width,this.height);
            ctx.font = "18px serif";
            ctx.strokeStyle = '#333';
            ctx.strokeText(String(obj.row + 1),obj.col*this.width + this.width/2.5,obj.row*this.height + this.height/1.2);
          }
        }
      }
    }
  }
</script>

<style scoped>
  .myCanvas{
    border:3px solid #dddddd;
  }
</style>
