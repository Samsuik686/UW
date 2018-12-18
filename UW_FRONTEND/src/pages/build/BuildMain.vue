<template>
  <div class="page">
    <box-main/>
  </div>
</template>

<script>
  import BoxMain from './details/BoxMain'
    export default {
        name: "BuildMain",
        components:{
          BoxMain
        }
    }
</script>

<style scoped>
  .page {
    position: absolute;
    box-sizing: border-box;
    padding: 50px 50px;
    width: 100%;
    height: 100%;
  }
</style>
