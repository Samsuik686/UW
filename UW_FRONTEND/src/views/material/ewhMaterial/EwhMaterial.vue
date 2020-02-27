<template>
  <div class="ewhMaterial" v-loading="isLoading">
    <el-form :inline="true" :model="ewhMaterialInfo" class="ewhMaterial-form">
      <el-form-item label="料号">
        <el-input v-model.trim="ewhMaterialInfo.no" placeholder="料号"></el-input>
      </el-form-item>
      <el-form-item label="仓库">
        <el-select v-model.trim="ewhMaterialInfo.whId" placeholder="仓库" value="">
          <el-option label="不限" selected="selected" value=''></el-option>
          <el-option v-for="item in destinations" :label="item.name" :value='item.id' :key="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="客户">
        <el-select v-model.trim="ewhMaterialInfo.supplierId" placeholder="客户" value="">
          <el-option label="不限" selected="selected" value=''></el-option>
          <el-option v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button type="primary" icon="el-icon-search" @click="setFilter">查询</el-button>
        <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
        <el-button type="primary" icon="el-icon-upload" @click="isUploading = true">导入调拨单</el-button>
        <el-button type="primary" icon="el-icon-upload" @click="isAdding = true">导入损耗单</el-button>
        <el-button type="primary" icon="el-icon-download" @click="exportEWhReport">导出库存单</el-button>
      </el-form-item>
    </el-form>
    <el-table
      @sort-change="sortChange"
      :data="tableData"
      style="width:100%">
      <el-table-column type="expand">
        <template slot-scope="props">
          <entity-details :row="props.row"></entity-details>
        </template>
      </el-table-column>
      <el-table-column
        label="序号"
        prop="showId"
        width="70">
      </el-table-column>
      <el-table-column
        sortable="custom"
        label="所在仓库"
        prop="wareHouse">
      </el-table-column>
      <el-table-column
        sortable="custom"
        label="料号"
        prop="no">
      </el-table-column>
      <el-table-column
        sortable="custom"
        label="客户"
        prop="supplier">
      </el-table-column>
      <el-table-column
        sortable="custom"
        label="规格"
        prop="specification">
      </el-table-column>
      <el-table-column
        label="盘前数量"
        prop="inventoryBeforeQuantity">
      </el-table-column>
      <el-table-column
        label="盘后数量"
        prop="inventoryAfterQuantity">
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
                    <span style="margin-right:10px;cursor: pointer;" title="填写损耗" @click="handleEdit(scope.row)">
                        <i class="el-icon-coke-edit"></i>
                    </span>
        </template>
      </el-table-column>
    </el-table>
    <div class="block">
      <el-pagination
        background
        :current-page.sync="pageNo"
        :page-size.sync="pageSize"
        :page-sizes="[20,40,80,100]"
        @size-change="handlePageSize"
        @current-change="select"
        layout="total,sizes,prev,pager,next,jumper"
        :total="totallyData">
        >
      </el-pagination>
    </div>
    <upload-ewh-material
      :isUploading.sync="isUploading"
      :origins="origins"
      :suppliers="suppliers"
      :destinations="destinations"></upload-ewh-material>
    <add-wastage :is-adding.sync="isAdding" :suppliers="suppliers" :destinations="destinations"></add-wastage>
    <edit-wastage :is-editing.sync="isEditing" :edit-data="editData"></edit-wastage>
  </div>
</template>

