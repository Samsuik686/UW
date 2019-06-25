<template>
  <div class="mt-3 mb-3">
    <nav>
      <div class="sidebar-items">
        <div class="sidebar-title">
          <a class="subtitle" draggable="false">抽检操作</a>
        </div>
        <div>
          <div @click="toggleState('tasks')">
            <div class="sidebar-link" @click="linkTo('tasks')"
                 :class="activeItem === 'tasks' ? 'active' : ''">抽检任务
            </div>
          </div>
          <div @click="toggleState('operation')">
            <div class="sidebar-link" @click="linkTo('operation')"
                 :class="activeItem === 'operation' ? 'active' : ''">抽检操作
            </div>
          </div>
        </div>
      </div>
    </nav>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        //控制列表active状态，当前已激活的项目
        activeItem: ""

      }
    },
    mounted: function () {
      let activeItem = this.$route.path.split('/')[2];
      this.toggleState(activeItem);
    },
    watch: {
      $route: function (route) {
        let activeItem = route.path.split('/')[2];
        this.toggleState(activeItem);
      },
    },
    methods: {
      toggleState: function (item) {
        this.activeItem = item;
      },
      linkTo: function (obj) {
        this.$router.push('/_empty');
        this.$router.replace({
          path: '/spot/' + obj,
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

  .sidebar-items {
    border: none;
    height: 100%;
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
</style>
