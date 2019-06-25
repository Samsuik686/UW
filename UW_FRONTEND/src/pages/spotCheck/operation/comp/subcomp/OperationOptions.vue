<template>
  <div class="form-group">
    <div class="btn btn-primary col-5" @click="outSingular">异常出库</div>
    <div class="btn btn-primary col-5" @click="outRegular">抽检出库</div>
  </div>
</template>

<script>
  import {outRegularSampleTaskUrl, outSingularSampleTaskUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "OperationOptions",
    props: {
      row: Object
    },
    data() {
      return {
        isPending: false
      }
    },
    methods: {
      outRegular: function () {
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:outRegularSampleTaskUrl,
            data:{
              materialId:this.row.materialId,
              groupId:this.row.groupId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess(res.data.data);
              eventBus.$emit('refreshSampleInfo',true);
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
            console.log(err);
          })
        }
      },
      outSingular: function () {
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:outSingularSampleTaskUrl,
            data:{
              materialId:this.row.materialId,
              groupId:this.row.groupId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess(res.data.data);
              eventBus.$emit('refreshSampleInfo',true);
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
            console.log(err);
          })
        }
      }
    }
  }
</script>

<style scoped>

</style>
