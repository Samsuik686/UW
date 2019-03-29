<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="删除" @click="showWarning(row)">
      <icon name="cancel" scale="1.8"></icon>
    </div>

    <div v-if="isDeleting" id="delete-window">
      <div class="delete-panel">
        <div class="delete-panel-container form-row flex-column justify-content-between">
          <div class="form-row">
            <div class="form-group mb-0">
              <h3>确认删除：</h3>
            </div>
          </div>
          <div class="form-row">
            <div class="form-row col pl-2 pr-2">
              你正在删除名字为 "{{rowData.name}}" 的供应商，请确认是否删除
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div class="form-row justify-content-around">
            <a class="btn btn-secondary col mr-1 text-white" @click="isDeleting = false">取消</a>
            <a class="btn btn-danger col ml-1 text-white" @click="submitDelete">确定</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {supplierUpdateUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "OperationOptions",
    props: ['row'],
    data() {
      return {
        isDeleting:false,
        rowData: {},
        isPending: false
      }
    },
    methods:{
      showWarning: function (val) {
        this.rowData = val;
        this.isDeleting = true;
      },
      submitDelete: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: supplierUpdateUrl,
            data: JSON.parse(JSON.stringify(this.rowData))
          };
          options.data.enabled = 0;
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('删除成功');
              this.isDeleting = false;
              let tempUrl = this.$route.path;
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else if (response.data.result === 412) {
              this.$alertWarning(response.data.data);
              this.isDeleting = false;

            } else {
              this.isPending = false;
              errHandler(response.data.result);
              this.isDeleting = false;
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
  .delete-panel {
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

  .delete-panel-container {
    background: #ffffff;
    height: 220px;
    width: 400px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
