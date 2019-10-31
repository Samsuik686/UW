<template>
  <el-dialog
    title="修改数量"
    :show-close="isCloseOnModal"
    :close-on-click-modal="isCloseOnModal"
    :close-on-press-escape="isCloseOnModal"
    :visible.sync="isEditMaterial"
    width="400px">
    <el-form label-width="50px" label-position="right">
      <el-form-item label="料盘">
        <el-input v-model.trim="materialId" size="large" disabled></el-input>
      </el-form-item>
      <el-form-item label="数量">
        <el-input v-model.trim="quantity" size="large"></el-input>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="edit">确 定</el-button>
      </span>
  </el-dialog>
</template>

<script>
  import {mapActions} from 'vuex'
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import {modifyRegularOutQuantityUrl} from "../../../../../plugins/globalUrl";

  export default {
    name: "EditMaterialId",
    data() {
      return {
        isCloseOnModal: false,
        editDialogVisible: false,
        materialId: '',
        quantity: '',
        taskLogId:'',
        isPending: false
      }
    },
    props:{
      isEditMaterial:Boolean,
      row:Object,
      packListItemId:Number
    },
    watch:{
      isEditMaterial:function(val){
        if(val === true){
          this.materialId = this.row.materialId;
          this.quantity = this.row.quantity;
          this.taskLogId = this.row.taskLogId;
        }else{
          this.materialId = '';
          this.quantity = '';
          this.taskLogId = '';
        }
      }
    },
    methods: {
        ...mapActions(['setIsBlur']),
      edit: function () {
        if (this.quantity === '') {
          this.$alertWarning('数量不能为空');
          return;
        }
        let reg = /^([1-9]\d*|[0])$/;
        if (!reg.test(this.quantity)) {
          this.$alertWarning('格式不对');
          return;
        }
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url:modifyRegularOutQuantityUrl,
            data:{
              taskLogId:this.taskLogId,
              packingListItemId:this.packListItemId,
              materialId:this.materialId,
              quantity:this.quantity
            }
          };
          axiosPost(options).then(res => {
            if(res.data.result === 200){
              this.$alertSuccess('修改成功');
              this.cancel();
            }else{
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() =>{
              this.isPending = false;
          })
        }
      },
      cancel:function(){
            this.setIsBlur(false);
          this.$emit('update:isEditMaterial',false);
      }
    }
  }
</script>

<style scoped>

</style>
