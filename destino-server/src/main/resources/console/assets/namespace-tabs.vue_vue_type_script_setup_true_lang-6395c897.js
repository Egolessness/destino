import{b as p,a4 as T,g as a,e as n,P as m,d as z,u as k,cf as V,S as D,dr as G,bi as H,h as E,k as W,ds as M,j as N,U as g,n as F,bM as O,bN as A,r as I,ab as S,an as P,ai as B,ah as K,ac as U,am as q,aj as J,ae as Q,$ as X,_ as Y}from"./index-0246bf31.js";const e="0!important",L="-1px!important";function u(t){return n(t+"-type",[a("& +",[p("button",{},[n(t+"-type",[m("border",{borderLeftWidth:e}),m("state-border",{left:L})])])])])}function f(t){return n(t+"-type",[a("& +",[p("button",[n(t+"-type",[m("border",{borderTopWidth:e}),m("state-border",{top:L})])])])])}const Z=p("button-group",`
 flex-wrap: nowrap;
 display: inline-flex;
 position: relative;
`,[T("vertical",{flexDirection:"row"},[T("rtl",[p("button",[a("&:first-child:not(:last-child)",`
 margin-right: ${e};
 border-top-right-radius: ${e};
 border-bottom-right-radius: ${e};
 `),a("&:last-child:not(:first-child)",`
 margin-left: ${e};
 border-top-left-radius: ${e};
 border-bottom-left-radius: ${e};
 `),a("&:not(:first-child):not(:last-child)",`
 margin-left: ${e};
 margin-right: ${e};
 border-radius: ${e};
 `),u("default"),n("ghost",[u("primary"),u("info"),u("success"),u("warning"),u("error")])])])]),n("vertical",{flexDirection:"column"},[p("button",[a("&:first-child:not(:last-child)",`
 margin-bottom: ${e};
 margin-left: ${e};
 margin-right: ${e};
 border-bottom-left-radius: ${e};
 border-bottom-right-radius: ${e};
 `),a("&:last-child:not(:first-child)",`
 margin-top: ${e};
 margin-left: ${e};
 margin-right: ${e};
 border-top-left-radius: ${e};
 border-top-right-radius: ${e};
 `),a("&:not(:first-child):not(:last-child)",`
 margin: ${e};
 border-radius: ${e};
 `),f("default"),n("ghost",[f("primary"),f("info"),f("success"),f("warning"),f("error")])])])]),tt={size:{type:String,default:void 0},vertical:Boolean},et=z({name:"ButtonGroup",props:tt,setup(t){const{mergedClsPrefixRef:r,mergedRtlRef:i}=k(t);return V("-button-group",Z,r),D(G,t),{rtlEnabled:H("ButtonGroup",i,r),mergedClsPrefix:r}},render(){const{mergedClsPrefix:t}=this;return E("div",{class:[`${t}-button-group`,this.rtlEnabled&&`${t}-button-group--rtl`,this.vertical&&`${t}-button-group--vertical`],role:"group"},this.$slots)}}),rt=p("h",`
 font-size: var(--n-font-size);
 font-weight: var(--n-font-weight);
 margin: var(--n-margin);
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
`,[a("&:first-child",{marginTop:0}),n("prefix-bar",{position:"relative",paddingLeft:"var(--n-prefix-width)"},[n("align-text",{paddingLeft:0},[a("&::before",{left:"calc(-1 * var(--n-prefix-width))"})]),a("&::before",`
 content: "";
 width: var(--n-bar-width);
 border-radius: calc(var(--n-bar-width) / 2);
 transition: background-color .3s var(--n-bezier);
 left: 0;
 top: 0;
 bottom: 0;
 position: absolute;
 `),a("&::before",{backgroundColor:"var(--n-bar-color)"})])]),at=Object.assign(Object.assign({},W.props),{type:{type:String,default:"default"},prefix:String,alignText:Boolean}),h=t=>z({name:`H${t}`,props:at,setup(r){const{mergedClsPrefixRef:i,inlineThemeDisabled:d}=k(r),s=W("Typography","-h",rt,M,r,i),c=N(()=>{const{type:$}=r,{common:{cubicBezierEaseInOut:b},self:{headerFontWeight:x,headerTextColor:y,[g("headerPrefixWidth",t)]:l,[g("headerFontSize",t)]:R,[g("headerMargin",t)]:v,[g("headerBarWidth",t)]:_,[g("headerBarColor",$)]:C}}=s.value;return{"--n-bezier":b,"--n-font-size":R,"--n-margin":v,"--n-bar-color":C,"--n-bar-width":_,"--n-font-weight":x,"--n-text-color":y,"--n-prefix-width":l}}),o=d?F(`h${t}`,N(()=>r.type[0]),c,r):void 0;return{mergedClsPrefix:i,cssVars:d?void 0:c,themeClass:o==null?void 0:o.themeClass,onRender:o==null?void 0:o.onRender}},render(){var r;const{prefix:i,alignText:d,mergedClsPrefix:s,cssVars:c,$slots:o}=this;return(r=this.onRender)===null||r===void 0||r.call(this),E(`h${t}`,{class:[`${s}-h`,`${s}-h${t}`,this.themeClass,{[`${s}-h--prefix-bar`]:i,[`${s}-h--align-text`]:d}],style:c},o)}});h("1");h("2");h("3");h("4");h("5");const ot=h("6"),st=z({name:"NamespaceTabs",__name:"namespace-tabs",props:{type:{default:"registration"}},emits:["update:activated"],setup(t,{expose:r,emit:i}){const d=t,{loadNamespaces:s,setActivated:c,getActivated:o,activated:$}=d.type==="registration"?O():A(),b=I([$]);async function x(){const l=await s();await o(),b.value=l}function y(l){c(l)&&i("update:activated",l)}return r({load:x,selected:o}),(l,R)=>{const v=Y,_=et,C=ot;return S(),P(C,{prefix:"bar",type:"default"},{default:B(()=>[K(_,{size:"small"},{default:B(()=>[(S(!0),U(X,null,q(b.value,(w,j)=>(S(),P(v,{key:j,bordered:!1,type:w.activated?"primary":"default",onClick:nt=>y(w)},{default:B(()=>[J(Q(w.name),1)]),_:2},1032,["type","onClick"]))),128))]),_:1})]),_:1})}}});export{st as _,et as a};
