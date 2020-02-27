<template>
  <div class="factory" v-loading="isLoading">
    <el-form :inline="true" :model="factoryInfo" class="supplier-form" @submit.native.prevent>
      <el-form-item>
        <el-button type="primary" icon="el-icon-plus" @click="isAdding=true">新增</el-button>
      </el-form-item>
    </el-form>
    <el-table
        :data="tableData"
        style="width:100%">
      <el-table-column
          label="序号"
          type="index"
          prop="showId">
      </el-table-column>
      <el-table-column
          label="公司编号"
          prop="companyCode">
      </el-table-column>
      <el-table-column
          label="简称"
          prop="nickname">
      </el-table-column>
      <el-table-column
          label="全称"
          prop="name">
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
                    <span style="margin-right:10px;cursor: pointer;" title="修改" @click="handleEdit(scope.row)">
                        <i class="el-icon-coke-edit"></i>
                    </span>
          <span style="cursor: pointer" title="删除" @click="handleDelete(scope.row)">
                        <i class="el-icon-coke-cancel"></i>
                    </span>
        </template>
      </el-table-column>
    </el-table>
    <edit-factory :isEditing.sync="isEditing" :editData="editData"></edit-factory>
    <add-factory :isAdding.sync="isAdding"></add-factory>
  </div>
</template>

<script>
  import {companyDeleteUrl, companySelectUrl, companyUpdateUrl} from "../../../plugins/globalUrl";
  import {axiosPost} from "../../../utils/fetchData";
  import {errHandler} from "../../../utils/errorHandler";
  import EditFactory from "./comp/EditFactory";
  import AddFactory from "./comp/AddFactory";
  import eventBus from '../../../utils/bus';

  export default {
    name: "Factory",
    components: {AddFactory, EditFactory},
    data() {
      return {
        factoryInfo: {
          name: '',
          nickname: '',
          companyCode: ''
        },
        tableData: [],
        isPending: false,
        isLoading: false,
        filter: '',
        isAdding: false,
        isEditing: false,
        editData: {},
      }
    },
    mounted() {
      this.select();
      eventBus.$off('reloadFactory');
      eventBus.$on('reloadFactory', () => {
        this.getCompaniesList().then(data => {
          window.localStorage.setItem('activeCompanyId', data[0].id);
          window.localStorage.setItem('companiesList', JSON.stringify(data));

          this.companies = data;
          let activeCompanyId = window.localStorage.getItem('activeCompanyId');
          this.companies.forEach(item => {
            if (item.id === parseInt(activeCompanyId)) {
              this.company = item.name
            }
          })
        });
      })
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
      }
    },
    methods: {
      initForm: function () {
        Object.assign(this.$data.factoryInfo, this.$options.data().factoryInfo);
      },
      select: function () {
        if (!this.isPending) {
          this.isPending = true;
          this.isLoading = true;
          let options = {
            url: companySelectUrl,
            data: {
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              let data = res.data.data;
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
      handleDelete: function (row) {
        this.$confirm('你正在删除名字为“' + row.name + '”的客户，请确认是否删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          showClose: false,
          type: 'warning',
          center: false
        }).then((action) => {
          if (action === "confirm") {
            this.submitDelete(row);
          }
        }).catch(() => {
          this.$alertInfo("已取消删除");
        });
      },
      submitDelete: function (row) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: companyDeleteUrl,
            data: {
              id: row.id
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess('删除成功');
              this.isPending = false;
              eventBus.$emit("reloadFactory");
              this.select();
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
          }).finally(() => {
            this.isPending = false;
          })
        }
      },
      handleEdit: function (row) {
        this.editData = row;
        this.isEditing = true;
      },
      handlePageSize: function () {
        this.pageNo = 1;
        this.select();
      },

      getCompaniesList() {
        return new Promise(resolve => {
          axiosPost({
            url: companySelectUrl
          }).then(response => {
            if (response.data.result === 200) {
              resolve(response.data.data)
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            resolve([])
          })
        })
      }
    }
  }
</script>

<style scoped lang="scss">
  .factory {
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