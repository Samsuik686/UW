<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>添加发料目的地：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col">
          <label for="supplier-name" class="col-form-label">发料目的地:</label>
          <input type="text" id="supplier-name" class="form-control" v-model="thisData.name" autocomplete="off">
        </div>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <button class="btn btn-secondary col mr-1 text-white" @click="closeAddPanel">取消</button>
        <button class="btn btn-primary col ml-1 text-white" @click="submitAdding">提交</button>
      </div>
    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus';
  import {destinationAddUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "AddDestination",
    data() {
      return {
        thisData: {
          name: ''
        },
        isPending: false
      }
    },
    methods: {
      closeAddPanel: function () {
        eventBus.$emit('closeAddPanel');
      },
      submitAdding: function () {
        if (!this.isPending) {
          for (let item in this.thisData) {
            if (this.thisData[item] === '') {
              this.$alertWarning('内容不能为空');
              return;
            }
          }
          this.isPending = true;
          let options = {
            url: destinationAddUrl,
            data: this.thisData
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('添加成功');
              this.closeAddPanel();
              let tempUrl = this.$route.fullPath;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else if (response.data.result === 412) {
              this.$alertWarning('请勿添加重复发料目的地')
            } else {
              this.isPending = false;
              errHandler(response.data);
            }
          })
        }
      },
    }
  }
</script>

<style scoped>
  .add-panel {
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

  .add-panel-container {
    background: #ffffff;
    min-height: 220px;
    max-width: 600px;
    z-index: 1002;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
</style>

