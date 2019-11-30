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
        props:{
            col:Number,
            row:Number,
            id:Number,
            list:Array
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
                        ctx.strokeRect(i*this.width,j*this.height,this.width,this.height);
                    }
                }
                for(let  i = 0;i<this.list.length;i++){
                    let obj = this.list[i];
                    if((obj.row !== -1 || obj.col !== -1) && obj.isScaned === false){
                        if(obj.row === row && obj.col === col){
                            ctx.fillStyle ='#4BC0C0';
                        }else{
                            ctx.fillStyle = '#eebcac';
                        }
                        ctx.fillRect(obj.col*this.width,obj.row*this.height,this.width,this.height);
                        ctx.font = "18px serif";
                        ctx.strokeStyle = '#888';
                        let num = 0;
                        if(obj.col === 0){
                            num = obj.row + 1;
                        }else{
                            num = 21 + obj.row;
                        }
                        ctx.strokeText(String(num),obj.col*this.width + this.width/2.5,obj.row*this.height + this.height/1.2);
                    }
                }
            },
            //文字转语音提示
            textToSpeak:function(text){
                let synth = window.speechSynthesis;
                let utterThis = new SpeechSynthesisUtterance(text);
                utterThis.volume = 1;
                utterThis.pitch = 2;
                synth.speak(utterThis);
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