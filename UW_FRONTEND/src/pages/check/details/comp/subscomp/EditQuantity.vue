<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>修改数量：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col-6 pl-2 pr-2">
          <label for="atrualNum" class="col-form-label">盘点数量:</label>
          <input type="text" id="atrualNum" class="form-control" v-model="thisData.atrualNum" autocomplete="off">
        </div>
        <div class="form-row col-6 pl-2 pr-2">
          <label for="returnNum" class="col-form-label">盘前盈亏:</label>
          <input type="text" id="returnNum" class="form-control" v-model="thisData.returnNum"
                 autocomplete="off">
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
    import eventBus from "../../../../../utils/eventBus";
    import {editEwhInventoryLogUrl} from "../../../../../config/globalUrl";
    import {axiosPost} from "../../../../../utils/fetchData";
    import {errHandler} from "../../../../../utils/errorHandler";

    export default {
        name: "EditQuantity",
        props:{
          editData:Object
        },
        mounted(){
          console.log(this.editData);
          this.thisData.atrualNum = this.editData.atrualNum;
          this.thisData.returnNum = this.editData.materialreturnNum;
        },
        data(){
          return{
            thisData:{
              atrualNum:'',
              returnNum:''
            }
          }
        },
      methods:{
        closeEditPanel:function(){
          eventBus.$emit('closeEditQuantityPanel',true);
        },
        submitUpdate: function () {
          if (!this.isPending) {
            for (let item in this.thisData) {
              if (this.thisData[item] === '') {
                this.$alertWarning('内容不能为空');
                return;
              }
            }
            for (let item in this.thisData) {
              if (!this.isNumber(this.thisData[item])) {
                this.$alertWarning('盘点数量、盘点盈亏必须为非负整数');
                return;
              }
            }
            this.isPending = true;
            let options = {
              url: editEwhInventoryLogUrl,
              data: {
                id:this.editData.id,
                acturalNum:this.thisData.atrualNum,
                returnNum:this.thisData.returnNum
              }
            };
            axiosPost(options).then(response => {
              this.isPending = false;
              if (response.data.result === 200) {
                this.$alertSuccess('更新成功');
                this.closeEditPanel();
                eventBus.$emit('checkDetailsRefresh');
              } else {
                this.isPending = false;
                errHandler(response.data);
              }
            }).catch(err => {
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试')
            })
          }
        },
        isNumber: function (num) {
          let val = num;
          let reg = /^\+?(0|[1-9][0-9]*)$/;
          if (val !== "") {
            return reg.test(val);
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
    z-index:120;
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
