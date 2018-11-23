<template>
  <div class="details">
    <form role="form">
      <div class="form-group">
        <label for="printerIP">打印机IP:</label>
        <input type="text" class="form-control" id="printerIP" v-model="editData.printerIP">
      </div>
      <div class="btn-group">
        <div class="form-group">
          <div class="btn btn-secondary mr-3" @click="reset">清空条件</div>
        </div>
        <div class="form-group">
          <div class="btn btn-primary" @click="save">保存</div>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
  import {mapGetters, mapActions} from 'vuex'
  export default {
    name: "ConfigModule",
    data(){
      return{
        editData:{
          printerIP:"",
        }
      }
    },
    mounted(){
      if(this.configData.printerIP !== ''){
        this.editData.printerIP = this.configData.printerIP;
      }
    },
    computed:{
      ...mapGetters([
        'configData'
      ]),
    },
    methods:{
      ...mapActions([
        'setConfigData'
      ]),
      reset:function(){
        this.editData.printerIP = "";
      },
      save:function(){
        if(this.editData.printerIP === ''){
          this.$alertWarning("参数不能为空");
          return;
        }
        let ipVerification = /^(?:(?:1[0-9][0-9]\.)|(?:2[0-4][0-9]\.)|(?:25[0-5]\.)|(?:[1-9][0-9]\.)|(?:[0-9]\.)){3}(?:(?:1[0-9][0-9])|(?:2[0-4][0-9])|(?:25[0-5])|(?:[1-9][0-9])|(?:[0-9]))$/;
        if(!ipVerification.test(this.editData.printerIP)){
          this.$alertWarning("IP格式不对");
          return;
        }
        localStorage.setItem('configData', JSON.stringify(this.editData));
        this.setConfigData(this.editData);
        this.$alertSuccess("保存成功");
      }
    }
  }
</script>

<style scoped>
  .details {
    width:600px;
    height: 100%;
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    margin:0 auto;
  }
  form {
    box-sizing: border-box;
    padding: 30px 30px;
    display: flex;
    flex-direction: column;
  }
  .btn-group{
    display:flex;
  }
</style>
