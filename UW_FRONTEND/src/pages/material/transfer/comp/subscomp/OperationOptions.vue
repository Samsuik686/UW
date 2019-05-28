<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="详细" @click="showDetails">
      <icon name="list" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="损耗" @click="isAddLog = !isAddLog">
      <icon name="edit" scale="1.8"></icon>
    </div>
    <transition name="fade" v-if="isAddLog">
      <edit-wastage :row="row"></edit-wastage>
    </transition>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus'
  import EditWastage from "./EditWastage";
  export default {
    name: "OperationOptions",
    components: {EditWastage},
    props: ['row'],
    data() {
      return {
        isAddLog:false
      }
    },
    mounted() {
      eventBus.$on('closeEditPanel',() => {
        this.isAddLog = false;
      });
    },
    methods:{
      showDetails:function(){
        eventBus.$emit('showDetails',this.row);
      }
    }
  }
</script>

<style scoped>
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }
  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
