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
    </div>
  </div>
</template>

<script>
  import {mapActions} from 'vuex';
  import {getInventoryTaskUrl, supplierSelectUrl} from "../../../../config/globalUrl";
  import Datetime from '@/components/vue-datetime/Datetime'
  import {errHandler} from "../../../../utils/errorHandler";
  import {axiosPost} from "../../../../utils/fetchData";

  export default {
    name: "Options",
    components: {
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
          '      <label>{{opt.name}}  从：</label>\n' +
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
        suppliers: [],
        queryOptions: [
          {
            id: 'supplier',
            name: '供应商',
            model: '',
            type: 'select',
            list: []
          },
          {
            id: 'taskId',
            name: '盘点任务',
            model: '',
            type: 'select',
            list: []
          },
          {
            id: 'no',
            type: 'text',
            model: '',
            name: '料号'
          }
        ],
        copyQueryOptions: [],
        queryString: "",
        isPending: false,
        tasks: []
      }
    },
    mounted: function () {
      this.selectSupplier();
      this.initForm();
    },
    computed: {
      supplierId: function () {
        return this.queryOptions[0].model;
      },
      taskId:function(){
        return this.queryOptions[1].model;
      }
    },
    watch: {
      supplierId: function (val) {
        this.getInventoryTask(val);
      },
      taskId:function(val){
        if(val !== ''){
          this.setUnInventoryData([]);
          this. thisFetch();
        }
      }
    },
    methods: {
      ...mapActions(['setLoading','setUnInventoryData']),
      initForm: function () {
        this.queryOptions[2].model = '';
      },
      fetchData: function () {
        let options = {
          path: '/check/UWDetails',
          query: {}
        };
        let taskId = this.queryOptions[1].model;
        let no = this.queryOptions[2].model;
        if (taskId === '') {
          this.$alertWarning('任务不能为空');
          return;
        }
        options.query.taskId = taskId;
        options.query.no = no;
        this.$router.push('_empty');
        this.$router.replace(options
          , () => {
            this.setLoading(true);
          })
      },
      thisFetch: function () {
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
                if (item.enabled === true) {
                  this.suppliers.push(item);
                }
              });
              this.queryOptions[0].list = this.suppliers;
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
      getInventoryTask: function (supplierId) {
        if (!this.isPending) {
          this.isPending = false;
          let options = {
            url: getInventoryTaskUrl,
            data: {
              supplierId: supplierId
            }
          };
          axiosPost(options).then(response => {
            this.tasks = response.data.data;
            this.tasks.map((item => {
              item.name = item.file_name
            }));
            this.queryOptions[1].model = '';
            this.queryOptions[1].list = this.tasks;
          }).catch(err => {
            this.isPending = false;
            console.log(err);
            this.$alertDanger('连接超时，请刷新重试');
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
</style>
