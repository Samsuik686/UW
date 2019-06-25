<template>
  <div :class="{highLight:isHighLight}">{{row.materialId}}</div>
</template>

<script>
  import {mapGetters} from 'vuex'
  export default {
    name: "MaterialHighLight",
    data(){
      return{
        isHighLight:false
      }
    },
    props: {
      row: Object
    },
    computed:{
      ...mapGetters(['spotMaterialId'])
    },
    watch:{
      spotMaterialId:function(val){
        this.isHighLight = val === this.row.materialId;
      }
    }
  }
</script>

<style scoped>
  .highLight{
    color:red
  }
</style>
