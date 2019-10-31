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

//添加普通仓物料类型
export const addRegularMaterialTypeUrl = url + '/manage/material/addRegularMaterialType';
//添加贵重仓物料类型
export const addPreciousMaterialTypeUrl = url + '/manage/material/addPreciousMaterialType';
//更新普通仓物料类型
export const updateRegularMaterialTypeUrl = url + '/manage/material/updateRegularMaterialType';
//更新贵重仓物料类型
export const updatePreciousMaterialTypeUrl = url + '/manage/material/updatePreciousMaterialType';
//批量删除普通仓物料类型
export const deleteRegularMaterialByIdsUrl = url + '/manage/material/deleteRegularMaterialByIds';
//批量删除贵重仓物料类型
export const deletePreciousMaterialByIdsUrl = url + '/manage/material/deletePreciousMaterialByIds';

export const getBoxesUrl = url + '/manage/material/getBoxes';
export const addBoxUrl = url + '/manage/material/addBox';
export const updateBoxUrl = url + '/manage/material/updateBox';
export const deleteBoxUrl = url + '/manage/material/deleteBox';
export const exportReportUrl = url + '/manage/material/exportMaterialReport';
export const getMaterialRecordsUrl = url + '/manage/material/getMaterialRecords';
export const importRegularMaterialTypeFileUrl = url + '/manage/material/importRegularMaterialTypeFile';
export const importPreicousMaterialTypeFileUrl = url + '/manage/material/importPreicousMaterialTypeFile';
export const getOverdueMaterialUrl = url + '/manage/material/getOverdueMaterial';

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



//创建普通仓任务
export const taskCreateRegularIOTaskUrl = url + '/task/createRegularIOTask';
//创建贵重仓任务
export const taskCreatePreciousIOTaskUrl = url + '/task/createPreciousIOTask';

export const taskCheckUrl = url + '/task/check';
export const taskGetIOTaskDetailsUrl = url + '/task/getIOTaskDetails';
export const taskWindowsUrl = url + '/task/getWindows';
export const taskWindowTaskItems = url + '/task/getWindowTaskItems';
export const taskWindowParkingItems = url + '/task/getWindowParkingItem';

//普通物料出入库
export const taskInRegularUrl = url + '/task/inRegular';
export const taskOutRegularUrl = url + '/task/outRegular';
//贵重料出入库
export const taskInPreciousUrl = url + '/task/inPrecious';
export const taskOutPreciousUrl = url + '/task/outPrecious';

export const taskDeleteRegularMaterialRecordUrl = url + '/task/deleteRegularMaterialRecord';
export const taskDeletePreciousMaterialRecordUrl = url + '/task/deletePreciousMaterialRecord';



export const taskSetPriorityUrl = url + '/task/setPriority';

//获取任务详情(贵重仓)
export const taskGetIOTaskInfosUrl = url + '/task/getIOTaskInfos';


//普通物料截料入库
export const taskBackRegularAfterCuttingUrl = url + '/task/backRegularAfterCutting';
//贵重物料截料入库
export const taskBackPreciousAfterCuttingUrl = url + '/task/backPreciousAfterCutting';




export const editTaskRemarksUrl = url + '/task/editTaskRemarks';
//导出普通任务未完成
export const exportUnfinishRegularTaskDetailsUrl = url + '/task/exportUnfinishRegularTaskDetails';
//导出贵重任务未完成
export const exportUnfinishPreciousTaskDetailsUrl = url + '/task/exportUnfinishPreciousTaskDetails';

//修改普通物料数量
export const modifyRegularOutQuantityUrl = url + '/task/modifyRegularOutQuantity';
//修改贵重物料数量
export const modifyPreciousOutQuantityUrl = url + '/task/modifyPreciousOutQuantity';


export const switchTaskUrl = url + '/task/switchTask';

//获取任务仓口
export const getTaskWindowUrl = url + '/task/getTaskWindow';
//更改任务仓口
export const setTaskWindowUrl = url + '/task/setTaskWindow';

