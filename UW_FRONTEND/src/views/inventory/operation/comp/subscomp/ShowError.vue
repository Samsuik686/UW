<template>
    <div class="show-error" v-if="isError">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <span style="font-size:18px;font-weight:bold;color:#333">机械臂操作异常</span>
            </div>
            <div class="item-text">{{error}}</div>
        </el-card>
    </div>
</template>

<script>
    export default {
        name: "ShowError",
        props:{
            error:String,
            isError:Boolean
        }
    }
</script>

<style scoped lang="scss">
    .show-error{
        position:absolute;
        top:10px;
        right:10px;
        z-index:9999;
        .box-card {
            width:320px;
            background: #fff;
            border:1px solid #4BC0C0;
            .clearfix:before,
            .clearfix:after {
                display: table;
                content: "";
            }
            .clearfix:after {
                clear: both
            }
            .item-text{
                font-size:16px;
                color:#333;
            }
        }
    }
</style>