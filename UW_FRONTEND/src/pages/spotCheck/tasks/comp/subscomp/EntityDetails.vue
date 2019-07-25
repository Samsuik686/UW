<template>
  <div class="add-panel">
    <div class="form-row justify-content-end add-panel-box">
      <div class="add-panel-container">
        <div class="form-row">
          <div class="form-group mb-0">
            <h5>详情：</h5>
          </div>
        </div>
        <el-table
          :data="sampleTaskDetails"
          style="width:100%;margin-bottom:40px;">
          <el-table-column type="expand">
            <template slot-scope="props">
              <el-table
                border
                :data="props.row.list"
                style="width:100%">
                <el-table-column
                  prop="materialId"
                  label="料盘号">
                </el-table-column>
                <el-table-column
                  prop="quantity"
                  label="数量">
                </el-table-column>
                <el-table-column
                  prop="isSingularString"
                  label="异常出库">
                </el-table-column>
                <el-table-column
                  prop="operator"
                  label="操作员">
                </el-table-column>
                <el-table-column
                  prop="time"
                  label="出库时间">
                </el-table-column>
              </el-table>
            </template>
          </el-table-column>
          <el-table-column
            prop="no"
            label="料号">
          </el-table-column>
          <el-table-column
            prop="storeQuantity"
            label="库存数">
          </el-table-column>
          <el-table-column
            prop="scanQuantity"
            label="扫描总数">
          </el-table-column>
          <el-table-column
            prop="singularOutQuantity"
            label="异常出库数">
          </el-table-column>
          <el-table-column
            prop="regularOutQuantity"
            label="抽检出库数">
          </el-table-column>
        </el-table>
      </div>
      <div id="cancel-btn" class="ml-2 mt-1" @click="closePanel">
        <icon name="cancel" scale="4" style="color: #fff;"></icon>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapActions} from 'vuex';
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import {getSampleTaskDetailsUrl} from "../../../../../config/globalUrl";
  export default {
    name: "EntityDetails",
    props:{
      row:Object
    },
    data() {
      return {
        sampleTaskDetails:[],
        isPending: false
      }
    },
    mounted() {
      this.fetchData();
    },
    methods: {
      ...mapActions(['setLoading']),
      fetchData: function () {
        let options = {
          url: getSampleTaskDetailsUrl,
          data: {
            taskId:this.row.id
          }
        };
        if (!this.isPending) {
          this.isPending = true;
          this.setLoading(true);
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              if(response.data.data.length > 0){
                this.sampleTaskDetails = response.data.data;
              }else{
                this.sampleTaskDetails = [];
              }
            } else {
              errHandler(response.data)
            }
            this.setLoading(false)
          }).catch(err => {
            this.isPending = false;
            console.log(err);
            this.$alertDanger('请求超时，请刷新重试');
            this.setLoading(false)
          })
        }
      },
      closePanel: function () {
        this.$emit('closePanel',true);
      },
    }
  }
</script>

<style scoped>
  .add-panel {
    position:fixed;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 101;
    overflow-y:auto;
  }
  .add-panel-box{
    width:95%;
    max-height:95%;
    box-sizing:border-box;
    display:flex;
  }
  .add-panel-container {
    background: #ffffff;
    min-height: 220px;
    width:90%;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }

  #cancel-btn {
    height: 100%;
    cursor: pointer;
  }
</style>
