import{aw as r}from"./index-0246bf31.js";const a=/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/,i=/^(?!^.*[\u4E00-\u9FA5].*$)([^(0-9a-zA-Z)]|[()]|[a-z]|[A-Z]|[0-9]){5,18}$/,u=/^\d{6}$/,s=e=>({required:!0,trigger:"input",renderMessage:()=>((e==null?void 0:e())||"")+r("message.tip.notNull")}),d={username:[s(()=>r("message.user.username"))],pwd:[s(()=>r("message.user.password")),{pattern:i,renderMessage:()=>r("message.tip.pwdRegex"),trigger:"input"}],code:[s(()=>"验证码"),{pattern:u,message:"验证码格式错误",trigger:"input"}],email:[{pattern:a,renderMessage:()=>r("message.user.email.formatError"),trigger:"blur"}]};function o(e){return e.trim()===""}function c(e){return[s(()=>r("message.user.confirmPassword.fullName")),{validator:(n,t)=>!o(t)&&t!==e.value?Promise.reject(n.message):Promise.resolve(),renderMessage:()=>r("message.user.passwordInconsistent"),trigger:"input"}]}export{s as c,d as f,c as g};