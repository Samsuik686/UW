/*vue-router 配置*/

import Vue from 'vue'
import Router from 'vue-router'
import store from '../store'
import Main from '../pages/Main'
import BuildMain from '../pages/build/BuildMain'
import MaterialMain from '../pages/material/MaterialMain'
import BoxesModule from '../pages/material/boxes/BoxesModule'
import BoxTypeModule from '../pages/material/boxType/BoxTypeModule'
import MaterialModule from '../pages/material/material/MaterialModule'
import SupplierModule from '../pages/material/supplier/SupplierModule'
import LogsMain from '../pages/logs/LogsMain'
import LogsModule from '../pages/logs/details/TableModule'
import TasksMain from '../pages/tasks/TaskMain'
import RobotMain from '../pages/robot/RobotMain'
import Login from '../pages/user/Login'
import ConfigMain from '../pages/config/ConfigMain'
import UserConfig from '../pages/user/UserConfig'
import IoMain from '../pages/io/IoMain'
import OutNow from '../pages/io/details/OutNow'
import InNow from '../pages/io/details/InNow'
import IoPreview from '../pages/io/details/IoPreview'


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
              path:'boxType',
              component:BoxTypeModule
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
            }
          ]
        }
      ]
    },
    {
      path: '/build',
      name: 'BuildMain',
      component: BuildMain,
      meta: {
        requireAuth: true
      }
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
  store.commit('setUser', localStorage.getItem('user'));
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
