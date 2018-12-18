<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>添加料盒类型：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-cellWidth" class="col-form-label">规格:</label>
          <input type="text" id="material-cellWidth" class="form-control" v-model="thisData.cellWidth"
                 @input="validate('cellWidth', '^[0-9]*[1-9][0-9]*$', '请输入正整数规格')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.cellWidthMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-cellRows" class="col-form-label">总行数:</label>
          <input type="text" id="material-cellRows" class="form-control" v-model="thisData.cellRows"
                 @input="validate('cellRows', '^[0-9]*[1-9][0-9]*$', '请输入正整数总行数')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.cellRowsMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-cellCols" class="col-form-label">总列数:</label>
          <input type="text" id="material-cellCols" class="form-control" v-model="thisData.cellCols"
                 @input="validate('cellCols', '^[0-9]*[1-9][0-9]*$', '请输入正整数总列数')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.cellColsMsg}}</span>
        </div>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <button class="btn btn-secondary col mr-1 text-white" @click="closeAddTypePanel">取消</button>
        <button class="btn btn-primary col ml-1 text-white" @click="submitAdding">提交</button>
      </div>
    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus';
  import {addBoxTypeUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import _ from 'lodash'
  export default {
    name: "AddBoxType",
    data() {
      return {
        thisData: {
          cellWidth:'',
          cellRows:'',
          cellCols:''
        },
        warningMsg: {

        },
        isPending: false
      }
    },
    methods: {
      closeAddTypePanel: function () {
        eventBus.$emit('closeAddPanel');
      },
      submitAdding: function () {
        if (!this.isPending) {
          for (let i in this.warningMsg) {
            if (this.warningMsg[i] !== "") {
              this.$alertWarning("请输入正确格式！");
              return
            }
          }
          for (let item in this.thisData) {
            if (this.thisData[item] === '') {
              this.$alertWarning('内容不能为空');
              return;
            }
          }
          this.isPending = true;
          for (let index in this.thisData){
            this.thisData[index] = _.trim(this.thisData[index])
          }
          let options = {
            url: addBoxTypeUrl,
            data: this.thisData
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('添加成功');
              this.closeAddTypePanel();
              let tempUrl = this.$route.path;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else if (response.data.result >= 412 && response.data.result < 500) {
              this.$alertWarning(response.data.data)
            } else {
              errHandler(response.data)
            }
            this.isPending = false;
          })
        }
      },
      validate: function (type, regx, msg) {
        let reg = new RegExp(regx);
        if (!reg.test(this.thisData[type])) {
          this.warningMsg[type + 'Msg'] = '*' + msg
        } else {
          this.warningMsg[type + 'Msg'] = ""
        }
      }
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
  .form-span {
    display: block;
    height: 20px;
    line-height: 20px;
    font-size: 10px;
    color: darkred;
  }
</style>
