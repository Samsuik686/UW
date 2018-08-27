<template>
  <div>
    <options/>
    <input type="text" title="scanner" id="call-check" v-model="scanText"
           @blur="setFocus" autofocus="autofocus" autocomplete="off" @keyup.enter="scannerHandler">

    <preview-details/>
  </div>
</template>

<script>
  import Options from './comp/QueryOptions'
  import PreviewDetails from './comp/PreviewDetails'
  import {mapGetters} from 'vuex'
  import {axiosPost} from "../../../utils/fetchData";
  import eventBus from "../../../utils/eventBus";
  import {robotCallUrl} from "../../../config/globalUrl";

  export default {
    name: "IoPreview",
    components: {
      Options,
      PreviewDetails
    },
    data() {
      return {
        scanText: ''
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
        if (this.$route.path === '/io/preview') {
          document.getElementById('call-check').focus();
        }
      },
      scannerHandler: function () {
        if (this.scanText === '###JUMPTOIO###') {
          this.scanText = "";
          this.$router.push('/io/innow')
        } else {
          let tempArray = this.scanText.split("@");
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
            this.scanText = "";
          }).catch(err => {
            if (JSON.stringify(err) !== '{}'){
              this.$alertDanger(JSON.stringify(err))
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
  }
</style>
