<template>
    <span :style="itemStyle">{{row.materialId}}</span>
</template>

<script>
    export default {
        name: "HighLight",
        props:{
            row:Object,
            activeMaterialId:String
        },
        watch:{
            row: function (val) {
                this.itemStyle.color = '';
                this.changeStyle(val);
            }
        },
        data(){
            return{
                itemStyle: {
                    color:''
                }
            }
        },
        mounted(){
            this.changeStyle(this.row);
        },
        methods: {
            changeStyle: function (row) {
                if(row.isOuted){
                    this.itemStyle.color = "#909399";
                    return;
                }
                if(row.isScaned){
                    if(row.materialId === this.activeMaterialId){
                        this.itemStyle.color = "#F56C6C"
                    }else{
                        this.itemStyle.color = "#e6941b"
                    }
                }
            }
        }
    }
</script>

<style scoped>

</style>