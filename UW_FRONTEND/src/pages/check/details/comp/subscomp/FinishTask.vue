<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>提示：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row">
          <span>请点击“完成任务”按钮完成盘点任务</span>
        </div>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <el-button  type="info"  class="col" @click="closeEditPanel">取消</el-button>
        <el-button  type="primary" class="col" @click="submitUpdate">完成任务</el-button>
      </div>
    </div>
  </div>
</template>

<script>
    import eventBus from "../../../../../utils/eventBus";
    import {finishInventoryTaskUrl} from "../../../../../config/globalUrl";
    import {axiosPost} from "../../../../../utils/fetchData";
    import {errHandler} from "../../../../../utils/errorHandler";
    export default {
        name: "FinishTask",
        props:{
          taskId:Number
        },
        data(){
          return{
            isPending:false
          }
        },
      methods:{
        closeEditPanel:function(){
          eventBus.$emit('closeFinishTask',true);
        },
        submitUpdate:function () {
          if(!this.isPending){
            this.isPending = true;
            let options = {
              url: finishInventoryTaskUrl,
              data: {
                taskId: this.taskId
              }
            };
            axiosPost(options).then(res => {
              this.isPending = false;
              if(res.data.result === 200){
                this.$alertSuccess('操作成功');
              }else{
                errHandler(res.data);
              }
              this.closeEditPanel();
            }).catch(err => {
              console.log(err);
              this.isPending = false;
              this.$alertDanger('连接超时，请刷新重试');
            })
          }
        }
      }
    }
</script>

<style scoped>
  .edit-panel {
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 1001;
  }

  .edit-panel-container {
    background: #ffffff;
    min-height: 220px;
    max-width: 600px;
    z-index: 1002;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }

</style>
