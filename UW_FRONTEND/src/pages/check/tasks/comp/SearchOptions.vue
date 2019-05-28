<template>
  <div class="options-area">
    <div class="form-row">
      <div v-for="item in queryOptions" class="row no-gutters pl-3 pr-3">
        <component :opt="item" :is="item.type + '-comp'" :callback="thisFetch"></component>
      </div>
      <div class="form-group row align-items-end">
        <div class="btn btn-secondary ml-3 mr-4" @click="initForm">清空条件</div>
      </div>
      <div class="form-group row align-items-end">
        <div class="btn btn-primary ml-3 mr-4" @click="thisFetch">查询</div>
      </div>
      <div class="form-group row align-items-end">
        <div class="btn btn-primary ml-3 mr-4" @click="isCreating = !isCreating">创建任务</div>
      </div>
    </div>
    <transition name="fade" class="upload-window" v-if="isCreating">
      <create-task></create-task>
    </transition>
  </div>
</template>

<script>
  import {mapActions} from 'vuex';
  import {supplierSelectUrl} from "../../../../config/globalUrl";
  import Datetime from '@/components/vue-datetime/Datetime'
  import _ from 'lodash'
  import {errHandler} from "../../../../utils/errorHandler";
  import {axiosPost} from "../../../../utils/fetchData";
  import CreateTask from "./subscomp/CreateTask";
  import eventBus from "../../../../utils/eventBus";

  export default {
    name: "SearchOptions",
    components: {
      CreateTask,
      'text-comp': {
        props: ['opt', 'callback'],
        template: '<div class="form-group col pr-3"">\n' +
          '           <label :for="opt.id">{{opt.name}}：</label>\n' +
          '           <input type="text" class="form-control" :id="opt.id" v-model="opt.model" @keyup.enter="callback" autocomplete="off">\n' +
          '          </div>'
      },
      'date-comp': {
        props: ['opt'],
        components: {
          Datetime
        },
        template: '<div class="row">\n' +
          '    <div class="form-group col pr-3">\n' +
          '      <label>创建时间  从：</label>\n' +
          '      <datetime v-model="opt.modelFrom" type="datetime" zone="Asia/Shanghai" value-zone="Asia/Shanghai"/>\n' +
          '    </div>\n' +
          '    <div class="form-group col pr-3">\n' +
          '      <label>至：</label>\n' +
          '      <datetime v-model="opt.modelTo" type="datetime" zone="Asia/Shanghai" value-zone="Asia/Shanghai"/>\n' +
          '    </div>\n' +
          '  </div>'

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
      }
    },
    data() {
      return {
        suppliers:[],
        queryOptions: [],
        copyQueryOptions: [],
        queryString: "",
        isCreating:false
      }
    },
    mounted(){
      this.selectSupplier();
      this.initForm();
      eventBus.$on('closeCreatePanel',() => {
        this.isCreating = false;
      })
    },
    methods: {
      ...mapActions(['setLoading']),
      initForm: function () {
        this.queryOptions = [
          {
            id: 'state',
            name: '状态',
            model: '',
            type: 'select',
            list: [
              {
                id: '1',
                name: '未开始'
              },
              {
                id: '2',
                name: '进行中'
              },
              {
                id: '3',
                name: '已完成'
              }
            ]
          },
          {
            id: 'create_time',
            name: '创建时间',
            modelFrom: '',
            modelTo: '',
            type: 'date'
          },
          {
            id: 'supplier',
            name: '供应商',
            model: '',
            type: 'select',
            list:this.suppliers
          },
        ]
      },
      createQueryString: function () {
        this.queryString = "";
        this.copyQueryOptions = this.queryOptions.filter((item) => {
          if ((item.modelFrom === '') ^ (item.modelTo === '')) {
            this.$alertInfo('日期选择不完整');
          }
          if (!(item.model === "" || item.modelFrom === "" || item.modelTo === "")) {
            return true;
          }
        });
        this.copyQueryOptions.map((item, index) => {
          if (item.type === 'text' || item.type === 'select') {
            if (_.trim(item.model) !== "") {
              if (index === 0) {
                  this.queryString += (item.id + "=" + _.trim(item.model));
              } else {
                  this.queryString += ("#&#" + item.id + "=" + _.trim(item.model))
              }
            } else {
              this.setLoading(false)
            }
          } else if (item.type === 'date') {
            if (item.modelFrom !== '' && item.modelTo !== '') {
              let tempFrom = item.modelFrom.replace('T', ' ').replace('Z', '');
              let tempTo = item.modelTo.replace('T', ' ').replace('Z', '');
              if (this.compareDate(tempFrom, tempTo) >= 0) {
                if (index === 0) {
                  this.queryString += (item.id + '>=' + tempFrom + '#&#' + item.id + '<=' + tempTo)
                } else {
                  this.queryString += ('#&#' + item.id + '>=' + tempFrom + '#&#' + item.id + '<=' + tempTo)
                }
              } else {
                this.$alertWarning('日期格式错误');
                this.setLoading(false);
              }
            }
          }
        })
      },
      fetchData: function () {
        let options = {
          path: '/check/tasks',
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
      compareDate: function (dateFrom, dateTo) {
        let compFrom = new Date(dateFrom);
        let compTo = new Date(dateTo);
        return (compTo - compFrom);
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
              data.map((item) => {
                if(item.enabled === true){
                  this.suppliers.push(item);
                }
              });
              this.queryOptions[2].list = this.suppliers;
            } else {
              errHandler(response.data);
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
      },
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

  #upload-window {
    z-index: 100;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
