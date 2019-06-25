<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="详细" @click="showDetails">
      <icon name="list" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="修改状态" @click="isEditing = true">
      <icon name="menu" scale="1.8"></icon>
    </div>
    <div v-if="isEditing" class="edit-window">
      <edit-status :editData="row"></edit-status>
    </div>
  </div>
</template>

<script>
  import EditStatus from "./EditStatus";
  import eventBus from "../../../../../utils/eventBus";

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
        isEditing: false
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
