/**
 * @return {string}
 */
export const addBox = function(obj){
    if(obj.area === ''){
        return '区域不能为空';
    }
    if(obj.isStandard === ''){
        return '料盒类型不能为空';
    }
    if(obj.supplierId === ''){
        return '供应商不能为空';
    }
    if(obj.row === ''){
        return '行号不能为空';
    }
    if(!judge(obj.row)){
        return '行号必须为正整数';
    }
    if(!judgeCodeLen11(obj.row)){
        return '行号长度不能超过11位';
    }
    if(obj.col === ''){
        return '列号不能为空';
    }
    if(!judge(obj.col)){
        return '列号必须为正整数';
    }
    if(!judgeCodeLen11(obj.col)){
        return '列号长度不能超过11位';
    }
    if(obj.height === ''){
        return '高度不能为空';
    }
    if(!judge(obj.height)){
        return '高度必须为正整数';
    }
    if(!judgeCodeLen11(obj.height)){
        return '高度长度不能超过11位';
    }
    return '';
};

export const handleUser = function(obj){
    if(obj.uid === ''){
        return '用户名不能为空';
    }
    if(obj.name === ''){
        return '用户描述不能为空';
    }
    if(obj.password === ''){
        return '用户密码不能为空';
    }
    if(obj.type === ''){
        return '用户类型不能为空';
    }
    if(obj.enabled === ''){
        return '请选择是否启用';
    }
    return '';
};

export const handleUwMaterial = function(obj){
    if(obj.no === ''){
        return '料号不能为空';
    }
    if(obj.specification === ''){
        return '规格不能为空';
    }
    if(obj.supplierId === ''){
        return '供应商不能为空';
    }
    if(obj.thickness === ''){
        return '厚度不能为空';
    }
    if(!judge(obj.thickness)){
        return '厚度必须为正整数';
    }
    if(obj.radius === ''){
        return '直径不能为空';
    }
    if(!judge(obj.radius)){
        return '直径必须为正整数';
    }
    return ''
};

export const handleUploadEwhMaterial = function(obj){
    if(obj.supplierId === ''){
        return '供应商不能为空';
    }
    if(obj.sourceWhId === ''){
        return '来源地不能为空';
    }
    if(obj.destinationwhId === ''){
        return '目的地不能为空';
    }
    if(obj.sourceWhId === obj.destinationwhId){
        return '来源地和目的地不能相同';
    }
    if(obj.thisFile === ''){
        return '文件不能为空';
    }
    if(obj.remarks === ''){
        return '备注不能为空';
    }
    return ''
};

export const handleAddWastage = function(obj){
    if(obj.supplierId === ''){
        return '供应商不能为空';
    }
    if(obj.whId === ''){
        return '所在仓库不能为空';
    }
    if(obj.thisFile === ''){
        return '文件不能为空';
    }
    if(obj.remarks === ''){
        return '备注不能为空';
    }
    return ''
};

export const handleEditWastage = function (obj) {
    if(obj.quantity === ''){
        return '数量不能为空';
    }
    if(!judge(obj.quantity) && obj.quantity !== "0" ){
        return '数量格式不对';
    }
    if(obj.remarks === ''){
        return '备注不能为空';
    }
    return '';
};

export const handleEditQuantity = function(obj){
    if(obj.atrualNum === '' || obj.atrualNum === null){
        return '盘点数量不能为空';
    }
    if(!judge(obj.atrualNum) && Number(obj.atrualNum) !== 0 ){
        return '盘点数量必须为非负整数';
    }
    if(obj.returnNum === '' || obj.returnNum === null){
        return '调拨多出数量不能为空';
    }
    if(!judge(obj.returnNum) && Number(obj.returnNum) !== 0 ){
        return '调拨多出数量必须为非负整数';
    }
    return '';
};

//判断是否为正整数
export const judge = function isNumber(num) {
    let val = num;
    let reg = /^[1-9]*[1-9][0-9]*$/;
    if (val !== "") {
        return reg.test(val);
    }
};

//判断字符长度不超过11
export const judgeCodeLen11 = function (str) {
    let len = 0;
    for (let i = 0; i < str.length; i++) {
        let c = str.charCodeAt(i);
        //单字节加1
        if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
            len++;
        }
        else {
            len += 2;
        }
    }
    return len < 11;
};

//判断字符长度不超过32
export const judgeCodeLen256 = function (str) {
    let len = 0;
    for (let i = 0; i < str.length; i++) {
        let c = str.charCodeAt(i);
        //单字节加1
        if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
            len++;
        }
        else {
            len += 2;
        }
    }
    return len <= 256;
};
