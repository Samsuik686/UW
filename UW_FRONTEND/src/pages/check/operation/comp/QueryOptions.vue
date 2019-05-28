<template>
  <div class="options-area">
    <div class="form-row">
      <div class="row no-gutters pl-3 pr-3">
        <div class="form-group col pr-3 pl-1">
          <label for="window-list">选择仓口:</label>
          <select v-model="thisWindow" id="window-list" class="custom-select"
                  @change="setWindow" :disabled="windowsList.length === 0">
            <option value="" disabled>请选择</option>
            <option v-for="item in windowsList" :value="item.id">{{item.id}}</option>
          </select>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
  import {mapActions} from 'vuex'
  import {taskWindowsUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";

  export default {
    name: "Options",
    components: {},
    data() {
      return {
        windowsList: [],
        thisWindow: '',
        windowType:'4'
      }
    },
    created(){
      this.setCheckWindowId('');
      this.setPreset();
    },
    methods: {
      ...mapActions(['setCheckWindowId']),
      setPreset: function () {
        let options = {
          url: taskWindowsUrl,
          data: {
            type: this.windowType
          }
        };
        axiosPost(options).then(response => {
          if (response.data.result === 200) {
            if(response.data.data !== null){
              this.windowsList = response.data.data;
              if (this.windowsList.length > 0) {
                this.thisWindow = this.windowsList[0].id;
                this.setCheckWindowId(this.thisWindow);
              }
            }
          }else{
            errHandler(response.data);
          }
        }).catch(err => {
          console.log(err);
          this.$alertDanger('连接超时，请刷新重试');
        })
      },
      setWindow: function () {
        this.setCheckWindowId(this.thisWindow);
      }
    }
  }
</script>

<style scoped>
  .options-area {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
  }
</style>
