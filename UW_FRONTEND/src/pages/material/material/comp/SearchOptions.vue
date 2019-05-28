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
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="isExporting = !isExporting">导出物料列表</a>
      </div>
      <div class="form-group row align-items-end">
        <a href="#" class="btn btn-primary ml-3 mr-4" @click="isUploading = !isUploading">导入物料类型表</a>
      </div>
      <div class="form-group row align-items-end">
        <button class="btn btn-primary ml-4" @click="deleteByIds">批量删除</button>
      </div>
    </div>
    <transition name="fade">
      <div v-if="isAdding" id="add-window">
        <add-material/>
      </div>
    </transition>
    <transition name="fade">
      <div v-if="isUploading" id="upload-window">
        <upload-material  :suppliers="suppliers"/>
      </div>
    </transition>
    <transition name="fade">
      <div v-if="isExporting">
        <div class="add-panel">
          <div class="add-panel-container form-row flex-column justify-content-between">
            <div class="form-row">
              <div class="form-group mb-0">
                <h4>导出物料报表：</h4>
              </div>
            </div>
            <div class="form-row">
                <label for="material-supplier" class="col-form-label">供应商:</label>
                <select id="material-supplier" v-model="supplier" class="custom-select">
                  <option  v-for="item in suppliers" :value="item.id" :key="item.id">{{item.name}}</option>
                </select>
            </div>
            <div class="dropdown-divider"></div>
            <div class="form-row justify-content-around">
              <button class="btn btn-secondary col mr-1 text-white" @click="isExporting = false">取消</button>
              <button class="btn btn-primary col ml-1 text-white" @click="exportReport">提交</button>
            </div>

          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
  import AddMaterial from './subscomp/AddMaterial'
  import UploadMaterial from './subscomp/UploadMaterial'
  import eventBus from '@/utils/eventBus'
  import {mapGetters, mapActions} from 'vuex';
  import {materialCountUrl, exportReportUrl, supplierSelectUrl} from "../../../../config/globalUrl";
  import 'vue-datetime/dist/vue-datetime.css'
  import _ from 'lodash'
  import {axiosPost, downloadFile} from "../../../../utils/fetchData";
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
      AddMaterial,
      UploadMaterial
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
          },
          {
            id: 'id',
            name: '物料类型号',
            model: '',
            type: 'text'
          },
          {
            id: 'supplier',
            name: '供应商',
            model: '',
            type: 'select',
            list:[]
          },
        ],
        copyQueryOptions: [],
        queryString: "",
        supplier:'',
        suppliers:[],
        isAdding: false,
        isPending: false,
        isUploading:false,
        isExporting:false
      }
    },
    mounted: function () {
      this.initForm();
      eventBus.$on('closeAddPanel', () => {
        this.isAdding = false;
      });
      eventBus.$on('closeUploadPanel', () => {
        this.isUploading = false;
      });
      this.selectSupplier();
    },
    computed: {
      ...mapGetters([
        'tableRouterApi'
      ]),
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
          if (item.type === 'text') {
            if (_.trim(item.model) !== "") {
              if (index === 0) {
                if(item.id !== "id"){
                  this.queryString += (item.id + "like" + _.trim(item.model))
                }else{
                  this.queryString += (item.id + "=" + _.trim(item.model))
                }
              } else {
                if(item.id !== "id"){
                  this.queryString += ("#&#" + item.id + "like" + _.trim(item.model))
                }else{
                  this.queryString += ("#&#" + item.id + "=" + _.trim(item.model))
                }
              }
            } else {
              this.setLoading(false)
            }
          }
          if (item.type === 'select') {
            if (_.trim(item.model) !== "") {
              if (index === 0) {
                this.queryString += (item.id + "=" + _.trim(item.model))
              } else {
                this.queryString += ("#&#" + item.id + "=" + _.trim(item.model))
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
        if(this.supplier === ''){
          this.$alertWarning('请选择供应商');
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let data = {
            supplier:this.supplier,
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
          this.isExporting = false;
        } else {
          this.$alertInfo('请稍后再试')
        }
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
              this.queryOptions[3].list = this.suppliers;
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
      deleteByIds:function(){
        this.$parent.$children[1].deleteByIds();
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

  #add-window ,#upload-window{
    z-index: 100;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
  .add-panel {
    position: fixed;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    width: 100%;
    left: 0;
    top: 0;
    background: rgba(0, 0, 0, 0.1);
    z-index: 1001;
  }

  .add-panel-container {
    background: #ffffff;
    min-height: 220px;
    max-width: 600px;
    z-index: 1002;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }
</style>
