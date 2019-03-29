<!--表单查看页面的条件过滤栏-->

<template>
  <div class="options-area">
    <div class="form-row">
      <div v-for="item in queryOptions" class="row no-gutters pl-3 pr-3">
        <component :opt="item" :is="item.type + '-comp'" :callback="thisFetch"></component>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-secondary ml-3 mr-4" @click="initForm">清空条件</a>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="thisFetch">查询</a>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="isAdding = !isAdding">新增供应商</a>
      </div>
    </div>
    <transition name="fade">
      <div v-if="isAdding" id="add-window">
        <add-supplier/>
      </div>
    </transition>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus'
  import AddSupplier from './subscomp/AddSupplier'
  import {mapActions} from 'vuex';
  import {materialCountUrl} from "../../../../config/globalUrl";
  import _ from 'lodash'

  export default {
    name: "Options",
    components: {
      'text-comp': {
        props: ['opt', 'callback'],
        template: '<div class="form-group col pr-3"">\n' +
          '           <label :for="opt.id">{{opt.name}}：</label>\n' +
          '           <input type="text" class="form-control" :id="opt.id" v-model="opt.model" @keyup.enter="callback"  autocomplete="off">\n' +
          '          </div>'
      },
      AddSupplier
    },
    data() {
      return {
        // pageSize: 2000,
        queryOptions: [
          {
            id: 'name',
            name: '供应商名',
            model: '',
            type: 'text'
          },
        ],
        copyQueryOptions: [],
        queryString: "",
        isAdding: false
      }
    },
    mounted: function () {
      this.initForm();
      eventBus.$on('closeAddPanel', () => {
        this.isAdding = false;
      })
    },
    watch: {
      $route: function (route) {
        if(JSON.stringify(route.params) === "{}" && JSON.stringify(route.query) === "{}"){
          this.initForm();
        }
      },
    },
    methods: {
      ...mapActions(['setLoading']),
      initForm: function () {
        this.queryOptions.map(item => {
          item.model = "";
        })
      },
      createQueryString: function () {
        this.queryString = "";
        this.copyQueryOptions = this.queryOptions.filter((item) => {
          if (!(item.model === "")) {
            return true;
          }
        });

        this.copyQueryOptions.map((item, index) => {
          if (item.type === 'text' || item.type === 'select') {
            if (_.trim(item.model) !== "") {
              if (index === 0) {
                this.queryString += (item.id + "like" + _.trim(item.model))
              } else {
                this.queryString += ("&" + item.id + "=" + _.trim(item.model))
              }

            } else {
              this.setLoading(false)
            }
          }

        })
      },
      fetchData: function () {
        let options = {
          path: '/material/supplier',
          query: {}
        };
        if (this.queryString !== "") {
          options.query.filter = this.queryString
        }

        this.$router.push('_empty');
        this.$router.replace(options
          , () => {
            this.setLoading(true);
          })

      },
      thisFetch: function () {
        this.createQueryString();
        this.fetchData()
      }
    }
  }
</script>

<style scoped>
  .options-area {
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
    padding: 10px;
  }
  #add-window {
    z-index: 100;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
