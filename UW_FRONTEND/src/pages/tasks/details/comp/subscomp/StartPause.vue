<template>
  <div>
    <span title="启动/暂停" @click="startPause" style="cursor:pointer" v-if="row.state === 2">
      <i :class="row.status === false?'el-icon-video-play':'el-icon-video-pause'" style="font-size:22px;"></i>
    </span>
  </div>
</template>

<script>
  import {switchTaskUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "StartPause",
    data(){
      return{
        isPending:false
      }
    },
    props: {
      row: Object
    },
    methods: {
      startPause: function () {
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:switchTaskUrl,
            data:{
              taskId:this.row.id,
              flag:!this.row.status
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess('操作成功');
              eventBus.$emit('refreshTask',true);
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            this.isPending = false;
            this.$alertDanger('连接超时，请刷新重试');
            console.log(err);
          })
        }
      }
    }
  }
</script>

<style scoped>

</style>
