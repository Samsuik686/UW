<template>
    <div class="uwMaterial" v-loading="isLoading">
        <el-form :inline="true" :model="uwMaterialInfo" class="uwMaterial-form">
            <el-form-item label="料号">
                <el-input v-model.trim="uwMaterialInfo.no" placeholder="料号"></el-input>
            </el-form-item>
            <el-form-item label="规格">
                <el-input v-model.trim="uwMaterialInfo.specification" placeholder="规格"></el-input>
            </el-form-item>
            <el-form-item label="物料类型号">
                <el-input v-model.trim="uwMaterialInfo.id" placeholder="物料类型号"></el-input>
            </el-form-item>
            <el-form-item label="供应商">
                <el-select v-model.trim="uwMaterialInfo.supplier" placeholder="供应商" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-search" @click="setFilter" :disabled="checked">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="isAdding=true">新增物料</el-button>
                <el-button type="primary" icon="el-icon-download" @click="isExporting=true">导出物料列表</el-button>
                <el-button type="primary" icon="el-icon-upload" @click="isUploading=true">导入物料类型表</el-button>
                <el-button type="primary" icon="el-icon-delete" @click="deleteById">批量删除</el-button>
            </el-form-item>
        </el-form>
        <el-form @submit.native.prevent>
            <el-form-item>
                <el-switch v-model="checked"></el-switch>
                仅显示入库日期超过
                    <el-input
                            type="text"
                            size="small"
                            style="width:80px;margin:0 5px;"
                            v-model="day"/>
                天
            </el-form-item>
        </el-form>
        <el-table
                @selection-change="handleSelectionChange"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    type="selection"
                    width="55">
            </el-table-column>
            <el-table-column type="expand">
                <template slot-scope="props">
                    <entity-details :type="props.row.id"></entity-details>
                </template>
            </el-table-column>
            <el-table-column
                    label="序号"
                    prop="showId"
                    width="70">
            </el-table-column>
            <el-table-column
                    label="客户专用码"
                    prop="supplier">
            </el-table-column>
            <el-table-column
                    label="物料类型号"
                    min-width="90"
                    prop="id">
            </el-table-column>
            <el-table-column
                    min-width="140"
                    label="料号"
                    prop="no">
            </el-table-column>
            <el-table-column
                    min-width="140"
                    label="供应商"
                    prop="supplierName">
            </el-table-column>
            <el-table-column
                    label="规格"
                    prop="specification">
            </el-table-column>
            <el-table-column
                    label="厚度"
                    prop="thickness">
            </el-table-column>
            <el-table-column
                    label="直径"
                    prop="radius">
            </el-table-column>
            <el-table-column
                    label="数量"
                    prop="quantity">
            </el-table-column>
            <el-table-column label="操作" min-width="100">
               <template slot-scope="scope">
                    <span style="margin-right:10px;cursor:pointer" title="收发记录" @click="showDetails(scope.row)">
                        <i class="el-icon-coke-card"></i>
                    </span>
                    <span style="margin-right:10px;cursor: pointer;" title="更新物料类型" @click="handleEdit(scope.row)">
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
        <add-material :isAdding.sync="isAdding" :suppliers="suppliers"></add-material>
        <edit-material :is-editing.sync="isEditing" :edit-data="editData"></edit-material>
        <export-material :isExporting.sync="isExporting" :suppliers="suppliers"></export-material>
        <upload-material :isUploading.sync="isUploading" :suppliers="suppliers"></upload-material>
        <i-o-details></i-o-details>
    </div>
</template>

