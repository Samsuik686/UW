<template>
  <el-dialog
      title="创建任务"
      :visible.sync="isAdding"
      :show-close="isCloseOnModal"
      :close-on-click-modal="isCloseOnModal"
      :close-on-press-escape="isCloseOnModal"
      width="30%">
    <el-form>
      <el-form-item label="选择文件">
        <input type="file"
               style="display:none;"
               id="fileUpload"
               @change="handleFileChange"
               accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"/>
        <el-input id="uploadFile" size="large" @click.native="handleUpload" v-model="fileName"
                  placeholder="请选择"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel">取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
  </el-dialog>
</template>

<script>
  import Bus from '../../../../utils/bus.js'
  import axios from '../../../../plugins/http'
  import {mapGetters} from 'vuex'
  import {
    taskCreateCarryBoxesTaskUrl,
  } from "../../../../plugins/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";

  export default {
    name: "AddTask",
    data() {
      return {
        isPending: false,
        isCloseOnModal: false,
        taskType: '',
        supplier: '',
        destination: '',
        thisFile: '',
        remarks: '',
        fileName: '',
        destinations: [],
        isInventoryApply: false,
        inventoryTaskId: '',
        inventoryTasks: [],
        isDestinationShow: false,
        destinationTip: '目的地'
      }
    },
    computed: {
      ...mapGetters(['token'])
    },
    created() {
    },
    watch: {},
    props: {
      isAdding: Boolean,
      suppliers: Array,
    },
    methods: {
      cancel: function () {
        this.taskType = '';
        this.supplier = '';
        this.inventoryTaskId = '';
        this.destination = '';
        this.remarks = '';
        this.fileName = '';
        this.thisFile = '';
        this.$emit("update:isAdding", false);
      },
      submit: function () {
        if (!this.isPending) {
          let formData = new FormData();
          if (this.thisFile === '') {
            this.$alertWarning('任务不能为空');
            return;
          }
          formData.append('file', this.thisFile);
          formData.append('#TOKEN#', this.token);
          this.isPending = true;
          axios.post(taskCreateCarryBoxesTaskUrl, formData).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess('创建成功');
              this.cancel();
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
          }).finally(() => {
            this.isPending = false;
          })
        }
      },
      handleUpload: function () {
        let file = document.getElementById('fileUpload');
        file.value = null;
        file.click();
      },
      handleFileChange: function () {
        let files = document.getElementById('fileUpload');
        let file = files.files[0];
        this.thisFile = file;
        this.fileName = file.name;
      }
    }
  }
</script>

<style scoped>

</style>