import {destinationSelectUrl, supplierSelectUrl} from "../../../plugins/globalUrl";
import {axiosPost} from "../../../utils/fetchData";
import {errHandler} from "../../../utils/errorHandler";


export default {
  data() {
    return {
      suppliers: [],
      origins: [],
      destinations: [],
      tableData: [],
      pageNo: 1,
      pageSize: 20,
      totalData: 0,

      pickerOptions: {
        shortcuts: [{
          text: '最近一周',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: '最近一个月',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
            picker.$emit('pick', [start, end]);
          }
        }]
      },

      isLoading: false,
      isPending: false,
      activeCompanyId: parseInt(window.localStorage.getItem('activeCompanyId'))

    }
  },

  methods: {
    _selectSupplier: function () {
      let options = {
        url: supplierSelectUrl,
        data: {
          filter: 'company.id=' + this.activeCompanyId
        }
      };
      axiosPost(options).then(res => {
        if (res.data.result === 200) {
          let data = res.data.data.list;
          data.map((item) => {
            if (item.enabled === true) {
              this.suppliers.push(item);
            }
          });
        } else {
          errHandler(res.data)
        }
      }).catch(err => {
        console.log(err);
        this.$alertError('连接超时，请刷新重试');
      })
    },
    _selectDestination: function () {
      let options = {
        url: destinationSelectUrl,
        data: {
          filter: 'company.id=' + this.activeCompanyId
        }
      };
      axiosPost(options).then(res => {
        if (res.data.result === 200) {
          let data = res.data.data.list;
          let list = [];
          data.map((item) => {
            if (item.id !== 0 && item.id !== -1) {
              list.push(item);
            }
          });
          this.origins = data;
          this.destinations = list;
        } else {
          errHandler(res.data);
        }
      }).catch(err => {
        console.log(err);
        this.$alertError('连接超时，请刷新重试');
      })
    },

    _selectData: function (url, queryData) {
      return new Promise((resolve, reject) => {
        if (!this.isPending) {
          this.isPending = true;
          this.isLoading = true;
          let options = {
            url: url,
            data: {
              pageNo: this.pageNo,
              pageSize: this.pageSize,
              ascBy: this.ascBy,
              descBy: this.descBy,
            }
          };
          Object.assign(options.data, queryData);
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              resolve(res.data.data)
            } else {
              errHandler(res.data)
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            this.isPending = false;
            this.isLoading = false;
          })
        } else {
          reject();
        }
      })
    },

    _pageSizeChange: function (callback) {
      this.pageNo = 1;
      callback()
    },
    _sortChange: function (data) {
      let prop = '';
      if (data.prop === "supplierName") {
        prop = "supplier";
      } else {
        prop = data.prop;
      }
      if (data.order === "ascending") {
        this.ascBy = prop;
        this.descBy = '';
      } else if (data.order === "descending") {
        this.descBy = prop;
        this.ascBy = '';
      } else {
        this.descBy = '';
        this.ascBy = '';
      }
      this.pageNo = 1;
      this.queryData();
    }
  }
}