<template>
  <div>
    <search-options></search-options>
    <table-details></table-details>
  </div>
</template>

<script>
  import TableDetails from "./comp/TableDetails";
  import SearchOptions from "./comp/SearchOptions";

  export default {
    name: "DetailsModule",
    components: {SearchOptions, TableDetails}
  }
</script>

<style scoped>

</style>
