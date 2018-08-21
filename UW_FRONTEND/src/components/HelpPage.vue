<template>
  <div id="help-page" v-if="isButtonShowing">
    <div class="help-panel" v-if="isPageShowing">
      <div class="form-row justify-content-end">
        <div class="help-panel-container">
          <h2>帮助页面:</h2>
          <div class="form-row">
            <div class="w-100" v-if="isImgLoading">
              <p class="text-center">加载中……</p>
            </div>
            <div class="w-100">
              <img :src="imgPath" class="w-100" draggable="false" @load="isImgLoading = false">
            </div>
          </div>
        </div>
        <div id="cancel-btn" class="ml-2 mt-1" @click="closePanel">
          <icon name="cancel" scale="4" style="color: #fff;"></icon>
        </div>
      </div>
    </div>
    <div class="position-fixed align-items-center justify-content-center help-btn"
         @click="isPageShowing = !isPageShowing">
      <div class="button-container">
        <icon name="help" scale="3.6" style="color: #fff;"></icon>
      </div>
    </div>
  </div>
</template>

<script>

  export default {
    name: "HelpPage",
    components: {},
    data() {
      return {
        content: '',
        isButtonShowing: false,
        isPageShowing: false,
        helpPage: [
          'material',
          'logs',
          'io',
          'user',
          'robot',
          'tasks'
        ],
        imgPath: '',
        isImgLoading: false
      }
    },
    mounted() {
      this.setImgPath(this.$route)
    },
    watch: {
      $route: function (route) {
        this.setImgPath(route)
      }
    },
    methods: {
      closePanel: function () {
        this.isPageShowing = false;
      },
      setImgPath: function (route) {
        let path = route.path.split('/');
        let page = path[path.length - 1];
        if (this.helpPage.indexOf(page) !== -1) {
          this.isButtonShowing = true;
          this.imgPath = 'static/helps/' + page + '.jpg';
          this.isImgLoading = true
        } else {
          this.isButtonShowing = false;
          this.imgPath = ''
        }
      }
    }
  }
</script>

<style scoped>
  .help-btn {
    right: 2rem;
    bottom: 2rem;
    width: 2.8em;
    height: 2.8em;
    border-radius: 1.4em;
    background-color: #458aff;
    display: flex;
    cursor: pointer;
  }

  .help-btn:hover {
    box-shadow: 0 0 5px #aaa;
  }

  .button-container {
    height: 2.1em;
    width: 2.1em;
  }

  .help-panel {
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 101;
  }

  .help-panel-container {
    background: #ffffff;
    min-width: 700px;
    min-height: 220px;
    max-height: 600px;
    max-width: 80%;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
    overflow: scroll;
    scroll-snap-type: inline;
  }

  .help-panel-container::-webkit-scrollbar {
    width: 10px;
    height: 1px;
  }

  .help-panel-container::-webkit-scrollbar-thumb {
    border-radius: 10px;
    /*-webkit-box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);*/
    background: rgba(54, 54, 54, 0.14);
  }

  .help-panel-container::-webkit-scrollbar-track {
    /*-webkit-box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);*/
    border-radius: 10px;
    background: #EDEDED;
  }

  #cancel-btn {
    height: 100%;
    cursor: pointer;
  }

  .markdown-styles img {
    width: auto;
  }
</style>
