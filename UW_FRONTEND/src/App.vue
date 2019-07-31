<template>
    <div id="app">
        <router-view/>
    </div>
</template>

<script>
    export default {
        name: 'app'
    }
</script>

<style lang="scss">
    html, body, #app, ul, li,div{
        margin: 0;
        padding: 0;
    }

    html, body, #app {
        width: 100%;
        height: 100%;
    }

    ul, li {
        list-style: none;
    }

    #app {
        font-family: 'Avenir', Helvetica, Arial, sans-serif, '微软雅黑';
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        background:#f4f3ef;
    }
    *::-webkit-scrollbar {
        width:5px;
        height:5px;
    }
    *::-webkit-scrollbar-thumb {
        border-radius: 10px;
        -webkit-box-shadow: inset 0 0 5px rgba(0,0,0,0.1);
        background: rgba(156, 159, 165, 0.2);
    }
    *::-webkit-scrollbar-track {
        background:transparent;
    }
</style>
