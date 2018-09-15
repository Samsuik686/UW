<!--表单-->
<template>
  <div>
    <!--<Header/>-->
    <loading v-if="$store.state.isLoading"/>
    <div class="" style="margin-left: 60px;">
      <div class="side-bar">
        <side-bar/>
      </div>
      <div class="row main-style" >
        <div class="col router-style mt-3 mb-3" style="margin-left: 255px">
          <router-view/>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
  import Header from '@/components/PageHeader'
  import Loading from '@/components/Loading'
  // import TableModule from './material/TableModule'
  import SideBar from './comp/SideBar'
  export default {
    name: "Main",
    components: {
      Header,
      Loading,
      // TableModule,
      SideBar
    },
    data() {
      return {}
    },
    mounted: function () {


    },
    methods: {}
  }
</script>

<style scoped>
  @media (min-width: 640px) {
    .side-bar {
      width: 250px;
    }

    .router-style {
      min-width: 340px;
    }
  }

  .side-bar {
    position: fixed;
    background: #fff;
    box-shadow: 5px 0 5px 0 #ddd;
    height: 100%;
    padding: 0 20px;
  }

  .main-style {
    margin: 0;
    height: 100%;
  }

</style>
