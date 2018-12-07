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
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="isAdding = !isAdding">新增物料</a>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="exportReport">导出物料列表</a>
      </div>
    </div>
    <transition name="fade">
      <div v-if="isAdding" id="add-window">
        <add-material/>
      </div>
    </transition>
  </div>
</template>

<script>
  import AddMaterial from './subscomp/AddMaterial'
  import eventBus from '@/utils/eventBus'
  import {mapGetters, mapActions} from 'vuex';
  import {materialCountUrl, exportReportUrl} from "../../../../config/globalUrl";
  import 'vue-datetime/dist/vue-datetime.css'
  import _ from 'lodash'
  import {downloadFile} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";

  export default {
    name: "Options",
    components: {
      'text-comp': {
        props: ['opt', 'callback'],
        template: '<div class="form-group col pr-3">\n' +
          '           <label :for="opt.id">{{opt.name}}：</label>\n' +
          '           <input type="text" class="form-control" :id="opt.id" v-model="opt.model" @keyup.enter="callback"  autocomplete="off">\n' +
          '        </div>\n'
      },
      AddMaterial
    },
    data() {
      return {
        // pageSize: 2000,
        queryOptions: [
          {
            id: 'no',
            name: '料号',
            model: '',
            type: 'text'
          },
          {
            id: 'specification',
            name: '规格',
            model: '',
            type: 'text'
          }
        ],
        copyQueryOptions: [],
        queryString: "",
        isAdding: false,
        isPending: false
      }
    },
    mounted: function () {
      this.initForm();
      eventBus.$on('closeAddPanel', () => {
        this.isAdding = false;
      })
    },
    computed: {
      ...mapGetters([
        'tableRouterApi'
      ]),
    },
    watch: {},
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
          if (item.type === 'text') {
            if (_.trim(item.model) !== "") {
              if (index === 0) {
                this.queryString += (item.id + "like" + _.trim(item.model))
              } else {
                this.queryString += ("#&#" + item.id + "like" + _.trim(item.model))
              }

            } else {
              this.setLoading(false)
            }
          }

        })
      },
      fetchData: function () {
        let options = {
          path: '/material/material',
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
      },
      exportReport: function () {
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            '#TOKEN#': this.$store.state.token
          };
          downloadFile(exportReportUrl, data);
          let count = 0;
          let mark = setInterval(() => {
            count++;
            if (count > 9) {
              count = 0;
              clearInterval(mark);
              this.isPending = false
            }
          }, 1000);
          this.$alertSuccess('请求成功，请等待下载');
        } else {
          this.$alertInfo('请稍后再试')
        }
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
