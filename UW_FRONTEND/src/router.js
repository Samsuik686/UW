import Vue from 'vue'
import Router from 'vue-router'
Vue.use(Router);
import store from './store'

//登录
const Login = () => import('./views/user/Login');
//主界面
const Main = () => import('./views/Main');
//建仓
const Build = () => import('./views/build/Build');
//配置
const Config = () => import ('./views/config/Config');
//盘点主界面
const Inventory = () => import('./views/inventory/Inventory');
//盘点任务
const InventoryTasks = () => import('./views/inventory/tasks/Tasks');
//UW仓盘点操作
const InventoryOperation = () => import('./views/inventory/operation/Operation');
//UW仓盘点详情
const InventoryUwDetails = () => import('./views/inventory/uwDetails/UwDetails');
//物料仓盘点详情
const InventoryEwhDetails = () => import('./views/inventory/ewhDetails/EwhDetails');
//出入库主界面
const Io = () => import('./views/io/Io');
//查看仓口详情
const IoPreview = () => import('./views/io/preview/Preview');
//呼叫叉车
const IoCall = () => import('./views/io/call/Call');
//入库
const IoInNow = () => import('./views/io/inNow/InNow');
//出库
const IoOutNow = () => import('./views/io/outNow/OutNow');
//物料主界面
const Material = () => import('./views/material/Material');
//料盒
const MaterialBoxes = () => import('./views/material/boxes/Boxes');
//供应商
const MaterialSupplier = () => import('./views/material/supplier/Supplier');
//发料目的地
const MaterialDestination = () => import('./views/material/destination/Destination');
//UW仓物料
const MaterialUwMaterial = () => import('./views/material/uwMaterial/UwMaterial');
//物料仓物料
const MaterialEwhMaterial = () => import('./views/material/ewhMaterial/EwhMaterial');
//叉车
const Robot = () => import('./views/robot/Robot');
//任务
const Tasks = () => import('./views/tasks/Tasks');
//人员
const User = () => import('./views/user/User');
//日志
const Logs = () => import('./views/logs/Logs');
//接口调用日志
const LogsActionLog = () => import('./views/logs/actionLog/ActionLog');
//物料位置转移日志
const LogsPositionLog = () => import('./views/logs/positionLog/PositionLog');
//任务日志
const LogsTaskLog = () => import('./views/logs/taskLog/TaskLog');
//抽检主界面
const Sample = () => import('./views/sample/Sample');
//抽检任务
const SampleTasks = () => import('./views/sample/tasks/Tasks');
//抽检操作
const SampleOperation = () => import('./views/sample/operation/Operation');
//FAQ
const Help = () => import('./views/help/Help');

const router = new Router({
  mode: 'history',
  base: window.g.SYSTEM_PATH,
  routes: [
      {
          path:'/',
          redirect: '/login'
      },
      {
          path:'/login',
          name:"Login",
          component:Login
      },
      {
          path:'/main',
          name:'Main',
          component:Main,
          redirect:'/material',
          meta: {
              requireAuth: true
          },
          children:[
              {
                  path:'/material',
                  name:'Material',
                  component:Material,
                  children: [
                      {
                          path:'uwMaterial',
                          component:MaterialUwMaterial
                      },
                      {
                          path:'ewhMaterial',
                          component:MaterialEwhMaterial
                      },
                      {
                          path:'boxes',
                          component:MaterialBoxes
                      },
                      {
                          path:'supplier',
                          component:MaterialSupplier
                      },
                      {
                          path:'destination',
                          component:MaterialDestination
                      }
                  ]
              },
              {
                  path:'/tasks',
                  name:'Tasks',
                  component:Tasks
              },
              {
                  path:'/logs',
                  name:'Logs',
                  component:Logs,
                  children:[
                      {
                          path:'taskLog',
                          component:LogsTaskLog
                      },
                      {
                          path:'actionLog',
                          component:LogsActionLog
                      },
                      /*{
                          path:'positionLog',
                          component:LogsPositionLog
                      }*/
                  ]
              },
              {
                  path:'/robot',
                  name:'Robot',
                  component:Robot
              },
              {
                  path:'/io',
                  name:'Io',
                  component:Io,
                  children: [
                      {
                          path:'preview',
                          component:IoPreview
                      },
                      {
                          path:'call',
                          component:IoCall
                      },
                      {
                          path:'inNow',
                          component:IoInNow
                      },
                      {
                          path:'outNow',
                          component:IoOutNow
                      },
                      {
                          path:'returnNow',
                          component:IoInNow
                      }
                  ]
              },
              {
                  path: '/inventory',
                  name: 'Inventory',
                  component: Inventory,
                  children: [
                      {
                          path: 'tasks',
                          component: InventoryTasks
                      },
                      {
                          path: 'operation',
                          component: InventoryOperation
                      },
                      {
                          path: 'uwDetails',
                          component: InventoryUwDetails
                      },
                      {
                          path: 'ewhDetails',
                          component: InventoryEwhDetails
                      }
                  ]
              },
              {
                  path: '/sample',
                  name: 'Sample',
                  component: Sample,
                  children: [
                      {
                          path: 'tasks',
                          component: SampleTasks
                      },
                      {
                          path: 'operation',
                          component: SampleOperation
                      }
                  ]
              },
              {
                  path:'/config',
                  name:'Config',
                  component:Config
              },
              {
                  path:'/build',
                  name:'Build',
                  component:Build
              },
              {
                  path:'/user',
                  name:'User',
                  component:User
              },
              {
                  path:'/help',
                  name:'Help',
                  component:Help
              }
          ]
      }
  ]
});

if (window.sessionStorage.getItem('token')) {
    store.commit('setToken', window.sessionStorage.getItem('token'))
}
if (window.sessionStorage.getItem('user')) {
    store.commit('setUser', JSON.parse(window.sessionStorage.getItem('user')));
}
if (window.sessionStorage.getItem('configData')) {
    store.commit('setConfigData', JSON.parse(window.sessionStorage.getItem("configData")));
}


router.beforeEach((to, from, next) => {
    if(to.path === '/login'){
        window.sessionStorage.clear();
        store.commit('setToken','');
        store.commit('setUser',{});
    }
    if (to.matched.some(r => r.meta.requireAuth)) {
        if (store.state.token) {
            next();
        } else {
            next({
                path: '/login',
            })
        }
    } else {
        next();
    }
});
export default router;

