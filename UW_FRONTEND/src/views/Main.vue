<template>
    <el-container class="page">
        <PageAside></PageAside>
        <div class="con-wrap" :class="{conCollapse: isCollapse}">
            <page-header></page-header>
            <div class="page-component-wrap">
                <router-view v-if="isRouterAlive"></router-view>
            </div>
        </div>
        <show-robot-status v-if="user.type !== 5"></show-robot-status>
    </el-container>
</template>

<script>
    import PageAside from './../components/PageAside'
    import PageHeader from './../components/PageHeader'
    import {mapGetters} from 'vuex'
    import ShowRobotStatus from "../components/ShowRobotStatus";
    export default {
        name: "Main",
        computed:{
            ...mapGetters(['isCollapse','user'])
        },
        components:{
            ShowRobotStatus,
            PageAside,
            PageHeader
        },
        provide() {
            return {
                reloadAll: this.reload
            }
        },
        data() {
            return {
                isRouterAlive: true
            }
        },
        methods: {
            reload: function () {
                this.isRouterAlive = false;
                this.$nextTick(function () {
                    this.isRouterAlive = true;
                })
            }
        }
    }
</script>

<style scoped lang="scss">
    .page {
        display:flex;
        width: 100%;
        height: 100%;
    }
    .con-wrap {
        width:100%;
        padding-left: 180px;
        transition: all 0.3s;
        overflow: hidden;
        .page-component-wrap {
            box-sizing:border-box;
            width:100%;
            height:calc(100% - 60px);
            position: relative;
            margin-top: 60px;
            padding: 20px 20px;
            overflow: hidden;
            &::before {
                background-color: #4BC0C0;
                content: "";
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 150px;
                z-index: -1;
            }
        }
    }
    .con-wrap.conCollapse {
        padding-left: 64px;
        transition: all 0.3s;
    }
</style>