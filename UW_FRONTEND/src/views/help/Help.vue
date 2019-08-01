<template>
    <div class="help">
        <div class="pdf-page" v-for="num in numPages">
            <pdf :src="src"  class="faq-pdf" :page="num"></pdf>
        </div>
    </div>
</template>

<script>
    import pdf from 'vue-pdf'
    export default {
        name: "Help",
        components: {pdf},
        data(){
            return{
                src:'./FAQ.pdf',
                numPages:0
            }
        },
        created(){
            let self = this;
            let loadingTask = pdf.createLoadingTask(this.src);
            loadingTask.then(pdf => {
                self.src = loadingTask;
                self.numPages = pdf.numPages;
            }).catch((err) => {
                console.error('pdf加载失败')
            });
        }
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
        .pdf-page{
            width:100%;
            .faq-pdf{
                width:100%;
            }
        }
    }
</style>