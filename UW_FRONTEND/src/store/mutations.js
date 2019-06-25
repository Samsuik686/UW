import {isRefresh} from "./getters";

export const setLoginToken = (state, token) => {
  state.token = token;
};

export const setUser = (state,user) => {
  state.user = user;
};

export const setLogsRouter = (state, logsRouterData) => {
  state.logsRouterApi = logsRouterData;
};

export const setLoading = (state, isLoading) => {
  state.isLoading = isLoading;
};
export const setDetailsActiveState = (state, isDetailsActive) => {
  state.isDetailsActive = isDetailsActive
};
export const setDetailsData = (state, materialDetails) => {
  state.materialDetails = materialDetails
};
export const setTaskActiveState = (state, isTaskDetailsActive) => {
  state.isTaskDetailsActive = isTaskDetailsActive
};
export const setTaskActiveState2 = (state, isTaskDetailsActive2) => {
  state.isTaskDetailsActive2 = isTaskDetailsActive2
};
export const setTaskData = (state, taskDetails) => {
  state.taskDetails = taskDetails
};

export const setUserTypeList = (state, userTypeList) => {
  state.userTypeList = userTypeList
};

export const setCurrentWindow = (state, currentWindowId) => {
  state.currentWindowId = currentWindowId
};
export const setCurrentOprType = (state, currentOprType) => {
  state.currentOprType = currentOprType
};
export const setConfigData = (state,configData) => {
  state.configData.printerIP = configData.printerIP;
};
export const setEditMaterialOutRecords = (state,obj) => {
  state.editMaterialOutRecords[obj.id] = obj.materialOutRecords;
};
export const setCheckWindowId = (state,checkWindowId) => {
  state.checkWindowId = checkWindowId;
};
export const setCheckWindowData = (state,obj) => {
  state.checkWindowData[obj.id] = obj.data;
};

export const setCheckOperationData = (state,obj) => {
  state.checkOperationData = obj
};
export const setIsRefresh = (state,isRefresh) => {
  state.isRefresh = isRefresh;
};
export const setIsScanner = (state,isScanner) => {
  state.isScanner = isScanner;
};
export const setUnInventoryData = (state,unInventoryData) => {
  state.unInventoryData = unInventoryData;
};
export const setFocusInput = (state,focusInput) => {
  state.focusInput = focusInput;
};
export const setIsOverdueMaterialCheck = (state,isOverdueMaterialCheck) => {
  state.isOverdueMaterialCheck = isOverdueMaterialCheck;
};
export const setSpotWindowId = (state,spotWindowId) => {
  state.spotWindowId = spotWindowId;
};
export const setSpotMaterialId = (state,spotMaterialId) => {
  state.spotMaterialId = spotMaterialId;
};
