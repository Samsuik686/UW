import axios from 'axios';
import store from "../store";

axios.interceptors.request.use(
    config => {
        if (config.headers['Content-type'] === "application/x-www-form-urlencoded; charset=UTF-8") {
            if (store.state.token !== '') {
                if (config.data === "") {
                    config.data += ("#TOKEN#=" + store.state.token);
                } else {
                    config.data += ("&#TOKEN#=" + store.state.token);
                }
            }
        }
        return config;
    },
    error => {
        return Promise.reject(error)
    }
);

export default axios;
