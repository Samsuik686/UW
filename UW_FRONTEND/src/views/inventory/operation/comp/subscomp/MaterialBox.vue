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
            col:Number,
            row:Number,
            id:Number
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
            }
        },
        methods:{
            showPosition:function(col,row){
                let myCanvas = document.getElementById(this.id);
                if(myCanvas === null){
                    return;
                }
                let ctx = myCanvas.getContext('2d');
                myCanvas.height = this.height * 20;
                for (let i=0;i<2;i++){
                    for (let j=0;j<20;j++){
                        ctx.strokeStyle = '#bababa';
                        if(i === col && row === j){
                            ctx.fillStyle ='#eebcac';
                            ctx.fillRect(i*this.width,j*this.height,this.width,this.height);
                            ctx.font = "18px serif";
                            ctx.strokeStyle = '#888';
                            let num = 0;
                            if(col === 0){
                                num = row + 1;
                            }else{
                                num = 21 + row;
                            }
                            ctx.strokeText(String(num),i*this.width + this.width/2.5,j*this.height + this.height/1.2);
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
        position: fixed;
        top:50%;
        right:90px;
        transform: translateY(-50%);
        border:3px solid #dddddd;
    }
</style>
