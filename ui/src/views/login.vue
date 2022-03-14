<template>
  <div class="login">
    <el-form ref="loginForm" :model="loginForm" :rules="rulesCheck" class="login-form">
      <h3 class="title">faasadmin后台管理系统</h3>
      <el-tabs v-model="loginType" type="card" @tab-click="switchLogin" stretch>
        <el-tab-pane label="账号密码登录" name="namePassword">
          <el-form-item prop="userName">
            <el-input v-model="loginForm.userName" type="text" auto-complete="off" placeholder="账号">
              <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              auto-complete="off"
              placeholder="密码"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>

          <el-form-item prop="code" v-if="captchaOnOff">
            <el-input
              v-model="loginForm.code"
              auto-complete="off"
              placeholder="验证码"
              style="width: 63%"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
            </el-input>
            <div class="login-code">
              <img :src="codeUrl" @click="getCode" class="login-code-img"/>
            </div>
          </el-form-item>
          <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
        </el-tab-pane>
        <!-- 手机号登录 -->
        <el-tab-pane label="手机号登录" name="mobileCode">
          <el-form-item prop="mobile">
            <el-input v-model="loginForm.mobile" type="text" auto-complete="off" placeholder="手机号">
              <svg-icon slot="prefix" icon-class="mobile" class="el-input__icon input-icon" />
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-input
              v-model="loginForm.code"
              auto-complete="off"
              placeholder="验证码"
              style="width: 63%"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
            </el-input>
            <div class="login-code">
              <el-button @click="getSmsCode()" v-bind:class="{active: isActive}">
                <span v-show="isActive">获取验证码</span>
                <span v-show="!isActive">{{count}}s</span>
              </el-button>
            </div>
          </el-form-item>
        </el-tab-pane>
      </el-tabs>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
        <a @click="toRegister()">注册账户</a>
      </el-form-item>

      <el-form-item style="width:100%;">
        <div class="oauth-login" style="display:flex">
          <div class="oauth-login-item" v-for="item in SysUserSocialTypeEnum" :key="item.type" @click="doSocialLogin(item)">
            <img :src="item.img" height="25px" width="25px" alt="登录" >
            <span>{{item.title}}</span>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2021-now faasadmin.com All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg,getSmsCode } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'
import {InfApiErrorLogProcessStatusEnum, SysUserSocialTypeEnum} from "@/utils/constants";
const TIME_COUNT = 60;
export default {
  name: "Login",
  computed: {
    rulesCheck: function() {
      if (this.loginType == 'namePassword') {
        return this.namePasswordLoginRules
      } else {
        return this.mobileCodeLoginRules
      }
    }
  },
  data() {
    return {
      loginType: 'namePassword',
      isActive: true,
      count: 0,
      timer: null,
      codeUrl: "",
      cookiePassword: "",
      loginForm: {
        userName: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      namePasswordLoginRules: {
        userName: [
          { required: true, trigger: "blur", message: "用户名不能为空" }
        ],
        password: [
          { required: true, trigger: "blur", message: "密码不能为空" }
        ],
        code: [{ required: true, trigger: "change", message: "验证码不能为空" }]
      },
      mobileCodeLoginRules: {
        mobile: [
          { required: true, trigger: "blur", message: "手机号不能为空" }
        ],
        code: [{ required: true, trigger: "change", message: "验证码不能为空" }]
      },
      loading: false,
      captchaOnOff: true,
      redirect: undefined,
      // 枚举
      SysUserSocialTypeEnum: SysUserSocialTypeEnum,
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
    this.getCookie();
  },
  methods: {
    getCode() {
      debugger
      getCodeImg().then(res => {
        res = res.data;
        this.codeUrl = "data:image/gif;base64," + res.img;
        this.loginForm.uuid = res.uuid;
      });
    },
    getCookie() {
      const userName = Cookies.get("userName");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        userName: userName === undefined ? this.loginForm.userName : userName,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    switchLogin(tab, event) {
      this.loginType=tab.name;
      this.$refs.loginForm.clearValidate();
      this.$nextTick(() => this.$refs.loginForm.clearValidate())
    },
    getSmsCode() {
      if (!this.timer) {
        getSmsCode(this.loginForm.mobile).then(res => {
          this.$message({
            message: res.msg,
            type: 'success'
          });
        });
        this.count = TIME_COUNT;
        this.isActive = false;
        this.timer = setInterval(() => {
          if (this.count > 0 && this.count <= TIME_COUNT) {
            this.count--;
          } else {
            this.isActive = true;
            clearInterval(this.timer);
            this.timer = null;
          }
        }, 1000);
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        debugger
        if (valid) {
          console.log(this.loginType)
          this.loading = true;
          if(this.loginType == 'namePassword'){
            if (this.loginForm.rememberMe) {
              Cookies.set("userName", this.loginForm.userName, { expires: 30 });
              Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
              Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
            } else {
              Cookies.remove("userName");
              Cookies.remove("password");
              Cookies.remove('rememberMe');
            }
            debugger
            this.$store.dispatch("NamePasswordlogin", this.loginForm).then(() => {
              this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
            }).catch(() => {
              this.loading = false;
              this.getCode();
            });
          }else {
            this.$store.dispatch("SmsCodelogin", this.loginForm).then(() => {
              this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
            }).catch(() => {
              this.loading = false;
            });
          }
        }
      });
    },
    toRegister(){
      this.$router.push({ path: "/register" });
    },
    doSocialLogin(socialTypeEnum) {
      // console.log("开始Oauth登录...%o", socialTypeEnum.code);
      // 设置登录中
      this.loading = true;
      // 计算 redirectUri
      const redirectUri = location.origin + '/social-login?type=' + socialTypeEnum.type + '&redirect=' + (this.redirect || "/"); // 重定向不能丢
      // const redirectUri = 'http://127.0.0.1:48080/api/gitee/callback';
      // const redirectUri = 'http://127.0.0.1:48080/api/dingtalk/callback';
      // 进行跳转
      socialAuthRedirect(socialTypeEnum.type, encodeURIComponent(redirectUri)).then((res) => {
        // console.log(res.url);
        window.location.href = res.data;
      });
    }
  }
};
</script>

<style rel="stylesheet/scss" lang="scss">
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 38px;
    input {
      height: 38px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 38px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 38px;
}
</style>
