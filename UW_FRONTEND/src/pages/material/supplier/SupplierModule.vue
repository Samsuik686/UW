<template>
  <div>
    <options/>
    <table-details/>
  </div>
</template>

<script>
  import Options from './comp/SearchOptions'
  import TableDetails from './comp/TableDetails'

  export default {
    name: "SupplierModule",
    components:{
      Options,
      TableDetails
    }
  }
</script>

<style scoped>

</style>
