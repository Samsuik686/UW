<template>
  <div>
    <search-options></search-options>
    <table-details></table-details>
  </div>
</template>

<script>
  import TableDetails from "./comp/UWTableDetails";
  import SearchOptions from "./comp/UWSearchOptions";

  export default {
    name: "DetailsModule",
    components: {SearchOptions, TableDetails}
  }
</script>

<style scoped>

</style>
