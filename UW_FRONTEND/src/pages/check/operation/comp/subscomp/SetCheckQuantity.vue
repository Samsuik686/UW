<template>
  <div class="form-group" style="margin-bottom:0">
    <label>
      <input type="text"
             class="form-control"
             :class="{highLight:isHighLight}"
             autocomplete="off"
             v-model="inputVal"
             @click="setFocus"
             ref="myInput"
             @blur="cancelFocus">
    </label>
  </div>
</template>

<script>
  import {mapGetters, mapActions} from 'vuex'
  import eventBus from "../../../../../utils/eventBus";

  export default {
    name: "SetCheckQuantity",
    props: {
      row: Object
    },
    data() {
      return {
        inputVal: '',
        isHighLight: false,
        index: ''
      }
    },
    computed: {
      ...mapGetters(['checkWindowId', 'checkWindowData', 'isRefresh', 'isScanner']),
    },
    mounted() {
      if (this.checkWindowData.hasOwnProperty(this.checkWindowId)) {
        this.setCheckData(this.checkWindowId);
      }
    },
    watch: {
      inputVal: function (val) {
        let data = JSON.parse(JSON.stringify(this.checkWindowData[this.checkWindowId]));
        if (this.index !== '' && data.length > 0) {
          if(data[this.index].checkQuantity !== val){
            data[this.index].isConfirm = false;
          }
          data[this.index].checkQuantity = val;
          eventBus.$emit('refreshConfirm');
          this.setCheckWindowData({
            id: this.checkWindowId,
            data: data
          })
        }
      },
      isHighLight: function (val) {
        let data = JSON.parse(JSON.stringify(this.checkWindowData[this.checkWindowId]));
        if (this.index !== '' && data.length > 0) {
          data[this.index].isHighLight = val;
          if (val === true) {
            data.map((item, index) => {
              if (index !== this.index) {
                item.isHighLight = false;
              }
            })
          }
          this.setCheckWindowData({
            id: this.checkWindowId,
            data: data
          })
        }
      },
      isRefresh: function (val) {
        //仓口改变，重新初始化
        if (val === true) {
          this.setCheckData(this.checkWindowId);
        }
      },
      isScanner: function (val) {
        //扫描
        if (val === true) {
          this.getScannerState();
        }
      }
    },
    methods: {
      ...mapActions(['setCheckWindowData', 'setIsScanner']),
      setFocus: function () {
        eventBus.$emit('setIsInput', true);
        this.isHighLight = true;
        this.$refs.myInput.focus();
      },
      cancelFocus: function () {
        this.isHighLight = false;
        eventBus.$emit('setIsInput', false);
      },
      //初始化
      setCheckData: function (val) {
        //获取当前窗口的数据
        let data = JSON.parse(JSON.stringify(this.checkWindowData[val]));
        //匹配
        for (let i = 0; i < data.length; i++) {
          let obj = data[i];
          if (obj.materialId === this.row.materialId) {
            this.inputVal = obj.checkQuantity;
            this.isHighLight = obj.isHighLight;
            this.index = i;
            break;
          }
        }
        if(this.index !== ''){
          data[this.index].isHighLight = false;
        }
        this.setCheckWindowData({
          id: this.checkWindowId,
          data: data
        })
      },
      //获取扫描状态，判断是否扫描
      getScannerState: function () {
        //获取当前窗口的数据
        let data = JSON.parse(JSON.stringify(this.checkWindowData[this.checkWindowId]));
        //匹配
        for (let i = 0; i < data.length; i++) {
          let obj = data[i];
          if (obj.materialId === this.row.materialId && obj.isScanner === true) {
            this.inputVal = obj.checkQuantity;
            this.isHighLight = obj.isHighLight;
            this.index = i;
            this.setIsScanner(false);
            break;
          }
        }
        if(this.index !== ''){
          data[this.index].isScanner = false;
        }
        this.setCheckWindowData({
          id: this.checkWindowId,
          data: data
        })
      }
    }
  }
</script>

<style scoped>
  .highLight {
    border-color: green;
  }
</style>
