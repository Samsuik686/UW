<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>更新发料目的地信息：</h3>
        </div>
      </div>
      <div class="form-row">
        <div class="form-row col pl-2 pr-2">
          <label for="supplier-name" class="col-form-label">发料目的地:</label>
          <input type="text" id="supplier-name" class="form-control" v-model="thisData.name" autocomplete="off">
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
  import {destinationUpdateUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "EditDestination",
    props: ['editData'],
    data() {
      return {
        thisData: {
          id: '',
          name:'',
          enabled: 1,
        },
        isPending: false
      }
    },
    mounted() {
      this.thisData.id = this.editData.id;
      this.thisData.name = this.editData.name;
      this.thisData.enabled = (this.editData.enabled === true)?1:0;
    },
    methods: {
      closeEditPanel: function () {
        eventBus.$emit('closeEditPanel');
      },
      submitUpdate: function () {
        if (!this.isPending) {
          for (let item in this.thisData) {
            if (this.thisData[item] === '') {
              this.$alertWarning('内容不能为空');
              return;
            }
          }
          this.isPending = true;
          let options = {
            url: destinationUpdateUrl,
            data: this.thisData
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('更新成功');
              this.closeEditPanel();
              let tempUrl = this.$route.path;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            }else if(response.data.result === 412){
              this.$alertWarning(response.data.data);
              this.closeEditPanel();
            } else {
              this.isPending = false;
              errHandler(response.data.result);
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
</style>


