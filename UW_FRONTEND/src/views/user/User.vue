<template>
    <div class="user" v-loading="isLoading">
        <el-form :inline="true" :model="userInfo" class="user-form">
            <el-form-item label="用户名">
                <el-input v-model.trim="userInfo.uid" placeholder="用户名"></el-input>
            </el-form-item>
            <el-form-item label="是否启用">
                <el-select v-model.trim="userInfo.enabled" placeholder="是否启用" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option label="禁用" value=0></el-option>
                    <el-option label="启用" value=1></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="用户类型">
                <el-select v-model.trim="userInfo.type" placeholder="用户类型" value="">
                    <el-option label="不限" selected="selected"  value=''></el-option>
                    <el-option v-for="item in userTypeList" :value="item.id" :label="item.name" :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item style="width:100%;">
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
                <el-button type="primary" icon="el-icon-plus" @click="isAdding=true">添加用户</el-button>
            </el-form-item>
        </el-form>
        <el-table
                @sort-change="sortChange"
                :data="tableData"
                style="width:100%">
            <el-table-column
                    label="序号"
                    prop="showId"
                    width="70">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="用户名"
                    prop="uid">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="用户描述"
                    prop="name">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="用户类型"
                    prop="typeString">
            </el-table-column>
            <el-table-column
                    sortable = "custom"
                    label="是否启用"
                    prop="enabledString">
            </el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <span style="cursor: pointer;" title="编辑" @click="handleEdit(scope.row)">
                        <i class="el-icon-coke-edit"></i>
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
        <add-user :isAdding.sync="isAdding" :user-type-list="userTypeList"></add-user>
        <edit-user :isEditing.sync="isEditing" :editData="editData" :user-type-list="userTypeList"></edit-user>
    </div>
</template>

<script>
    import {axiosPost} from "../../utils/fetchData";
    import {userSelectUrl, userTypeUrl} from "../../plugins/globalUrl";
    import {errHandler} from "../../utils/errorHandler";
    import EditUser from "./comp/EditUser";
    import AddUser from "./comp/AddUser";

    export default {
        name: "User",
        components: {AddUser, EditUser},
        data(){
            return{
                userInfo:{
                    uid:'',
                    enabled:'',
                    type:''
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
                userTypeList:[],
                ascBy:'',
                descBy:''
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
            }
        },
        created(){
            this.getUserType();
        },
        mounted(){
            this.setFilter();
        },
        methods:{
            getUserType:function(){
                axiosPost({
                    url: userTypeUrl
                }).then(res => {
                    if(res.data.result === 200){
                        this.userTypeList = res.data.data;
                    }else{
                        errHandler(res.data);
                    }
                })
            },
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url:userSelectUrl,
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
                let copyUserInfo = {};
                for(let i in this.userInfo){
                    if(this.userInfo[i] !== ''){
                        copyUserInfo[i] = this.userInfo[i];
                    }
                }
                let isFirst = true;
                let filter = '';
                for(let i in copyUserInfo){
                    if(isFirst === true){
                        filter = filter + (i + "=" +  copyUserInfo[i]);
                        isFirst = false;
                    }else{
                        filter = filter + ("#&#" + i + "=" +  copyUserInfo[i]);
                    }
                }
                this.filter = filter;
                this.pageNo = 1;
                this.pageSize = 20;
                this.select();
            },
            initForm:function(){
                this.userInfo.enabled = '';
                this.userInfo.type = '';
                this.userInfo.uid = '';
                this.filter = '';
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
                switch (data.prop) {
                    case "typeString":
                        prop = "type";
                        break;
                    case "enabledString":
                        prop = "enabled";
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
    .user{
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