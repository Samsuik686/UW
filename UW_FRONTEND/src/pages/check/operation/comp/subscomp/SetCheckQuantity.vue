<template>
  <div class="form-group" style="margin-bottom:0">
    <label>
      <input type="text"
             :id="row.materialId"
             class="form-control"
             :class="{highLight:isHighLight}"
             autocomplete="off"
             v-model.trim="inputVal"
             @click="setActiveMaterialId"
             ref="myInput"
             @keyup.enter="cancelConfirm"
             @blur="cancelFocus">
    </label>
  </div>
</template>

<script>
  import {mapGetters,mapActions} from 'vuex'
  import eventBus from "../../../../../utils/eventBus";
  import {inventoryMaterialUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";

  export default {
    name: "SetCheckQuantity",
    props: {
      row: Object,
      activeName:String,
      activeMaterialId:String,
      activeQuantity:Number,
      boxId:Number,
      taskId:Number,
      isScanner:Boolean
    },
    data() {
      return {
        inputVal: '',
        isHighLight: false,
        isPending:false
      }
    },
    watch:{
      activeMaterialId:function(val){
        if(val === this.row.materialId && this.boxId === Number(this.activeName)){
          this.isHighLight = true;
          if(this.activeQuantity !== -1){
            this.inputVal = this.activeQuantity;
            eventBus.$emit('setActiveQuantity',true);
          }else{
            this.inputVal = this.row.actualNum;
          }
          if(this.isScanner === false){
            this.setFocus();
          }else{
            this.checkQuantity();
          }
        }else{
          this.isHighLight = false;
          //this.inputVal = this.row.actualNum;
          this.$refs.myInput.blur();
        }
      }
    },
    computed: {
      ...mapGetters([
        'printMaterialIdArr'
      ]),
    },
    mounted(){
      this.inputVal = this.row.actualNum;
      //根据返回值刷新界面
      eventBus.$on('changeInputVal',() => {
        this.inputVal = this.row.actualNum;
      });
    },
    methods: {
      ...mapActions(['setPrintMaterialIdArr','setLoading']),
      setFocus: function () {
        eventBus.$emit('setIsInputFocus',true);
        eventBus.$emit('setDisabledMaterialId',this.row.materialId);
        this.$refs.myInput.focus();
      },
      setActiveMaterialId:function(){
        eventBus.$emit('setActiveMaterialId',this.row.materialId);
      },
      cancelFocus: function () {
        eventBus.$emit('setIsInputFocus',false);
        eventBus.$emit('setDisabledMaterialId','');
        if(this.inputVal === ''){
          this.inputVal = this.row.actualNum;
          return;
        }
        if(Number(this.inputVal) === Number(this.row.actualNum) && this.row.actualNum !== null){
          return;
        }
        this.checkQuantity();
      },
      checkQuantity:function(){
        if(this.inputVal === '' || this.inputVal === null){
          return;
        }
        if(!this.isNumber(this.inputVal)){
          this.$alertWarning('盘点数量必须为非负整数');
          this.inputVal =  this.row.actualNum;
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: inventoryMaterialUrl,
            data: {
              materialId: this.row.materialId,
              boxId: this.boxId,
              taskId: this.taskId,
              acturalNum: this.inputVal
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.cancelPrintArr(this.row.materialId);
              eventBus.$emit('refreshCheckTask',true);
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            this.$alertDanger('连接超时，请刷新重试');
            console.log(err);
            this.isPending = false;
            this.setLoading(false);
          })
        }
      },
      isNumber: function (num) {
        let val = num;
        let reg = /^\+?(0|[1-9][0-9]*)$/;
        if (val !== "") {
          return reg.test(val);
        }
      },
      cancelPrintArr:function (materialId) {
        let arr = [];
        for(let i=0;i<this.printMaterialIdArr.length;i++){
          if(this.printMaterialIdArr[i] !== materialId){
            arr.push(this.printMaterialIdArr[i]);
          }
        }
        this.setPrintMaterialIdArr(arr);
      },
      cancelConfirm:function(){}
    }
  }
</script>

<style scoped>
  .highLight {
    background: rgba(255, 161, 117, 0.44)
  }
</style>
