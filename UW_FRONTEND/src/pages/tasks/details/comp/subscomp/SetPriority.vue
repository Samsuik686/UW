<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>设置优先级：</h3>
        </div>
      </div>
      <div class="form-row">
        <label for="examine-select" class="col-form-label">优先级:</label>
        <select id="examine-select" class="custom-select"
                v-model.number="thisPriority">
          <option value="" disabled>请选择</option>
          <option value=1>1</option>
          <option value=2>2</option>
          <option value=3>3</option>
        </select>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <button class="btn btn-secondary col mr-1 text-white" @click="closeEditPanel">取消</button>
        <button class="btn btn-primary col ml-1 text-white" @click="setPriority">提交</button>
      </div>
    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus';
  import {taskSetPriorityUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "SetPriority",
    props: ['editData'],
    data() {
      return {
        thisData: {},
        isPending: false,
        thisPriority:"",
        originPriority:""
      }
    },
    mounted() {
      this.thisData.id = this.editData.id;
    },
    methods: {
      closeEditPanel: function () {
        eventBus.$emit('closeTaskPriorityPanel');
      },
      setPriority:function () {
        if(this.thisPriority === ""){
          this.$alertWarning("请选择优先级");
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
            let options = {
              url:taskSetPriorityUrl ,
              data: {
                id: this.thisData.id,
                priority:this.thisPriority
              }
            };
            axiosPost(options).then(res => {
              this.isPending = false;
              if (res.data.result === 200) {
                this.$alertSuccess('设置成功');
                this.closeEditPanel();
              } else {
                errHandler(res.data.result)
              }
            }).catch(err => {
              if (JSON.stringify(err) !== '{}'){
                this.$alertDanger(JSON.stringify(err))
              }
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
    z-index: 101;
  }

  .edit-panel-container {
    background: #ffffff;
    min-height: 220px;
    max-width: 600px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
</style>
