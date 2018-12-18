<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>添加目标地址：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col-4 pl-2 pr-2">
          <label for="startX" class="col-form-label">起始坐标X:</label>
          <input type="text" id="startX" class="form-control" v-model="thisData.startX"
                 @input="validate('startX', '^[0-9]*[1-9][0-9]*$', '请输入正整数')" autocomplete="off">
          <span class="form-span col">{{warningMsg.startXMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="startY" class="col-form-label">起始坐标Y:</label>
          <input type="text" id="startY" class="form-control" v-model="thisData.startY"
                 @input="validate('startY', '^[0-9]*[1-9][0-9]*$', '请输入正整数')" autocomplete="off">
          <span class="form-span col">{{warningMsg.startYMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="startZ" class="col-form-label">起始坐标Z:</label>
          <input type="text" id="startZ" class="form-control" v-model="thisData.startZ"
                 @input="validate('startZ', '^[0-9]*[1-9][0-9]*$', '请输入正整数')" autocomplete="off">
          <span class="form-span col">{{warningMsg.startZMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="endX" class="col-form-label">终止坐标X:</label>
          <input type="text" id="endX" class="form-control" v-model="thisData.endX"
                 @input="validate('endX', '^[0-9]*[1-9][0-9]*$', '请输入正整数')" autocomplete="off">
          <span class="form-span col">{{warningMsg.endXMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="endY" class="col-form-label">终止坐标Y:</label>
          <input type="text" id="endY" class="form-control" v-model="thisData.endY"
                 @input="validate('endY', '^[0-9]*[1-9][0-9]*$', '请输入正整数')" autocomplete="off">
          <span class="form-span col">{{warningMsg.endYMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="endZ" class="col-form-label">终止坐标Z:</label>
          <input type="text" id="endZ" class="form-control" v-model="thisData.endZ"
                 @input="validate('endZ', '^[0-9]*[1-9][0-9]*$', '请输入正整数')" autocomplete="off">
          <span class="form-span col">{{warningMsg.endZMsg}}</span>
        </div>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <button class="btn btn-secondary col mr-1 text-white" @click="closeAddDstPanel">取消</button>
        <button class="btn btn-primary col ml-1 text-white" @click="submitAdding">提交</button>
      </div>

    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus';

  export default {
    name: "AddDst",
    data() {
      return {
        thisData: {
          startX: '',
          startY: '',
          startZ: '',
          endX: '',
          endY: '',
          endZ: ''
        },
        warningMsg: {},
      }
    },
    methods: {
      closeAddDstPanel: function () {
        eventBus.$emit('closeDstAddPanel');
      },
      submitAdding: function () {
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
        this.$emit('getAddDstData',this.thisData);
      },
      validate: function (type, regx, msg) {
        let reg = new RegExp(regx);
        if (!reg.test(this.thisData[type])) {
          this.warningMsg[type + 'Msg'] = '*' + msg
        } else {
          this.warningMsg[type + 'Msg'] = ""
        }
      }
    },
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
