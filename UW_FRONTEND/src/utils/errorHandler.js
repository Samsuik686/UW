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
};

export const errHandler = function (data) {
  switch (data.result) {
    case 400:
      alertDanger(data.data);
      break;
    case 401:
      alertWarning(data.data);
      break;
    case 412:
      alertWarning(data.data);
      break;
    case 500:
      alertDanger(data.data);
      break;
    case 501:
      alertDanger(data.data);
      break;
    default:
      break;
  }
};
