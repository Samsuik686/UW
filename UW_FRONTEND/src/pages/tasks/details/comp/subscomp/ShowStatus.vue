<template>
  <span :style="itemStyle">{{row.itemStatus}}</span>
</template>

<script>
  export default {
    name: "ShowStatus",
    props: {
      row: Object
    },
    data() {
      return {
        itemStyle: {
          color: 'black',
          fontWeight: 'bold'
        }
      }
    },
    mounted(){
      this.changeStyle(this.row.itemStatus);
    },
    watch: {
      row: function (val) {
        this.changeStyle(this.row.itemStatus);
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
          case '欠料':
            this.itemStyle.color = 'blue'; break;
          default:
            this.itemStyle.color = 'black'; break;
        }
      }
    }
  }
</script>

<style scoped>

</style>
