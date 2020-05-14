<template>
  <div class="table">
    <router-view></router-view>
  </div>
</template>

<script>
  export default {
    name: "Table"
  }
</script>

<style scoped>
  .table {
    width: 100%;
    height: 100%;
  }
</style>