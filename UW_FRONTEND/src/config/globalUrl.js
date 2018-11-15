let url;
if (process.env.NODE_ENV === 'production') {
  url = window.g.API_URL
} else {
  url = window.g.LOCAL_URL
}

//material manage
export const materialCountUrl = url + '/manage/material/count';
export const materialEntityUrl = url + '/manage/material/getEntities';
export const materialAddUrl = url + '/manage/material/addType';
export const materialUpdateUrl = url + '/manage/material/updateType';
export const getBoxesUrl = url + '/manage/material/getBoxes';
export const addBoxUrl = url + '/manage/material/addBox';
export const updateBoxUrl = url + '/manage/material/updateBox';

//logs
export const logsUrl = url + '/log/select';


//users
export const loginUrl = url + '/manage/user/login';
export const logoutUrl = url + '/manage/user/logout';
export const userAddUrl = url + '/manage/user/add';
export const userUpdateUrl = url + '/manage/user/update';
export const userSelectUrl = url + '/manage/user/select';
export const userTypeUrl = url + '/manage/user/getTypes';



//tasks
export const taskUrl = url + '/task';
export const taskSelectUrl = url + '/task/select';
export const taskCreateUrl = url + '/task/create';
export const taskCheckUrl = url + '/task/check';
export const taskWindowsUrl = url + '/task/getWindows';

export const taskWindowTaskItems = url + '/task/getWindowTaskItems';
export const taskWindowParkingItems = url + '/task/getWindowParkingItem';

export const taskInUrl = url + '/task/in';
export const taskOutUrl = url + '/task/out';

export const taskDeleteMaterialRecordUrl = url + '/task/deleteMaterialRecord';

export const taskUpdateOutQuantityUrl = url + '/task/updateOutQuantity';

export const taskFinishUrl = url + '/task/finishItem';
export const taskSetPriorityUrl = url + '/task/setPriority';
export const taskBackAfterCuttingUrl = url + '/task/backAfterCutting'

//robot
export const robotSelectUrl = url + '/manage/robot/select';
export const robotSwitchUrl = url + '/manage/robot/switch';
export const robotPauseUrl = url + '/manage/robot/pause';
export const robotBackUrl = url + '/manage/robot/back';
export const robotCallUrl = url + '/manage/robot/call';
