/*vue-router 配置*/

import Vue from 'vue'
import Router from 'vue-router'
import store from '../store'
import Main from '../pages/Main'
import BuildMain from '../pages/build/BuildMain'
import MaterialMain from '../pages/material/MaterialMain'
import BoxesModule from '../pages/material/boxes/BoxesModule'
import MaterialModule from '../pages/material/material/MaterialModule'
import SupplierModule from '../pages/material/supplier/SupplierModule'
import DestinationModule from '../pages/material/destination/DestinationModule'
import TransferModule from '../pages/material/transfer/TransferModule'
import LogsMain from '../pages/logs/LogsMain'
import LogsModule from '../pages/logs/details/TableModule'
import TasksMain from '../pages/tasks/TaskMain'
import RobotMain from '../pages/robot/RobotMain'
import Login from '../pages/user/Login'
import ConfigMain from '../pages/config/ConfigMain'
import UserConfig from '../pages/user/UserConfig'
import IoMain from '../pages/io/IoMain'
import OutNow from '../pages/io/outNow/OutNow'
import InNow from '../pages/io/inNow/InNow'
import IoPreview from '../pages/io/ioPreview/IoPreview'
import CallRobot from '../pages/io/callRobot/CallRobot'
import CheckMain from '../pages/check/CheckMain'
import UWDetailsModule from '../pages/check/details/UWDetailsModule'
import TransferDetailsModule from '../pages/check/details/TransferDetailsModule'
import TasksModule from '../pages/check/tasks/TasksModule'
import OperationModule from '../pages/check/operation/OperationModule'
import SpotCheckMain from '../pages/spotCheck/SpotCheckMain'
import SpotTaskModule from '../pages/spotCheck/tasks/TasksModule'
import SpotOperationModule from '../pages/spotCheck/operation/OperationModule'
Vue.use(Router);

const router = new Router({
  base: window.g.SYSTEM_PATH,
  routes: [
    {
      path: '/_empty'
    },
    {
      path: '/',
      component: Main,
      redirect: '/material',
      meta: {
        requireAuth: true
      },
      children: [
        {
          path: '/material',
          name: 'Material',
          component: MaterialMain,
          children: [
            {
              path: 'material',
              component: MaterialModule
            },
            {
              path: 'boxes',
              component: BoxesModule
            },
            {
              path: 'supplier',
              component: SupplierModule
            },
            {
              path:'destination',
              component:DestinationModule
            },
            {
              path:'transfer',
              component:TransferModule
            }
          ]
        },
        {
          path: 'logs',
          component: LogsMain,
          children: [
            {
              path: 'action_log',
              component: LogsModule
            },
            {
              path: 'task_log',
              component: LogsModule
            },
            {
              path: 'position_log',
              component: LogsModule
            }
          ]
        },
        {
          path: 'tasks',
          name: 'Tasks',
          component: TasksMain,
        },
        {
          path: 'robot',
          name: 'Robot',
          component: RobotMain
        },
        {
          path: 'user',
          name: 'UserConfig',
          component: UserConfig
        },
        {
          path: 'config',
          name: 'ConfigMain',
          component: ConfigMain
        },
        {
          path: '/build',
          name: 'BuildMain',
          component: BuildMain
        },
        {
          path: 'io',
          name: 'IoMain',
          component: IoMain,
          children: [
            {
              path: 'innow',
              component: InNow
            },
            {
              path: 'outnow',
              component: OutNow
            },
            {
              path: 'preview',
              component: IoPreview
            },
            {
              path: 'return',
              component: InNow
            },
            {
              path:'call',
              component:CallRobot
            }
          ]
        },
        {
          path:'check',
          name:'CheckMain',
          component:CheckMain,
          children:[
            {
              path:'tasks',
              component:TasksModule
            },
            {
              path:'operation',
              component:OperationModule
            },
            {
              path:'UWDetails',
              component:UWDetailsModule
            },
            {
              path:'transferDetails',
              component:TransferDetailsModule
            }
          ]
        },
        {
          path:'spot',
          name:'SpotCheckMain',
          component:SpotCheckMain,
          children:[
            {
              path:'tasks',
              component:SpotTaskModule
            },
            {
              path:'operation',
              component:SpotOperationModule
            }
          ]
        }
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    }
  ]
});

if (localStorage.getItem('token')) {
  store.commit('setLoginToken', localStorage.getItem('token'))
}
if (localStorage.getItem('user')) {
  store.commit('setUser', JSON.parse(localStorage.getItem('user')));
}
if (localStorage.getItem('configData')) {
  store.commit('setConfigData', JSON.parse(localStorage.getItem("configData")));
}


router.beforeEach((to, from, next) => {
  store.commit('setLoading', false);
  if (!(to.path === '/_empty' || from.path === '/_empty')) {
    for (let index in window.g.ROBOT_INTERVAL) {
      clearInterval(window.g.ROBOT_INTERVAL[index])
    }
    for (let index in window.g.PARKING_ITEMS_INTERVAL_IN) {
      clearInterval(window.g.PARKING_ITEMS_INTERVAL_IN[index])
    }
    for (let index in window.g.PARKING_ITEMS_INTERVAL_OUT) {
      clearInterval(window.g.PARKING_ITEMS_INTERVAL_OUT[index])
    }
    for (let index in window.g.PREVIEW_ITEMS_INTERVAL) {
      clearInterval(window.g.PREVIEW_ITEMS_INTERVAL[index])
    }
  }
  if (to.matched.some(r => r.meta.requireAuth)) {
    if (store.state.token) {
      next();
    } else {
      next({
        path: '/login',
        query: {redirect: to.fullPath}
      })
    }
  } else {
    next();
  }
});

export default router;
