let url;
if (process.env.NODE_ENV === 'production') {
  url = window.g.API_URL
} else {
  url = window.g.LOCAL_URL
}

//build
export const buildUrl = url + '/build';

//material manage
export const materialCountUrl = url + '/manage/material/count';
export const materialEntityUrl = url + '/manage/material/getEntities';
export const materialAddUrl = url + '/manage/material/addType';
export const materialUpdateUrl = url + '/manage/material/updateType';
export const getBoxesUrl = url + '/manage/material/getBoxes';
export const addBoxUrl = url + '/manage/material/addBox';
export const updateBoxUrl = url + '/manage/material/updateBox';
export const deleteBoxUrl = url + '/manage/material/deleteBox';
export const exportReportUrl = url + '/manage/material/exportMaterialReport';
export const getMaterialRecordsUrl = url + '/manage/material/getMaterialRecords';
export const importReportUrl = url + '/manage/material/import';
export const deleteByIdsUrl = url + '/manage/material/deleteByIds';

//log
export const logsUrl = url + '/log/select';


//user manage
export const loginUrl = url + '/manage/user/login';
export const logoutUrl = url + '/manage/user/logout';
export const userAddUrl = url + '/manage/user/add';
export const userUpdateUrl = url + '/manage/user/update';
export const userSelectUrl = url + '/manage/user/select';
export const userTypeUrl = url + '/manage/user/getTypes';



//task
export const taskUrl = url + '/task';
export const taskSelectUrl = url + '/task/select';
export const taskCreateUrl = url + '/task/create';
export const taskCheckUrl = url + '/task/check';
export const taskGetIOTaskDetailsUrl = url + '/task/getIOTaskDetails';
export const taskWindowsUrl = url + '/task/getWindows';
export const taskWindowTaskItems = url + '/task/getWindowTaskItems';
export const taskWindowParkingItems = url + '/task/getWindowParkingItem';
export const taskInUrl = url + '/task/in';
export const taskOutUrl = url + '/task/out';
export const taskDeleteMaterialRecordUrl = url + '/task/deleteMaterialRecord';
export const taskFinishUrl = url + '/task/finishItem';
export const taskSetPriorityUrl = url + '/task/setPriority';
export const taskBackAfterCuttingUrl = url + '/task/backAfterCutting';
export const taskImportInRecordsUrl = url + '/task/importInRecords';
export const taskImportOutRecordsUrl = url + '/task/importOutRecords';
export const setWindowRobotsUrl = url + '/task/setWindowRobots';
export const getWindowRobotsUrl = url + '/task/getWindowRobots';

//robot manage
export const robotSelectUrl = url + '/manage/robot/select';
export const robotSwitchUrl = url + '/manage/robot/switch';
export const robotPauseUrl = url + '/manage/robot/pause';
export const robotBackUrl = url + '/manage/robot/back';
export const robotCallUrl = url + '/manage/robot/call';

//supplier manage
export const supplierAddUrl = url + '/manage/supplier/add';
export const supplierUpdateUrl = url + '/manage/supplier/update';
export const supplierSelectUrl = url + '/manage/supplier/getSuppliers';

//destination manage
export const destinationAddUrl = url + '/manage/destination/add';
export const destinationDeleteUrl = url + '/manage/destination/delete';
export const destinationSelectUrl = url + '/manage/destination/get';

//externalWh manage
export const externalWhImportTaskUrl = url + '/manage/externalWh/importTask';
export const externalWhImportWastageTaskUrl = url + '/manage/externalWh/importWastageTask';
export const externalWhSelectExternalWhInfoUrl = url + '/manage/externalWh/selectExternalWhInfo';
export const externalWhSelectEWhMaterialDetailsUrl = url + '/manage/externalWh/selectEWhMaterialDetails';
export const externalWhDeleteExternalWhLogUrl = url + '/manage/externalWh/deleteExternalWhLog';
export const externalWhAddWorstageLogUrl = url + '/manage/externalWh/addWorstageLog';
export const exportEWhReportUrl = url + '/manage/externalWh/exportEWhReport';

//inventory task
export const  createInventoryTaskUrl = url + '/task/inventory/createInventoryTask';
export const  startInventoryTaskUrl = url + '/task/inventory/startInventoryTask';
export const  finishInventoryTaskUrl = url + '/task/inventory/finishInventoryTask';
export const  backInventoryBoxUrl = url + '/task/inventory/backInventoryBox';
export const  selectAllInventoryTaskUrl = url + '/task/inventory/selectAllInventoryTask';
export const  getPackingInventoryUrl = url + '/task/inventory/getPackingInventory';
export const  getInventoryTaskInfoUrl = url + '/task/inventory/getInventoryTaskInfo';
export const  getInventoryTaskDetailsUrl = url + '/task/inventory/getInventoryTaskDetails';
export const  getInventoryTaskUrl = url + '/task/inventory/getInventoryTask';
export const  importEWhInventoryRecordUrl = url + '/task/inventory/importEWhInventoryRecord';
export const getUnStartInventoryTaskUrl = url + '/task/inventory/getUnStartInventoryTask';
export const exportEWhReportInventoryUrl = url + '/task/inventory/exportEWhReport';
//一键平仓
export const coverMaterialByTaskIdUrl = url + '/task/inventory/coverMaterialByTaskId';
//平UW仓
export const coverMaterialUrl = url + '/task/inventory/coverMaterial';
//平外仓
export const coverEWhMaterialUrl = url + '/task/inventory/coverEWhMaterial';
export const setInventoryTaskRobotsUrl = url + '/task/inventory/setTaskRobots';
export const getInventoryWindowRobotsUrl = url + '/task/inventory/getWindowRobots';


//盘点UW物料
export const inventoryMaterialUrl = url + '/task/inventory/inventoryMaterial';

//批量平UW仓
export const coverUwMaterialByTaskIdUrl = url + '/task/inventory/coverUwMaterialByTaskId';
//批量平外仓
export const coverEwhMaterialByTaskIdUrl = url + '/task/inventory/coverEwhMaterialByTaskId';

//审核UW仓盘点任务
export const checkInventoryTaskUrl = url + '/task/inventory/checkInventoryTask';
//审核物料仓盘点任务
export const checkEwhInventoryTaskUrl = url + '/task/inventory/checkEwhInventoryTask';

//修改盘点任务的盘点数量、退料盘盈
export const editEwhInventoryLogUrl = url + '/task/inventory/editEwhInventoryLog';

//查询物料仓盘点任务详情
export const getEwhInventoryTaskDetailsUrl = url + '/task/inventory/getEwhInventoryTaskDetails';
//查询UW仓盘点任务详情
export const getUwInventoryTaskDetailsUrl = url + '/task/inventory/getUwInventoryTaskDetails';

//查询UW仓盘点任务
export const getUwInventoryTaskInfoUrl = url + '/task/inventory/getUwInventoryTaskInfo';
//查询物料仓盘点任务
export const getEwhInventoryTaskInfoUrl = url + '/task/inventory/getEwhInventoryTaskInfo';

