<template>
    <div class="copyright">
        <div class="copyright-container">
            <div class="copyright-text">
                <span>Copyright © 2018 {{(new Date()).getFullYear() === 2018 ? '' : ('- ' + new
                    Date().getFullYear())}}</span>
                <span class="copyright-logo"><img src="./../assets/JimiLogo.jpg" id="JimiLogo" draggable="false"></span>
                <span>All rights reserved.</span>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "Copyright"
    }
</script>

<style scoped>
    .copyright {
        position: absolute;
        bottom: 30px;
        width: 100%;
        display: flex;
        justify-content: center;
    }
    .copyright-container {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 12px;
        border-radius: 20px;
        background-color: #fff;
        height: 30px;
        line-height: 30px;
        font-size: 14px;
        border: 1px solid #eee;
    }
    .copyright-text {
        display:flex;
        justify-content: center;
        align-items: center;
    }
    .copyright-logo {
        display: flex;
        justify-content: center;
        align-items: center;
    }
    #JimiLogo {
        height: 26px;
    }
</style>
