<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="修改备注" @click="isEditRemarks = true">
      <icon name="edit" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="删除" @click="showWarning(row)" v-if="row.taskType === 6">
      <icon name="cancel" scale="1.8"></icon>
    </div>
    <div v-if="isEditRemarks" class="edit-window">
      <edit-remarks :editData="row"/>
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
              你正在删除任务为{{row.taskName}}的任务条目，请确认是否删除
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div class="form-row">
            <a class="btn btn-secondary col mr-1 text-white" @click="isDeleting = false">取消</a>
            <a class="btn btn-danger col ml-1 text-white" @click="submitDelete">确定</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

  import {externalWhDeleteExternalWhLogUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import eventBus from "../../../../../utils/eventBus";
  import EditRemarks from './EditRemarks'

  export default {
    name: "DeleteDetails",
    props: ['row'],
    components:{
      EditRemarks
    },
    data() {
      return {
        isDeleting:false,
        isEditRemarks:false,
        logId:'',
        isPending: false
      }
    },
    mounted(){
      eventBus.$on('closeEditRemarksPanel', () => {
        this.isEditRemarks = false;
      });
    },
    methods:{
      showWarning: function (val) {
        this.logId = val.id;
        this.isDeleting = true;
      },
      submitDelete: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: externalWhDeleteExternalWhLogUrl,
            data: {
              logId:this.logId
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('删除成功');
              this.isDeleting = false;
              eventBus.$emit('refresh',true);
            } else if (response.data.result === 412) {
              this.$alertWarning(response.data.data);
              this.isDeleting = false;
            } else {
              this.isPending = false;
              errHandler(response.data);
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
    z-index:103;
  }

  .delete-panel-container {
    background: #ffffff;
    width: 400px;
    z-index: 104;
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
