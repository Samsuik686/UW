export const setToken = (state, token) => {
  state.token = token;
};
export const setUser = (state, user) => {
    state.user = user;
};
export const setIsCollapse = (state, isCollapse) => {
    state.isCollapse = isCollapse;
};
export const setConfigData = (state,configData) => {
    state.configData.printerIP = configData.printerIP;
};
export const setActiveName = (state, activeName) => {
    state.activeName = activeName;
};
export const setScanFinishBoxId = (state,scanFinishBoxId) => {
    state.scanFinishBoxId = scanFinishBoxId;
};
export const setScanCutBoxId = (state,scanCutBoxId) => {
    state.scanCutBoxId = scanCutBoxId;
};
export const setIsFocus= (state,isFocus) => {
    state.isFocus = isFocus;
};
export const setIsScanner= (state,isScanner) => {
    state.isScanner = isScanner;
};
export const setDisabledMaterialId= (state,disabledMaterialId) => {
    state.disabledMaterialId = disabledMaterialId;
};
export const setPrintMaterialIdArr = (state,printMaterialIdArr) => {
    state.printMaterialIdArr = printMaterialIdArr;
};
export const setUnInventoryData = (state,unInventoryData) => {
    state.unInventoryData = unInventoryData;
};
export const setIsBlur= (state,isBlur) => {
    state.isBlur = isBlur;
};
