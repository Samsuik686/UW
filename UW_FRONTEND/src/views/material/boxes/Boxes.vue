<template>
    <div class="boxes" v-loading="isLoading">
        <el-form :inline="true" :model="boxInfo" class="box-form" label-width="70px">
            <el-form-item label="料盒号">
                <el-input v-model.trim="boxInfo.id" placeholder="料盒号"></el-input>
            </el-form-item>
            <el-form-item label="区域">
                <el-select v-model.trim="boxInfo.area" placeholder="区域" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option label="A" value='A'></el-option>
                    <el-option label="B" value='B'></el-option>
                    <el-option label="C" value='C'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="客户">
                <el-select v-model.trim="boxInfo.supplier" placeholder="客户" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="行">
                <el-input v-model.trim="boxInfo.row" placeholder="行"></el-input>
            </el-form-item>
            <el-form-item label="列">
                <el-input v-model.trim="boxInfo.col" placeholder="列"></el-input>
            </el-form-item>
            <el-form-item label="高度">
                <el-input v-model.trim="boxInfo.height" placeholder="高度"></el-input>
            </el-form-item>
            <el-form-item label="是否在架">
                <el-select v-model.trim="boxInfo.is_on_shelf" placeholder="是否在架" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option label="是" value='1'></el-option>
                    <el-option label="否" value='0'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="料盒类型">
                <el-select v-model.trim="boxInfo.type" placeholder="料盒类型" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option label="标准" value='1'></el-option>
                    <el-option label="非标准" value='2'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="isAdding=true">手动添加料盒</el-button>
                <el-button type="primary" icon="el-icon-coke-edit" @click="isBatchEditSupplier = true" v-if="(user.type === 1 || user.type === 3)">批量修改料盒客户</el-button>
                <el-button type="primary" icon="el-icon-coke-edit" @click="isBatchEditType = true" v-if="(user.type === 1 || user.type === 3)">批量修改料盒类型</el-button>
            </el-form-item>
        </el-form>
        <el-table
                @sort-change="sortChange"
                @selection-change="handleSelectionChange"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    type="selection"
                    width="55">
            </el-table-column>
            <el-table-column
                    label="序号"
                    prop="showId"
                    width="70">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="料盒号"
                    prop="id">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="客户"
                    prop="supplierName">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="类型"
                    prop="typeName">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="所在区域"
                    prop="area">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="行号"
                    prop="row">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="列号"
                    prop="col">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="高度"
                    prop="height">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="是否在架"
                    prop="isOnShelfString">
            </el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <span style="margin-right:10px;cursor:pointer" title="详细" @click="showDetails(scope.row)">
                        <i class="el-icon-coke-list"></i>
                    </span>
                    <span style="margin-right:10px;cursor: pointer;" title="更新料盒在架情况" @click="handleEdit(scope.row)">
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
                    :total="totallyData"
            >
            </el-pagination>
        </div>
        <add-box :isAdding.sync="isAdding" :suppliers="suppliers"></add-box>
        <edit-box :isEditing.sync="isEditing" :editData="editData"></edit-box>
        <box-details></box-details>
        <BatchEditSupplier :is-batch-edit-supplier.sync="isBatchEditSupplier" :selection="selection" :suppliers="suppliers"></BatchEditSupplier>
        <BatchEditType :is-batch-edit-type.sync="isBatchEditType" :selection="selection"></BatchEditType>
    </div>
</template>

