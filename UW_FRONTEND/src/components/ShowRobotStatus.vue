<template>
    <div class="show-robot">

    </div>
</template>

<script>
    import {robotSelectUrl} from "../config/globalUrl";
    import {axiosPost} from "../utils/fetchData";

    export default {
        name: "ShowRobotStatus",
        data(){
          return{
            isPending:false
          }
        },
        methods:{
          getRobots:function(){
            if(!this.isPending){
              this.isPending = true;
              let options = {
                url: robotSelectUrl,
              };
              axiosPost(options).then(res => {

              }).catch(err => {
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
