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
            },
            activeMaterialId:function () {
                this.itemStyle.color = '';
                this.changeStyle(this.row);
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
                if(row.materialId === this.activeMaterialId){
                    this.itemStyle.color = "#F56C6C";
                    return;
                }
                if(row.isOuted !== -1){
                    this.itemStyle.color = "#909399";
                    return;
                }
                if(row.isScaned){
                    this.itemStyle.color = "#e6941b"
                }
            }
        }
    }
</script>

<style scoped>

</style>