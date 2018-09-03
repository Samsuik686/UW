<template>
  <div id="app">
    <router-view/>
    <help-page/>
    <notify-me
      :event-bus="eventBus">
      <template slot="content" slot-scope="{data}">
        <div style="width: 100%; word-break: break-all; text-align: left">
          <h4><b>{{data.title}}</b></h4>
          <p style="margin: 0">{{data.text}}</p>
        </div>
      </template>
    </notify-me>
  </div>

</template>

<script>
  import Notify from './components/Notify'
  import HelpPage from './components/HelpPage'
  import eventBus from './utils/eventBus'
  export default {
    name: 'App',
    components: {
      HelpPage,
      'notify-me': Notify
    },
    data() {
      return {
        eventBus: eventBus,
        status: '',
        permanent: false,
        text: '',
        timeout: 3000,
      }
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
  .notification{
    background-color: #ffffff;
  }
</style>
