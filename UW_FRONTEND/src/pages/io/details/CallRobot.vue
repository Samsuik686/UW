<template>
  <div>
    <video controls="controls" id="sAudio" hidden>
      <source src="./../../../assets/005-System05.ogg" type="video/ogg">
    </video>
    <video controls="controls" id="fAudio" hidden>
      <source src="./../../../assets/141-Burst01.ogg" type="video/ogg">
    </video>
    <options/>
    <input type="text" title="scanner" id="call-check" v-model.trim="scanText"
           @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">
  </div>
</template>

<script>
  import Options from './comp/QueryOptions'
  import {mapGetters} from 'vuex'
  import {axiosPost} from "../../../utils/fetchData";
  import {robotCallUrl} from "../../../config/globalUrl";
  import {errHandler} from "../../../utils/errorHandler";
  import {handleScanText} from "../../../utils/scan";

  export default {
    name: "CallRobot",
    components: {
      Options
    },
    data() {
      return {
        scanText: '',
        isPending: false
      }
    },
    mounted() {
      this.setFocus();
    },
    computed: {
      ...mapGetters([
        'currentWindowId'
      ]),
    },
    methods: {
      setFocus: function () {
        if (this.$route.path === '/io/call') {
          document.getElementById('call-check').focus();
        }
      },
      scannerHandler: function () {
        let scanText = this.scanText;
        this.scanText = "";

        //判断扫描的条码格式
        let result = handleScanText(scanText);
        if(result !== ''){
          this.$alertWarning(result);
          this.failAudioPlay();
          return;
        }

        //仓口为空
        if(this.currentWindowId === ''){
          this.$alertWarning('未选择仓口');
          return;
        }

        if (this.isPending === false) {
          this.isPending = true;
          let tempArray = scanText.split("@");
          let options = {
            url: robotCallUrl,
            data: {
              id: this.currentWindowId,
              no: tempArray[0],
              supplierName:tempArray[4]
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.successAudioPlay();
              this.$alertSuccess("调用成功")
            } else {
              this.failAudioPlay();
              errHandler(res.data);
            }
            this.isPending = false;
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.failAudioPlay();
              this.$alertDanger(JSON.stringify(err));
              this.isPending = false;
            }
          })
        }
      },
      // 扫描成功提示
      successAudioPlay: function () {
        let audio = document.getElementById('sAudio');
        if (audio !== null) {
          if (audio.paused) {
            audio.play();
          }
        }
      },
      // 扫描失败提示
      failAudioPlay: function () {
        let audio = document.getElementById('fAudio');
        if (audio !== null) {
          if (audio.paused) {
            audio.play();
          }
        }
      }
    }
  }
</script>

<style scoped>
 #call-check {
    opacity: 0;
    height: 0;
    line-height: 0;
    border: none;
    padding: 0;
    position: fixed;
  }
</style>
