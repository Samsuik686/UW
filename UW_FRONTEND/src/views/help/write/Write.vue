<template>
    <div class="write">
        <el-form :inline="true">
            <el-form-item>
                <el-select v-model.trim="index" placeholder="标题" value="" filterable>
                    <el-option label="请选择" selected="selected"  value=''></el-option>
                    <el-option  v-for="(item,index) in titles" :label="item.problem_name" :value='index' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="el-icon-edit" @click="handleEdit">编辑</el-button>
                <el-button type="primary" icon="el-icon-delete" @click="handleDelete">删除</el-button>
                <el-divider direction="vertical"></el-divider>
                <el-button type="primary" icon="el-icon-plus" @click="isAdding = true">新增</el-button>
            </el-form-item>
        </el-form>
        <div class="result">
            <div v-html="result" class="ql-editor"></div>
        </div>
        <add-content :is-adding.sync="isAdding"></add-content>
        <edit-content :is-editing.sync="isEditing" :editData = "editData"></edit-content>
    </div>
</template>

<script>
    import AddContent from "./comp/AddContent";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    import {deleteFAQUrl, selectFAQUrl} from "../../../plugins/globalUrl";
    import EditContent from "./comp/EditContent";
    export default {
        name: "Write",
        data(){
            return{
                index:'',
                titles:[],
                editData:{},
                result:'',
                isAdding:false,
                isEditing:false,
                isPending:false
            }
        },
        components:{EditContent, AddContent},
        watch:{
            isAdding:function(val){
                if(val === false){
                    this.select();
                }
            },
            isEditing:function(val){
                if(val === false){
                    this.select();
                }
            },
            index:function(val){
                if(val !== ''){
                    this.result = this.htmlEscape(this.titles[val].result_html);
                }else{
                    this.result = '';
                }
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
                        url:selectFAQUrl,
                        data:{
                        }
                    };
                    axiosPost(options).then(res =>{
                        if(res.data.result === 200){
                            this.titles = res.data.data;
                            if(this.titles.length > 0){
                                if(this.index !== ''){
                                    this.result = this.htmlEscape(this.titles[this.index].result_html);
                                }
                            }else{
                                this.result = '';
                            }
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
            handleEdit:function(){
                if(this.index === ''){
                    this.$alertWarning('请选择标题');
                    return;
                }
                this.editData = this.titles[this.index];
                this.isEditing = true;
            },
            handleDelete:function(){
                if(this.index === ''){
                    this.$alertWarning('请选择标题');
                    return;
                }
                this.editData = this.titles[this.index];
                this.$confirm('你正在删除标题名为“'+this.editData.problem_name+'”的帮助内容，请确认是否删除', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    showClose:false,
                    center: false
                }).then((action) => {
                    if (action === "confirm") {
                        this.submitDelete();
                    }
                }).catch(() => {
                    this.$alertInfo("已取消删除");
                });
            },
            submitDelete:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:deleteFAQUrl,
                        data:{
                            id:this.editData.id
                        }
                    };
                    axiosPost(options).then(res =>{
                        if(res.data.result === 200){
                            this.$alertSuccess('删除成功');
                            this.index = '';
                            this.isPending = false;
                            this.select();
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
            htmlEscape: function (a) {
                a = "" + a;
                return a.replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&").replace(/&quot;/g, '"').replace(/&apos;/g, "'");
            }
        }
    }
</script>

<style scoped lang="scss">
    @import '../../../../public/css/quill.core.css';
    @import '../../../../public/css/quill.snow.css';
    @import '../../../../public/css/quill.bubble.css';
    @import "../../../../public/css/quil-font.css";
    .write{
        width:100%;
        .result{
            width:100%;
            box-sizing:border-box;
            padding:20px 20px;
            border:1px solid #ddd;
            border-radius:6px;
        }
    }
</style>