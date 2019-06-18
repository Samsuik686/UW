<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="状态" @click="isEditing = true">
      <icon name="menu" scale="1.8"></icon>
    </div>
    <!--<div class="btn pl-1 pr-1" title="分配叉车" @click="isSelecting = true">
      <icon name="edit" scale="1.8"></icon>
    </div>-->
    <div v-if="isEditing" class="edit-window">
      <edit-status :editData="row"></edit-status>
    </div>
    <div v-if="isSelecting" class="edit-window">
      <select-robots :editData="row"></select-robots>
    </div>
  </div>
</template>

<script>
  import EditStatus from "./EditStatus";
  import eventBus from "../../../../../utils/eventBus";
  import SelectRobots from "./SelectRobots";

  export default {
    name: "OperationOptions",
    components: {SelectRobots, EditStatus},
    props: {
      row: Object
    },
    data() {
      return {
        isEditing: false,
        isSelecting:false
      }
    },
    mounted() {
      eventBus.$on('closeCheckPanel', () => {
        this.isEditing = false;
      });
      eventBus.$on('closeSelectRobotsPanel', () => {
        this.isSelecting = false;
      });
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
