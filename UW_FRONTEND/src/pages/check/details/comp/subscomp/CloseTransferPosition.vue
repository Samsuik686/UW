<template>
  <div class="form-row">
    <div class="btn btn-primary mr-2 col-5" @click="closePosition"  v-if="row.isChecked && row.coverTime === null && row.isFinished === false">平仓</div>
    <div class="btn btn-primary col-5" @click="isEditing = true">修改</div>
    <div v-if="isEditing">
      <edit-quantity :editData="row"></edit-quantity>
    </div>
  </div>
</template>

<script>
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import {coverEWhMaterialUrl} from "../../../../../config/globalUrl";
  import eventBus from "../../../../../utils/eventBus";
  import EditQuantity from './EditQuantity';

  export default {
    name: "ClosePosition",
    props: {
      row: Object
    },
    components: {
      EditQuantity
    },
    mounted(){
      eventBus.$on('closeEditQuantityPanel',() => {
        this.isEditing = false;
      });
    },
    data() {
      return {
        isPending: false,
        isEditing: false
      }
    },
    methods: {
      closePosition: function () {
        if (!this.isPending) {
          this.isPending = false;
          let options = {
            url: coverEWhMaterialUrl,
            data: {
              id: this.row.id,
              taskId: this.row.taskId
            }
          };
          axiosPost(options).then(res => {
            this.isPending = false;
            if (res.data.result === 200) {
              this.$alertSuccess('平仓成功');
              eventBus.$emit('checkDetailsRefresh', true);
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
            this.isPending = false;
          })
        }
      }
    }
  }
</script>

<style scoped>
</style>
