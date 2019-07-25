<template>
  <span :style="itemStyle">{{row.materialId}}</span>
</template>

<script>
  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "HighLight",
    props: {
      spotCheckItem:Object,
      row:Object,
      activeMaterialId:String,
      isScan:Boolean
    },
    data () {
      return {
        itemStyle: {
          color:''
        }
      }
    },
    mounted () {
      this.changeStyle(this.row)
    },
    watch: {
      row: function (val) {
        this.itemStyle.color = '';
        this.changeStyle(this.row);
      },
      isScan:function(val){
        if(val === true){
          if(this.activeMaterialId === this.row.materialId){
            let that = this;
            setTimeout(function(){
              that.textToSpeak('已扫'+that.spotCheckItem.scanNum+'盘还剩'+(that.spotCheckItem.totalNum - that.spotCheckItem.scanNum)+'盘');
            },500);
            eventBus.$emit('setIsScan',false);
          }
        }
      }
    },
    methods: {
      changeStyle: function (row) {
        if(row.isOuted){
          this.itemStyle.color = "#909399"
          return;
        }
        if(row.isScaned){
          if(row.materialId === this.activeMaterialId){
            this.itemStyle.color = "#F56C6C"
          }else{
            this.itemStyle.color = "#e6941b"
          }
        }
      },
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

</style>
