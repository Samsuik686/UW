<template>
  <div>
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
        console.log(this.scanText);
        let scanText = this.scanText;
        this.scanText = "";
        if (this.isPending === false) {
          this.isPending = true;
          let tempArray = scanText.split("@");
          let options = {
            url: robotCallUrl,
            data: {
              id: this.currentWindowId,
              no: tempArray[0]
            }
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.$alertSuccess("调用成功")
            } else {
              this.$alertWarning(res.data.data)
            }
            this.isPending = false;
          }).catch(err => {
            if (JSON.stringify(err) !== '{}') {
              this.$alertDanger(JSON.stringify(err));
              this.isPending = false;
            }
          })
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
