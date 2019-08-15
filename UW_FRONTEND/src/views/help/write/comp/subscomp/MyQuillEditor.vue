<template>
    <div>
        <el-upload
                accept="image/jpeg,image/png,image/jpg"
                :before-upload="onBeforeUploadImage"
                class="avatar-uploader"
                :show-file-list="false"
                :action="uploadUrl"
                :http-request="uploadImg">
        </el-upload>
        <quill-editor
                ref="myQuillEditor"
                @change="onEditorChange"
                v-model="editContent"
                :options="editorOption">
        </quill-editor>
    </div>
</template>

<script>
    import {mapGetters} from 'vuex'
    import Quill from 'quill';
    import {quillEditor} from 'vue-quill-editor'
    import ImageResize from 'quill-image-resize-module';
    Quill.register('modules/ImageResize', ImageResize);

    //quill编辑器的font-family
    let fonts = ['SimSun', 'SimHei','Microsoft-YaHei','KaiTi','FangSong','Arial','Times-New-Roman','sans-serif'];
    let Font = Quill.import('formats/font');
    Font.whitelist = fonts;
    Quill.register(Font, true);

    //quill编辑器的font-size
    let sizes = ['10px','11px','12px','14px','16px','18px','20px','24px'];
    let Size = Quill.import('attributors/style/size');
    Size.whitelist = sizes;
    Quill.register(Size, true);

    import 'quill/dist/quill.core.css'
    import 'quill/dist/quill.snow.css'
    import 'quill/dist/quill.bubble.css'
    import {uploadPhotoUrl} from "../../../../../plugins/globalUrl";
    import axios from './../../../../../plugins/http'
    import {errHandler} from "../../../../../utils/errorHandler";
    export default {
        name: "QuillEditor",
        props:{
            content:String
        },
        components:{
            quillEditor
        },
        computed:{
            ...mapGetters(['token'])
        },
        mounted(){
            if(this.content !== ''){
                this.editContent = this.htmlEscape(this.content);
            }
        },
        watch:{
            content:function (val) {
                this.editContent = this.htmlEscape(this.content);
            }
        },
        data(){
            return{
                editorOption:{
                    modules:{
                        ImageResize: {
                            modules: [ 'Resize', 'DisplaySize']
                        },
                        toolbar: {
                            container: [
                                ['bold', 'italic', 'underline', 'strike'],
                                ['blockquote', 'code-block'],
                                [{ 'header': 1 }, { 'header': 2 }],
                                [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                                [{ 'script': 'sub'}, { 'script': 'super' }],
                                [{ 'indent': '-1'}, { 'indent': '+1' }],
                                [{ 'direction': 'rtl' }],
                                [{ 'size': sizes}],
                                [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
                                [{ 'color': [] }, { 'background': [] }],
                                [{ 'font':fonts}],
                                [{ 'align': [] }],
                                ['clean'],
                                ['image'],
                            ],
                            handlers: {
                                'image': function (value) {
                                    if (value) {
                                        document.querySelector('.avatar-uploader input').click();
                                    } else {
                                        this.quill.format('image', false);
                                    }
                                }
                            }
                        }
                    },
                    theme:'snow'
                },
                changeContent:'',
                changeHtml:'',
                uploadUrl:uploadPhotoUrl,
                editContent:''
            }
        },
        methods:{
            //html转义
            htmlEncode:function(a){
                a = "" + a;
                return a.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&apos;");
            },
            //html反转义
            htmlEscape: function (a) {
                a = "" + a;
                return a.replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&").replace(/&quot;/g, '"').replace(/&apos;/g, "'");
            },
            onEditorChange:function({ editor, html, text }){
                this.changeContent = text;
                this.changeHtml = this.htmlEncode(html);
            },
            uploadImg:function(param){
                let formData = new FormData();
                formData.append('file',param.file);
                formData.append('#TOKEN#',this.token);
                axios.post(uploadPhotoUrl, formData).then(res => {
                    if(res.data.result === 200){
                        let quill = this.$refs.myQuillEditor.quill;
                        let length = quill.getSelection().index;
                        // 插入图片  res.info为服务器返回的图片地址
                        quill.insertEmbed(length, 'image',window.g.API_URL+res.data.data);
                    }else{
                        errHandler(res.data);
                    }
                }).catch(err => {
                    console.log(err);
                })
            },
            onBeforeUploadImage(file) {
                const isIMAGE = file.type === 'image/jpeg' || 'image/jpg' || 'image/png'
                const isLt1M = file.size / 1024 / 1024 < 1
                if (!isIMAGE) {
                    this.$message.error('上传文件只能是图片格式!')
                }
                if (!isLt1M) {
                    this.$message.error('上传文件大小不能超过 1MB!')
                }
                return isIMAGE && isLt1M
            }
        }
    }
</script>

<style scoped lang="scss">
    @import "../../../../../../public/css/quil-font.css";
    .avatar-uploader{
        display:none;
    }
</style>