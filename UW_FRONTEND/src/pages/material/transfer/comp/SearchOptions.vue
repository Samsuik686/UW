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
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="isAdding = !isAdding">导入调拨单</a>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="isWastageAdding = !isWastageAdding">导入损耗单</a>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="exportEWhReport">导出库存报表</a>
      </div>
      <transition name="fade" v-if="isAdding">
        <add-transfer :suppliers="suppliers"></add-transfer>
      </transition>
      <transition name="fade" v-if="isWastageAdding">
        <add-wastage :suppliers="suppliers"></add-wastage>
      </transition>
    </div>
  </div>
</template>

<script>
  import eventBus from '@/utils/eventBus'
  import {mapActions} from 'vuex';
  import {destinationSelectUrl, exportEWhReportUrl, supplierSelectUrl} from "../../../../config/globalUrl";
  import {axiosPost, downloadFile} from "../../../../utils/fetchData";
  import {errHandler} from "../../../../utils/errorHandler";
  import AddTransfer from "./subscomp/AddTransfer";
  import AddWastage from "./subscomp/AddWastage";

  export default {
    name: "Options",
    components: {
      AddWastage,
      AddTransfer,
      'text-comp': {
        props: ['opt', 'callback'],
        template: '<div class="form-group col pr-3">\n' +
          '           <label :for="opt.id">{{opt.name}}：</label>\n' +
          '           <input type="text" class="form-control" :id="opt.id" v-model="opt.model" @keyup.enter="callback"  autocomplete="off">\n' +
          '        </div>\n'
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
        queryOptions: [
          {
            id: 'no',
            name: '料号',
            model: '',
            type: 'text'
          },
          {
            id: 'whId',
            name: '仓库',
            model: '',
            type: 'select',
            list:[]
          },
          {
            id: 'supplierId',
            name: '供应商',
            model: '',
            type: 'select',
            list:[]
          },
        ],
        copyQueryOptions: [],
        supplier:'',
        suppliers:[],
        destinations:[],
        isPending: false,
        isAdding:false,
        isWastageAdding:false,
        searchInfo:{}
      }
    },
    mounted: function () {
      this.initForm();
      this.selectSuppliers();
      eventBus.$on('closeTransferUploadPanel',() => {
        this.isAdding = false;
      });
      eventBus.$on('closeWastageUploadPanel',() => {
        this.isWastageAdding = false;
      });
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
        this.searchInfo = {};
        this.copyQueryOptions = this.queryOptions.filter((item) => {
          if (!(item.model === "")) {
            return true;
          }
        });
        this.copyQueryOptions.map((item, index) => {
          this.searchInfo[item.id] = item.model;
        });
      },
      fetchData: function () {
        let options = {
          path: '/material/transfer',
          query: {}
        };
        if (this.searchInfo !== {}) {
          options.query.searchInfo = this.searchInfo;
        }
        this.$router.push('_empty');
        this.$router.replace(options, () => {
            this.setLoading(true);
        });
      },
      thisFetch: function () {
        this.createQueryString();
        this.fetchData()
      },
      selectSuppliers: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: supplierSelectUrl,
            data: {}
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            this.selectDestinations();
            if (response.data.result === 200) {
              let data = response.data.data.list;
              data.map((item) => {
                if(item.enabled === true){
                  this.suppliers.push(item);
                }
              });
              this.queryOptions[2].list = this.suppliers;
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
      },
      selectDestinations:function(){
        if(!this.isPending){
          this.isPending = true;
          let options = {
            url: destinationSelectUrl,
            data: {
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              let data = response.data.data.list;
              let list = [];
              data.map((item) => {
                if(item.id !== 0 && item.id !== -1){
                  list.push(item);
                }
              });
              this.destinations = list;
              this.queryOptions[1].list = this.destinations;
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            if (JSON.stringify(err) !== '{}'){
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试');
            }
          });
        }
      },
      exportEWhReport:function(){
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            whId:this.queryOptions[1].model,
            supplierId:this.queryOptions[2].model,
            no:this.queryOptions[0].model,
            '#TOKEN#': this.$store.state.token
          };
          downloadFile(exportEWhReportUrl, data);
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
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }
  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
