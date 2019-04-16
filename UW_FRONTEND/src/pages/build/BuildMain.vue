<template>
  <div class="build">
    <div class="build-main">
      <h4>建仓</h4>
      <form class="build-form form-horizontal" role="form">
        <div class="form-group ">
          <label for="supplier" class="control-label">供应商：</label>
          <div>
            <select id="supplier" v-model.trim.number="buildInfo.supplierId" class="custom-select">
              <option value="" disabled>请选择</option>
              <option :value="item.id" v-for="item in suppliers">{{item.name}}</option>
            </select>
          </div>
        </div>
        <div class="form-group">
          <label class="control-label">料盒类型：</label>
          <div>
            <label class="radio-inline">
              <input type="radio" name="optionsRadio" id="optionsRadios1" value='1' v-model="isStandard">标准
            </label>
            <label class="radio-inline">
              <input type="radio" name="optionsRadio" id="optionsRadios2" value='0' v-model="isStandard">不标准
            </label>
          </div>
        </div>
        <div class="form-group ">
          <label class="control-label">起始位置：</label>
          <div>
            <input type="number" id="srcX" class="form-control col-1" placeholder="srcX"
                   v-model.trim.number="buildInfo.parameters.srcX">
            <input type="number" id="srcY" class="form-control col-1" placeholder="srcY"
                   v-model.trim.number="buildInfo.parameters.srcY">
            <input type="number" id="srcZ" class="form-control col-1" placeholder="srcZ"
                   v-model.trim.number="buildInfo.parameters.srcZ">
          </div>
        </div>
        <div style="display:flex;">
          <div class="form-group ">
            <label class="control-label">目标位置 - 起始坐标：</label>
            <div>
              <input type="number" id="startX" class="form-control col-1" placeholder="startX"
                     v-model.trim.number="buildInfo.parameters.startX">
              <input type="number" id="startY" class="form-control col-1" placeholder="startY"
                     v-model.trim.number="buildInfo.parameters.startY">
              <input type="number" id="startZ" class="form-control col-1" placeholder="startZ"
                     v-model.trim.number="buildInfo.parameters.startZ">
            </div>
          </div>
          <div class="form-group ">
            <label class="control-label">目标位置 - 终止坐标：</label>
            <div>
              <input type="number" id="endX" class="form-control col-1" placeholder="endX" disabled
                     v-model.trim.number="buildInfo.parameters.startX">
              <input type="number" id="endY" class="form-control col-1" placeholder="endY"
                     v-model.trim.number="buildInfo.parameters.endY">
              <input type="number" id="endZ" class="form-control col-1" placeholder="endZ"
                     v-model.trim.number="buildInfo.parameters.endZ">
            </div>
          </div>
          <div class="form-group ">
            <label class="control-label">极限坐标：</label>
            <div>
              <input type="number" id="limitX" class="form-control col-1" placeholder="limitYL"
                     v-model.trim.number="buildInfo.parameters.limitYL">
              <input type="number" id="limitY" class="form-control col-1" placeholder="limitYR"
                     v-model.trim.number="buildInfo.parameters.limitYR">
            </div>
          </div>
        </div>
        <div class="form-group ">
          <button type="button" class="btn btn-primary" @click="build">建仓</button>
          <button type="button" class="btn btn-secondary" @click="reset">清除条件</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
  import {buildUrl, supplierSelectUrl} from "../../config/globalUrl";
  import {axiosPost} from "../../utils/fetchData";
  import {errHandler} from "../../utils/errorHandler";

  export default {
    name: "BuildMain",
    data() {
      return {
        buildInfo: {
          parameters: {
            srcX: '',
            srcY: '',
            srcZ: '',
            startX: '',
            startY: '',
            startZ: '',
            endX:'',
            endY: '',
            endZ: '',
            limitYL: '',
            limitYR: ''
          },
          supplierId:'',
          isStandard:'',
        },
        isStandard: '',
        suppliers: [],
        isPending: false,
        isReturn:false
      }
    },
    created() {
      this.reset();
      this.selectSupplier();
    },
    methods: {
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
              data.map((item, index) => {
                if (item.enabled === true) {
                  this.suppliers.push(item);
                }
              });
            } else {
              errHandler(response.data);
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
      build: function () {
        this.buildInfo.parameters.endX = this.buildInfo.parameters.startX;
        if (this.buildInfo.supplierId === '') {
          this.$alertWarning('supplierId不能为空');
          return;
        }
        if (this.isStandard === '') {
          this.$alertWarning('isStandard不能为空');
          return;
        }
        for (let item in this.buildInfo.parameters) {
          if (this.buildInfo.parameters[item] === '') {
            this.$alertWarning(item + '不能为空');
            return;
          }
        }
        this.buildInfo.isStandard = this.isStandard === '1';
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: buildUrl,
            data: {
              parameters: JSON.stringify(this.buildInfo)
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('该供应商的料盒入仓成功');
            } else {
              errHandler(response.data)
            }
            this.isPending = false;
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试');
            }
          })
        }
      },
      reset: function () {
        this.buildInfo.isStandard = '';
        this.buildInfo.supplierId = '';
        this.buildInfo.parameters.limitYL = '';
        this.buildInfo.parameters.limitYR = '';
        this.buildInfo.parameters.srcX = '';
        this.buildInfo.parameters.srcY = '';
        this.buildInfo.parameters.srcZ = '';
        this.buildInfo.parameters.startX = '';
        this.buildInfo.parameters.startY = '';
        this.buildInfo.parameters.startZ = '';
        this.buildInfo.parameters.endX = '';
        this.buildInfo.parameters.endY = '';
        this.buildInfo.parameters.endZ = '';
        this.isStandard = '';
      }
    }
  }
</script>

<style scoped>
  .build{
    position: absolute;
    height: 100%;
    width: 100%;
  }
  .build-main {
    position: relative;
    margin:20px 0 0 100px;
    box-sizing: border-box;
    padding: 30px 30px;
    width:90%;
    background: #fff;
    border-radius: 6px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .build-form {
    box-sizing: border-box;
    width: 100%;
    padding: 20px 0 0 20px;
  }

  .radio-inline {
    margin-right: 20px;
  }

  .form-control {
    display: inline-block;
    min-width: 120px;
  }
</style>
