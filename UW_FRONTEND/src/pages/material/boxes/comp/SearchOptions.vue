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
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="isAdding = !isAdding">手动添加料盒</a>
      </div>
    </div>
    <transition name="fade">
      <div v-if="isAdding" id="add-window">
        <add-box/>
      </div>
    </transition>
  </div>
</template>

<script>
  import AddBox from './subscomp/AddBox'
  import eventBus from '@/utils/eventBus'
  import {mapGetters, mapActions} from 'vuex';
  import 'vue-datetime/dist/vue-datetime.css'
  import _ from 'lodash'
  import {supplierSelectUrl} from "../../../../config/globalUrl";
  import {axiosPost} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";

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
      'select-comp': {
        props: ['opt'],
        template: '<div class="row">\n' +
          '      <div class="form-group col pr-3">\n' +
          '        <label :for="opt.id">{{opt.name}}：</label>\n' +
          '        <select :id="opt.id" v-model="opt.model" class="custom-select">\n' +
          '          <option value="" disabled>请选择</option>\n' +
          '          <option :value="item.id"  v-for="item in opt.list">{{item.name}}</option>\n' +
          '        </select>\n' +
          '      </div>\n' +
          '    </div>'
      },
      AddBox
    },
    data() {
      return {
        queryOptions: [
          {
            id: 'id',
            name: '料盒号',
            model: '',
            type: 'text'
          },
          {
            id: 'area',
            name: '区域',
            model: '',
            type: 'select',
            list:[
              {
                id:'A',
                name:'A'
              },{
                id:'B',
                name:'B'
              },{
                id:'C',
                name:'C'
              }
            ]
          },
          {
            id: 'row',
            name: '行',
            model: '',
            type: 'text'
          },
          {
            id: 'col',
            name: '列',
            model: '',
            type: 'text'
          },
          {
            id: 'height',
            name: '高度',
            model: '',
            type: 'text'
          },
          {
            id: 'is_on_shelf',
            name: '是否在架',
            model: '',
            type: 'select',
            list:[
              {
                id:1,
                name:'是'
              },{
                id:0,
                name:'否'
              }
            ]
          },
          {
            id: 'supplier',
            name: '供应商',
            model: '',
            type: 'select',
            list:[]
          },
          {
            id: 'type',
            name: '料盒类型',
            model: '',
            type: 'select',
            list:[
              {
                id:1,
                name:'标准'
              },{
                id:2,
                name:'' +
                  '非标准'
              }
            ]
          }
        ],
        copyQueryOptions: [],
        queryString: "",
        isAdding: false,
        suppliers:[]
      }
    },
    mounted: function () {
      this.initForm();
      this.selectSupplier();
      eventBus.$on('closeAddPanel', () => {
        this.isAdding = false;
      })
    },
    computed: {
      ...mapGetters([
        'tableRouterApi'
      ]),
    },
    watch: {
      /*$route: function (route) {
        if(JSON.stringify(route.params) === "{}" && JSON.stringify(route.query) === "{}"){
          this.initForm();
        }
      },*/
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
                if(item.id === "id"){
                  this.queryString += ("material_box.id" + "=" + _.trim(item.model))
                }else{
                  this.queryString += (item.id + "=" + _.trim(item.model))
                }
              } else {
                if(item.id === "id"){
                  this.queryString += ("#&#" + "material_box.id"+ "=" + _.trim(item.model))
                }else {
                  this.queryString += ("#&#" + item.id + "=" + _.trim(item.model))
                }
              }

            } else {
              this.setLoading(false)
            }
          }

        })
      },
      fetchData: function () {
        let options = {
          path: '/material/boxes',
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
      selectSupplier: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: supplierSelectUrl,
            data: {}
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              let data = response.data.data.list;
              data.map((item,index) => {
                if(item.enabled === true){
                  this.suppliers.push(item);
                }
              });
              this.queryOptions[6].list = this.suppliers;
            } else {
              errHandler(response.data)
            }
          })
            .catch(err => {
              if (JSON.stringify(err) !== '{}') {
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试');
              }
            })
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
  #add-window{
    z-index:100;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
