<template>
    <el-dialog
            title="修改内容"
            :visible.sync="isEditing"
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
                    :content="resultHtml"
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
    import {updateFAQUrl} from "../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../utils/fetchData";
    import {errHandler} from "../../../../utils/errorHandler";
    export default {
        name: "EditContent",
        components:{
            MyQuillEditor
        },
        props:{
            isEditing:Boolean,
            editData:Object
        },
        data(){
            return{
                resultHtml:'',
                title:'',
                content:'',
                isPending: false,
                isCloseOnModal: false
            }
        },
        watch:{
            isEditing:function(val){
                if(val === true){
                    this.title = this.editData.problem_name;
                    this.resultHtml = this.editData.result_html;
                }else{
                    this.resultHtml = '';
                    this.title = '';
                    this.content = '';
                }
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
                this.$emit("update:isEditing",false);
            },
            submit:function(){
                this.content = this.$refs.myQuillEditor.changeContent.trim();
                this.resultHtml = this.$refs.myQuillEditor.changeHtml.trim();
                if(this.title === ''){
                    this.$alertWarning('标题不能为空');
                    return;
                }
                if(this.content === '' || this.content === null){
                    this.$alertWarning('内容不能为空');
                    return;
                }

                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:updateFAQUrl,
                        data:{
                            id:this.editData.id,
                            title:this.title,
                            content:this.$refs.myQuillEditor.changeContent,
                            resultHtml:this.$refs.myQuillEditor.changeHtml
                        }
                    };
                    axiosPost(options).then(res =>{
                        if(res.data.result === 200){
                            this.$alertSuccess('修改成功');
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