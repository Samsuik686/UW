<template>
  <span :style="itemStyle">{{row.resultString}}</span>
</template>

<script>
  export default {
    name: "HighLight",
    props: ['row'],
    data () {
      return {
        itemStyle: {
          color: 'black',
          fontWeight: 'bold'
        }
      }
    },
    mounted () {
      this.changeStyle(this.row.resultCode)
    },
    watch: {
      row: function (val) {
        this.changeStyle(val.resultCode)
      }
    },
    methods: {
      changeStyle: function (val) {
        switch (val){
          case 200:
            this.itemStyle.color = 'green'; break;
          case 400:
          case 412:
          case 401:
            this.itemStyle.color = 'orange'; break;
          case 500:
          case 501:
          case 503:
            this.itemStyle.color = 'red'; break;
          default:
            this.itemStyle.color = 'black'; break;
        }
      }
    }
  }
</script>

<style scoped>

</style>
