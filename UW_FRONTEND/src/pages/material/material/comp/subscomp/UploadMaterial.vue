<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>导入物料类型表：</h3>
        </div>
      </div>
      <div class="form-row">
        <label for="type-supplier" class="col-form-label">供应商:</label>
        <select id="type-supplier" v-model="supplierName" class="custom-select">
          <option  v-for="item in suppliers" :value="item.name" :key="item.id">{{item.name}}</option>
        </select>
      </div>
      <div class="form-row">
        <div class="form-row pl-1 pr-1">
          <label for="upload-comp" class="col-form-label">选择文件:</label>
          <input id="upload-comp" type="text" class="form-control" v-model="fileName" onfocus="this.blur()"
                 @click="setUpload" autocomplete="off">
          <input type="file" id="upload-file-comp"
                 accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                 class="d-none" ref="taskUpload" @change="uploadFile">
        </div>
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
  import {importReportUrl} from "../../../../../config/globalUrl";
  import {errHandler} from "../../../../../utils/errorHandler";
  import store from '../../../../../store'

  export default {
    name: "UploadMaterial",
    props:{
      suppliers:Array
    },
    data() {
      return {
        fileName: '',
        isPending: false,
        thisFile: '',
        supplierName: ''
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
        if(e.target.files[0]){
          this.thisFile = e.target.files[0];
          this.fileName = e.target.files[0].name + " 加载成功";
        }else{
          this.thisFile = '';
          this.fileName = '';
        }
      },
      submitUploading: function () {
        if (!this.isPending) {
          this.isPending = true;
          this.setLoading(true);
          let formData = new FormData();
          if (this.thisFile !== "" && this.supplierName !== "") {
            formData.append('file', this.thisFile);
            formData.append('supplierName', this.supplierName);
          } else {
            this.$alertWarning("内容不可为空");
            this.isPending = false;
            this.setLoading(false);
            return;
          }
          formData.append('#TOKEN#', store.state.token);
          let config = {
            header: {
              'Content-Type': 'multipart/form-data'
            }
          };
          this.$axios.post(importReportUrl, formData, config).then(res => {
            if (res.data.result === 200) {
              this.isPending = false;
              this.setLoading(false);
              this.$alertSuccess('导入成功');
              this.closeUploadPanel();
              let tempUrl = this.$route.path;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else if (res.data.result === 412) {
              this.isPending = false;
              this.setLoading(false);
              this.$alertWarning(res.data.data);
            } else {
              this.isPending = false;
              this.setLoading(false);
              errHandler(res.data)
            }
          }).catch(err => {
            this.isPending = false;
            this.setLoading(false);
            console.log(err);
          })
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

</style>
