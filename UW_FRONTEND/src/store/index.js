import Vue from 'vue'
import Vuex from 'vuex'
import * as mutations from './mutations'
import * as actions from './actions'
import * as getters from './getters'

Vue.use(Vuex);

const state = {
    //登录标识
    token: '',

    //用户信息
    user:{},

    //侧边栏是否折叠
    isCollapse:true,

    //配置页面信息
    configData:{
        printerIP:""
    },

    //当前路由名字
    activeName:'物料 / UW仓物料管理',

    //出入库-是否扫描到操作完成二维码
    scanFinishBoxId:'',
    //出入库-是否扫描截料入库二维码
    scanCutBoxId: '',
    //出入库-外部输入框是否失焦
    isBlur:false,

    //盘点-外部输入框是否聚焦
    isFocus:false,
    //盘点-是否扫描
    isScanner:false,
    //盘点-当前正操作的料盘
    disabledMaterialId: '',
    //盘点-已点击过确定的料盘数组
    printMaterialIdArr:[],
    //盘点-未平仓的数据
    unInventoryData:[],

    //贵重仓出入库-外部扫描框是否失焦
    isPreciousBlur:false,
    //贵重仓出入库-当前截料页面
    isPreciousCut:false
};

const store = new Vuex.Store({
    state,
    getters,
    actions,
    mutations
});

export default store;
