<template>
  <div class="user-options form-row">
    <div class="btn pl-1 pr-1" title="收发记录" @click="getMaterialRecords(row)">
      <icon name="card" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="详细" @click="checkMaterialDetails(row)">
      <icon name="list" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="更新" @click="isEditing = true">
      <icon name="edit" scale="1.8"></icon>
    </div>
    <div class="btn pl-1 pr-1" title="删除" @click="showWarning(row)">
      <icon name="cancel" scale="1.8"></icon>
    </div>


    <div v-if="isEditing" id="edit-window">
      <edit-material :editData="row"/>
    </div>


    <div v-if="isDeleting" id="delete-window">
      <div class="delete-panel">
        <div class="delete-panel-container form-row flex-column justify-content-between">
          <div class="form-row">
            <div class="form-group mb-0">
              <h3>确认删除：</h3>
            </div>
          </div>
          <div class="form-row w-100">
            <div class="text-center">
                <p>你正在删除料号为 "{{rowData.no}}" 的物料，请确认是否删除</p>
            </div>
          </div>
          <div class="dropdown-divider"></div>
          <div class="form-row justify-content-around">
            <a class="btn btn-secondary col mr-1 text-white" @click="isDeleting = false">取消</a>
            <a class="btn btn-danger col ml-1 text-white" @click="submitDelete">确定</a>
          </div>
        </div>
      </div>
    </div>
    <div v-if="isDetailsShowing" >
      <io-details :detailsID="detailsID"/>
    </div>
    </div>
</template>

<script>
  import EditMaterial from './EditMaterial'
  import eventBus from '@/utils/eventBus'
  import {mapActions, mapGetters} from 'vuex'
  import {materialUpdateUrl, getMaterialRecordsUrl} from "../../../../../config/globalUrl";
  import {axiosPost} from "../../../../../utils/fetchData";
  import {errHandler} from "../../../../../utils/errorHandler";
  import IODetails from "./IODetails";

  export default {
    name: "OperationOptions",
    props: ['row'],
    components: {
      'io-details': IODetails,
      EditMaterial
    },
    data() {
      return {
        isEditing: false,
        isDeleting: false,
        rowData: {},
        isDetailsShowing: false,

        isPending: false,
        detailsID: ''
      }
    },
    watch: {
      'detailsData.query': {
        handler(val) {
          this.setLoading(true);
          this.dataFilter(val);
        },
        deep: true
      }
    },
    mounted() {
      eventBus.$on('closeEditPanel', () => {
        this.isEditing = false;
      });
      eventBus.$on('closeIODetailsPanel', () => {
        this.isDetailsShowing = false;
      })
    },
    computed: {
      //...mapGetters['isDetailsActive']
    },
    methods: {
      ...mapActions(['setDetailsActiveState', 'setDetailsData', 'setLoading']),
      checkMaterialDetails: function (val) {
        this.setLoading(true);
        this.setDetailsActiveState(true);
        this.setDetailsData({});
        this.setDetailsData({
          type: val.id
        })
      },
      showWarning: function (val) {
        this.rowData = val;
        this.isDeleting = true;
      },
      submitDelete: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: materialUpdateUrl,
            data: JSON.parse(JSON.stringify(this.rowData))
          };
          options.data.enabled = 0;

          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('删除成功');
              this.isDeleting = false;
              let tempUrl = this.$route.fullPath;
              console.log(tempUrl);
              this.$router.push('_empty');
              this.$router.replace(tempUrl);
            } else if (response.data.result === 412) {
              this.$alertWarning(response.data.data);
              this.isDeleting = false;

            } else {
              this.isPending = false;
              errHandler(response.data);
              this.isDeleting = false;
            }
          }).catch(err => {
            if (JSON.stringify(err)) {
              this.isPending = false;
              console.log(JSON.stringify(err));
              this.$alertDanger('请求超时，请刷新重试')
            }
          })
        }
      },
      getMaterialRecords: function (val) {
        this.detailsID = val.id;
        this.isDetailsShowing = true;
      }
    }
  }
</script>

<style scoped>
  #edit-window {
    z-index: 100;
  }

  .delete-panel {
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

  .delete-panel-container {
    background: #ffffff;
    min-height: 220px;
    width: 400px;
    z-index: 102;
    border-radius: 10px;
    box-shadow: 3px 3px 20px 1px #bbb;
    padding: 30px 60px 10px 60px;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }

</style>
