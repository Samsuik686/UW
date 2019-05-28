<template>
  <div :class="{highLight:row.isHightLight}">{{row.no}}</div>
</template>

<script>
  export default {
    name: "MaterialHighLight",
    data() {
      return {
      }
    },
    props: {
      row: Object
    }
  }
</script>

<style scoped>
  .highLight{
    color:red;
  }
</style>
