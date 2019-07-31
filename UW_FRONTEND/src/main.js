import '@babel/polyfill'
import Vue from 'vue'
import './plugins/element.js'
import App from './App.vue'
import router from './router'
import store from './store'
import './assets/icon/iconfont.css'
import {alertError, alertInfo, alertSuccess,alertWarning} from "./utils/notification"
Vue.prototype.$alertError = alertError;
Vue.prototype.$alertInfo = alertInfo;
Vue.prototype.$alertSuccess = alertSuccess;
Vue.prototype.$alertWarning = alertWarning;

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');