//完成所有缺料条目
export const finishPreciousTaskLackItemUrl = url + '/task/finishPreciousTaskLackItem';
//完成任务条目
export const finishPreciousTaskItemUrl = url + '/task/finishPreciousTaskItem';

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
export const supplierChangeNameUrl = url + '/manage/supplier/changeName';
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
//创建普通仓盘点任务
export const  createInventoryRegularTask = url + '/task/inventory/createRegularTask';
//创建贵重仓盘点任务
export const  createInventoryPreciousTask = url + '/task/inventory/createPreciousTask';

//开始普通仓盘点任务
export const  startInventoryRegularTaskUrl = url + '/task/inventory/startRegularTask';
//开始贵重仓盘点任务
export const  startInventoryPreciousTaskUrl = url + '/task/inventory/startPreciousTask';

//完成普通仓盘点任务
export const  finishInventoryRegularTaskUrl = url + '/task/inventory/finishRegularTask';
//完成贵重仓盘点任务
export const  finishInventoryPreciousTaskUrl = url + '/task/inventory/finishPreciousTask';


//作废未完成的盘点任务
export const cancelRegularInventoryTaskUrl = url + '/task/inventory/cancelRegularTask';

export const  backInventoryBoxUrl = url + '/task/inventory/backInventoryRegularUWBox';
export const  selectAllInventoryTaskUrl = url + '/task/inventory/selectAllInventoryTask';
export const  getPackingInventoryUrl = url + '/task/inventory/getPackingInventory';
export const  getInventoryTaskInfoUrl = url + '/task/inventory/getInventoryTaskInfo';
export const  getInventoryTaskDetailsUrl = url + '/task/inventory/getInventoryTaskDetails';
export const  getInventoryTaskUrl = url + '/task/inventory/getInventoryTask';
export const  importEWhInventoryRecordUrl = url + '/task/inventory/importEWhInventoryRecord';
export const getUnStartInventoryTaskUrl = url + '/task/inventory/getUnStartInventoryTask';
export const exportEWhReportInventoryUrl = url + '/task/inventory/exportEWhReport';

//平UW仓
export const coverRegularUWMaterialUrl = url + '/task/inventory/coverRegularUWMaterial';
//平外仓
export const coverRegularEWhMaterialUrl = url + '/task/inventory/coverRegularEWhMaterial';
//平贵重仓
export const coverPreciousUWMaterialUrl = url + '/task/inventory/coverPreciousUWMaterial';


//盘点UW物料
export const inventoryMaterialUrl = url + '/task/inventory/inventoryRegularUWMaterial';
//盘点贵重仓物料
export const inventoryPreciousUWMaterialUrl = url + '/task/inventory/inventoryPreciousUWMaterial';

//批量平UW仓
export const coverRegularUWMaterialByTaskIdUrl = url + '/task/inventory/coverRegularUWMaterialByTaskId';
//批量平外仓
export const coverRegularEwhMaterialByTaskIdUrl = url + '/task/inventory/coverRegularEwhMaterialByTaskId';
//批量平贵重仓
export const coverPreciousUWMaterialByTaskIdUrl = url + '/task/inventory/coverPreciousUWMaterialByTaskId';

//审核UW仓盘点任务
export const checkRegularTaskUrl = url + '/task/inventory/checkRegularTask';
//审核物料仓盘点任务
export const checkEwhInventoryTaskUrl = url + '/task/inventory/checkEwhInventoryTask';
//审核贵重仓盘点任务
export const checkPreciousTaskUrl = url + '/task/inventory/checkPreciousTask';


//修改盘点任务的盘点数量、退料盘盈
export const editEwhInventoryLogUrl = url + '/task/inventory/editEwhInventoryLog';

//查询物料仓盘点任务详情
export const getEwhInventoryTaskDetailsUrl = url + '/task/inventory/getEwhInventoryTaskDetails';
//查询UW仓盘点任务详情
export const getUwInventoryTaskDetailsUrl = url + '/task/inventory/getUwInventoryTaskDetails';

//获取盘点任务绑定的目的仓库
export const getInventoryTaskDestinationUrl = url + '/task/inventory/getInventoryTaskDestination';
//获取盘点任务基础信息
export const getInventoryTaskBaseInfoUrl = url + '/task/inventory/getInventoryTaskBaseInfo';


//查询UW仓盘点任务
export const getUwInventoryTaskInfoUrl = url + '/task/inventory/getUwInventoryTaskInfo';
//查询物料仓盘点任务
export const getEwhInventoryTaskInfoUrl = url + '/task/inventory/getEwhInventoryTaskInfo';

