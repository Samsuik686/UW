<template>
  <div class="header" :class="{ topCollapsed: isCollapse }">
    <div class="header-collapse" style="display:flex;align-items:center;min-width:210px;">
      <i :class="[isCollapse ? 'el-icon-s-unfold': 'el-icon-s-fold']" @click="toggleSiderBar"></i>
      <span class="active-name">{{activeName}}</span>
    </div>
    <div class="company-dropdown">
      <el-dropdown
        trigger="click"
        @command="setCompany" placement="bottom-end" v-if="setCompanyShowing">
        <span class="el-dropdown-link">
          {{company}}<i class="el-icon-arrow-down el-icon--right"></i>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-for="item in companies" :command="item">{{item.name}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
    <div class="user-info">
      <div style="min-width:100px;">
        <a>{{user.name}}</a>
      </div>
      <div style="min-width:50px;">
        <i class="el-icon-coke-power" title="退出" @click.prevent="logout"></i>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapGetters, mapActions} from 'vuex'
  import {companySelectUrl, logoutUrl} from "../plugins/globalUrl";
  import {axiosPost} from "../utils/fetchData";
  import {errHandler} from "../utils/errorHandler";

  export default {
    name: "PageHeader",
    inject: ['reloadAll'],
    data() {
      return {
        isPending: false,
        company: '',
        companies: [],
      }
    },

    watch: {
      $route: function (val) {
        //当更新公司时亦更新当前候选
        this.companies = JSON.parse(window.localStorage.getItem('companiesList'));
        let activeCompanyId = window.localStorage.getItem('activeCompanyId');
        this.companies.forEach(item => {
          if (item.id === parseInt(activeCompanyId)) {
            this.company = item.name
          }
        })
      }
    },

    computed: {
      ...mapGetters(['isCollapse', 'user', 'activeName']),

      setCompanyShowing() {
        let path = this.$route.path;
        let pathGroup = ['/logs/taskLog', '/infos/factory', '/logs/actionLog', '/robot', '/io/preview', '/io/call', '/io/inNow', '/io/outNow', '/io/returnNow', '/io/preciousNow', '/io/outEmergency', '/inventory/operation', '/sample/operation', '/sample/preciousOperation', '/user', '/config', '/help/write', '/help/read'];
        return pathGroup.indexOf(path) === -1
      }
    },
    async mounted() {
      let companies = JSON.parse(window.localStorage.getItem('companiesList'));
      if (!companies) {
        await this.getCompaniesList().then(data => {
          window.localStorage.setItem('activeCompanyId', data[0].id);
          window.localStorage.setItem('companiesList', JSON.stringify(data));

          this.companies = data;
          let activeCompanyId = window.localStorage.getItem('activeCompanyId');
          this.companies.forEach(item => {
            if (item.id === parseInt(activeCompanyId)) {
              this.company = item.name
            }
          })
        });
      } else {
        this.companies = companies;
        let activeCompanyId = window.localStorage.getItem('activeCompanyId');
        this.companies.forEach(item => {
          if (item.id === parseInt(activeCompanyId)) {
            this.company = item.name
          }
        })
      }

    },
    methods: {
      ...mapActions(['setToken', 'setIsCollapse']),
      toggleSiderBar: function () {
        this.setIsCollapse(!this.isCollapse);
      },
      logout: function () {
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: logoutUrl,
            data: {}
          };
          axiosPost(options).then(res => {
            if (res.data.result === 200) {
              this.setToken('');
              window.sessionStorage.clear();
              this.$router.replace('/login');
            } else {
              errHandler(res.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('请求超时，请刷新重试');
          }).finally(() => {
            this.isPending = false;
          })
        }
      },
      setCompany(item) {
        this.company = item.name;
        window.localStorage.setItem('activeCompanyId', item.id);
        this.reloadAll();
      },

      getCompaniesList() {
        return new Promise(resolve => {
          axiosPost({
            url: companySelectUrl
          }).then(response => {
            if (response.data.result === 200) {
              resolve(response.data.data)
            } else {
              errHandler(response.data);
            }
          }).catch(err => {
            console.log(err);
            this.$alertError('连接超时，请刷新重试');
          }).finally(() => {
            resolve([])
          })
        })
      }
    }
  }
</script>

<style scoped lang="scss">
  .header {
    overflow: hidden;
    position: fixed;
    right: 0;
    width: calc(100% - 180px);
    background: #4BC0C0;
    color: #fff;
    padding: 0 15px;
    z-index: 4;
    box-sizing: border-box;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.15);
    transition: all 0.5s;
    height: 60px;
    display: flex;

    a {
      font-size: 18px;
      line-height: 60px;
      font-weight: bold;
      letter-spacing: 1px;
    }

    .header-collapse i {
      font-size: 24px;
      line-height: 60px;
      transition: all 0.5s;

      &:hover {
        cursor: pointer;
      }
    }

    .logout {
      text-decoration: none;
    }

    .active-name {
      font-size: 18px;
      font-weight: bold;
      line-height: 60px;
      margin-left: 20px;
    }

    .company-dropdown {
      margin-left: auto;
      margin-right: 20px;
      display: flex;
      justify-content: flex-end;
      width: 100px;
      line-height: 60px;

      span {
        color: #ffffff;
        cursor: pointer;
      }
    }

    .user-info {
      display: flex;
      min-width: 150px;
      align-items: center;
      text-align: center;

      i {
        cursor: pointer;
      }
    }
  }

  .row-right > div {
    text-align: center;
  }

  .header.topCollapsed {
    width: calc(100% - 64px);
  }

</style>