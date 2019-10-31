<template>
    <div class="uwMaterial-details">
        <el-table
                border
                :data="tableData"
                style="width:100%">
            <el-table-column
                    min-width="120"
                    label="料盘唯一码"
                    prop="id">
            </el-table-column>
            <el-table-column
                    label="类型"
                    prop="type">
            </el-table-column>
            <el-table-column
                    min-width="150"
                    label="料号"
                    prop="materialNo">
            </el-table-column>
            <el-table-column
                    label="剩余数量"
                    prop="remainderQuantity">
            </el-table-column>
            <el-table-column
                    label="入库日期"
                    min-width="160"
                    prop="store_time">
            </el-table-column>
        </el-table>
        <div class="block">
            <el-pagination
                    background
                    :current-page.sync="pageNo"
                    :page-size.sync="pageSize"
                    :page-sizes="[20,40,80,100]"
                    @size-change="handlePageSize"
                    @current-change="select"
                    layout="total,sizes,prev,pager,next,jumper"
                    :total="totallyData">
                >
            </el-pagination>
        </div>
    </div>
</template>

<script>
    import {materialEntityUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";

    export default {
        name: "EntityDetails",
        props:{
            type:Number
        },
        data(){
            return{
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false
            }
        },
        mounted(){
            this.select();
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: materialEntityUrl,
                        data: {
                            materialTypeId: this.type,
                            pageNo:this.pageNo,
                            pageSize:this.pageSize
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.tableData = res.data.data.list;
                            this.totallyData = res.data.data.totalRow;
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            }
        }
    }
</script>

<style scoped lang="scss">
    .block{
        margin-top:15px;
    }
</style>