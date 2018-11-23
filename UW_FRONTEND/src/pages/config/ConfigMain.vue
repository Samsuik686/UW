<template>
    <div class="config">
      <div class="form-area">
        <ConfigModule></ConfigModule>
      </div>
    </div>
</template>

<script>
    import ConfigModule from "./details/ConfigModule";
    export default {
        name: "ConfigMain",
      components: {ConfigModule}
    }
</script>

<style scoped>
  .config {
    position: absolute;
    height: 100%;
    width: 100%;
  }
  .form-area {
    position: relative;
    box-sizing:border-box;
    margin-left: 60px;
    padding: 60px 60px;
  }
</style>
