<template>
  <div>
    <query-options></query-options>
    <table-details></table-details>
  </div>
</template>

<script>
  import QueryOptions from "./comp/QueryOptions";
  import TableDetails from "./comp/TableDetails";

  export default {
    name: "OperationModule",
    components: {TableDetails, QueryOptions}
  }
</script>

<style scoped>

</style>
