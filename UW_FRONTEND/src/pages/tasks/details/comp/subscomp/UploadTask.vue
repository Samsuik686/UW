<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>创建任务：</h3>
        </div>
      </div>
      <div class="form-row">
        <label for="type-select" class="col-form-label">类型选择:</label>
        <select id="type-select" class="custom-select" v-model="taskType">
          <option value="" disabled>请选择</option>
          <option value="0">入库</option>
          <option value="1">出库</option>
          <!--<option value="2">盘点</option>-->
          <!--<option value="3">位置优化</option>-->
          <option value="4">调拨入库</option>
          <option value="11">出库 - 盘点前申补</option>
        </select>
      </div>
      <div class="form-row">
        <label for="type-supplier" class="col-form-label">供应商:</label>
        <select id="type-supplier" v-model="supplier" class="custom-select">
          <option v-for="item in suppliers" :value="item.id">{{item.name}}</option>
        </select>
      </div>
      <div class="form-row" v-if="isInventoryApply">
        <label for="type-inventoryTask" class="col-form-label">盘点任务:</label>
        <select id="type-inventoryTask" v-model="inventoryTaskId" class="custom-select">
          <option v-for="item in inventoryTasks" :value="item.id">{{item.file_name}}</option>
        </select>
      </div>
      <div class="form-row" v-if="isShow">
        <label for="type-destination" class="col-form-label">{{tip}}:</label>
        <select id="type-destination" v-model="destination" class="custom-select">
          <option v-for="item in destinations" :value="item.id">{{item.name}}</option>
        </select>
      </div>
      <div class="form-row" v-if="taskType < 2 || taskType == 4 || taskType == 11">
        <label for="upload-comp" class="col-form-label">选择文件:</label>
        <input id="upload-comp" type="text" class="form-control" v-model="fileName" onfocus="this.blur()"
               @click="setUpload" autocomplete="off">
        <input type="file" id="upload-file-comp"
               accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
               class="d-none" ref="taskUpload" @change="uploadFile">
      </div>
      <div class="form-row">
        <label for="type-remarks" class="col-form-label">备注:</label>
        <textarea class="form-control" id="type-remarks" autocomplete="false" v-model.trim="remarks"></textarea>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <a class="btn btn-secondary col mr-1 text-white" @click="closeUploadPanel">取消</a>
        <a class="btn btn-primary col ml-1 text-white" @click="submitUploading">提交</a>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapActions} from 'vuex'
  import eventBus from '@/utils/eventBus';
  import {
    destinationSelectUrl,
    getUnStartInventoryTaskUrl,
    supplierSelectUrl,
    taskCreateUrl
  } from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import store from '../../../../../store'

  export default {
    name: "UploadTask",
    data() {
      return {
        fileName: '',
        taskType: '',
        isPending: false,
        thisFile: '',
        supplier: '',
        suppliers: [],
        destination: '',
        destinations: [],
        isShow: false,
        tip:'目的地',
        isInventoryApply:false,
        inventoryTaskId:'',
        inventoryTasks:[],
        remarks:''
      }
    },
    created() {
      this.selectSupplier();
    },
    watch: {
      taskType: function (val) {
        this.isShow = val === "1" || val === "4" || val === "11";
        this.isInventoryApply = val === "11";
        if(val === '1'){
          this.tip="目的地"
        }else{
          this.tip="退料仓位"
        }
      },
      supplier:function(val){
        if(val !== ''){
          this.getUnStartInventoryTasks();
        }
      }
    },
    methods: {
      ...mapActions(['setLoading']),
      closeUploadPanel: function () {
        eventBus.$emit('closeUploadPanel');
      },
      setUpload: function () {
        this.$refs.taskUpload.click()
      },
      uploadFile: function (e) {
        if (e.target.files[0]) {
          this.thisFile = e.target.files[0];
          this.fileName = e.target.files[0].name + " 加载成功";
        } else {
          this.thisFile = '';
          this.fileName = '';
        }
      },
      submitUploading: function () {
        if (!this.isPending) {
          if (this.taskType !== "") {
            this.isPending = true;
            this.setLoading(true);
            let formData = new FormData();
            if (this.taskType < 2 || this.taskType == 4 || this.taskType == 11) {
              if (this.thisFile !== "" && this.supplier !== "" && this.remarks !== "") {
                formData.append('file', this.thisFile);
                formData.append('supplier', this.supplier);
                formData.append('remarks',this.remarks);
                formData.append('destination', this.destination);
              } else {
                this.$alertWarning("内容不可为空");
                this.setLoading(false);
                this.isPending = false;
                return;
              }
            }
            if(this.isInventoryApply === true){
              if(this.inventoryTaskId === ''){
                this.$alertWarning("盘点任务不可为空");
                this.setLoading(false);
                this.isPending = false;
                return;
              }
              formData.append('isInventoryApply', this.isInventoryApply);
              formData.append('inventoryTaskId', this.inventoryTaskId);
            }
            if(this.taskType === '11')this.taskType = 1;
            formData.append('type', this.taskType);
            formData.append('#TOKEN#', store.state.token);
            let config = {
              header: {
                'Content-Type': 'multipart/form-data'
              }
            };
            this.$axios.post(taskCreateUrl, formData, config).then(res => {
              if (res.data.result === 200) {
                this.isPending = false;
                this.setLoading(false);
                this.$alertSuccess('添加成功');
                this.closeUploadPanel();
                let tempUrl = this.$route.fullPath;
                this.$router.push('_empty');
                this.$router.replace(tempUrl);
              } else if (res.data.result === 412) {
                this.isPending = false;
                this.setLoading(false);
                this.$alertWarning(res.data.data);
              }
              else {
                this.isPending = false;
                this.setLoading(false);
                errHandler(res.data)
              }
            }).catch(err => {
              console.log(err);
              this.isPending = false;
              this.setLoading(false);
            })
          } else {
            this.$alertWarning("类型不能为空");
          }
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
              this.selectDestination();
              let data = response.data.data.list;
              data.map((item, index) => {
                if (item.enabled === true) {
                  this.suppliers.push(item);
                }
              })
            } else {
              errHandler(response.data)
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
      selectDestination: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: destinationSelectUrl,
            data: {}
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              let data = response.data.data.list;
              let list = [];
              data.map((item) => {
                if(item.id !== 0 && item.id !== -1){
                  list.push(item);
                }
              });
              this.destinations = list;
            } else {
              errHandler(response.data)
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
      getUnStartInventoryTasks: function () {
        if(this.supplier === ''){
          this.$alertWarning('请先选择供应商');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: getUnStartInventoryTaskUrl,
            data: {
              supplierId:this.supplier
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.inventoryTasks = response.data.data;
            } else {
              errHandler(response.data)
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
