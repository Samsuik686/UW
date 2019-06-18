<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="设置优先级" @click="confirmSetting(row)">
      <icon name="config" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="详细" @click="checkTaskDetails(row)">
      <icon name="list" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="状态" @click="isEditing = true">
      <icon name="menu" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="修改备注" @click="isEditRemarks = true">
      <icon name="edit" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="导出报表" v-if="row.state === 4" @click="exportUnfinishTaskDetails">
      <icon name="download" scale="1.8"></icon>
    </div>
    <div v-if="isEditing" class="edit-window">
      <edit-status :editData="row"/>
    </div>
    <div v-if="isSetting" class="edit-window">
      <set-priority :editData="row"/>
    </div>
    <div v-if="isUpload" class="edit-window">
      <manual-out :editData="row"/>
    </div>
    <div v-if="isEditRemarks" class="edit-window">
      <edit-remarks :editData="row"/>
    </div>
  </div>
</template>

<script>
  import EditStatus from './EditStatus'
  import eventBus from '@/utils/eventBus'
  import {mapActions} from 'vuex'
  import SetPriority from "./SetPriority";
  import ManualOut from "./ManualOut";
  import EditRemarks from './EditRemarks';
  import {downloadFile} from "../../../../../utils/fetchData";
  import {exportUnfinishTaskDetailsUrl} from "../../../../../config/globalUrl";

  export default {
    name: "OperationOptions",
    props: ['row'],
    components: {
      EditRemarks,
      ManualOut,
      SetPriority,
      EditStatus
    },
    data() {
      return {
        isEditing: false,
        isSetting:false,
        isUpload:false,
        isEditRemarks:false,
        isPending:false
      }
    },
    mounted() {
      eventBus.$on('closeTaskStatusPanel', () => {
        this.isEditing = false;
      });
      eventBus.$on('closeTaskPriorityPanel', () => {
        this.isSetting = false;
      });
      eventBus.$on('closeManualUploadPanel', () => {
        this.isUpload = false;
      });
      eventBus.$on('closeEditRemarksPanel', () => {
        this.isEditRemarks = false;
      });
    },
    methods: {
      ...mapActions(['setTaskActiveState', 'setTaskData', 'setLoading']),
      checkTaskDetails: function (val) {
        if (val.type <= 1 || val.type > 3) {
          this.setLoading(true);
          this.setTaskActiveState(true);
          this.setTaskData('');
          this.setTaskData(val)
        } else {
          this.$alertInfo('暂不支持此类型任务的详情查看');
        }
      },
      confirmSetting:function(row){
        if(row.stateString === "未开始" || row.stateString === "进行中"){
          this.isSetting = true;
        }else{
          this.$alertWarning("该状态不能设置优先级");
        }
      },
      exportUnfinishTaskDetails:function(){
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            id:this.row.id,
            type:this.row.type,
            '#TOKEN#': this.$store.state.token
          };
          downloadFile(exportUnfinishTaskDetailsUrl, data);
          let count = 0;
          let mark = setInterval(() => {
            count++;
            if (count > 9) {
              count = 0;
              clearInterval(mark);
              this.isPending = false
            }
          }, 1000);
          this.$alertSuccess('请求成功，请等待下载');
        } else {
          this.$alertInfo('请稍后再试')
        }
      }
    }
  }
</script>

<style scoped>
  .edit-window {
    z-index: 100;
  }
</style>
