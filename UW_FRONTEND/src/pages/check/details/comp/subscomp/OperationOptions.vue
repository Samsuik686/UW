<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="详细" @click="showDetails">
      <icon name="list" scale="1.8"></icon>
    </div>
  </div>
</template>

<script>

  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "OperationOptions",
    props:{
      row:Object
    },
    data() {
      return {
      }
    },
    methods:{
      showDetails:function(){
        eventBus.$emit('showCheckDetails',this.row);
      }
    }
  }
</script>

<style scoped>
</style>