<script>
    import Bus from './../../../utils/bus'
    import {
        deleteByIdsUrl,
        getOverdueMaterialUrl,
        materialCountUrl, materialUpdateUrl,
        supplierSelectUrl
    } from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import AddMaterial from "./comp/AddMaterial";
    import ExportMaterial from "./comp/ExportMaterial";
    import UploadMaterial from "./comp/UploadMaterial";
    import EntityDetails from "./comp/EntityDetails";
    import IODetails from './comp/IODetails'
    import EditMaterial from "./comp/EditMaterial";
    export default {
        name: "UwMaterial",
        components: {EditMaterial, EntityDetails, UploadMaterial, ExportMaterial, AddMaterial,IODetails},
        data(){
            return{
                uwMaterialInfo:{
                    no:'',//料号
                    specification:'',//规格
                    id:'',//物料类型号
                    supplier:''//供应商
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
                isExporting:false,
                isUploading:false,
                editData:{},
                selection:[],
                checked:false,
                day:90
            }
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
            checked:function (val) {
                this.pageNo = 1;
                this.pageSize = 20;
                if(val === true){
                    this.getOverdueMaterial();
                }else{
                    this.setFilter();
                }
            }
        },
        created(){
            this.selectSupplier();
        },
        mounted(){
            this.setFilter();
        },
        methods:{
            select:function(){
                if(this.checked === true){
                    this.getOverdueMaterial();
                    return;
                }
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:materialCountUrl,
                        data:{
                            pageNo:this.pageNo,
                            pageSize: this.pageSize,
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
                let copyUwMaterialInfo = {};
                for(let i in this.uwMaterialInfo){
                    if(this.uwMaterialInfo[i] !== ''){
                        copyUwMaterialInfo[i] = this.uwMaterialInfo[i];
                    }
                }
                let isFirst = true;
                let filter = '';
                for(let i in copyUwMaterialInfo){
                    if(isFirst === true){
                        if(i === "id"){
                            filter = filter + (i + "=" +  copyUwMaterialInfo[i]);
                        }else{
                            filter = filter + (i + "like" +  copyUwMaterialInfo[i]);
                        }
                        isFirst = false;
                    }else{
                        if(i === "id"){
                            filter = filter + ("#&#" + i + "=" +  copyUwMaterialInfo[i]);
                        }else{
                            filter = filter + ("#&#" + i + "like" +  copyUwMaterialInfo[i]);
                        }
                    }
                }
                this.filter = filter;
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            initForm:function(){
                this.uwMaterialInfo.id = '';
                this.uwMaterialInfo.no = '';
                this.uwMaterialInfo.specification = '';
                this.uwMaterialInfo.supplier = '';
                this.filter = '';
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
            deleteById:function(){
                if (this.selection.length === 0) {
                    this.$alertWarning('请选择你要删除的物料');
                    return;
                }
                let filter = '';
                this.selection.map((item, index) => {
                    if (index !== this.selection.length - 1) {
                        filter = filter + item.id + ',';
                    } else {
                        filter = filter + item.id;
                    }
                });
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: deleteByIdsUrl,
                        data: {
                            filter: filter
                        }
                    };
                    axiosPost(options).then(res => {
                        this.isPending = false;
                        if (res.data.result === 200) {
                            this.$alertSuccess(res.data.data);
                            this.setFilter();
                        } else {
                            errHandler(res.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                        this.isPending = false;
                    })
                }
            },
            handleSelectionChange:function(val){
                this.selection = val;
            },
            getOverdueMaterial: function () {
                if(this.checked === false){
                    this.$alertWarning('请先勾选');
                    return;
                }
                if (this.day === '') {
                    this.$alertWarning('天数不能为空');
                    return;
                }
                if (!this.isNumber(this.day)) {
                    this.$alertWarning('天数必须为非负整数');
                    return;
                }
                let options = {
                    url: getOverdueMaterialUrl,
                    data: {
                        day: this.day,
                        pageNo:this.pageNo,
                        pageSize:this.pageSize
                    }
                };
                if (!this.isPending) {
                    this.isPending = true;
                    this.isLoading = true;
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
            isNumber: function (num) {
                let val = num;
                let reg = /^\+?(0|[1-9][0-9]*)$/;
                if (val !== "") {
                    return reg.test(val);
                }
            },
            showDetails:function(row){
                Bus.$emit('showIODetails',row);
            },
            handleEdit:function(row){
                this.editData = row;
                this.isEditing = true;
            },
            handleDelete: function (row) {
                this.$confirm('你正在删除料号为“'+row.no+'”的物料，请确认是否删除', {
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
                        url: materialUpdateUrl,
                        data:JSON.parse(JSON.stringify(row))
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
            }
        }
    }
</script>

<style scoped lang="scss">
    .uwMaterial{
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