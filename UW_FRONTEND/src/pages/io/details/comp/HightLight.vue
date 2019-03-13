<template>
  <span :style="activeStyle">{{row['stateString']}}</span>
</template>

<script>
  export default {
    name: "HightLight",
    props: {
      row: Object
    },
    computed: {
      activeStyle: function () {
        if (this.row['state'] === -3) {
          return {
            color:'red',
            fontWeight:'bold'
          }
        } else {
          return {}
        }
      }
    }
  }
</script>

<style scoped>
</style>
