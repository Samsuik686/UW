export const setToken = ({commit}, token) => {
  commit('setToken', token)
};
export const setUser = ({commit}, user) => {
    commit('setUser', user)
};
export const setIsCollapse = ({commit}, isCollapse) => {
    commit('setIsCollapse', isCollapse)
};
export const setConfigData = ({commit},configData) => {
    commit('setConfigData',configData)
};
export const setActiveName = ({commit},activeName) => {
    commit('setActiveName',activeName)
};
export const setScanFinishBoxId= ({commit},scanFinishBoxId) => {
    commit('setScanFinishBoxId',scanFinishBoxId);
};
export const setScanCutBoxId= ({commit},scanCutBoxId) => {
    commit('setScanCutBoxId',scanCutBoxId);
};
export const setIsFocus= ({commit},isFocus) => {
    commit('setIsFocus',isFocus);
};
export const setIsScanner= ({commit},isScanner) => {
    commit('setIsScanner',isScanner);
};
export const setDisabledMaterialId= ({commit},disabledMaterialId) => {
    commit('setDisabledMaterialId',disabledMaterialId);
};
export const setPrintMaterialIdArr= ({commit},printMaterialIdArr) => {
    commit('setPrintMaterialIdArr',printMaterialIdArr);
};
export const setUnInventoryData = ({commit},unInventoryData) => {
    commit('setUnInventoryData',unInventoryData);
};
export const setIsBlur= ({commit},isBlur) => {
    commit('setIsBlur',isBlur);
};