<!--表单查看页面的统一侧边栏导航-->

<template>
  <div class="sidebar pt-3 pb-3 h-100">
    <nav class="w-100">
      <div class="sidebar-items">
        <div class="sidebar-title">
          <a class="subtitle" draggable="false">出入库操作</a>
        </div>
        <div>
          <div @click="toggleState('preview')">
            <div class="sidebar-link" @click="linkTo('preview')"
               :class="activeItem === 'preview' ? 'active' : ''">查看仓口任务</div>
          </div>
          <!--<div @click="toggleState('call')">
            <div class="sidebar-link" @click="linkTo('call')"
                 :class="activeItem === 'call' ? 'active' : ''">扫码呼叫叉车</div>
          </div>
          <div @click="toggleState('innow')">
            <div class="sidebar-link" @click="linkTo('innow')"
               :class="activeItem === 'innow' ? 'active' : ''">入库任务操作</div>
          </div>
          <div @click="toggleState('outnow')">
            <div class="sidebar-link" @click="linkTo('outnow')"
                 :class="activeItem === 'outnow' ? 'active' : ''">出库任务操作</div>
          </div>
          <div @click="toggleState('return')">
            <div class="sidebar-link" @click="linkTo('return')"
                 :class="activeItem === 'return' ? 'active' : ''">退料任务操作</div>
          </div>-->
        </div>
      </div>
    </nav>
   <!-- <div class="m-2 mt-auto" v-if="$route.path === '/io/preview'">
      <div class="row ml-auto mr-auto mt-4">
        <img src="static/img/toIOQRcode.png" class="img-style">
      </div>
      <span class=" mt-auto">* 扫描此二维码或点击侧边栏以跳转到仓口入库页面</span>
    </div>
    <div class="m-2 mt-auto" v-else-if="$route.path === '/io/innow' || $route.path === '/io/return'">
      <div class="row ml-auto mr-auto mt-4">
        <img src="static/img/toCallQRCode.png" class="img-style">
      </div>
      <span class=" mt-auto">* 扫描此二维码或点击侧边栏以跳转到任务预览页面</span>
    </div>-->
  </div>
</template>

<script>
  import {mapGetters, mapActions} from 'vuex'

  export default {
    data() {
      return {


        //控制列表active状态，当前已激活的项目
        activeItem: ""

      }
    },
    mounted: function () {
      switch (this.$route.path) {
        case '/io/innow':
          this.toggleState('innow');
          break;
        case '/io/outnow':
          this.toggleState('outnow');
          break;
        case '/io/preview':
          this.toggleState('preview');
          break;
        case '/io/return':
          this.toggleState('return');
          break;
        case 'io/call':
          this.toggleState('call');
          break;
      }
    },
    watch: {
      $route: function (route) {
        switch (route.path) {
          case '/io/innow':
            this.toggleState('innow');
            break;
          case '/io/outnow':
            this.toggleState('outnow');
            break;
          case '/io/preview':
            this.toggleState('preview');
            break;
          case '/io/return':
            this.toggleState('return');
            break;
          case '/io/call':
            this.toggleState('call');
            break;
        }
      }
    },
    computed: {
      ...mapGetters([
        'isLoading'
      ]),
    },
    methods: {
      ...mapActions(['setTableRouter', 'setLoading']),
      toggleState: function (item) {
        this.activeItem = item;

      },
      linkTo: function (obj) {
        //this.setLoading(true);
        // if (this.$route.path === '/io/preview') {
        //   this.$router.replace('/_empty')
        // }
        this.$router.push({
          path: '/io/' + obj,
        })
      }
    }


  }
</script>

<style scoped>
  a {
    text-decoration: none;
    color: #000;
  }

  .sidebar {

    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .sidebar-items {
    /*border: 1px solid #eeeeee;*/
    /*border-top: none;*/
    /*border-bottom: none;*/
    border: none;
    height: 100%;
    /*border-radius: 8px;*/
  }

  .sidebar-title {
    height: 2em;
    line-height: 2em;
    font-size: 1.2em;
    font-weight: 500;
    padding-left: 0.5em;
    border-bottom: 1px solid #eeeeee;
    background-color: #458aff;
    color: #fff;
    border-radius: 8px;
  }

  .sidebar-title a {
    color: #fff;
  }

  .sidebar-link {
    text-decoration: none;
    display: block;
    height: 2em;
    line-height: 2em;
    padding-left: 1.4em;
    font-size: 1em;
    border-bottom: 1px solid #eeeeee;
    font-weight: normal;
    background: #fff;
    cursor: pointer;
  }

  .sidebar-link:hover {
    background-color: #8bdaff;
    color: #fff;
    border-radius: 5px;
  }

  .sidebar-items .active {
    background-color: #7bbfff;
    box-shadow: 2px 4px 10px 1px #e5e7eb;
    color: #fff;
    border-radius: 5px;
  }

  .subtitle {
    cursor: default;
    display: block;
    width: 100%;
    height: 100%;
  }
  .img-style {
    height: 200px;
  }
</style>
