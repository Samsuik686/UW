<template>
  <div>
    <search-options></search-options>
    <table-details></table-details>
  </div>
</template>

<script>
  import TableDetails from "./comp/TransferTableDetails";
  import SearchOptions from "./comp/TransferSearchOptions";

  export default {
    name: "DetailsModule",
    components: {SearchOptions, TableDetails}
  }
</script>

<style scoped>

</style>
