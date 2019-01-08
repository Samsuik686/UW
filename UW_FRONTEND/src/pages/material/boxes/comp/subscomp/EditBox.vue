<template>
  <div class="edit-panel">
    <div class="edit-panel-container form-row flex-column justify-content-between">
      <div class="form-row">
        <div class="form-group mb-0">
          <h3>更新料盒在架情况：</h3>
        </div>
      </div>
      <div class="form-row">
        <label for="material-isOnShelf" class="col-form-label">是否在架:</label>
        <select id="material-isOnShelf" class="form-control" v-model="thisData.isOnShelf">
          <option value=1>是</option>
          <option value=0>否</option>
        </select>
        <span class="form-span col"></span>
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
  import {updateBoxUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "EditMaterial",
    props: ['editData'],
    data() {
      return {
        thisData: {
          id: '',
          isOnShelf: 0
        },
        isPending: false
      }
    },
    mounted() {
      this.thisData.id = this.editData.id;
      this.thisData.isOnShelf = (this.editData.isOnShelf === true) ? 1 : 0;
    },
    methods: {
      closeEditPanel: function () {
        eventBus.$emit('closeEditPanel');
      },
      submitUpdate: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: updateBoxUrl,
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
            } else if (response.data.result >= 412 && response.data.result < 500) {
              this.$alertWarning(response.data.data)
              this.closeEditPanel();
            } else {
              errHandler(response.data);
              this.closeEditPanel()
            }
            this.isPending = false;
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

  .form-span {
    display: block;
    height: 20px;
    line-height: 20px;
    font-size: 10px;
    color: darkred;
  }
</style>
