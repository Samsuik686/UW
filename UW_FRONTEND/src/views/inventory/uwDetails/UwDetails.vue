<template>
  <div class="uw-details" v-loading="isLoading">
    <el-form :inline="true" class="uw-details-form">
      <el-form-item label="客户">
        <el-select v-model.trim="supplier" placeholder="客户" value="">
          <el-option v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="盘点任务">
        <el-select v-model.trim="taskId" placeholder="盘点任务" value="">
          <el-option v-for="item in tasks" :label="item.file_name" :value='item.id' :key="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="料号">
        <el-input v-model.trim="no" placeholder="料号"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="select">查询</el-button>
        <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
      </el-form-item>
    </el-form>
    <el-table
      @expand-change="toggleRowExpansion"
      :data="tableData"
      style="width:100%">
      <el-table-column type="expand">
        <template slot-scope="props">
          <uw-details-item :row="props.row"></uw-details-item>
        </template>
      </el-table-column>
      <el-table-column
        label="序号"
        prop="showId"
        width="70">
      </el-table-column>
      <el-table-column
        label="料号"
        prop="no"
        min-width="150">
      </el-table-column>
      <el-table-column
        min-width="150"
        label="客户"
        prop="supplier_name">
      </el-table-column>
      <el-table-column
        label="盘前库存"
        prop="before_num">
      </el-table-column>
      <el-table-column
        label="盘点数量"
        prop="actural_num">
      </el-table-column>
      <el-table-column
        label="盘盈/盘亏"
        prop="different_num">
      </el-table-column>
      <el-table-column
        label="盘点人"
        prop="inventory_operatior">
      </el-table-column>
      <el-table-column
        label="审核人"
        prop="check_operator">
      </el-table-column>
      <el-table-column
        label="审核时间"
        min-width="160"
        prop="check_time">
      </el-table-column>
    </el-table>
    <div style="width:100%;text-align:right;margin-top:8px;">
      <el-button type="primary" size="small" @click="checkInventoryData">审核盘点数据</el-button>
      <el-button type="primary" size="small" @click="coverUwMaterial">一键平仓</el-button>
      <el-button type="primary" size="small" @click="exportInventoryReport">导出盘点报表</el-button>
    </div>
  </div>
</template>

<script>
  import {
    checkInventoryTaskUrl, checkRegularTaskUrl, coverRegularUWMaterialOneKeyUrl, coverUwMaterialOneKeyUrl,
    exportUwReportUrl, finishInventoryRegularTaskUrl,
    getInventoryTaskUrl,
    getUwInventoryTaskInfoUrl,
    supplierSelectUrl
  } from "../../../plugins/globalUrl";
  import {axiosPost, downloadFile} from "../../../utils/fetchData";
  import {errHandler} from "../../../utils/errorHandler";
  import UwDetailsItem from "./comp/UwDetailsItem";
  import {mapGetters, mapActions} from 'vuex'

  export default {
    name: "UwDetails",
    components: {UwDetailsItem},
    data() {
      return {
        tableData: [],
        isPending: false,
        isLoading: false,
        suppliers: [],
        tasks: [],
        supplier: '',
        taskId: '',
        no: '',
        activeCompanyId: parseInt(window.localStorage.getItem('activeCompanyId'))
      }
    },
    created() {
      this.selectSupplier();
    },
    watch: {
      supplier: function (val) {
        if (val !== '') {
          this.tasks = [];
          this.taskId = '';
          this.tableData = [];
          this.getInventoryTask(val);
        }
      },
      taskId: function (val) {
        if (val !== '') {
          this.select();
        }
      }
    },
    methods: {
      selectSupplier: function () {
        let options = {
          url: supplierSelectUrl,
          data: {
            filter: 'company.id=' + this.activeCompanyId
          }
        };
        axiosPost(options).then(res => {
          if (res.data.result === 200) {
            let data = res.data.data.list;
            data.map((item) => {
              if (item.enabled === true) {
                this.suppliers.push(item);
              }
            });
          } else {
            errHandler(res.data)
          }
        }).catch(err => {
          console.log(err);
          this.$alertError('连接超时，请刷新重试');
        })
      },
      getInventoryTask: function (supplierId) {
        if (!this.isPending) {
          this.isPending = false;
          let options = {
            url: getInventoryTaskUrl,
            data: {
              supplierId: supplierId,
              warehouseType: 0
            }
          };
          axiosPost(options).then(response => {
            this.tasks = response.data.data;
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            this.isPending = false;
          })
        }
      },
      setStateString: function (state) {
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
      select: function () {
        if (!this.isPending) {
          this.isPending = true;
          this.isLoading = true;
          let options = {
            url: getUwInventoryTaskInfoUrl,
            data: {
              taskId: this.taskId,
              no: this.no
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              let data = res.data.data;
              data.map((item, index) => {
                item.showId = index + 1;
                item.stateString = this.setStateString(item.state);
              });
              this.tableData = data;
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            this.isPending = false;
            this.isLoading = false;
          })
        }
      },
      initForm: function () {
        this.no = '';
      },
      toggleRowExpansion: function (row, expanded) {
        if (expanded.length === 0) {
          this.select();
        }
      },
      exportInventoryReport: function () {
        if (this.taskId === '') {
          this.$alertWarning('盘点任务不能为空');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            taskId: this.taskId,
            no: this.no,
            '#TOKEN#': this.$store.state.token
          };
          downloadFile(exportUwReportUrl, data);
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
      },
      checkInventoryData: function () {
        if (this.taskId === '') {
          this.$alertWarning('请先选择盘点任务');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          this.isLoading = true;
          let options = {
            url: checkRegularTaskUrl,
            data: {
              taskId: this.taskId
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess('审核成功');

              this.isPending = false;
              this.select();
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            this.isPending = false;
            this.isLoading = false;
          })
        }
      },
      coverUwMaterial: function () {
        if (this.taskId === '') {
          this.$alertWarning('请先选择盘点任务');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          this.isLoading = true;
          let options = {
            url: coverRegularUWMaterialOneKeyUrl,
            data: {
              taskId: this.taskId
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess(res.data.data);
              this.isPending = false;
              this.select();
              //this.confirmFinishTask();
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            this.isPending = false;
            this.isLoading = false;
          })
        }
      },
      confirmFinishTask: function () {
        this.$confirm('请点击“完成任务”按钮完成盘点任务?', '提示', {
          confirmButtonText: '完成任务',
          cancelButtonText: '取消',
          type: 'warning'
        }).then((action) => {
          if (action === "confirm") {
            this.finishTask();
          }
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消'
          });
        });
      },
      finishTask: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: finishInventoryRegularTaskUrl,
            data: {
              taskId: this.taskId
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess('操作成功');
              this.isPending = false;
              this.select();
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            this.isPending = false;
          })
        }
      }
    }
  }
</script>

<style scoped lang="scss">
  .uw-details {
    box-sizing: border-box;
    width: 100%;
    height: 100%;
    padding: 30px 30px;
    background: #fff;
    border-radius: 6px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow-y: auto;

    .highLight {
      color: red;
    }
  }
</style>