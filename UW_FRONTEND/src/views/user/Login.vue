<template>
  <div class="login">
    <div class="login-box">
      <div class="login-name">智能仓储系统</div>
      <el-form ref="loginForm"
               :model="loginInfo"
               class="login-form"
               :rules="rules"
               status-icon
               @submit.native.prevent="login('loginForm')">
        <el-form-item prop="uid">
          <el-input v-model.trim="loginInfo.uid"
                    ref="login-input"
                    placeholder="用户名"
                    prefix-icon="el-icon-user"
                    size="large"
                    autocomplete="false"
                    spellcheck="false"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model.trim="loginInfo.password"
                    placeholder="密码"
                    prefix-icon="el-icon-lock"
                    size="large"
                    type="password"
                    show-password
                    autocomplete="false"
                    spellcheck="false"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary"
                     style="width:100%;"
                     native-type="submit">登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <copyright></copyright>
  </div>
</template>

<script>
  import Copyright from './../../components/Copyright'
  import {mapActions} from 'vuex'
  import {axiosPost} from "../../utils/fetchData";
  import {errHandler} from "../../utils/errorHandler";
  import {loginUrl} from "../../plugins/globalUrl";

  export default {
    name: "Login",
    data() {
      return {
        loginInfo: {
          uid: '',
          password: ''
        },
        rules: {
          uid: [
            {required: true, message: '请输入用户名', trigger: 'change'},
          ],
          password: [
            {required: true, message: '请输入密码', trigger: 'change'}
          ]
        },
        isPending: false
      }
    },
    components: {
      Copyright
    },
    mounted() {
      this.$nextTick(() => {
        this.$refs['login-input'].focus();
      })
    },
    methods: {
      ...mapActions(['setToken', 'setUser']),
      login: function (loginForm) {
        this.$refs[loginForm].validate((valid) => {
          if (valid) {
            if (!this.isPending) {
              this.isPending = true;
              let options = {
                url: loginUrl,
                data: {
                  uid: this.loginInfo.uid,
                  password: this.loginInfo.password
                }
              };
              axiosPost(options).then(response => {
                this.isPending = false;
                if (response.data.result === 200) {
                  this.$alertSuccess("登录成功");
                  let data = response.data.data;
                  this.setToken(data['#TOKEN#']);
                  this.setUser(data);
                  window.sessionStorage.setItem("token", data['#TOKEN#']);
                  window.sessionStorage.setItem("user", JSON.stringify(data));
                  this.$router.replace('/material/uwMaterial');
                } else {
                  errHandler(response.data);
                }
              }).catch(err => {
                this.$alertError('请求超时，请刷新重试');
                console.log(err);
              }).finally(() => {
                this.isPending = false;
              })
            }
          }
        });
      }
    }
  }
</script>

<style scoped lang="scss">
  .login {
    width: 100%;
    height: 100%;

    .login-box {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 360px;
      background: #4BC0C0;
      box-shadow: 0 0 25px #cacaca;
      border-radius: 4px;

      .login-name {
        width: 100%;
        text-align: center;
        font-size: 20px;
        font-weight: bold;
        padding: 20px 0;
        color: #fff;
      }

      .login-form {
        box-sizing: border-box;
        background: #fff;
        padding: 35px 40px 20px 40px;

        .el-form-item {
          margin-bottom: 24px;
        }

        .el-form-item__label {
          font-size: 16px;
          font-weight: bold;
        }
      }
    }
  }
</style>