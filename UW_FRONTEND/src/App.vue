<template>
  <div id="app">
    <router-view/>
    <help-page/>
  </div>

</template>

<script>
  import HelpPage from './components/HelpPage'
  export default {
    name: 'App',
    components: {
      HelpPage
    },
    mounted: function () {
      console.log(process.env.NODE_ENV)
    }
  }
</script>

<style>
  #app {
  }

  body {
    background: url("/uw_system/static/img/background.png") repeat;

  }

  html, body {
    height: 100%;
  }
</style>
