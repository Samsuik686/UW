import router from '../router'
import {alertDanger, alertWarning} from "../utils/modal";

const resHandler = function (attr) {
  return new Promise((resolve, reject) => {
    if (attr.code === '200') {
      resolve(attr.data)
    } else {
      let error = {};
      switch (attr.code) {
        default:
          error.code = attr.code;
          error.msg = '未知错误';
          break;

      }
      reject(error)
    }
  })
}

export const errHandler = function (code) {
  switch (code) {
    case 400:
      alertDanger("请求逻辑错误，请联系管理员");
      break;
    case 401:
      alertWarning("权限不足");
      break;
    case 412:
      alertWarning("操作错误");
      break;
    case 500:
      alertDanger("服务器异常，请联系管理员");
      break;
    case 501:
      alertDanger("未知错误，请联系管理员");
      break;
    default:
      break;
  }
};
