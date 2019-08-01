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
      col:Number,
      row:Number,
      id:Number,
      list:Array
    },
    mounted(){
      if(this.col !== '' && this.row !== ''){
        this.showPosition(this.col,this.row);
      }
    },
    watch:{
      col:function(val){
        if(val !== ''){
            this.showPosition(this.col,this.row);
        }
      },
      row:function(val){
        if(val !== ''){
            this.showPosition(this.col,this.row);
        }
      },
      list:function () {
          this.showPosition(this.col,this.row);
      }
    },
    methods:{
      showPosition:function(col,row){
        let myCanvas = document.getElementById(this.id);
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
          if((obj.row !== -1 || obj.col !== -1) && (this.id === obj.boxId)){
            if(obj.row === row && obj.col === col){
              ctx.fillStyle ='red';
            }else{
              ctx.fillStyle ='orange';
            }
            ctx.fillRect(obj.col*this.width,obj.row*this.height,this.width,this.height);
            ctx.font = "18px serif";
            ctx.strokeStyle = '#333';
            let num = 0;
            if(obj.col === 0){
              num = obj.row + 1;
            }else{
              num = 40 - obj.row;
            }
            ctx.strokeText(String(num),obj.col*this.width + this.width/2.5,obj.row*this.height + this.height/1.2);
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

