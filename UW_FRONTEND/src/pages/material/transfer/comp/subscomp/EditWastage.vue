<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>填写损耗数量：</h3>
        </div>
      </div>

      <div class="form-row col pl-2 pr-2">
        <label for="quantity" class="col-form-label">数量:</label>
        <input type="text" id="quantity" class="form-control" v-model="quantity" autocomplete="off">
      </div>
      <div class="form-row col pl-2 pr-2">
        <label for="remarks" class="col-form-label">备注:</label>
        <textarea id="remarks" class="form-control" v-model.trim="remarks" autocomplete="off"></textarea>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <button class="btn btn-secondary col mr-1 text-white" @click="closeEditPanel">取消</button>
        <button class="btn btn-primary col ml-1 text-white" @click="submitUpdate">提交</button>
      </div>
    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus';
  import {axiosPost} from "../../../../../utils/fetchData";
  import {externalWhAddWorstageLogUrl} from "../../../../../config/globalUrl";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "EditWastage",
    props: {
      row: Object
    },
    data() {
      return {
        quantity: '',
        remarks: '',
        isPending: false
      }
    },
    methods: {
      closeEditPanel: function () {
        eventBus.$emit('closeEditPanel');
      },
      submitUpdate: function () {
        if (this.quantity === '' || this.remarks === '') {
          this.$alertWarning('内容不能为空');
          return;
        }
        let reg = /^([1-9]\d*|[0])$/;
        if (!reg.test(this.quantity)) {
          this.$alertWarning('格式不对');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: externalWhAddWorstageLogUrl,
            data: {
              materialTypeId: this.row.materialTypeId,
              whId: this.row.whId,
              quantity: this.quantity,
              remarks: this.remarks
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('更新成功');
              this.closeEditPanel();
              let tempUrl = this.$route.fullPath;
              console.log(tempUrl);
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else if (response.data.result >= 412 && response.data.result < 500) {
              this.$alertWarning(response.data.data);
            } else {
              errHandler(response.data);
            }
            this.isPending = false;
          }).catch(err => {
            if (JSON.stringify(err)) {
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试')
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
    min-height:330px;
    width:500px;
    max-width: 600px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
</style>
