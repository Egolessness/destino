import{b as f,ac as T,g as a,e as n,R as m,d as S,u as P,cq as H,W as V,dG as D,bt as j,h as W,n as E,dH as q,k,X as g,q as F,bY as M,bZ as O,r as A,ak as B,aw as N,ar as R,aq as I,al as K,av as X,as as Y,an as Z,a6 as J,a4 as Q}from"./index-14be5eda.js";const e="0!important",L="-1px!important";function u(t){return n(t+"-type",[a("& +",[f("button",{},[n(t+"-type",[m("border",{borderLeftWidth:e}),m("state-border",{left:L})])])])])}function p(t){return n(t+"-type",[a("& +",[f("button",[n(t+"-type",[m("border",{borderTopWidth:e}),m("state-border",{top:L})])])])])}const U=f("button-group",`
 flex-wrap: nowrap;
 display: inline-flex;
 position: relative;
`,[T("vertical",{flexDirection:"row"},[T("rtl",[f("button",[a("&:first-child:not(:last-child)",`
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
 `),u("default"),n("ghost",[u("primary"),u("info"),u("success"),u("warning"),u("error")])])])]),n("vertical",{flexDirection:"column"},[f("button",[a("&:first-child:not(:last-child)",`
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
 `),p("default"),n("ghost",[p("primary"),p("info"),p("success"),p("warning"),p("error")])])])]),tt={size:{type:String,default:void 0},vertical:Boolean},et=S({name:"ButtonGroup",props:tt,setup(t){const{mergedClsPrefixRef:r,mergedRtlRef:i}=P(t);return H("-button-group",U,r),V(D,t),{rtlEnabled:j("ButtonGroup",i,r),mergedClsPrefix:r}},render(){const{mergedClsPrefix:t}=this;return W("div",{class:[`${t}-button-group`,this.rtlEnabled&&`${t}-button-group--rtl`,this.vertical&&`${t}-button-group--vertical`],role:"group"},this.$slots)}}),rt=f("h",`
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
 `),a("&::before",{backgroundColor:"var(--n-bar-color)"})])]),at=Object.assign(Object.assign({},E.props),{type:{type:String,default:"default"},prefix:String,alignText:Boolean}),h=t=>S({name:`H${t}`,props:at,setup(r){const{mergedClsPrefixRef:i,inlineThemeDisabled:d}=P(r),s=E("Typography","-h",rt,q,r,i),c=k(()=>{const{type:$}=r,{common:{cubicBezierEaseInOut:b},self:{headerFontWeight:x,headerTextColor:v,[g("headerPrefixWidth",t)]:l,[g("headerFontSize",t)]:z,[g("headerMargin",t)]:y,[g("headerBarWidth",t)]:_,[g("headerBarColor",$)]:C}}=s.value;return{"--n-bezier":b,"--n-font-size":z,"--n-margin":y,"--n-bar-color":C,"--n-bar-width":_,"--n-font-weight":x,"--n-text-color":v,"--n-prefix-width":l}}),o=d?F(`h${t}`,k(()=>r.type[0]),c,r):void 0;return{mergedClsPrefix:i,cssVars:d?void 0:c,themeClass:o==null?void 0:o.themeClass,onRender:o==null?void 0:o.onRender}},render(){var r;const{prefix:i,alignText:d,mergedClsPrefix:s,cssVars:c,$slots:o}=this;return(r=this.onRender)===null||r===void 0||r.call(this),W(`h${t}`,{class:[`${s}-h`,`${s}-h${t}`,this.themeClass,{[`${s}-h--prefix-bar`]:i,[`${s}-h--align-text`]:d}],style:c},o)}});h("1");h("2");h("3");h("4");h("5");const ot=h("6"),st=S({name:"NamespaceTabs",__name:"namespace-tabs",props:{type:{default:"registration"}},emits:["update:activated"],setup(t,{expose:r,emit:i}){const d=t,{loadNamespaces:s,setActivated:c,getActivated:o,activated:$}=d.type==="registration"?M():O(),b=A([$]);async function x(){const l=await s();await o(),b.value=l}function v(l){c(l)&&i("update:activated",l)}return r({load:x,selected:o}),(l,z)=>{const y=Q,_=et,C=ot;return B(),N(C,{prefix:"bar",type:"default"},{default:R(()=>[I(_,{size:"small"},{default:R(()=>[(B(!0),K(J,null,X(b.value,(w,G)=>(B(),N(y,{key:G,bordered:!1,type:w.activated?"primary":"default",onClick:nt=>v(w)},{default:R(()=>[Y(Z(w.name),1)]),_:2},1032,["type","onClick"]))),128))]),_:1})]),_:1})}}});export{st as _,et as a};
