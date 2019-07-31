<template>
    <div class="header" :class="{ topCollapsed: isCollapse }">
        <el-row type="flex" justify="space-between">
            <el-col style="display:flex;align-items:center;min-width:210px;">
                <i :class="[isCollapse ? 'el-icon-s-unfold': 'el-icon-s-fold']" @click="toggleSiderBar"></i>
                <span class="active-name">{{activeName}}</span>
            </el-col>
            <el-col>
                <el-row type="flex" class="row-right" justify="end" style="margin-right:-15px;min-width:400px;">
                    <el-col :span="2" style="min-width:100px;">
                        <a>{{user.name}}</a>
                    </el-col>
                    <el-col :span="2" style="min-width:50px;">
                        <i class="el-icon-coke-power" title="退出" @click.prevent="logout"></i>
                    </el-col>
                </el-row>
            </el-col>
        </el-row>
    </div>
</template>

<script>
    import {mapGetters,mapActions} from 'vuex'
    import {logoutUrl} from "../plugins/globalUrl";
    import {axiosPost} from "../utils/fetchData";
    import {errHandler} from "../utils/errorHandler";

    export default {
        name: "PageHeader",
        data() {
            return {
                isPending:false,
            }
        },
        computed: {
            ...mapGetters(['isCollapse', 'user','activeName'])
        },
        methods: {
            ...mapActions(['setToken','setIsCollapse']),
            toggleSiderBar:function(){
                this.setIsCollapse(!this.isCollapse);
            },
            logout: function () {
                if(!this.isPending){
                    this.isPending = true;
                    let options = {
                        url:logoutUrl,
                        data:{}
                    };
                    axiosPost(options).then(res => {
                        if(res.data.result === 200){
                            this.setToken('');
                            window.sessionStorage.clear();
                            this.$router.replace('/login');
                        }else{
                            errHandler(res.data);
                        }
                    }).catch(err =>{
                        console.log(err);
                        this.$alertError('请求超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .header {
        overflow: hidden;
        position: fixed;
        right: 0;
        width: calc(100% - 180px);
        background: #4BC0C0;
        color: #fff;
        padding: 0 26px 0 15px;
        z-index: 4;
        box-sizing: border-box;
        box-shadow: 0 0 20px rgba(0,0,0,0.15);
        transition: all 0.5s;
        a {
            font-size:18px;
            line-height: 60px;
            font-weight: bold;
            letter-spacing: 1px;
        }
        i {
            font-size:24px;
            line-height: 60px;
            transition: all 0.5s;
            &:hover {
                cursor: pointer;
            }
        }
        .logout {
            text-decoration: none;
        }
        .active-name{
            font-size:18px;
            font-weight:bold;
            line-height: 60px;
            margin-left:20px;
        }
    }
    .row-right > div {
        text-align: center;
    }
    .header.topCollapsed {
        width: calc(100% - 64px);
    }
</style>