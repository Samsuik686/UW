<template>
  <span :style="itemStyle">{{row.status}}</span>
</template>

<script>
  export default {
    name: "HighLight",
    props: {
        row:Object
    },
    data () {
      return {
        itemStyle: {
          color: 'black',
          fontWeight: 'bold'
        }
      }
    },
    mounted () {
      this.changeStyle(this.row.status)
    },
    watch: {
      row: function (val) {
        this.changeStyle(this.row.status)
      }
    },
    methods: {
      changeStyle: function (val) {
        switch (val){
          case '正常':
            this.itemStyle.color = 'green'; break;
          case '超发':
            this.itemStyle.color = 'orange'; break;
          case '缺料':
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
