<!--表单查看根组件-->
<template>
  <div>
    <options/>
    <table-details/>
  </div>
</template>

<script>
  import Options from './comp/QueryOptions'
  import TableDetails from './comp/TableDetails'

  export default {
    name: "Details",
    components: {
      Options,
      TableDetails
    }
  }
</script>

<style scoped>

</style>
