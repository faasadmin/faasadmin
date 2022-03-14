<template>
  <div class="login">
    <el-form ref="registerForm" :model="registerForm"  :rules="registerRules" class="register-form">
      <h3 class="title">乐盒后台管理系统 - 用户注册</h3>
      <el-form-item prop="applyModule">
        <el-select v-model="registerForm.applyModule" placeholder="请选择应用模块" clearable style="width: 100%">
          <el-option v-for="dict in applys"
                     :key="dict.id" :label="dict.name" :value="dict.id"/>
        </el-select>
      </el-form-item>
      <el-form-item prop="userName">
        <el-input v-model="registerForm.userName" type="text" auto-complete="off" placeholder="用户名">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="loginAccount">
        <el-input v-model="registerForm.loginAccount" type="text" auto-complete="off" placeholder="登陆账号">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="phonenumber">
        <el-input v-model="registerForm.phonenumber" type="text" auto-complete="off" placeholder="手机号">
          <svg-icon slot="prefix" icon-class="phone" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-input
          v-model="registerForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="handleRegister"
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
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleRegister"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleRegister"
        >
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <a @click="toLogin()">登陆账户</a>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-login-footer">
      <span>Copyright © 2021 luobosheng All Rights Reserved. 客服微信：csmszf</span>
    </div>
  </div>
</template>

<script>
import {register,getSmsCode,getApplyModules } from "@/api/register";
import {listSimpleFolders} from "@/api/system/fileRecordFolder";
const TIME_COUNT = 60;
export default {
  name: "Register",
  data() {
    return {
      isActive: true,
      count: 0,
      timer: null,
      cookiePassword: "",
      applys:[],
      registerForm: {
        loginAccount: "",
        userName: "",
        password: "",
        phonenumber: "",
        code: "",
        applyModule: "",
      },
      registerRules: {
        applyModule: [
          { required: true, trigger: "blur", message: "应用模块不能为空" }
        ],
        loginAccount: [
          { required: true, trigger: "blur", message: "登陆账号不能为空" }
        ],
        userName: [
          { required: true, trigger: "blur", message: "用户名不能为空" }
        ],
        password: [
          { required: true, trigger: "blur", message: "密码不能为空" }
        ],
        phonenumber: [
          { required: true, trigger: "blur", message: "手机号不能为空" }
        ],
        code: [{ required: true, trigger: "change", message: "验证码不能为空" }]
      },
      loading: false,
      captchaOnOff: true,
      redirect: undefined
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
    this.getApplyModules();
  },
  methods: {
    getApplyModules(){
      getApplyModules().then(response => {
        this.applys = response.data;
      });
    },
    getSmsCode() {
      if (!this.timer) {
        getSmsCode(this.registerForm.phonenumber).then(res => {
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
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true;
          this.$store.dispatch("userRegister", this.registerForm).then((res) => {
            this.loading = false;
            //this.$router.push({ path: this.redirect || "/login" }).catch(()=>{});
            this.$message({
              message: '注册成功!',
              type: 'success'
            });
            setTimeout(()=>{
              //需要延迟的代码 :3秒后延迟跳转到首页，可以加提示什么的
              this.$router.push({ path: "/login" });
              //延迟时间：3秒
            },3000)
          })
        }
      });
    },
    toLogin(){
      this.$router.push({ path: "/login" });
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

.register-form {
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
