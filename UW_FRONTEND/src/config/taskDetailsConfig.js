export const getTaskDetailsConfig = (name) => {
  if (name === 'io'){
    return IO_CONFIG;
  }
  if (name === 'io2'){
    return IO_CONFIG2;
  }
};

const IO_CONFIG = [
  {field: 'id', title: 'ID', colStyle: {'width': '60px'}},
  {field: 'materialNo', title: '料号', colStyle: {'width': '120px'}},
  {field: 'planQuantity', title: '计划数量', colStyle: {'width': '90px'}},
  {field: 'actualQuantity', title: '实际数量', colStyle: {'width': '90px'}},
  {field: 'deductQuantity', title: '抵扣数', colStyle: {'width': '90px'}},
  {field: 'lackNum', title: '欠料数量', colStyle: {'width': '90px'}},
  {field: 'finishTime', title: '完成时间', colStyle: {'width': '120px'}},
  {field: 'status', title: '状态', tdComp: 'ShowStatus', colStyle: {'width': '90px'}},
  {field: 'operation', title: '操作', tdComp: 'SubsOperation', colStyle: {'width': '90px'}},
];

const IO_CONFIG2 = [
  {field: 'id', title: 'ID', colStyle: {'width': '60px'}},
  {field: 'materialNo', title: '料号', colStyle: {'width': '120px'}},
  {field: 'planQuantity', title: '计划数量', colStyle: {'width': '90px'}},
  {field: 'uwStoreNum', title: 'UW库存', colStyle: {'width': '90px'}},
  {field: 'whStoreNum', title: '物料仓库存', colStyle: {'width': '90px'}},
  {field: 'actualQuantity', title: '实际数量', colStyle: {'width': '90px'}},
  {field: 'status', title: '状态', colStyle: {'width': '70px'},tdComp: 'HighLight'},
  {field: 'finishTime', title: '完成时间', colStyle: {'width': '120px'}}
];
