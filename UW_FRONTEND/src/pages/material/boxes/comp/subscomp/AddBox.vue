<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>添加料盒：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-area" class="col-form-label">区域:</label>
          <!--<input type="text" id="material-area" class="form-control" v-model="thisData.area"
                 @input="validate('area', '^[0-9]*[1-9][0-9]*$', '请输入正整数区域号')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.areaMsg}}</span>-->
          <select id="material-area" v-model="thisData.area" class="custom-select">
            <option value="A" label="A"></option>
            <option value="B" label="B"></option>
            <option value="C" label="C"></option>
          </select>
          <span class="form-span col"></span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-row" class="col-form-label">行号:</label>
          <input type="text" id="material-row" class="form-control" v-model="thisData.row"
                 @input="validate('row', '^[0-9]*[1-9][0-9]*$', '请输入正整数行号')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.rowMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-col" class="col-form-label">列号:</label>
          <input type="text" id="material-col" class="form-control" v-model="thisData.col"
                 @input="validate('col', '^[0-9]*[1-9][0-9]*$', '请输入正整数列号')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.colMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-height" class="col-form-label">高度:</label>
          <input type="text" id="material-height" class="form-control" v-model="thisData.height"
                 @input="validate('height', '^[0-9]*[1-9][0-9]*$', '请输入正整数高度')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.heightMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-isStandard" class="col-form-label">料盒类型:</label>
          <!--<input type="text" id="material-cellWidth" class="form-control" v-model="thisData.cellWidth"
                 @input="validate('cellWidth', '^[0-9]*[1-9][0-9]*$', '请输入正整数规格')"  autocomplete="off">
          <span class="form-span col">{{warningMsg.cellWidthMsg}}</span>-->
          <select id="material-isStandard" v-model="isStandard" class="custom-select">
            <option value="1" label="标准"></option>
            <option value="0" label="不标准"></option>
          </select>
          <span class="form-span col"></span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-supplier" class="col-form-label">供应商:</label>
          <select id="material-supplier" v-model="thisData.supplierId" class="custom-select">
            <option  v-for="item in suppliers">{{item.name}}</option>
          </select>
          <span class="form-span col"></span>
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
  import {addBoxUrl, supplierSelectUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import _ from 'lodash'
  export default {
    name: "AddMaterial",
    data() {
      return {
        thisData: {
          area: '',
          row: '',
          col: '',
          height: '',
          supplierId:'',
          isStandard:''
        },
        isStandard:'',
        warningMsg: {
        },
        isPending: false,
        suppliers:[]
      }
    },
    mounted(){
      this.selectSupplier();
    },
    methods: {
      closeAddPanel: function () {
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
            if (this.thisData[item] === '' && item !== 'isStandard') {
              this.$alertWarning('内容不能为空');
              return;
            }
          }
          if(this.isStandard === ''){
            this.$alertWarning('内容不能为空');
            return;
          }
          this.thisData.isStandard = this.isStandard === '1';
          this.isPending = true;
          for (let index in this.thisData){
            this.thisData[index] = _.trim(this.thisData[index])
          }

          let options = {
            url: addBoxUrl,
            data: this.thisData
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('添加成功');
              this.closeAddPanel();
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
      selectSupplier: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: supplierSelectUrl,
            data: {}
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              let data = response.data.data.list;
              data.map((item,index) => {
                if(item.enabled === true){
                  this.suppliers.push(item);
                }
              })
            } else {
              errHandler(response.data.result)
            }
          })
            .catch(err => {
              if (JSON.stringify(err) !== '{}') {
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试');
              }
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
