<template>
  <div class="position-relative" id="copyright">
    <div class="text-container" v-if="offsetDistance > 500">
      <p class="m-0">Copyright © 2018 {{(new Date()).getFullYear() === 2018 ? '' : ('- ' + new Date().getFullYear())}}
        <span>
          <img src="@/assets/JimiLogo.jpg" id="JimiLogo"  draggable="false">
        </span>
        All rights reserved.</p>
    </div>

  </div>
</template>

<script>
  export default {
    name: "Copyright",
    data() {
      return {
        offsetDistance: 0
      }
    },
    mounted() {
      this.$nextTick(function () {
        this.offsetDistance = document.getElementById('copyright').offsetTop
      })
    },
    watch: {
      $route: function (val) {
        this.$nextTick(function () {
          this.offsetDistance = document.getElementById('copyright').offsetTop
        })
      }
    }
  }
</script>

<style scoped>
  #copyright {
    width: 100%;
    display: flex;
    justify-content: center;
  }

  .text-container {
    padding: 0 10px;
    border-radius: 20px;
    background-color: #fff;
    margin-bottom: 10px;
    line-height: 30px;
    font-size: 14px;
    border: 1px solid #eeeeee;
  }
  #JimiLogo {
    margin-top: -4px;
    height: 26px;
  }
</style>
