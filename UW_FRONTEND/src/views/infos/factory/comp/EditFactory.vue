<template>
  <el-dialog
      title="更新公司信息"
      :visible.sync="isEditing"
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
  import {companyUpdateUrl} from "../../../../plugins/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import {judgeCodeLen256, judgeCodeLen32} from "../../../../utils/formValidate";
  import eventBus from '../../../../utils/bus';

  export default {
    name: "EditFactory",
    props: {
      isEditing: Boolean,
      editData: Object
    },
    data() {
      return {
        isPending: false,
        isCloseOnModal: false,
        companyInfo: {
          id: '',
          companyCode: '',
          name: '',
          nickname: ''
        }
      }
    },
    watch: {
      isEditing: function (val) {
        if (val === true) {
          this.companyInfo = {
            id: this.editData.id,
            name: this.editData.name,
            companyCode: this.editData.companyCode,
            nickname: this.editData.nickname
          }
        } else {
          this.companyInfo = {
            id: '',
            name: '',
            companyCode: '',
            nickname: ''
          }
        }
      }
    },
    methods: {
      cancel: function () {
        this.$emit("update:isEditing", false);
      },
      submit: function () {
        if (this.companyInfo.companyCode === '' || this.companyInfo.name === '' || this.companyInfo.nickname === '') {
          this.$alertWarning('填写信息不完整');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: companyUpdateUrl,
            data: {
              id: this.companyInfo.id,
              companyCode: this.companyInfo.companyCode,
              name: this.companyInfo.name,
              nickname: this.companyInfo.nickname
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess('更新成功');
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