//导出UW仓盘点报表
export const exportUwReportUrl = url + '/task/inventory/exportUwReport';

//一键平UW仓
export const coverRegularUWMaterialOneKeyUrl = url + '/task/inventory/coverRegularUWMaterialOneKey';
//一键平物料仓
export const coverEwhMaterialOneKeyUrl = url + '/task/inventory/coverEwhMaterialOneKey';
//一键平贵重仓
export const coverPreciousUWMaterialOneKeyUrl = url + '/task/inventory/coverPreciousUWMaterialOneKey';

//sample task
//创建普通仓抽检任务
export const createRegularSampleTaskUrl = url + '/task/sampleTask/createRegularSampleTask';
//创建贵重仓抽检任务
export const createPreciousSampleTaskUrl = url + '/task/sampleTask/createPreciousSampleTask';

//开始普通仓抽检任务
export const startRegularTaskUrl = url + '/task/sampleTask/startRegularTask';
//开始贵重仓抽检任务
export const startPreciousTaskUrl = url + '/task/sampleTask/startPreciousTask';

//作废普通仓抽检任务
export const cancelRegularTaskUrl = url + '/task/sampleTask/cancelRegularTask';
//作废贵重仓抽检任务
export const cancelPreciousTaskUrl = url + '/task/sampleTask/cancelPreciousTask';

//完成任务
export const finishPreciousTaskUrl = url + '/task/sampleTask/finishPreciousTask';

//抽检回库
export const backBoxSampleTaskUrl = url + '/task/sampleTask/backRegularUWBox';


//普通仓异常出库
export const outRegularTaskSingularUrl = url + '/task/sampleTask/outRegularTaskSingular';
//贵重仓异常出库
export const outPreciousTaskSingularUrl = url + '/task/sampleTask/outPreciousTaskSingular';
//普通仓抽检出库
export const outRegularTaskRegularUrl = url + '/task/sampleTask/outRegularTaskRegular';
//贵重仓抽检出库
export const outPreciousTaskRegularUrl = url + '/task/sampleTask/outPreciousTaskRegular';
//普通仓料盘丢失
export const outRegularTaskLostUrl = url + '/task/sampleTask/outRegularTaskLost';
//贵重仓料盘丢失
export const outPreciousTaskLostUrl = url + '/task/sampleTask/outPreciousTaskLost';
//获取贵重仓任务数据
export const getSampleTaskMaterialInfoUrl = url + '/task/sampleTask/getSampleTaskMaterialInfo';

//获取仓口数据
export const getPackingSampleMaterialInfoUrl = url + '/task/sampleTask/getPackingSampleMaterialInfo';
//获取详情
export const  getSampleTaskDetailsUrl = url + '/task/sampleTask/getSampleTaskDetials';
//查询所有抽检任务
export const  selectSampleTasksUrl = url + '/task/sampleTask/select';
//设置仓口叉车
export const setWindowRobotsSampleTaskUrl = url + '/task/sampleTask/setWindowRobots';
//获取仓口叉车
export const getWindowRobotsSampleTaskUrl = url + '/task/sampleTask/getWindowRobots';
//扫描料盘
export const scanMaterialUrl = url + '/task/sampleTask/scanMaterial';
//普通仓扫描料盘
export const sampleRegularUWMaterialUrl = url + '/task/sampleTask/sampleRegularUWMaterial';
//抽检报表导出
export const exportSampleTaskInfoUrl = url + '/task/sampleTask/exportSampleTaskInfo';

//帮助页面-上传图片
export const uploadPhotoUrl = url + '/faq/uploadPic';
//上传内容
export const uploadFAQUrl = url + '/faq/uploadFAQ';
//修改内容
export const updateFAQUrl = url + '/faq/updateFAQ';
//查询内容
export const selectFAQUrl = url + '/faq/selectFAQ';
//删除内容
export const deleteFAQUrl = url + '/faq/deleteFAQ';

//打印
export const printUrl = url + '/task/printer/printSingle';

//截料
export const getCuttingMaterialUrl = url + '/task/getCuttingMaterial';
export const getCuttingTaskUrl = url + '/task/getCuttingTask';
export const printCutUrl = url + '/task/printer/print';