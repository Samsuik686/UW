<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="详细" @click="showDetails">
      <icon name="list" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="修改状态" @click="isEditing = true">
      <icon name="menu" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="下载报表" @click="downloadReport">
      <icon name="download" scale="1.8"></icon>
    </div>
    <div v-if="isEditing" class="edit-window">
      <edit-status :editData="row"></edit-status>
    </div>
  </div>
</template>

<script>
  import EditStatus from "./EditStatus";
  import eventBus from "../../../../../utils/eventBus";
  import {downloadFile} from "../../../../../utils/fetchData";
  import {exportSampleTaskInfoUrl} from "../../../../../config/globalUrl";

  export default {
    name: "OperationOptions",
    props: {
      row: Object
    },
    components: {
      EditStatus
    },
    data() {
      return {
        isEditing: false,
        isPending:false
      }
    },
    mounted() {
      eventBus.$on('closeSpotPanel', () => {
        this.isEditing = false;
      });
    },
    methods:{
      showDetails:function(){
        eventBus.$emit('showSampleDetails',this.row);
      },
      downloadReport:function(){
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            taskId:this.row.id,
            type:this.row.type,
            '#TOKEN#': this.$store.state.token
          };
          downloadFile(exportSampleTaskInfoUrl,data);
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

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
