<template>
  <div class="materialDetails" v-loading="isLoading">
    <el-form :inline="true" :model="materialDetailsInfo" class="uwMaterial-form">
      <el-form-item label="料号">
        <el-input clearable v-model.trim="materialDetailsInfo.no" placeholder="料号"></el-input>
      </el-form-item>
      <el-form-item label="仓库类型*">
        <el-select clearable v-model.trim="materialDetailsInfo.warehouseType" placeholder="仓库" value="">
          <el-option label="普通仓" selected="selected" :value='0'></el-option>
          <el-option label="贵重仓" :value='1'></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="仓库" v-if="materialDetailsInfo.warehouseType === 0">
        <el-select clearable v-model.trim="materialDetailsInfo.whId" placeholder="仓库" value="">
          <el-option label="未选择" selected="selected" value=''></el-option>
          <el-option v-for="item in destinations" :label="item.name" :value='item.id' :key="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="客户*">
        <el-select clearable v-model.trim="materialDetailsInfo.supplierId" placeholder="客户" value="">
          <el-option label="未选择" selected="selected" value=''></el-option>
          <el-option v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间*">
        <el-date-picker
          :clearable="true"
          v-model="queryTimeRange"
          type="datetimerange"
          align="right"
          value-format="yyyy-MM-dd HH:mm:ss"
          :picker-options="pickerOptions"
          range-separator="-"
          :default-time="['00:00:00','23:59:59']"
          start-placeholder="开始日期"
          end-placeholder="结束日期">
        </el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="queryData">查询</el-button>
        <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
      </el-form-item>
      <span class="query-comment">"*"项目为必填项</span>
    </el-form>
    <el-table
      :data="tableData"
      style="width:100%"
      default-expand-all>
      <!---->
      <el-table-column type="expand">
        <template slot-scope="scope">
          <el-table :data="scope.row.list" border>
            <el-table-column type="expand">
              <!---->
              <template slot-scope="_scope">
                <el-table :data="_scope.row.details" border>
                  <el-table-column
                    label="操作类型"
                    prop="operationType">
                  </el-table-column>
                  <el-table-column
                    label="入库数量"
                    prop="numberInStock">
                  </el-table-column>
                  <el-table-column
                    label="出库数量"
                    prop="numberOutStock">
                  </el-table-column>
                </el-table>
              </template>
            </el-table-column>
            <el-table-column
              label="仓库"
              prop="warehouse">
            </el-table-column>
            <el-table-column
              label="上期结存数量"
              prop="oldBalance">
            </el-table-column>
            <el-table-column
              label="本期结存数量"
              prop="currentBalance">
            </el-table-column>
            <el-table-column
              label="入库数量"
              prop="numberInStock">
            </el-table-column>
            <el-table-column
              label="出库数量"
              prop="numberOutStock">
            </el-table-column>
          </el-table>

        </template>
      </el-table-column>
      <el-table-column
        label="序号"
        prop="showId"
        width="70">
      </el-table-column>
      <el-table-column
        sortable="custom"
        min-width="140"
        label="料号"
        prop="no">
      </el-table-column>
      <el-table-column
        sortable="custom"
        min-width="140"
        label="规格"
        prop="specification">
      </el-table-column>
      <!--<el-table-column
        sortable="custom"
        min-width="140"
        label="客户"
        prop="supplierName">
      </el-table-column>-->
      <el-table-column
        label="上期结存数量"
        prop="oldBalance">
      </el-table-column>
      <el-table-column
        label="本期结存数量"
        prop="currentBalance">
      </el-table-column>
      <el-table-column
        label="入库数量"
        prop="numberInStock">
      </el-table-column>
      <el-table-column
        label="出库数量"
        prop="numberOutStock">
      </el-table-column>
    </el-table>
    <div class="block">
      <el-pagination
        background
        :current-page.sync="pageNo"
        :page-size.sync="pageSize"
        :page-sizes="[20,40,80,100]"
        @size-change="_pageSizeChange(queryData)"
        @current-change="queryData"
        layout="total,sizes,prev,pager,next,jumper"
        :total="totalData">
        >
      </el-pagination>
    </div>
  </div>
</template>

<script>
  import mixin from '../mixin'
  import {selectMaterialDetailsUrl} from "../../../plugins/globalUrl";

  export default {
    name: "MaterialDetails",
    mixins: [mixin],
    data() {
      return {
        materialDetailsInfo: {
          no: '',
          warehouseType: 0,
          whId: '',
          supplierId: '',
          startTime: '',
          endTime: '',
        },
        queryTimeRange: []

      }
    },
    created() {
      this._selectSupplier();
      this._selectDestination();
    },
    methods: {
      initForm: function() {
        Object.assign(this.$data.materialDetailsInfo, this.$options.data().materialDetailsInfo);
        this.queryTimeRange = [];
      },
      queryData: function () {
        let info = this.materialDetailsInfo;
        info.startTime = this.queryTimeRange[0];
        info.endTime = this.queryTimeRange[1];

        //验证必填项目
        if (!info.supplierId || !info.startTime || !info.endTime) {
          this.$alertInfo('条件填写不完整');
          return;
        }

        this._selectData(selectMaterialDetailsUrl, this.materialDetailsInfo).then(data => {
          data.list.map((item, index) => {
            item.showId = index + 1 + (this.pageNo - 1) * this.pageSize;
          });
          this.totalData = data.totalRow;
          this.tableData = data.list;
        })
      },

    }
  }
</script>

<style scoped lang="scss">
  .materialDetails {
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
    .query-comment {
      font-size: 12px;
      line-height: 56px;
      color: #8e8e8e;
      vertical-align: bottom;
    }
  }
</style>