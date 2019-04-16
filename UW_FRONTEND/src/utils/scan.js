export const handleScanText = function(text){
   /*sample: 03.01.0001@1000@1531817296428@A008@范例表@A-1@9@2018-07-17@*/
  let tempArray = text.split("@");
  if(!judgeNumber(tempArray[1])){
    return '二维码格式错误，数量必须为正整数';
  }
  if(!judgeTime(tempArray[7])){
    return '二维码格式错误，生产日期格式不对';
  }
  return '';
};
//判断是否为正整数
export const judgeNumber = function isNumber(num) {
  let reg = /^[1-9]*[1-9][0-9]*$/;
  return reg.test(num);
};

//判断是否为日期格式
export const judgeTime = function isTime(time){
  let  regDate = /^[1-9]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$/;  //2019-04-10
  let  regTime = /^[1-9]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\s+(20|21|22|23|[0-1]\d):[0-5]\d:[0-5]\d$/;  //2019-04-10 00:00:00
  if(!regDate.test(time)){
    return regTime.test(time)
  }else{
    return regDate.test(time)
  }
};
