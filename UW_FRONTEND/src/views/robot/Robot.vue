<template>
    <div class="robot" v-loading="isLoading">
        <el-form :inline="true" :model="robotInfo" class="robot-form">
            <el-form-item label="叉车ID">
                <el-input v-model.trim="robotInfo.id" placeholder="叉车ID"></el-input>
            </el-form-item>
            <el-form-item label="状态">
                <el-select v-model.trim="robotInfo.status" placeholder="状态" value="">
                    <el-option label="闲置" value='0'></el-option>
                    <el-option label="忙碌" value='1'></el-option>
                    <el-option label="错误" value='3'></el-option>
                    <el-option label="充电" value='4'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="是否启用">
                <el-select v-model.trim="robotInfo.enabled" placeholder="是否启用" value="">
                    <el-option label="系统停用" value='0'></el-option>
                    <el-option label="手动停用" value='1'></el-option>
                    <el-option label="启用" value='2'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
            </el-form-item>
        </el-form>
        <el-form :inline="true">
            <el-button type="primary" size="small" @click="selectAll">
                {{checkRobots.length !== robots.length ? '全选' : '取消全选'}}
            </el-button>
            <el-button type="info" size="small" @click="setEnable(checkRobots.toString(), 2)">禁用所选</el-button>
            <el-button type="primary" size="small" @click="setEnable(checkRobots.toString(), 1)">启用所选</el-button>
            <el-divider direction="vertical"></el-divider>
            <el-button
                    @click="setPause"
                    v-if="robots.length>0"
                    size="small"
                    :type="robots[0].pause === true?'primary':'info'">
                {{robots[0].pause === true?'运行所有叉车' : '暂停所有叉车'}}
            </el-button>
        </el-form>
        <div v-if="robots.length > 0" style="margin-top:20px;">
            <el-row :gutter="10">
                <el-col :xs="12" :sm="8" :md="6" :lg="6" :xl="4"
                        v-for="(item, index) in robots" :key="item.id" style="margin-bottom:20px;">
                    <el-card
                            shadow="hover"
                            class="robot-card"
                            @click.native="toggleSelected(item.id)"
                            :class="[item.warn === 255 ? '':'warn-card',item.error === 255 ? '' : 'danger-card']"
                    >
                        <span style="display:block">
                            <i :class="checkRobots.indexOf(item.id) === -1  ? 'el-icon-coke-check-empty' : 'el-icon-coke-check' " style="font-size:28px;"></i>
                        </span>
                        <el-alert
                                v-if="item.error !== 255"
                                :title="item.errorString"
                                type="error"
                                show-icon>
                        </el-alert>
                        <el-alert
                                v-if="item.loadException === true"
                                :title="item.loadExceptionString"
                                type="error"
                                show-icon>
                        </el-alert>
                        <el-alert
                                v-if="item.warn !== 255"
                                :title="item.warnString"
                                type="error"
                                show-icon>
                        </el-alert>
                        <img style="width:85%;align-self:center" src="./../../assets/robot.jpg">
                        <div class="text-box">
                            <span>ID: {{item.id}}</span>
                            <span>状态: {{item.statusString}}</span>
                        </div>
                        <div class="text-box">
                            <span>X: {{item.x}}</span>
                            <span>Y: {{item.y}}</span>
                        </div>
                        <div class="img-box">
                            <el-progress
                                    :color="item.battery < 30 ? '#F56C6C' : ''"
                                    :text-inside="true"
                                    :stroke-width="20"
                                    :percentage="item.battery">
                            </el-progress>
                        </div>
                        <el-divider></el-divider>
                        <el-button
                                style="width:100%"
                                @click.stop="setEnable(item.id, item.enabled)"
                                :type="item.enabled === 2 ? 'info' : 'primary'">
                            {{item.enabled === 2 ? '点击禁用' : '点击启用'}}
                        </el-button>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script>
    import {robotPauseUrl, robotSelectUrl, robotSwitchUrl} from "../../plugins/globalUrl";
    import {axiosPost} from "../../utils/fetchData";
    import {errHandler} from "../../utils/errorHandler";

    export default {
        name: "Robots",
        data(){
            return{
                robotInfo:{
                   id:'',//叉车ID
                   status:'',//状态
                   enabled:''//是否启用
                },
                query:[],
                robots:[],
                checkRobots:[],
                isPending:false,
                isLoading:false,
                filter:'',
                myTimeOut: '',
                isTimeOut: false
            }
        },
        beforeDestroy(){
            this.clearMyTimeOut();
        },
        mounted(){
            this.setMyTimeOut();
        },
        methods:{
            select:function(){
                if(!this.isPending){
                    this.isPending = true;
                    this.isLoading = true;
                    let options = {
                        url: robotSelectUrl,
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            let data = res.data.data;
                            //filter
                            for (let i = 0; i < this.query.length; i++) {
                                let arr = this.query[i];
                                data = data.filter((item) => {
                                    if (item[arr[0]].toString() === arr[1].toString()) {
                                        return true;
                                    }
                                });
                            }
                            this.robots = data;
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
                this.query = [];
                for(let i in this.robotInfo){
                    if(this.robotInfo[i] !== ''){
                        let arr = [];
                        arr.push(i);
                        arr.push(this.robotInfo[i]);
                        this.query.push(arr);
                    }
                }
                this.select();
            },
            initForm:function(){
                this.robotInfo.enabled = '';
                this.robotInfo.status = '';
                this.robotInfo.id = '';
                this.filter = '';
            },
            setMyTimeOut:function(){
                if (this.isTimeOut === true) {
                    this.clearMyTimeOut();
                }
                this.isTimeOut = true;
                let that = this;
                this.select();
                this.myTimeOut = setInterval(function () {
                    that.select();
                }, 3000);
            },
            clearMyTimeOut:function () {
                this.isTimeOut = false;
                clearTimeout(this.myTimeOut);
                this.myTimeOut = null;
            },
            selectAll: function () {
                if (this.checkRobots.length === 0) {
                    this.checkRobots = this.robots.map((item) => {
                        return item.id;
                    })
                } else {
                    this.checkRobots = [];
                }
            },
            setEnable: function (id, enabled) {
                let thisEnabled = enabled === 2 ? 1 : 2;
                if (!this.isPending && id !== "") {
                    this.isPending = true;
                    let options = {
                        url: robotSwitchUrl,
                        data: {
                            id: id,
                            enabled: thisEnabled
                        }
                    };
                    axiosPost(options).then(res => {
                        if (res.data.result === 200) {
                            this.$alertSuccess((thisEnabled === 1 ? "停用" : "启用") + "成功");
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
            setPause:function(){
                let thisPause = this.robots[0].pause === true ? 1 : 0;
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: robotPauseUrl,
                        data: {
                            pause: thisPause
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess((thisPause === 0 ? "暂停" : "启用") + "成功");
                        } else {
                            errHandler(response.data);
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            },
            toggleSelected: function (id) {
                let index = this.checkRobots.indexOf(id);
                if (index > -1) {
                    this.checkRobots.splice(index, 1)
                } else {
                    this.checkRobots.push(id)
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .robot{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
    }
    .warn-card{
        border-color:#E6A23C ;
        color:#E6A23C;
    }
    .danger-card{
        border-color:#F56C6C;
        color:#F56C6C;
    }
    .robot-card{
        display:flex;
        flex-direction:column;
        cursor:pointer;
        .text-box{
            margin-bottom:10px;
            span{
                display: inline-block;
                width:50%;
                text-align:left;
            }
        }
        .img-box{
            margin-bottom:10px;
            //display:flex;
        }
    }
</style>