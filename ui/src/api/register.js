import request from '@/utils/request'

// 注册
export function register(mobile, code, name, password,applyModule,loginAccount) {
  const data = {
    name,
    password,
    code,
    mobile,
    applyModule,
    loginAccount,
  }
  return request({
    url: '/system/register/lessee',
    method: 'post',
    data: data
  })
}

// 获取短信验证码
export function getSmsCode(mobile) {
  const data = {
    mobile
  }
  return request({
    url: '/system/sms/register/smsCode',
    method: 'POST',
    data: data
  })
}

//获取所有应用
export function getApplyModules(){
  return request({
    url: '/system/register/applys',
    method: 'POST'
  })
}
