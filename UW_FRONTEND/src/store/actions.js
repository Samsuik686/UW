import {checkWindowData, isRefresh} from "./getters";

export const setLoginToken = ({commit}, token) => {
  commit('setLoginToken', token)
};

export const setLogsRouter = ({commit}, logsRouterData) => {
  commit('setLogsRouter', logsRouterData)
};

export const setLoading = ({commit}, isLoading) => {
  commit('setLoading', isLoading)
};

export const setMaterialAdding = ({commit}, isMaterialAdding) => {
  commit('setMaterialAdding', isMaterialAdding)
};
export const setMaterialEditing = ({commit}, isMaterialEditing) => {
  commit('setMaterialEditing', isMaterialEditing)
};
export const setEditData = ({commit}, editData) => {
  commit('setEditData', editData)
};
export const setCopyData = ({commit}, copyData) => {
  commit('setCopyData', copyData)
};
export const setDetailsActiveState = ({commit}, isDetailsActive) => {
  commit('setDetailsActiveState', isDetailsActive)
};
export const setDetailsData = ({commit}, materialDetails) => {
  commit('setDetailsData', materialDetails)
};
export const setTaskActiveState = ({commit}, isTaskDetailsActive) => {
  commit('setTaskActiveState', isTaskDetailsActive)
};
export const setTaskActiveState2 = ({commit},isTaskDetailsActive2) => {
  commit('setTaskActiveState2', isTaskDetailsActive2)
};
export const setTaskData = ({commit}, taskDetails) => {
  commit('setTaskData', taskDetails)
};
export const setUser = ({commit},user) => {
  commit('setUser',user);
};
export const setUserTypeList = ({commit}, userTypeList) => {
  commit('setUserTypeList', userTypeList)
};

export const setCurrentWindow = ({commit}, currentWindowId) => {
  commit('setCurrentWindow', currentWindowId)
};
export const setCurrentOprType = ({commit}, currentOprType) => {
  commit('setCurrentOprType', currentOprType)
};
export const setConfigData = ({commit},configData) => {
  commit('setConfigData',configData)
};
export const setEditMaterialOutRecords = ({commit},obj) => {
  commit('setEditMaterialOutRecords',obj)
};
export const setCheckWindowId = ({commit},checkWindowId) => {
  commit('setCheckWindowId',checkWindowId);
};
export const setCheckWindowData = ({commit},checkWindowData) => {
  commit('setCheckWindowData',checkWindowData);
};
export const setCheckOperationData = ({commit},checkOperationData) => {
  commit('setCheckOperationData',checkOperationData);
};
export const setIsRefresh = ({commit},isRefresh) => {
  commit('setIsRefresh',isRefresh);
};
export const setIsScanner = ({commit},isScanner) => {
  commit('setIsScanner',isScanner);
};
export const setUnInventoryData = ({commit},unInventoryData) => {
  commit('setUnInventoryData',unInventoryData);
};
export const setFocusInput = ({commit},focusInput) => {
  commit('setFocusInput',focusInput);
};
export const setIsOverdueMaterialCheck = ({commit},isOverdueMaterialCheck) => {
  commit('setIsOverdueMaterialCheck',isOverdueMaterialCheck);
};
export const setSpotWindowId = ({commit},spotWindowId) => {
  commit('setSpotWindowId',spotWindowId);
};
export const setSpotMaterialId = ({commit},spotMaterialId) => {
  commit('setSpotMaterialId',spotMaterialId);
};
