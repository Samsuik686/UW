<!--系统主要功能页面根组件-->
<template>
  <div class="main-body">
    <side-setting/>
    <router-view/>
    <show-robot-status></show-robot-status>
  </div>
</template>

<script>
  import SideSetting from '../components/SideSetting'
  import ShowRobotStatus from "./../components/ShowRobotStatus";
  export default {
    name: "Main",
    components: {
      ShowRobotStatus,
      SideSetting
    }
  }
</script>

<style scoped>

</style>
