<template>
  <div class="main-details mt-1 mb-3">
    <div class="form-group row">
      <div class="btn btn-primary ml-3 mr-4" @click="isUploadCheck = true">导入物料仓盘点数据</div>
    </div>
    <datatable
      v-bind="$data"
    ></datatable>
    <div class="form-group row check-btn">
      <div class="btn btn-primary" @click="checkInventoryData">审核盘点数据</div>
      <div class="btn btn-primary ml-3 mr-4" @click="exportCheckReport">导出盘点报表</div>
    </div>
    <check-details v-if="isShow" :row="row"></check-details>
    <upload-check-task v-if="isUploadCheck" :taskId = "taskId"></upload-check-task>
  </div>
</template>

<script>
  import {mapActions,mapGetters} from 'vuex'
  import CheckDetails from './subscomp/CheckDetails'
  import eventBus from "../../../../utils/eventBus";
  import {
    checkInventoryTaskUrl,
    exportEWhReportInventoryUrl,
    getInventoryTaskInfoUrl
  } from "../../../../config/globalUrl";
  import {axiosPost, downloadFile} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import UploadCheckTask from './subscomp/UploadCheckTask'
  import OperationOptions from './subscomp/OperationOptions'
  import MaterialHighLight from './subscomp/MaterialHighLight'
  export default {
    name: "TableDetails",
    created() {
      this.init();
    },
    components: {
      CheckDetails,
      UploadCheckTask,
      OperationOptions,
      MaterialHighLight
    },
    mounted() {
      eventBus.$on('showCheckDetails', row => {
        this.row = row;
        this.isShow = true;
      });
      eventBus.$on('closeCheckPanel', () => {
        this.isShow = false;
        this.getCheckData();
      });
      eventBus.$on('closeUploadPanel',() => {
        this.isUploadCheck = false;
      });
    },
    data() {
      return {
        fixHeaderAndSetBodyMaxHeight: 650,
        tblStyle: {
          'font-size': '14px',
          'word-break': 'break-all',
          'table-layout': 'fixed',
          'white-space': 'pre-wrap'
        },
        HeaderSettings: false,
        pageSizeOptions: [20, 40, 80, 100],
        Pagination:false,
        data: [],
        columns: [],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,
        isShow: false,
        row: {},
        taskId: '',
        no: '',
        isUploadCheck:false
      }
    },
    watch: {
      $route: function (route) {
        this.init();
        this.setLoading(true);
        let options = {
          url: getInventoryTaskInfoUrl,
          data: {}
        };
        if (route.query.taskId) {
          this.taskId = route.query.taskId;
        }else{
          this.taskId = '';
        }
        if (route.query.no) {
          this.no = route.query.no
        }else{
          this.no = '';
        }
        if(this.taskId === ''){
          this.setLoading(false);
          return;
        }
        options.data.taskId = this.taskId;
        options.data.no = this.no;
        this.fetchData(options)
      },
    },
    computed:{
      ...mapGetters(['unInventoryData'])
    },
    methods: {
      ...mapActions(['setLoading','setUnInventoryData']),
      init: function () {
        this.columns = [
          {title: '', tdComp: 'OperationOptions', colStyle: {'width': '60px'}},
          {field: 'showId', title: '序号', colStyle: {'width': '60px'}},
          //{field: 'stateString', title: '状态', colStyle: {'width': '70px'}},
          {field: 'no', title: '料号', tdComp:'MaterialHighLight',colStyle: {'width': '100px'}},
          {field: 'supplier_name', title: '供应商', colStyle: {'width': '80px'}},
          //{field: 'specification', title: '规格', colStyle: {'width': '120px'}},
          {field: 'before_num', title: '盘前库存', colStyle: {'width': '80px'}},
          {field: 'actural_num', title: '盘点数量', colStyle: {'width': '80px'}},
          {field: 'different_num', title: '盘盈/盘亏', colStyle: {'width': '80px'}},
          {field: 'inventory_operatior', title: '盘点人', colStyle: {'width': '70px'}},
          {field: 'start_time', title: '盘点开始时间', colStyle: {'width': '100px'}},
          {field: 'end_time', title: '盘点结束时间', colStyle: {'width': '100px'}},
          {field: 'checked_operatior', title: '审核人', colStyle: {'width': '80px'}},
          {field: 'checked_time', title: '审核时间', colStyle: {'width': '100px'}}
        ];
        this.total = 0;
        this.query = {"limit": 20, "offset": 0};
        this.data = [];
      },
      fetchData: function (options) {
        if (!this.isPending) {
          this.isPending = true;
          axiosPost(options).then(response => {
            this.setLoading(false);
            this.isPending = false;
            if (response.data.result === 200) {
              this.data = response.data.data;
              this.data.map((item, index) => {
                item.showId = index + 1 + this.query.offset;
                item.stateString = this.setStateString(item.state);
                item.isHightLight = false;
              });
              this.judgeUnInventoryData();
            } else {
              errHandler(response.data)
            }
          })
            .catch(err => {
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试');
              this.setLoading(false);
            })
        }
      },
      checkInventoryData:function(){
        if(this.taskId === ''){
          this.$alertWarning('请先选择盘点任务');
          return;
        }
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:checkInventoryTaskUrl,
            data:{
              taskId:this.taskId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              if(res.data.data === "操作成功"){
                this.$alertSuccess('审核成功');
                this.setUnInventoryData([]);
              }else{
                this.$alertWarning('存在物料未盘点');
                let data = res.data.data;
                let arr = data.split('[')[1].split(']')[0].split(',');
                this.setUnInventoryData(arr);
              }
              this.getCheckData();
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
          })
        }
      },
      setStateString:function(state){
        let stateString = '';
        switch (state) {
          case 0:
            stateString = '未审核';
            break;
          case 1:
            stateString = '未开始';
            break;
          case 2:
            stateString = '进行中';
            break;
          case 3:
            stateString = '已完成';
            break;
          case 4:
            stateString = '已作废';
            break;
          case 5:
            stateString = '存在缺料';
            break;
          default:
            break;
        }
        return stateString;
      },
      getCheckData:function(){
        if(this.taskId === ''){
          return;
        }
        this.setLoading(true);
        let options = {
          url: getInventoryTaskInfoUrl,
          data: {
            taskId:this.taskId,
            no:this.no
          }
        };
        this.fetchData(options);
      },
      judgeUnInventoryData:function(){
        if(this.unInventoryData.length === 0){
          for(let i =0;i<this.data.length;i++){
            this.data[i].isHightLight = false;
          }
          return;
        }
        for(let i=0;i<this.data.length;i++){
          let obj = this.data[i];
          for(let j=0;j<this.unInventoryData.length;j++){
            if(Number(obj.material_type_id) === Number(this.unInventoryData[j])){
              obj.isHightLight = true;
              break;
            }
          }
        }
      },
      exportCheckReport:function(){
        if(this.taskId === ''){
          this.$alertWarning('盘点任务不能为空');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            taskId:this.taskId,
            no:this.no,
            '#TOKEN#': this.$store.state.token
          };
          downloadFile(exportEWhReportInventoryUrl, data);
          let count = 0;
          let mark = setInterval(() => {
            count++;
            if (count > 9) {
              count = 0;
              clearInterval(mark);
              this.isPending = false
            }
          }, 1000);
          this.$alertSuccess('请求成功，请等待下载');
        } else {
          this.$alertInfo('请稍后再试')
        }
      }
    }
  }
</script>

<style scoped>
  .main-details {
    position: relative;
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 25px 20px 80px 20px;
    min-height: 500px;
  }

  .check-btn {
    position: absolute;
    bottom: 0;
    right:40px;
  }
</style>
