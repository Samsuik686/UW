<template>
  <div class="add-panel">
    <div class="add-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>导入物料仓物料表：</h3>
        </div>
      </div>
      <div class="form-row">
        <label for="type-supplier" class="col-form-label">供应商:</label>
        <select id="type-supplier" v-model="supplierId" class="custom-select">
          <option  v-for="item in suppliers" :value="item.id" :key="item.id">{{item.name}}</option>
        </select>
      </div>
      <div class="form-row">
        <label for="type-origin" class="col-form-label">来源地:</label>
        <select id="type-origin"  class="custom-select" v-model="sourceWhId">
          <option  v-for="item in origins" :value="item.id" :key="item.id">{{item.name}}</option>
        </select>
      </div>
      <div class="form-row">
        <label for="type-destination" class="col-form-label">目的地:</label>
        <select id="type-destination"  class="custom-select" v-model="destinationwhId">
          <option  v-for="item in destinations" :value="item.id" :key="item.id">{{item.name}}</option>
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
  import {destinationSelectUrl, externalWhImportTaskUrl} from "../../../../../config/globalUrl";
  import {errHandler} from "../../../../../utils/errorHandler";
  import store from '../../../../../store'
  import {axiosPost} from "../../../../../utils/fetchData";

  export default {
    name: "AddTransfer",
    props:{
      suppliers:Array
    },
    data() {
      return {
        fileName: '',
        isPending: false,
        thisFile: '',
        supplierId: '',
        sourceWhId:'',
        destinationwhId:'',
        origins:[],
        destinations:[],
        remarks:''
      }
    },
    created(){
      this.selectDestinations();
    },
    methods: {
      ...mapActions(['setLoading']),
      closeUploadPanel: function () {
        eventBus.$emit('closeTransferUploadPanel');
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
          if (this.thisFile !== "" && this.supplierId!== "" && this.destinationwhId !== '' && this.sourceWhId !== '' && this.remarks !== '') {
            if(this.destinationwhId === this.sourceWhId){
              this.$alertWarning('目的地和来源地不能相同');
              this.isPending = false;
              this.setLoading(false);
              return;
            }
            formData.append('file', this.thisFile);
            formData.append('supplierId', this.supplierId);
            formData.append('destinationwhId', this.destinationwhId);
            formData.append('sourceWhId', this.sourceWhId);
            formData.append('remarks',this.remarks);
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
          this.$axios.post(externalWhImportTaskUrl, formData, config).then(res => {
            this.isPending = false;
            this.setLoading(false);
            if (res.data.result === 200) {
              this.$alertSuccess('导入成功');
              this.closeUploadPanel();
              let tempUrl = this.$route.fullPath;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else if (res.data.result === 412) {
              this.$alertWarning(res.data.data);
            } else {
              errHandler(res.data)
            }
          }).catch(err => {
            console.log(err);
            this.isPending = false;
            this.setLoading(false);
          })
        }
      },
      selectDestinations:function(){
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url: destinationSelectUrl,
            data: {
            }
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
              this.origins = data;
              this.destinations = list;
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            if (JSON.stringify(err) !== '{}'){
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试');
            }
          });
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
