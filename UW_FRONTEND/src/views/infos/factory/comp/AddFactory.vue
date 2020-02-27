<template>
  <el-dialog
      title="添加公司信息"
      :visible.sync="isAdding"
      :show-close="isCloseOnModal"
      :close-on-click-modal="isCloseOnModal"
      :close-on-press-escape="isCloseOnModal"
      width="30%">
    <el-form>
      <el-form-item label="公司编号">
        <el-input v-model.trim="companyInfo.companyCode" placeholder="公司编号" :maxlength="16"></el-input>
      </el-form-item>
      <el-form-item label="简称">
        <el-input v-model.trim="companyInfo.nickname" placeholder="简称" :maxlength="32"></el-input>
      </el-form-item>
      <el-form-item label="全称">
        <el-input v-model.trim="companyInfo.name" placeholder="全称" :maxlength="32"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
            <el-button type="info" size="mini" @click="cancel">取 消</el-button>
            <el-button type="primary" size="mini" @click="submit">确定</el-button>
        </span>
  </el-dialog>
</template>

<script>
  import {companyAddUrl} from "../../../../plugins/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {judgeCodeLen256} from "../../../../utils/formValidate";
  import eventBus from '../../../../utils/bus';

  export default {
    name: "AddFactory",
    props: {
      isAdding: Boolean
    },
    data() {
      return {
        isCloseOnModal: false,
        isPending: false,
        companyInfo: {
          companyCode: '',
          name: '',
          nickname: ''
        }
      }
    },
    methods: {
      cancel: function () {
        this.companyInfo = {
          companyCode: '',
          name: '',
          nickname: ''
        };
        this.$emit("update:isAdding", false);
      },
      submit: function () {
        if (this.companyInfo.companyCode === '' || this.companyInfo.name === '' || this.companyInfo.nickname === '') {
          this.$alertWarning('填写信息不完整');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: companyAddUrl,
            data: {
              companyCode: this.companyInfo.companyCode,
              name: this.companyInfo.name,
              nickname: this.companyInfo.nickname
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess('添加成功');
              eventBus.$emit("reloadFactory");
              this.cancel();
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

<style scoped>

</style>