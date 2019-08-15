<template>
    <el-dialog
            title="添加内容"
            :visible.sync="isAdding"
            :show-close="isCloseOnModal"
            :close-on-click-modal="isCloseOnModal"
            :close-on-press-escape="isCloseOnModal"
            width="95%">
        <el-form>
            <el-form-item label="标题" label-position="top">
                <el-input v-model.trim="title" size="large"></el-input>
            </el-form-item>
            <el-form-item label="内容" label-position="top" style="margin-bottom:0">
            </el-form-item>
            <my-quill-editor
                    ref="myQuillEditor"
                    class="quill-editor"
                    :content="content"
            ></my-quill-editor>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel" >取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
    </el-dialog>
</template>

<script>
    import MyQuillEditor from "./subscomp/MyQuillEditor";
    import {uploadFAQUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "AddContent",
        components:{
            MyQuillEditor
        },
        props:{
            isAdding:Boolean
        },
        data(){
            return{
                title:'',
                content:'',
                resultHtml:'',
                isPending: false,
                isCloseOnModal: false
            }
        },
        methods:{
            cancel:function () {
                this.title = '';
                this.content='';
                this.resultHtml = '';
                this.$refs.myQuillEditor.editContent = '';
                this.$refs.myQuillEditor.changeContent = '';
                this.$refs.myQuillEditor.changeHtml = '';
                this.$emit("update:isAdding",false);
            },
            submit:function(){
                this.content = this.$refs.myQuillEditor.changeContent;
                this.resultHtml = this.$refs.myQuillEditor.changeHtml;
                if(this.title === ''){
                    this.$alertWarning('标题不能为空');
                    return;
                }
                if(this.content === ''){
                    this.$alertWarning('内容不能为空');
                    return;
                }

                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:uploadFAQUrl,
                        data:{
                            title:this.title,
                            content:this.content,
                            resultHtml:this.resultHtml
                        }
                    };
                    axiosPost(options).then(res =>{
                        if(res.data.result === 200){
                            this.$alertSuccess('添加成功');
                            this.cancel();
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
            }
        }
    }
</script>

<style scoped>

</style>