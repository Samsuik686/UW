<template>
    <div class="destination" v-loading="isLoading">
        <el-form :inline="true" :model="destinationInfo" class="destination-form" @submit.native.prevent>
            <el-form-item label="发料目的地">
                <el-input v-model.trim="destinationInfo.name" placeholder="发料目的地"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="isAdding=true">新增发料目的地</el-button>
            </el-form-item>
        </el-form>
        <el-table
                @sort-change="sortChange"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="序号"
                    prop="showId">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="发料目的地ID"
                    prop="id"
            >
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="发料目的地"
                    prop="name">
            </el-table-column>
            <el-table-column label="操作" align="center">
                <template slot-scope="scope">
                    <span style="cursor: pointer" title="删除" @click="handleDelete(scope.row)">
                        <i class="el-icon-coke-cancel"></i>
                    </span>
                </template>
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
        <add-destination :isAdding.sync="isAdding"></add-destination>
    </div>
</template>

<script>
    import {destinationDeleteUrl, destinationSelectUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import AddDestination from "./comp/AddDestination";

    export default {
        name: "Destination",
        components: {AddDestination},
        data(){
            return{
                destinationInfo:{
                    name:''
                },
                tableData: [],
                pageNo: 1,
                pageSize: 20,
                totallyData:0,
                isPending:false,
                isLoading:false,
                filter:'',
                isAdding:false,
                ascBy:'',
                descBy:''
            }
        },
        mounted(){
            this.setFilter();
        },
        watch:{
            isAdding:function (val) {
                if(val === false){
                    this.setFilter();
                }
            }
        },
        methods:{
            initForm:function(){
                this.destinationInfo.name = '';
                this.filter = '';
            },
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:destinationSelectUrl,
                        data:{
                            pageNo:this.pageNo,
                            pageSize: this.pageSize,
                            ascBy:this.ascBy,
                            descBy:this.descBy,
                            filter:this.filter
                        }
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            let data = res.data.data.list;
                            let tableData = [];
                            data.map((item) => {
                                if(item.id !== -1 && item.id !== 0){
                                    tableData.push(item);
                                }
                            });
                            tableData.map((item,index) => {
                                item.showId = index + 1 + (this.pageNo - 1)*this.pageSize;
                            });
                            this.tableData = tableData;
                            this.totallyData = tableData.length;
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                        this.isLoading = false;
                    })
                }
            },
            setFilter:function(){
                if(this.destinationInfo.name !== ''){
                    this.filter = 'name' + 'like' + this.destinationInfo.name;
                }else{
                    this.filter = '';
                }
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            handleDelete: function (row) {
                this.$confirm('你正在删除名字为“'+row.name+'”的发料目的地，请确认是否删除', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    showClose:false,
                    type: 'warning',
                    center: false
                }).then((action) => {
                    if (action === "confirm") {
                        this.submitDelete(row);
                    }
                }).catch(() => {
                    this.$alertInfo("已取消删除");
                });
            },
            submitDelete:function(row){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: destinationDeleteUrl,
                        data: {
                            id:row.id,
                            enabled:0
                        }
                    };
                    options.data.enabled = 0;
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.$alertSuccess('删除成功');
                            this.isPending = false;
                            this.setFilter();
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            },
            sortChange:function(data){
                let prop = data.prop;
                if(data.order === "ascending"){
                    this.ascBy = prop;
                    this.descBy = '';
                }else if(data.order === "descending"){
                    this.descBy = prop;
                    this.ascBy = '';
                }else{
                    this.descBy = '';
                    this.ascBy = '';
                }
                this.select();
            }
        }
    }
</script>

<style scoped lang="scss">
    .destination{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        .block {
            margin-top: 15px;
        }
    }
</style>