<template>
    <div class="show-robot" v-if="isShow">
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <span style="font-size:18px;font-weight:bold;color:#fff">叉车异常</span>
                <span style="float: right; padding: 3px 0;cursor:pointer;font-weight:bold;color:#fff;" @click="closePanel">
                    <i class="el-icon-close" style="font-size:22px;"></i>
                </span>
            </div>
            <div v-for="item in robotStatus" :key="item.id" class="item-text">
                {{'叉车 ' + item.id + ' : ' + item.str}}
            </div>
        </el-card>
    </div>
</template>

<script>
    import {robotSelectUrl} from "../plugins/globalUrl";
    import {axiosPost} from "../utils/fetchData";
    import {errHandler} from "../utils/errorHandler";

    export default {
        name: "ShowRobotStatus",
        data(){
            return{
                isPending:false,
                isShow:false,
                robotStatus:[],
                myTimeOut: '',
                isTimeOut: false,
                isTip:false,
                isOperation:false
            }
        },
        beforeDestroy(){
            this.clearMyTimeOut();
        },
        mounted(){
            //this.getRobots();
            //this.setMyTimeOut();
        },
        methods:{
            getRobots:function(){
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url: robotSelectUrl,
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            let robotStatus = [];
                            let data = res.data.data;
                            data.map((item) => {
                                let str = '';
                                if(item.errorString !== ''){
                                    str = item.errorString;
                                }else if(item.loadExceptionString !== ''){
                                    str = item.loadExceptionString;
                                }else if(item.warnString !== ''){
                                    str = item.warnString;
                                }else{
                                    str = '';
                                }
                                if(str !== ''){
                                    robotStatus.push({
                                        id:item.id,
                                        str:str
                                    })
                                }
                            });
                            if(robotStatus.length > 0){
                                if(this.isOperation === false){
                                    this.robotStatus = robotStatus;
                                    this.isShow = true;
                                }else{
                                    this.robotStatus = [];
                                    this.isShow = false;
                                }
                            }else{
                                this.robotStatus = [];
                                this.isShow = false;
                                this.isOperation = false;
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
            setMyTimeOut: function () {
                if (this.isTimeOut === true) {
                    this.clearMyTimeOut();
                }
                this.isTimeOut = true;
                let that = this;
                this.myTimeOut = setInterval(function () {
                    that.getRobots();
                }, 5000);
            },
            clearMyTimeOut: function () {
                this.isTimeOut = false;
                clearTimeout(this.myTimeOut);
                this.myTimeOut = null;
            },
            closePanel:function(){
                this.isTip = true;
                this.$confirm('请尽快解决叉车问题', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    showClose:false,
                    center: false
                }).then((action) => {
                    if (action === "confirm") {
                        this.setOperation();
                    }
                }).catch(() => {
                    this.isTip = false;
                });
            },
            setOperation:function(){
                this.isTip = false;
                this.isShow = false;
                this.isOperation = true;
            }
        }
    }
</script>

<style scoped lang="scss">
    .show-robot{
        position:absolute;
        top:10px;
        right:10px;
        z-index:9999;
        .box-card {
            width:320px;
            background: #FF9900;
            .clearfix:before,
            .clearfix:after {
                display: table;
                content: "";
            }
            .clearfix:after {
                clear: both
            }
            .item-text{
                font-size:16px;
                margin-bottom: 18px;
                color:#fff;
            }
        }
    }
</style>