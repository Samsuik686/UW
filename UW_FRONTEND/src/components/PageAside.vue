<template>
    <div class="aside" :class="{collapsed:isCollapse}">
        <el-scrollbar style="height:100%">
            <el-menu
                    @select="setRouteName"
                    background-color="#fff"
                    text-color="#888"
                    :default-active="$route.path"
                    active-text-color="#4BC0C0"
                    :unique-opened="true"
                    :router="true"
                    mode="vertical"
                    :collapse="isCollapse">
                <template v-for="(item,index) in navData">
                    <el-submenu v-if="item.children.length !== 0" :index="item.index" :key="item.index">
                        <template slot="title">
                            <i :class="item.icon"></i>
                            <span slot="title">{{item.name}}</span>
                        </template>
                        <el-menu-item v-for="child in item.children" :index="child.index" :key="child.index">
                            <span slot="title">{{child.name}}</span>
                        </el-menu-item>
                    </el-submenu>
                    <el-menu-item v-else :index="item.index" :key="item.index">
                        <i :class="item.icon"></i>
                        <span slot="title">{{item.name}}</span>
                    </el-menu-item>
                </template>
            </el-menu>
        </el-scrollbar>
    </div>
</template>

<script>
    import {mapGetters,mapActions} from 'vuex'
    export default {
        name: "PageAside",
        data() {
            return {
                showData:[],
                navData: [
                    {
                        index: 'material',
                        name: '物料',
                        icon: 'el-icon-coke-table',
                        children: [
                            {
                                index: '/material/boxes',
                                name: '料盒管理'
                            },
                            {
                                index: '/material/supplier',
                                name: '客户管理'
                            },
                            {
                                index: '/material/destination',
                                name: '发料目的地管理'
                            },
                            {
                                index: '/material/uwMaterial',
                                name: 'UW仓物料管理'
                            },
                            {
                                index: '/material/ewhMaterial',
                                name: '物料仓物料管理'
                            },
                            {
                                index: '/material/precious',
                                name: '贵重仓物料管理'
                            }
                        ]
                    },
                    {
                        index: 'tasks',
                        name: '任务',
                        icon: 'el-icon-coke-tasks',
                        children:[
                            {
                                index: '/tasks/regularTasks',
                                name: 'UW仓任务'
                            },
                            {
                                index: '/tasks/preciousTasks',
                                name: '贵重仓任务'
                            }
                        ]
                    },
                    {
                        index: 'logs',
                        name: '日志',
                        icon: 'el-icon-coke-logs',
                        children: [
                            {
                                index: '/logs/taskLog',
                                name: '任务日志'
                            },
                            {
                                index: '/logs/actionLog',
                                name: '接口调用日志'
                            }
                            /*{
                                index: '/logs/positionLog',
                                name: '物料位置转移日志'
                            }*/
                        ]
                    },
                    {
                        index: '/robot',
                        name: '叉车',
                        icon: 'el-icon-coke-forklift',
                        children:[]
                    },
                    {
                        index: 'io',
                        name: '出入库',
                        icon: 'el-icon-coke-transfer',
                        children: [
                            {
                                index: '/io/preview',
                                name: '查看仓口任务'
                            },
                            {
                                index: '/io/call',
                                name: '扫码呼叫叉车'
                            },
                            {
                                index: '/io/inNow',
                                name: '入库任务操作'
                            },
                            {
                                index: '/io/outNow',
                                name: '出库任务操作'
                            },
                            {
                                index: '/io/returnNow',
                                name: '调拨入库任务操作'
                            },
                            {
                                index: '/io/preciousNow',
                                name: '贵重仓任务操作'
                            }
                        ]
                    },
                    {
                        index: 'inventory',
                        name: '盘点',
                        icon: 'el-icon-coke-check-material',
                        children: [
                            {
                                index: '/inventory/tasks',
                                name: '盘点任务'
                            },
                            {
                                index: '/inventory/operation',
                                name: 'UW仓盘点操作'
                            },
                            {
                                index: '/inventory/uwDetails',
                                name: 'UW仓盘点详情'
                            },
                            {
                                index: '/inventory/ewhDetails',
                                name: '物料仓盘点详情'
                            },
                            {
                                index: '/inventory/preciousTasks',
                                name: '贵重仓盘点任务'
                            },
                            {
                                index: '/inventory/preciousDetails',
                                name: '贵重仓盘点详情'
                            }
                        ]
                    },
                    {
                        index: 'sample',
                        name: '抽检',
                        icon: 'el-icon-coke-spot-check',
                        children: [
                            {
                                index: '/sample/tasks',
                                name: '抽检任务'
                            },
                            {
                                index: '/sample/operation',
                                name: '抽检操作'
                            },
                            {
                                index: '/sample/preciousTasks',
                                name: '贵重仓抽检任务'
                            },
                            {
                                index: '/sample/preciousOperation',
                                name: '贵重仓抽检操作'
                            }
                        ]
                    },
                    {
                        index: '/build',
                        name: '建仓',
                        icon: 'el-icon-coke-build',
                        children:[]
                    },
                    {
                        index: '/user',
                        name: '人员',
                        icon: 'el-icon-coke-users',
                        children:[]
                    },
                    {
                        index: '/config',
                        name: '配置',
                        icon: 'el-icon-coke-config',
                        children:[]
                    },
                    {
                        index: 'help',
                        name: '帮助',
                        icon: 'el-icon-coke-help',
                        children:[
                            {
                                index: '/help/write',
                                name: '在线编辑'
                            },
                            {
                                index: '/help/read',
                                name: '在线查阅'
                            }
                        ]
                    }
                ],
            }
        },
        computed:{
            ...mapGetters(['isCollapse','user'])
        },
        mounted(){
            this.setNowRoute();
            this.changeRoute();
        },
        watch:{
            $route:function(){
                this.setNowRoute();
                this.changeRoute();
            }
        },
        methods:{
            ...mapActions(['setActiveName']),
            setRouteName:function(index,indexPath){
                let activeName = '';
                for(let i = 0;i<this.navData.length;i++){
                    let first = indexPath[0];
                    if(this.navData[i].index === first){
                        activeName = this.navData[i].name;
                        if(indexPath.length === 1){
                            this.setActiveName(activeName);
                            return;
                        }
                        let second = indexPath[1];
                        for(let j =0;j<this.navData[i].children.length;j++){
                            if(this.navData[i].children[j].index === second){
                                activeName = activeName + ' / ' + this.navData[i].children[j].name;
                                this.setActiveName(activeName);
                                return;
                            }
                        }
                    }
                }
            },
            setNowRoute:function () {
                let matched = this.$route.matched;
                let path = this.$route.path;
                let arr = [];
                if(matched.length === 2){
                    arr.push(path);
                }else{
                    let arrPath = path.split('/');
                    arr.push(arrPath[1]);
                    arr.push(path);
                }
                this.setRouteName('',arr);
            },
            changeRoute:function(){
                if(this.user.type === 1){
                    for(let i =0;i<this.navData.length;i++){
                        let obj = this.navData[i];
                        if(obj.name === '帮助'){
                            this.navData[i].index = '/help/read';
                            this.navData[i].children = [];
                            break;
                        }
                        if(obj.name === '建仓'){
                            this.navData.splice(i,1);
                            i--;
                        }
                    }
                    return;
                }
                if(this.user.type === 2){
                    for(let i =0;i<this.navData.length;i++){
                        let obj = this.navData[i];
                        if(obj.name === '任务'){
                            this.navData[i].index = '/tasks/regularTasks';
                            this.navData[i].children = [];
                        }
                        if(obj.name === '出入库' || obj.name === '盘点' || obj.name === '抽检'){
                            let data = this.navData[i].children;
                            for(let j = 0;j<data.length;j++){
                                if(data[j].name.includes('贵重仓')){
                                    data.splice(j,1);
                                    i--
                                }
                            }
                        }
                        if(obj.name === '建仓'){
                            this.navData.splice(i,1);
                            i--;
                        }
                        if(obj.name === '人员'){
                            this.navData.splice(i,1);
                            i--;
                        }
                        if(obj.name === '帮助'){
                            this.navData[i].index = '/help/read';
                            this.navData[i].children = [];
                        }
                    }
                    return;
                }
                if(this.user.type === 5){
                    for(let i =0;i<this.navData.length;i++){
                        let obj = this.navData[i];
                        if(obj.name === '任务'){
                            this.navData[i].index = '/tasks/preciousTasks';
                            this.navData[i].children = [];
                        }
                        if(obj.name === '叉车'){
                            this.navData.splice(i,1);
                            i--;
                        }
                        if(obj.name === '出入库'){
                            this.navData[i].index = '/io/preciousNow';
                            this.navData[i].children = [];
                        }
                        if(obj.name === '盘点' || obj.name === '抽检'){
                            let data = this.navData[i].children;
                            for(let j = 0;j<data.length;j++){
                                if(!data[j].name.includes('贵重仓')){
                                    data.splice(j,1);
                                    i--
                                }
                            }
                        }
                        if(obj.name === '建仓'){
                            this.navData.splice(i,1);
                            i--;
                        }
                        if(obj.name === '人员'){
                            this.navData.splice(i,1);
                            i--;
                        }
                        if(obj.name === '帮助'){
                            this.navData[i].index = '/help/read';
                            this.navData[i].children = [];
                        }
                    }
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .aside{
        box-sizing:border-box;
        padding-top:10px;
        width: 180px;
        height: 100%;
        position: fixed;
        left: 0;
        top: 0;
        bottom: 0;
        z-index: 5;
        transition: all 0.5s;
        background: #fff;
        color: #4BC0C0;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        &.collapsed {
            width: 64px;
            transition: all 0.5s;
        }
    }
    .el-menu {
        height: 100%;
    }
    .el-submenu .el-menu-item {
        padding: 0 20px;
        min-width: 160px;
        padding-left: 30px !important;
        i {
            padding-right: 10px;
        }
    }
</style>