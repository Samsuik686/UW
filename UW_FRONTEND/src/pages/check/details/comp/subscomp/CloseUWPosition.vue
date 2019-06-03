<template>
  <div v-if="row.isChecked && row.coverTime === null && row.isFinished === false">
    <div class="btn btn-primary" @click="closePosition">平仓</div>
  </div>
</template>

<script>
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import {coverEWhMaterialUrl, coverMaterialUrl} from "../../../../../config/globalUrl";
  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "ClosePosition",
    props:{
      row:Object
    },
    data(){
      return{
        isPending:false
      }
    },
    methods:{
      closePosition:function(){
        if(!this.isPending){
          this.isPending = false;
          let options = {
            url:coverMaterialUrl,
            data:{
              id:this.row.id,
              taskId:this.row.taskId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess('平仓成功');
              eventBus.$emit('checkDetailsRefresh',true);
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
          })
        }
      }
    }
  }
</script>

<style scoped>
</style>
