import {alertDanger, alertWarning} from "../utils/modal";

export const errHandler = function (data) {
  switch (data.result) {
    case 400:
      alertDanger(data.data);
      break;
    case 401:
      alertWarning('权限不足');
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
