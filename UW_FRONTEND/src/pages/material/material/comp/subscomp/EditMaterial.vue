<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>更新物料类型：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-no" class="col-form-label">料号:</label>
          <input type="text" id="material-no" class="form-control" v-model="thisData.no" autocomplete="off" disabled>
          <span class="form-span col"></span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-specification" class="col-form-label">规格:</label>
          <input type="text" id="material-specification" class="form-control" v-model="thisData.specification" autocomplete="off">
          <span class="form-span col"></span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-supplier" class="col-form-label">供应商:</label>
          <select id="material-supplier" v-model="thisData.supplierName" class="custom-select">
            <option  v-for="item in suppliers">{{item.name}}</option>
          </select>
          <span class="form-span col"></span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-thickness" class="col-form-label">厚度:</label>
          <input type="text" id="material-thickness" class="form-control" v-model="thisData.thickness"
                 @input="validate('thickness', '^[0-9]*[1-9][0-9]*$', '请输入正整数厚度')" autocomplete="off">
          <span class="form-span col">{{warningMsg.thicknessMsg}}</span>
        </div>
        <div class="form-row col-4 pl-2 pr-2">
          <label for="material-radius" class="col-form-label">半径:</label>
          <input type="text" id="material-radius" class="form-control" v-model="thisData.radius"
                 @input="validate('radius', '^[0-9]*[1-9][0-9]*$', '请输入正整数半径')" autocomplete="off">
          <span class="form-span col">{{warningMsg.radiusMsg}}</span>
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
  import {materialUpdateUrl, supplierSelectUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "EditMaterial",
    props: ['editData'],
    data() {
      return {
        thisData: {
          id: '',
          specification: '',
          no: '',
          supplierName:'',
          thickness: '',
          radius: ''
        },
        suppliers:[],
        warningMsg: {

        },
        isPending: false
      }
    },
    created(){
      this.selectSupplier();
    },
    mounted() {
      this.thisData.id = this.editData.id;
      this.thisData.no = this.editData.no;
      this.thisData.specification = this.editData.specification;
      this.thisData.supplierName = this.editData.supplierName;
      this.thisData.thickness = this.editData.thickness;
      this.thisData.radius = this.editData.radius;
    },
    methods: {
      closeEditPanel: function () {
        eventBus.$emit('closeEditPanel');
      },
      submitUpdate: function () {
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
          let options = {
            url: materialUpdateUrl,
            data: {
              id: this.thisData.id,
              specification: this.thisData.specification,
              enabled: 1,
              supplierName:this.thisData.supplierName,
              thickness:this.thisData.thickness,
              radius:this.thisData.radius
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('更新成功');
              this.closeEditPanel();
              let tempUrl = this.$route.path;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else {
              this.isPending = false;
              errHandler(response.data);
              this.closeEditPanel()
            }
          }).catch(err => {
            if (JSON.stringify(err)) {
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试')
            }
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
      //根据传入的条目、正则表达式以及错误信息进行信息验证
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
