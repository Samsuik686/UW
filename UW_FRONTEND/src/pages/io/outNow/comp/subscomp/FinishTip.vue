<template>
  <el-dialog
    title="提示"
    :visible.sync="dialogVisible"
    :show-close="isCloseOnModal"
    :close-on-click-modal="isCloseOnModal"
    :close-on-press-escape="isCloseOnModal"
    width="30%">
    <p>该任务计划出库数量: {{taskItem.planQuantity}}</p>
    <p>实际出库数量: {{taskItem.actualQuantity}}</p>
    <p>{{overQuantity(taskItem.planQuantity, taskItem.actualQuantity)}}</p>
    <div class="dropdown-divider"></div>
    <p v-if="taskItem.planQuantity - taskItem.actualQuantity>0">
      当前实际出库数少于计划数，如果要将该任务条目置为已完成，请点击“确认完成”按钮</p>
    <p v-else>请确定是否出库</p>
    <span slot="footer" class="dialog-footer">
      <el-button type="info" @click="cancel" size="mini">取 消</el-button>
      <el-button type="warning"  size="mini" @click="delay"
                 :disabled="isHide">稍后再见</el-button>
      <el-button type="primary" size="mini" @click="submit">确认完成</el-button>
    </span>
  </el-dialog>
</template>

<script>
  import {robotBackUrl, taskFinishUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "FinishTip",
    props: {
      taskItem: Object,
      state: Number,
      isFinishTip: Boolean
    },
    data() {
      return {
        dialogVisible: false,
        isPending: false,
        isCloseOnModal: false,
        isHide: false
      }
    },
    watch: {
      isFinishTip: function (val) {
        this.dialogVisible = val === true;
        if (val === true) {
          for (let i = 0; i < this.taskItem.details.length; i++) {
            let item = this.taskItem.details[i];
            if (item.remainderQuantity !== item.quantity) {
              this.isHide = true;
              return;
            }
          }
        }
        this.isHide = false;
      }
    },
    methods: {
      overQuantity: function (plan, actual) {
        let overQty = plan - actual;
        if (plan > actual) {
          return ("缺发数量: " + Math.abs(overQty)).toString();
        } else if (plan < actual) {
          return ("超发数量: " + Math.abs(overQty)).toString();
        } else {
          return "--"
        }
      },
      setFinishItem: function (boolean, callback) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: taskFinishUrl,
            data: {
              packListItemId: this.taskItem.id,
              isFinish: boolean
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              callback();
            } else {
              errHandler(response.data)
            }
          }).catch(err => {
            this.isPending = false;
            console.log(err);
          })
        }
      },
      submit: function () {
        this.$emit('setIsFinishTip', true);
        this.setBack(false);
      },
      delay: function () {
        this.$emit('setIsFinishTip', true);
        this.setBack(true);
      },
      setBack: function (isLater) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: robotBackUrl,
            data: {
              id: this.taskItem.id,
              state: this.state,
              isLater: isLater
            }
          };
          axiosPost(options).then(response => {
            if (response.data.result === 200) {
              this.$alertSuccess('操作成功');
            } else {
              errHandler(response.data);
            }
            this.isPending = false;
            this.state = 1;
          }).catch(err => {
            console.log(err);
            this.isPending = false;
          })
        }
      },
      cancel: function () {
        this.$emit('setIsFinishTip', true);
      }
    }
  }
</script>

<style scoped>

</style>