<script>
    import {mapGetters} from 'vuex'
    import {deleteBoxUrl, getBoxesUrl, supplierSelectUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import EditBox from "./comp/EditBox";
    import BoxDetails from "./comp/BoxDetails";
    import Bus from './../../../utils/bus'
    import AddBox from "./comp/AddBox";
    import BatchEditSupplier from "./comp/BatchEditSupplier";
    import BatchEditType from "./comp/BatchEditType";
    export default {
        name: "Boxes",
        components: {BatchEditType, BatchEditSupplier, AddBox, BoxDetails, EditBox},
        data(){
            return{
                boxInfo:{
                    id:'',//料盘号
                    area:'',//区域
                    row:'',//行
                    col:'',//列
                    height:'',//高度
                    is_on_shelf:'',//是否在架
                    supplier:'',//供应商
                    type:''//料盒类型
                },
                suppliers:[],
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
                descBy:'',
                selection:[],
                isBatchEditSupplier:false,
                isBatchEditType:false
            }
        },
        created(){
            this.selectSupplier();
        },
        mounted(){
            this.setFilter();
        },
        computed:{
            ...mapGetters([
                'user'
            ])
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
            },
            isBatchEditSupplier:function(val){
                if(val === false){
                    this.setFilter();
                }
            },
            isBatchEditType:function(val){
                if(val === false){
                    this.setFilter();
                }
            }
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:getBoxesUrl,
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
                let copyBoxInfo = {};
                for(let i in this.boxInfo){
                    if(this.boxInfo[i] !== ''){
                        copyBoxInfo[i] = this.boxInfo[i];
                    }
                }
                let isFirst = true;
                let filter = '';
                for(let i in copyBoxInfo){
                    let name = i === 'id'?"material_box.id":i;
                    if(isFirst === true){
                        filter = filter + (name + "=" +  copyBoxInfo[i]);
                        isFirst = false;
                    }else{
                        filter = filter + ("#&#" + name + "=" +  copyBoxInfo[i]);
                    }
                }
                this.filter = filter;
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            initForm:function(){
                this.boxInfo.supplier = '';
                this.boxInfo.is_on_shelf = '';
                this.boxInfo.area = '';
                this.boxInfo.col = '';
                this.boxInfo.height = '';
                this.boxInfo.id = '';
                this.boxInfo.type = '';
                this.boxInfo.row = '';
                this.filter = '';
            },
            handleSelectionChange:function(val){
                this.selection = val;
            },
            handleDelete: function (row) {
                this.$confirm('你正在删除料盒号为“'+row.id+'”的料盒，请确认是否删除', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    showClose:false,
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
                        url: deleteBoxUrl,
                        data: {
                            id: row.id,
                            enabled: 0
                        }
                    };
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
            showDetails:function(row){
                Bus.$emit('showBoxDetails',row);
            },
            selectSupplier:function(){
                let options = {
                    url: supplierSelectUrl,
                    data: {}
                };
                axiosPost(options).then(res => {
                    if(res.data.result === 200){
                        let data = res.data.data.list;
                        data.map((item) => {
                            if(item.enabled === true){
                                this.suppliers.push(item);
                            }
                        });
                    }else{
                        errHandler(res.data)
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            },
            handlePageSize:function(){
                this.pageNo = 1;
                this.select();
            },
            sortChange:function(data){
                let prop = '';
                switch (data.prop) {
                    case "id":
                        prop = "material_box.id";
                        break;
                    case "supplierName":
                        prop = "supplier";
                        break;
                    case "typeName":
                        prop = "type";
                        break;
                    case "isOnShelfString":
                        prop = "is_on_shelf";
                        break;
                    default:
                        prop = data.prop;
                        break;
                }
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
    .boxes{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        .box-form{
            .el-form-item{
                width:20%;
            }
        }
        .block {
            margin-top: 15px;
        }
    }
    @media screen and (max-width: 1600px) {
        .boxes{
            .box-form{
                .el-form-item{
                    width:30%;
                }
            }
        }
    }
    @media screen and (max-width: 1400px) {
        .boxes{
            .box-form{
                .el-form-item{
                    width:30%;
                }
            }
        }
    }
    @media screen and (max-width: 1200px) {
        .boxes{
            .box-form{
                .el-form-item{
                    width:35%;
                }
            }
        }
    }
    @media screen and (max-width: 900px) {
        .boxes{
            .box-form{
                .el-form-item{
                    width:45%;
                }
            }
        }
    }
</style>