<script>
  import {
    destinationSelectUrl, exportEWhReportUrl,
    externalWhSelectExternalWhInfoUrl,
    supplierSelectUrl
  } from "../../../plugins/globalUrl";
  import {axiosPost, downloadFile} from "../../../utils/fetchData";
  import {errHandler} from "../../../utils/errorHandler";
  import UploadEwhMaterial from "./comp/UploadEwhMaterial";
  import AddWastage from "./comp/AddWastage";
  import EditWastage from "./comp/EditWastage";
  import EntityDetails from './comp/EntityDetails'

  export default {
    name: "EwhMaterial",
    components: {EditWastage, AddWastage, UploadEwhMaterial, EntityDetails},
    data() {
      return {
        ewhMaterialInfo: {
          no: '',//料号
          whId: '',//仓库
          supplierId: ''//客户
        },
        suppliers: [],
        origins: [],
        destinations: [],
        tableData: [],
        pageNo: 1,
        pageSize: 20,
        totallyData: 0,
        isPending: false,
        isLoading: false,
        isAdding: false,
        isEditing: false,
        isExporting: false,
        isUploading: false,
        editData: {},
        selection: [],
        checked: false,
        day: 90,
        ascBy: '',
        descBy: '',
        activeCompanyId: parseInt(window.localStorage.getItem('activeCompanyId'))
      }
    },
    watch: {
      isEditing: function (val) {
        if (val === false) {
          this.select();
        }
      },
      isAdding: function (val) {
        if (val === false) {
          this.select();
        }
      },
      isUploading: function (val) {
        if (val === false) {
          this.select();
        }
      }
    },
    created() {
      this.selectSupplier();
      this.selectDestination();
    },
    mounted() {
      this.select();
    },
    methods: {
      select: function () {
        if (!this.isPending) {
          this.isPending = true;
          this.isLoading = true;
          let options = {
            url: externalWhSelectExternalWhInfoUrl,
            data: {
              no: this.ewhMaterialInfo.no,
              whId: this.ewhMaterialInfo.whId,
              supplierId: this.ewhMaterialInfo.supplierId,
              companyId: this.activeCompanyId,
              ascBy: this.ascBy,
              descBy: this.descBy,
              pageNo: this.pageNo,
              pageSize: this.pageSize
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              let data = res.data.data.list;
              data.map((item, index) => {
                item.showId = index + 1 + (this.pageNo - 1) * this.pageSize;
              });
              this.tableData = data;
              this.totallyData = res.data.data.totalRow;
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
      setFilter: function () {
        this.pageNo = 1;
        this.pageSize = 20;
        this.select();
      },
      initForm: function () {
        Object.assign(this.$data.ewhMaterialInfo, this.$options.data().ewhMaterialInfo);
      },
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
      selectDestination: function () {
        let options = {
          url: destinationSelectUrl,
          data: {
            filter: 'company.id=' + this.activeCompanyId
          }
        };
        axiosPost(options).then(res => {
          if (res.data.result === 200) {
            let data = res.data.data.list;
            let list = [];
            data.map((item) => {
              if (item.id !== 0 && item.id !== -1) {
                list.push(item);
              }
            });
            this.origins = data;
            this.destinations = list;
          } else {
            errHandler(res.data);
          }
        }).catch(err => {
          console.log(err);
          this.$alertError('连接超时，请刷新重试');
        })
      },
      handleEdit: function (row) {
        this.editData = row;
        this.isEditing = true;
      },
      exportEWhReport: function () {
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            whId: this.ewhMaterialInfo.whId,
            supplierId: this.ewhMaterialInfo.supplierId,
            companyId: this.activeCompanyId,
            no: this.ewhMaterialInfo.no,
            '#TOKEN#': this.$store.state.token
          };
          downloadFile(exportEWhReportUrl, data);
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
      handlePageSize: function () {
        this.pageNo = 1;
        this.select();
      },
      sortChange: function (data) {
        let prop = data.prop;
        switch (data.prop) {
          case "wareHouse":
            prop = "wh_name";
            break;
          case "supplier":
            prop = "supplier_name";
            break;
          default:
            prop = data.prop;
            break;
        }
        if (data.order === "ascending") {
          this.ascBy = prop;
          this.descBy = '';
        } else if (data.order === "descending") {
          this.descBy = prop;
          this.ascBy = '';
        } else {
          this.descBy = '';
          this.ascBy = '';
        }
        this.pageNo = 1;
        this.select();
      }
    }
  }
</script>

<style scoped lang="scss">
  .ewhMaterial {
    box-sizing: border-box;
    width: 100%;
    height: 100%;
    padding: 30px 30px;
    background: #fff;
    border-radius: 6px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow-y: auto;

    .block {
      margin-top: 15px;
    }
  }
</style>