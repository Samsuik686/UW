<template>
    <div class="help">
        <router-view/>
    </div>
</template>

<script>
    export default {
        name: "Help",
    }
</script>

<style scoped lang="scss">
    .help{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
    }
</style>