import '@babel/polyfill'
import Vue from 'vue'
import './plugins/element.js'
import App from './App.vue'
import router from './router'
import store from './store'
import './assets/icon/iconfont.css'
import {alertError, alertInfo, alertSuccess, alertWarning} from "./utils/notification"

Vue.prototype.$alertError = alertError;
Vue.prototype.$alertInfo = alertInfo;
Vue.prototype.$alertSuccess = alertSuccess;
Vue.prototype.$alertWarning = alertWarning;

Vue.config.productionTip = false;

import {axiosPost} from "./utils/fetchData";
import {companySelectUrl} from "./plugins/globalUrl";
import {errHandler} from "./utils/errorHandler";

//获取公司列表
let getCompaniesList = function () {
  return new Promise(resolve => {
    axiosPost({
      url: companySelectUrl
    }).then(response => {
      if (response.data.result === 200) {
        resolve(response.data.data)
      } else {
        errHandler(response.data);
      }
    }).catch(err => {
      console.log(err);
      alertError('连接超时，请刷新重试');
    }).finally(() => {
      resolve([])
    })
  })
};

let init = async function () {
  if (window.sessionStorage.token) {
    await getCompaniesList().then(data => {
      //当列表与新获取列表不一致 或 列表一致但没设定活动公司时 指定默认公司
      let companiesList = window.localStorage.getItem('companiesList');
      let activeCompanyId = window.localStorage.getItem('activeCompanyId');
      if (companiesList !== JSON.stringify(data)
        || (companiesList === JSON.stringify(data) && !activeCompanyId)) {
        window.localStorage.setItem('activeCompanyId', data[0].id)
      }
      window.localStorage.setItem('companiesList', JSON.stringify(data))
    });
  }


  new Vue({
    router,
    store,
    render: h => h(App)
  }).$mount('#app');

};


init();
