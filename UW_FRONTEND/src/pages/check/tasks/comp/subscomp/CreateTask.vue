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
        <select id="type-supplier" v-model="supplierId" class="custom-select">
          <option v-for="item in suppliers" :value="item.id">{{item.name}}</option>
        </select>
      </div>
      <div class="dropdown-divider"></div>
      <div class="form-row justify-content-around">
        <a class="btn btn-secondary col mr-1 text-white" @click="closeCreatePanel">取消</a>
        <a class="btn btn-primary col ml-1 text-white" @click="submitUploading">提交</a>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapActions} from 'vuex'
  import eventBus from '@/utils/eventBus';
  import {supplierSelectUrl, createInventoryTaskUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "CreateTask",
    data() {
      return {
        isPending: false,
        supplierId: '',
        suppliers: []
      }
    },
    created() {
      this.selectSupplier();
    },
    methods: {
      ...mapActions(['setLoading']),
      submitUploading: function () {
        if (this.supplierId === '') {
          this.$alertWarning('供应商不能为空');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          this.setLoading(true);
          let options = {
            url: createInventoryTaskUrl,
            data: {
              supplierId: this.supplierId
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            this.setLoading(false);
            if (response.data.result === 200) {
              this.$alertSuccess('创建任务成功');
              this.closeCreatePanel();
              let tempUrl = this.$route.fullPath;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            this.isPending = false;
            this.setLoading(false);
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
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
              data.map((item) => {
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
      closeCreatePanel: function () {
        eventBus.$emit('closeCreatePanel', true);
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

