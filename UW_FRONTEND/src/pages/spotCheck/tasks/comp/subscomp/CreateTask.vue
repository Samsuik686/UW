<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>创建任务：</h3>
        </div>
      </div>
      <div class="form-row">
        <label for="type-supplier" class="col-form-label">供应商:</label>
        <select id="type-supplier" v-model="supplier" class="custom-select">
          <option v-for="item in suppliers" :value="item.id">{{item.name}}</option>
        </select>
      </div>
      <div class="form-row">
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
    createSampleTaskUrl,
    supplierSelectUrl
  } from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import store from '../../../../../store'

  export default {
    name: "CreateTask",
    data() {
      return {
        fileName: '',
        isPending: false,
        thisFile: '',
        supplier: '',
        suppliers: [],
        remarks:''
      }
    },
    created() {
      this.selectSupplier();
    },
    methods: {
      ...mapActions(['setLoading']),
      closeUploadPanel: function () {
        eventBus.$emit('closeCreatePanel');
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
          this.isPending = true;
          this.setLoading(true);
          let formData = new FormData();
          if (this.thisFile !== "" && this.supplier !== "" && this.remarks !== "") {
            formData.append('file', this.thisFile);
            formData.append('supplierId', this.supplier);
            formData.append('remarks',this.remarks);
          } else {
            this.$alertWarning("内容不可为空");
            this.setLoading(false);
            this.isPending = false;
            return;
          }
          formData.append('#TOKEN#', store.state.token);
          let config = {
            header: {
              'Content-Type': 'multipart/form-data'
            }
          };
          this.$axios.post(createSampleTaskUrl, formData, config).then(res => {
            if (res.data.result === 200) {
              this.isPending = false;
              this.setLoading(false);
              this.$alertSuccess('创建任务成功');
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
