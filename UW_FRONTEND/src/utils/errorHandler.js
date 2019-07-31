import {alertError,alertWarning} from "./notification"
import router from './../router'
import store from './../store'
export const errHandler = function (data) {
    switch (data.result) {
        case 210:
            alertWarning(data.data);
            break;
        case 401:
            alertWarning('权限不足');
            store.commit('setToken', '');
            window.sessionStorage.removeItem('token');
            router.replace({
                path: '/login',
            });
            break;
        case 400:
            alertWarning(data.data);
            break;
        case 412:
            alertWarning(data.data);
            break;
        case 500:
            alertError(data.data);
            break;
        case 501:
            alertWarning(data.data);
            break;
        case 510:
            alertError(data.data);
            break;
        case 300:
            alertError("第三方："+data.data);
            break;
        case 310:
            alertError("第三方："+data.data);
            break;
        default:
            break;
    }
};