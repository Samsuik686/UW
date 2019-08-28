<template>
    <div class="supplier" v-loading="isLoading">
        <el-form :inline="true" :model="supplierInfo" class="supplier-form" @submit.native.prevent>
            <el-form-item label="供应商名">
                <el-input v-model.trim="supplierInfo.name" placeholder="供应商名"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="isAdding=true">新增供应商</el-button>
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
                    label="供应商号"
                    prop="id">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="供应商名"
                    prop="name">
            </el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <span style="margin-right:10px;cursor: pointer;" title="修改供应商名" @click="handleEdit(scope.row)">
                        <i class="el-icon-coke-edit"></i>
                    </span>
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
        <edit-supplier :isEditing.sync="isEditing" :editData="editData"></edit-supplier>
        <add-supplier :isAdding.sync="isAdding"></add-supplier>
    </div>
</template>

<script>
    import {supplierSelectUrl, supplierUpdateUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import EditSupplier from "./comp/EditSupplier";
    import AddSupplier from "./comp/AddSupplier";

    export default {
        name: "Supplier",
        components: {AddSupplier, EditSupplier},
        data(){
            return{
                supplierInfo:{
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
                isEditing:false,
                editData:{},
                ascBy:'',
                descBy:''
            }
        },
        mounted(){
            this.setFilter();
        },
        watch:{
            isEditing:function (val) {
                if(val === false){
                    this.setFilter();
                }
            },
            isAdding:function (val) {
                if(val === false){
                    this.setFilter();
                }
            }
        },
        methods:{
            initForm:function(){
                this.supplierInfo.name = '';
                this.filter = '';
            },
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:supplierSelectUrl,
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
                            data.map((item,index) => {
                                item.showId = index + 1 + (this.pageNo - 1)*this.pageSize;
                            });
                            this.tableData = data;
                            this.totallyData = res.data.data.totalRow;
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
                if(this.supplierInfo.name !== ''){
                    this.filter = 'name' + 'like' + this.supplierInfo.name;
                }else{
                    this.filter = '';
                }
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            handleDelete: function (row) {
                this.$confirm('你正在删除名字为“'+row.name+'”的供应商，请确认是否删除', {
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
                        url: supplierUpdateUrl,
                        data: JSON.parse(JSON.stringify(row))
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
            handleEdit:function(row){
                this.editData = row;
                this.isEditing = true;
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
                this.pageNo = 1;
                this.select();
            }
        }
    }
</script>

<style scoped lang="scss">
    .supplier{
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