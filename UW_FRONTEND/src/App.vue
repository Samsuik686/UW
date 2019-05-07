<template>
  <div id="app">
    <router-view/>
    <copyright/>
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
  import eventBus from './utils/eventBus'
  import Copyright from "./components/Copyright";
  export default {
    name: 'App',
    components: {
      Copyright,
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
    background: url("assets/background.png") repeat;

  }

  html, body {
    height: 100%;
  }
  .notification{
    background-color: #ffffff;
  }
  .table td, .table th {
    vertical-align: middle !important;
  }

  *::-webkit-scrollbar {
    width:8px;
    height:8px;
  }
  *::-webkit-scrollbar-thumb {
    border-radius: 10px;
    -webkit-box-shadow: inset 0 0 5px rgba(0,0,0,0.2);
    background:rgba(144,147,153,.3);
  }
  *::-webkit-scrollbar-track {
    background:#fff;
  }
</style>
