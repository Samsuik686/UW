<template>
  <el-dialog
    title="修改数量"
    :show-close="isCloseOnModal"
    :close-on-click-modal="isCloseOnModal"
    :close-on-press-escape="isCloseOnModal"
    :visible.sync="editDialogVisible"
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
  import eventBus from "../../../../../utils/eventBus";
  import {modifyOutQuantityUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

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
          this.editDialogVisible = true;
        }else{
          this.materialId = '';
          this.quantity = '';
          this.taskLogId = '';
          this.editDialogVisible = false;
        }
      }
    },
    methods: {
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
            url:modifyOutQuantityUrl,
            data:{
              taskLogId:this.taskLogId,
              packListItemId:this.packListItemId,
              materialId:this.materialId,
              quantity:this.quantity
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if(res.data.result === 200){
              this.$alertSuccess('修改成功');
              this.$emit('setIsEditMaterial',true);
              eventBus.$emit('refreshTaskItem');
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
      cancel:function(){
        this.$emit('setIsEditMaterial',true);
        eventBus.$emit('blurFocus',false);
      }
    }
  }
</script>

<style scoped>

</style>
