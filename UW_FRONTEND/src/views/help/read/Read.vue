<template>
    <div class="read">
        <div class="left">
            <el-input
                    @keyup.enter.native="select"
                    v-model="keyword"
                    placeholder="请输入内容"
                    prefix-icon="el-icon-search"
                    style="margin-bottom:5px;"
                    type="text" >
            </el-input>
            <div
                    v-for="(item,index) in titles"
                    @click="setActiveIndex(index)"
                    :class="{activeItem:activeIndex === index}"
                    :key="item.id"
                    class="item">
                {{item.problem_name}}
            </div>
        </div>
        <div class="right">
            <div v-html="result" class="ql-editor"></div>
        </div>
    </div>
</template>

<script>
    import {selectFAQUrl} from "../../../plugins/globalUrl";
    import {axiosPost} from "../../../utils/fetchData";
    import {errHandler} from "../../../utils/errorHandler";
    export default {
        name: "Read",
        data(){
            return{
                isPending:false,
                titles:[],
                result:'',
                activeIndex:0,
                keyword:''
            }
        },
        mounted(){
            this.select();
        },
        watch:{
          activeIndex:function(val){
              if(val !== ''){
                  this.result = this.htmlEscape(this.titles[val].result_html);
              }
          }
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:selectFAQUrl,
                        data:{
                            keyword: this.keyword
                        }
                    };
                    axiosPost(options).then(res =>{
                        if(res.data.result === 200){
                            this.titles = res.data.data;
                            if(this.titles.length > 0){
                                this.activeIndex = 0;
                                this.result = this.htmlEscape(this.titles[0].result_html);
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
            setActiveIndex:function(index){
                this.activeIndex = index;
            },
            htmlEscape: function (a) {
                a = "" + a;
                return a.replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&").replace(/&quot;/g, '"').replace(/&apos;/g, "'");
            },
        }
    }
</script>

<style scoped lang="scss">
    @import '../../../../public/css/quill.core.css';
    @import '../../../../public/css/quill.snow.css';
    @import '../../../../public/css/quill.bubble.css';
    @import "../../../../public/css/quil-font.css";
    .read{
        display:flex;
        width:100%;
        height:100%;
        .left{
            width:240px;
            height:100%;
            box-sizing:border-box;
            padding: 20px 10px 0 0;
            border-right:1px solid #eee;
            overflow-y:auto;
            .item{
                margin-top:10px;
                cursor:pointer;
                text-align:left;
                word-wrap: break-word;
                word-break: normal;
            }
            .activeItem{
                color:#4BC0C0;
            }
        }
        .right{
            width:calc(100% - 240px);
            height:100%;
            box-sizing:border-box;
            padding: 20px 0 0 20px;
            overflow-y: auto;
        }
    }

</style>