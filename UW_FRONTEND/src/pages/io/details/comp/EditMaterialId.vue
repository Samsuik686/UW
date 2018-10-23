<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>修改料盘数量：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col-6 pl-2 pr-2">
          <label for="materialId" class="col-form-label">料盘:</label>
          <input type="text" id="materialId" class="form-control" v-model="thisData.materialId" autocomplete="off"
                 disabled>
          <span class="form-span col"></span>
        </div>
        <div class="form-row col-6 pl-2 pr-2">
          <label for="quantity" class="col-form-label">数量:</label>
          <input type="text" id="quantity" class="form-control" v-model="thisData.quantity" autocomplete="off">
          <span class="form-span col"></span>
        </div>

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
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {taskUpdateOutQuantityUrl} from "../../../../config/globalUrl";

  export default {
    name: "EditMaterialId",
    props: ['editData'],
    data() {
      return {
        thisData: {
          packListItemId: "",
          materialId: '',
          quantity:0,
          initQuantity:0
        },
        isPending: false
      }
    },
    mounted() {
      eventBus.$on('replaceFocus', (type) => {
        if (type === true) {
          document.getElementById("quantity").focus();
        }
      });
      this.thisData.quantity = this.editData.quantity;
      this.thisData.materialId = this.editData.materialId;
      this.thisData.packListItemId = this.editData.packListItemId;
      this.thisData.initQuantity = this.editData.initQuantity;
    },
    methods: {
      closeEditPanel: function () {
        eventBus.$emit('closeEditPanel');
      },
      submitUpdate: function () {
        for (let item in this.thisData) {
          if (this.thisData[item] === '') {
            this.$alertWarning('内容不能为空');
            return;
          }
        }
        let reg = /^([1-9]\d*|[0])$/;
        if (!reg.test(this.thisData.quantity)) {
          this.$alertWarning('格式不对');
          return;
        }
        if(this.thisData.quantity> this.thisData.initQuantity){
          this.$alertWarning('修改的数量不能大于该料盘剩余数量');
          return;
        }
        this.thisData.quantity = Number(this.thisData.quantity);
        this.$emit("getEditData",this.thisData);
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

  .form-span {
    display: block;
    height: 20px;
    line-height: 20px;
    font-size: 10px;
    color: darkred;
  }
</style>
