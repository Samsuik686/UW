<template>
  <div class="add-panel">
    <div class="form-row justify-content-end">
      <div class="add-panel-container">
        <div class="form-row">
          <div class="form-group mb-0">
            <h3>详情：</h3>
          </div>
          <datatable v-bind="$data"/>
        </div>
      </div>
      <div id="cancel-btn" class="ml-2 mt-1" @click="closePanel">
        <icon name="cancel" scale="4" style="color: #fff;"></icon>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapActions} from 'vuex';
  import store from '../../../store/index'
  import {axiosPost} from "../../../utils/fetchData";
  import {materialEntityUrl} from "../../../config/globalUrl";
  import {errHandler} from "../../../utils/errorHandler";

  export default {
    name: "EntityDetails",
    data() {
      return {
        fixHeaderAndSetBodyMaxHeight: 550,
        tblStyle: {
          'word-break': 'break-all',
          'table-layout': 'fixed'
        },
        HeaderSettings: false,
        pageSizeOptions: [20, 40, 80],
        data: [],
        columns: [
          {title: '料盘唯一码', field: 'id', colStyle: {width: '60px'}},
          {title: '类型', field: 'type', colStyle: {width: '60px'}, visible: false},
          {title: '所在料盒', field: 'box', colStyle: {width: '60px'}},
          {title: '盒内行号', field: 'row', colStyle: {width: '60px'}},
          {title: '盒内列号', field: 'col', colStyle: {width: '60px'}},
          {title: '剩余数量', field: 'remainderQuantity', colStyle: {width: '60px'}},
          {title: '生产日期', field: 'productionTimeString', colStyle: {width: '160px'}},
        ],
        total: 0,
        query: {"limit": 20, "offset": 0},
        isPending: false,

      }
    },
    computed: {},
    watch: {
      query: {
        handler(query) {
          this.setLoading(true);
          this.dataFilter(query);
        },
        deep: true
      }
    },
    mounted() {
      this.fetchData(store.state.materialDetails);
    },
    methods: {
      ...mapActions(['setDetailsActiveState', 'setDetailsData', 'setLoading']),
      init: function () {
        this.data = [];
        this.total = 0;
      },
      fetchData: function (val) {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: materialEntityUrl,
            data: {
              type: val.type,
              box: val.box,
              pageNo: 1,
              pageSize: 20
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              if (response.data.data.list !== null) {
                this.data = response.data.data.list;
                this.total = response.data.data.totalRow;
              } else {
                this.init()
              }
            } else if (response.data.result === 501) {
              this.$alertWarning(response.data.data)
            } else {
              errHandler(response.data.result)
            }
            this.setLoading(false)
          })
            .catch(err => {
              if (JSON.stringify(err) !== '{}'){
                this.isPending = false;
                console.log(JSON.stringify(err));
                this.$alertDanger('请求超时，请刷新重试')
                this.setLoading(false)
              }
            })
        } else {
          this.setLoading(false)
        }
      },
      closePanel: function () {
        this.setDetailsActiveState(false);
        this.setDetailsData({})
      },
      dataFilter: function () {
        let val = store.state.materialDetails;
        let options = {
          url: materialEntityUrl,
          data: {
            type: val.type,
            box: val.box,
          }
        };
        options.data.pageNo = this.query.offset / this.query.limit + 1;
        options.data.pageSize = this.query.limit;
        this.fetchData(options);
      }
    }

  }
</script>

<style scoped>
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
    z-index: 101;
  }

  .add-panel-container {
    background: #ffffff;
    min-height: 220px;
    max-width: 800px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }

  #cancel-btn {
    height: 100%;
    cursor: pointer;
  }
</style>
