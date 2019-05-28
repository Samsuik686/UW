// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import Vuex from 'vuex'
import App from './App'
import router from './router'
import axios from './config/http'
import store from './store'
import  Icon from 'vue-svg-icon/Icon'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min'
import Datatable from '../static/js/datatable.min.js'
import locale from './locale/zh-cn'
import {alertDanger, alertInfo, alertSuccess,alertWarning} from "./utils/modal";

Vue.prototype.$alertDanger = alertDanger;
Vue.prototype.$alertInfo = alertInfo;
Vue.prototype.$alertSuccess = alertSuccess;
Vue.prototype.$alertWarning = alertWarning;

Vue.component('icon', Icon);
Vue.use(Datatable, {locale});
Vue.prototype.$axios = axios;
Vue.config.productionTip = false;
Vue.use(Vuex);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
});
