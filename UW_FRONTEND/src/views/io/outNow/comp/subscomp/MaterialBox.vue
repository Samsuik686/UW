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
      id:Number,
      materials:Array
    },
    mounted(){
      this.showPosition();
    },
    watch:{
      materials:function(){
        this.showPosition();
      }
    },
    methods:{
      showPosition:function(){
        let myCanvas = document.getElementById(this.id);
          if(myCanvas === null){
              return;
          }
        let ctx = myCanvas.getContext('2d');
        myCanvas.height = this.height * 20;
        for (let i=0;i<2;i++){
          for (let j=0;j<20;j++){
            ctx.strokeStyle = '#bababa';
            ctx.strokeRect(i*this.width,j*this.height,this.width,this.height);
          }
        }
        if(this.materials === null){
            return;
        }
        for(let  i = 0;i<this.materials.length;i++){
          let obj = this.materials[i];
          if(obj.row !== -1 || obj.col !== -1){
            ctx.fillStyle ='orange';
            ctx.fillRect(obj.col*this.width,obj.row*this.height,this.width,this.height);
            ctx.font = "18px serif";
            ctx.strokeStyle = '#333';
            let num = 0;
            if(obj.col === 0){
              num = obj.row + 1;
            }else{
              num = 21 + obj.row;
            }
            ctx.strokeText(String(num),obj.col*this.width + this.width/2.5,obj.row*this.height + this.height/1.1);
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
