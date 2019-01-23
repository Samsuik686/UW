<template>
  <div>
    <options/>
    <preview-details/>
  </div>
</template>

<script>
  import Options from './comp/QueryOptions'
  import PreviewDetails from './comp/PreviewDetails'
  import {mapGetters} from 'vuex'

  export default {
    name: "IoPreview",
    components: {
      Options,
      PreviewDetails
    },
    computed: {
      ...mapGetters([
        'currentWindowId'
      ]),
    },
  }
</script>

<style scoped>
</style>
