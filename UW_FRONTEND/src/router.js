import Vue from 'vue'
import Router from 'vue-router'
Vue.use(Router);
import store from './store'

//登录
import Login from "./views/user/Login";
//主界面
import Main from './views/Main';
//建仓
import Build from "./views/build/Build";
//配置
import Config from './views/config/Config';
//盘点主界面
import Inventory from "./views/inventory/Inventory";
//盘点任务
import InventoryTasks from './views/inventory/tasks/Tasks';
//UW仓盘点操作
import InventoryOperation from './views/inventory/operation/Operation';
//UW仓盘点详情
import InventoryUwDetails from './views/inventory/uwDetails/UwDetails';
//物料仓盘点详情
import InventoryEwhDetails from './views/inventory/ewhDetails/EwhDetails';
//出入库主界面
import Io from './views/io/Io';
//查看仓口详情
import IoPreview from './views/io/preview/Preview';
//呼叫叉车
import IoCall from './views/io/call/Call';
//入库
import IoInNow from './views/io/inNow/InNow';
//出库
import IoOutNow from './views/io/outNow/OutNow';
//物料主界面
import Material from './views/material/Material';
//料盒
import MaterialBoxes from './views/material/boxes/Boxes';
//供应商
import MaterialSupplier from './views/material/supplier/Supplier';
//发料目的地
import MaterialDestination from './views/material/destination/Destination';
//UW仓物料
import MaterialUwMaterial from './views/material/uwMaterial/UwMaterial';
//物料仓物料
import MaterialEwhMaterial from './views/material/ewhMaterial/EwhMaterial';
//叉车
import Robot from './views/robot/Robot';
//任务
import Tasks from './views/tasks/Tasks';
//人员
import User from './views/user/User';
//日志
import Logs from './views/logs/Logs';
//接口调用日志
import LogsActionLog from './views/logs/actionLog/ActionLog';
//物料位置转移日志
import LogsPositionLog from './views/logs/positionLog/PositionLog';
//任务日志
import LogsTaskLog from './views/logs/taskLog/TaskLog';
//抽检主界面
import Sample from './views/sample/Sample';
//抽检任务
import SampleTasks from './views/sample/tasks/Tasks';
//抽检操作
import SampleOperation from './views/sample/operation/Operation';
//FAQ
import Help from './views/help/Help';
//FAQ编写
import Write from './views/help/write/Write';
//FAQ查看
import Read from "./views/help/read/Read";

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
                  component:Help,
                  children: [
                      {
                          path: 'write',
                          component: Write
                      },
                      {
                          path: 'read',
                          component: Read
                      }
                  ]
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

