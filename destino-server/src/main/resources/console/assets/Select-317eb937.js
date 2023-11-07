import{d as ce,dd as So,o as Ie,dS as Jo,dv as et,j as M,r as O,a0 as De,dy as lo,h as s,q as ot,dg as ho,bg as He,v as so,af as tt,dT as vo,m as ko,b as E,P as _,g as re,u as co,k as he,dU as nt,H as uo,co as lt,U as X,n as Be,N as Ro,Z as ze,T as zo,e as Q,a4 as Se,f as To,t as ne,dV as it,i as Te,S as io,cC as ro,bd as rt,c7 as at,s as st,bj as Je,c as ct,dW as dt,cg as U,bi as ut,ce as ft,G as ht,Y as ue,dX as go,dY as vt,w as gt,dZ as bt,$ as pt,Q as mt,p as Ct,W as yt,c5 as wt,X as bo,d_ as xt,V as St,cr as kt}from"./index-0246bf31.js";import{l as Fo,m as We,a as Oo,n as fo,q as Rt,k as zt,i as je,s as Tt,N as Ft,u as po,g as Ot,b as ao,V as Pt,c as Mt,d as It,e as Bt}from"./Input-8d4ddc63.js";import{b as _t}from"./Space-5a2962b0.js";function $t(e){switch(typeof e){case"string":return e||void 0;case"number":return String(e);default:return}}function eo(e){const n=e.filter(t=>t!==void 0);if(n.length!==0)return n.length===1?n[0]:t=>{e.forEach(r=>{r&&r(t)})}}function mo(e){return e&-e}class Et{constructor(n,t){this.l=n,this.min=t;const r=new Array(n+1);for(let i=0;i<n+1;++i)r[i]=0;this.ft=r}add(n,t){if(t===0)return;const{l:r,ft:i}=this;for(n+=1;n<=r;)i[n]+=t,n+=mo(n)}get(n){return this.sum(n+1)-this.sum(n)}sum(n){if(n===void 0&&(n=this.l),n<=0)return 0;const{ft:t,min:r,l:i}=this;if(n>i)throw new Error("[FinweckTree.sum]: `i` is larger than length.");let c=n*r;for(;n>0;)c+=t[n],n-=mo(n);return c}getBound(n){let t=0,r=this.l;for(;r>t;){const i=Math.floor((t+r)/2),c=this.sum(i);if(c>n){r=i;continue}else if(c<n){if(t===i)return this.sum(t+1)<=n?t+1:i;t=i}else return i}return t}}let Ve;function At(){return Ve===void 0&&("matchMedia"in window?Ve=window.matchMedia("(pointer:coarse)").matches:Ve=!1),Ve}let oo;function Co(){return oo===void 0&&(oo="chrome"in window?window.devicePixelRatio:1),oo}const Nt=We(".v-vl",{maxHeight:"inherit",height:"100%",overflow:"auto",minWidth:"1px"},[We("&:not(.v-vl--show-scrollbar)",{scrollbarWidth:"none"},[We("&::-webkit-scrollbar, &::-webkit-scrollbar-track-piece, &::-webkit-scrollbar-thumb",{width:0,height:0,display:"none"})])]),Lt=ce({name:"VirtualList",inheritAttrs:!1,props:{showScrollbar:{type:Boolean,default:!0},items:{type:Array,default:()=>[]},itemSize:{type:Number,required:!0},itemResizable:Boolean,itemsStyle:[String,Object],visibleItemsTag:{type:[String,Object],default:"div"},visibleItemsProps:Object,ignoreItemResize:Boolean,onScroll:Function,onWheel:Function,onResize:Function,defaultScrollKey:[Number,String],defaultScrollIndex:Number,keyField:{type:String,default:"key"},paddingTop:{type:[Number,String],default:0},paddingBottom:{type:[Number,String],default:0}},setup(e){const n=So();Nt.mount({id:"vueuc/virtual-list",head:!0,anchorMetaName:Fo,ssr:n}),Ie(()=>{const{defaultScrollIndex:v,defaultScrollKey:b}=e;v!=null?f({index:v}):b!=null&&f({key:b})});let t=!1,r=!1;Jo(()=>{if(t=!1,!r){r=!0;return}f({top:x.value,left:m})}),et(()=>{t=!0,r||(r=!0)});const i=M(()=>{const v=new Map,{keyField:b}=e;return e.items.forEach((P,W)=>{v.set(P[b],W)}),v}),c=O(null),u=O(void 0),l=new Map,w=M(()=>{const{items:v,itemSize:b,keyField:P}=e,W=new Et(v.length,b);return v.forEach((j,V)=>{const A=j[P],K=l.get(A);K!==void 0&&W.add(V,K)}),W}),y=O(0);let m=0;const x=O(0),z=De(()=>Math.max(w.value.getBound(x.value-lo(e.paddingTop))-1,0)),C=M(()=>{const{value:v}=u;if(v===void 0)return[];const{items:b,itemSize:P}=e,W=z.value,j=Math.min(W+Math.ceil(v/P+1),b.length-1),V=[];for(let A=W;A<=j;++A)V.push(b[A]);return V}),f=(v,b)=>{if(typeof v=="number"){k(v,b,"auto");return}const{left:P,top:W,index:j,key:V,position:A,behavior:K,debounce:a=!0}=v;if(P!==void 0||W!==void 0)k(P,W,K);else if(j!==void 0)S(j,K,a);else if(V!==void 0){const g=i.value.get(V);g!==void 0&&S(g,K,a)}else A==="bottom"?k(0,Number.MAX_SAFE_INTEGER,K):A==="top"&&k(0,0,K)};let R,F=null;function S(v,b,P){const{value:W}=w,j=W.sum(v)+lo(e.paddingTop);if(!P)c.value.scrollTo({left:0,top:j,behavior:b});else{R=v,F!==null&&window.clearTimeout(F),F=window.setTimeout(()=>{R=void 0,F=null},16);const{scrollTop:V,offsetHeight:A}=c.value;if(j>V){const K=W.get(v);j+K<=V+A||c.value.scrollTo({left:0,top:j+K-A,behavior:b})}else c.value.scrollTo({left:0,top:j,behavior:b})}}function k(v,b,P){c.value.scrollTo({left:v,top:b,behavior:P})}function D(v,b){var P,W,j;if(t||e.ignoreItemResize||te(b.target))return;const{value:V}=w,A=i.value.get(v),K=V.get(A),a=(j=(W=(P=b.borderBoxSize)===null||P===void 0?void 0:P[0])===null||W===void 0?void 0:W.blockSize)!==null&&j!==void 0?j:b.contentRect.height;if(a===K)return;a-e.itemSize===0?l.delete(v):l.set(v,a-e.itemSize);const L=a-K;if(L===0)return;V.add(A,L);const ee=c.value;if(ee!=null){if(R===void 0){const ae=V.sum(A);ee.scrollTop>ae&&ee.scrollBy(0,L)}else if(A<R)ee.scrollBy(0,L);else if(A===R){const ae=V.sum(A);a+ae>ee.scrollTop+ee.offsetHeight&&ee.scrollBy(0,L)}le()}y.value++}const Z=!At();let q=!1;function H(v){var b;(b=e.onScroll)===null||b===void 0||b.call(e,v),(!Z||!q)&&le()}function G(v){var b;if((b=e.onWheel)===null||b===void 0||b.call(e,v),Z){const P=c.value;if(P!=null){if(v.deltaX===0&&(P.scrollTop===0&&v.deltaY<=0||P.scrollTop+P.offsetHeight>=P.scrollHeight&&v.deltaY>=0))return;v.preventDefault(),P.scrollTop+=v.deltaY/Co(),P.scrollLeft+=v.deltaX/Co(),le(),q=!0,_t(()=>{q=!1})}}}function J(v){if(t||te(v.target)||v.contentRect.height===u.value)return;u.value=v.contentRect.height;const{onResize:b}=e;b!==void 0&&b(v)}function le(){const{value:v}=c;v!=null&&(x.value=v.scrollTop,m=v.scrollLeft)}function te(v){let b=v;for(;b!==null;){if(b.style.display==="none")return!0;b=b.parentElement}return!1}return{listHeight:u,listStyle:{overflow:"auto"},keyToIndex:i,itemsStyle:M(()=>{const{itemResizable:v}=e,b=He(w.value.sum());return y.value,[e.itemsStyle,{boxSizing:"content-box",height:v?"":b,minHeight:v?b:"",paddingTop:He(e.paddingTop),paddingBottom:He(e.paddingBottom)}]}),visibleItemsStyle:M(()=>(y.value,{transform:`translateY(${He(w.value.sum(z.value))})`})),viewportItems:C,listElRef:c,itemsElRef:O(null),scrollTo:f,handleListResize:J,handleListScroll:H,handleListWheel:G,handleItemResize:D}},render(){const{itemResizable:e,keyField:n,keyToIndex:t,visibleItemsTag:r}=this;return s(ho,{onResize:this.handleListResize},{default:()=>{var i,c;return s("div",ot(this.$attrs,{class:["v-vl",this.showScrollbar&&"v-vl--show-scrollbar"],onScroll:this.handleListScroll,onWheel:this.handleListWheel,ref:"listElRef"}),[this.items.length!==0?s("div",{ref:"itemsElRef",class:"v-vl-items",style:this.itemsStyle},[s(r,Object.assign({class:"v-vl-visible-items",style:this.visibleItemsStyle},this.visibleItemsProps),{default:()=>this.viewportItems.map(u=>{const l=u[n],w=t.get(l),y=this.$slots.default({item:u,index:w})[0];return e?s(ho,{key:l,onResize:m=>this.handleItemResize(l,m)},{default:()=>y}):(y.key=l,y)})})]):(c=(i=this.$slots).empty)===null||c===void 0?void 0:c.call(i)])}})}}),xe="v-hidden",Ht=We("[v-hidden]",{display:"none!important"}),yo=ce({name:"Overflow",props:{getCounter:Function,getTail:Function,updateCounter:Function,onUpdateOverflow:Function},setup(e,{slots:n}){const t=O(null),r=O(null);function i(){const{value:u}=t,{getCounter:l,getTail:w}=e;let y;if(l!==void 0?y=l():y=r.value,!u||!y)return;y.hasAttribute(xe)&&y.removeAttribute(xe);const{children:m}=u,x=u.offsetWidth,z=[],C=n.tail?w==null?void 0:w():null;let f=C?C.offsetWidth:0,R=!1;const F=u.children.length-(n.tail?1:0);for(let k=0;k<F-1;++k){if(k<0)continue;const D=m[k];if(R){D.hasAttribute(xe)||D.setAttribute(xe,"");continue}else D.hasAttribute(xe)&&D.removeAttribute(xe);const Z=D.offsetWidth;if(f+=Z,z[k]=Z,f>x){const{updateCounter:q}=e;for(let H=k;H>=0;--H){const G=F-1-H;q!==void 0?q(G):y.textContent=`${G}`;const J=y.offsetWidth;if(f-=z[H],f+J<=x||H===0){R=!0,k=H-1,C&&(k===-1?(C.style.maxWidth=`${x-J}px`,C.style.boxSizing="border-box"):C.style.maxWidth="");break}}}}const{onUpdateOverflow:S}=e;R?S!==void 0&&S(!0):(S!==void 0&&S(!1),y.setAttribute(xe,""))}const c=So();return Ht.mount({id:"vueuc/overflow",head:!0,anchorMetaName:Fo,ssr:c}),Ie(i),{selfRef:t,counterRef:r,sync:i}},render(){const{$slots:e}=this;return so(this.sync),s("div",{class:"v-overflow",ref:"selfRef"},[tt(e,"default"),e.counter?e.counter():s("span",{style:{display:"inline-block"},ref:"counterRef"}),e.tail?e.tail():null])}});function Po(e,n){n&&(Ie(()=>{const{value:t}=e;t&&vo.registerHandler(t,n)}),ko(()=>{const{value:t}=e;t&&vo.unregisterHandler(t)}))}const Vt=ce({name:"Checkmark",render(){return s("svg",{xmlns:"http://www.w3.org/2000/svg",viewBox:"0 0 16 16"},s("g",{fill:"none"},s("path",{d:"M14.046 3.486a.75.75 0 0 1-.032 1.06l-7.93 7.474a.85.85 0 0 1-1.188-.022l-2.68-2.72a.75.75 0 1 1 1.068-1.053l2.234 2.267l7.468-7.038a.75.75 0 0 1 1.06.032z",fill:"currentColor"})))}}),Dt=ce({name:"Empty",render(){return s("svg",{viewBox:"0 0 28 28",fill:"none",xmlns:"http://www.w3.org/2000/svg"},s("path",{d:"M26 7.5C26 11.0899 23.0899 14 19.5 14C15.9101 14 13 11.0899 13 7.5C13 3.91015 15.9101 1 19.5 1C23.0899 1 26 3.91015 26 7.5ZM16.8536 4.14645C16.6583 3.95118 16.3417 3.95118 16.1464 4.14645C15.9512 4.34171 15.9512 4.65829 16.1464 4.85355L18.7929 7.5L16.1464 10.1464C15.9512 10.3417 15.9512 10.6583 16.1464 10.8536C16.3417 11.0488 16.6583 11.0488 16.8536 10.8536L19.5 8.20711L22.1464 10.8536C22.3417 11.0488 22.6583 11.0488 22.8536 10.8536C23.0488 10.6583 23.0488 10.3417 22.8536 10.1464L20.2071 7.5L22.8536 4.85355C23.0488 4.65829 23.0488 4.34171 22.8536 4.14645C22.6583 3.95118 22.3417 3.95118 22.1464 4.14645L19.5 6.79289L16.8536 4.14645Z",fill:"currentColor"}),s("path",{d:"M25 22.75V12.5991C24.5572 13.0765 24.053 13.4961 23.5 13.8454V16H17.5L17.3982 16.0068C17.0322 16.0565 16.75 16.3703 16.75 16.75C16.75 18.2688 15.5188 19.5 14 19.5C12.4812 19.5 11.25 18.2688 11.25 16.75L11.2432 16.6482C11.1935 16.2822 10.8797 16 10.5 16H4.5V7.25C4.5 6.2835 5.2835 5.5 6.25 5.5H12.2696C12.4146 4.97463 12.6153 4.47237 12.865 4H6.25C4.45507 4 3 5.45507 3 7.25V22.75C3 24.5449 4.45507 26 6.25 26H21.75C23.5449 26 25 24.5449 25 22.75ZM4.5 22.75V17.5H9.81597L9.85751 17.7041C10.2905 19.5919 11.9808 21 14 21L14.215 20.9947C16.2095 20.8953 17.842 19.4209 18.184 17.5H23.5V22.75C23.5 23.7165 22.7165 24.5 21.75 24.5H6.25C5.2835 24.5 4.5 23.7165 4.5 22.75Z",fill:"currentColor"}))}}),Wt=ce({props:{onFocus:Function,onBlur:Function},setup(e){return()=>s("div",{style:"width: 0; height: 0",tabindex:0,onFocus:e.onFocus,onBlur:e.onBlur})}}),jt=E("empty",`
 display: flex;
 flex-direction: column;
 align-items: center;
 font-size: var(--n-font-size);
`,[_("icon",`
 width: var(--n-icon-size);
 height: var(--n-icon-size);
 font-size: var(--n-icon-size);
 line-height: var(--n-icon-size);
 color: var(--n-icon-color);
 transition:
 color .3s var(--n-bezier);
 `,[re("+",[_("description",`
 margin-top: 8px;
 `)])]),_("description",`
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
 `),_("extra",`
 text-align: center;
 transition: color .3s var(--n-bezier);
 margin-top: 12px;
 color: var(--n-extra-text-color);
 `)]),Kt=Object.assign(Object.assign({},he.props),{description:String,showDescription:{type:Boolean,default:!0},showIcon:{type:Boolean,default:!0},size:{type:String,default:"medium"},renderIcon:Function}),Ut=ce({name:"Empty",props:Kt,setup(e){const{mergedClsPrefixRef:n,inlineThemeDisabled:t}=co(e),r=he("Empty","-empty",jt,nt,e,n),{localeRef:i}=Oo("Empty"),c=uo(lt,null),u=M(()=>{var m,x,z;return(m=e.description)!==null&&m!==void 0?m:(z=(x=c==null?void 0:c.mergedComponentPropsRef.value)===null||x===void 0?void 0:x.Empty)===null||z===void 0?void 0:z.description}),l=M(()=>{var m,x;return((x=(m=c==null?void 0:c.mergedComponentPropsRef.value)===null||m===void 0?void 0:m.Empty)===null||x===void 0?void 0:x.renderIcon)||(()=>s(Dt,null))}),w=M(()=>{const{size:m}=e,{common:{cubicBezierEaseInOut:x},self:{[X("iconSize",m)]:z,[X("fontSize",m)]:C,textColor:f,iconColor:R,extraTextColor:F}}=r.value;return{"--n-icon-size":z,"--n-font-size":C,"--n-bezier":x,"--n-text-color":f,"--n-icon-color":R,"--n-extra-text-color":F}}),y=t?Be("empty",M(()=>{let m="";const{size:x}=e;return m+=x[0],m}),w,e):void 0;return{mergedClsPrefix:n,mergedRenderIcon:l,localizedDescription:M(()=>u.value||i.value.description),cssVars:t?void 0:w,themeClass:y==null?void 0:y.themeClass,onRender:y==null?void 0:y.onRender}},render(){const{$slots:e,mergedClsPrefix:n,onRender:t}=this;return t==null||t(),s("div",{class:[`${n}-empty`,this.themeClass],style:this.cssVars},this.showIcon?s("div",{class:`${n}-empty__icon`},e.icon?e.icon():s(Ro,{clsPrefix:n},{default:this.mergedRenderIcon})):null,this.showDescription?s("div",{class:`${n}-empty__description`},e.default?e.default():this.localizedDescription):null,e.extra?s("div",{class:`${n}-empty__extra`},e.extra()):null)}});function qt(e,n){return s(zo,{name:"fade-in-scale-up-transition"},{default:()=>e?s(Ro,{clsPrefix:n,class:`${n}-base-select-option__check`},{default:()=>s(Vt)}):null})}const wo=ce({name:"NBaseSelectOption",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0}},setup(e){const{valueRef:n,pendingTmNodeRef:t,multipleRef:r,valueSetRef:i,renderLabelRef:c,renderOptionRef:u,labelFieldRef:l,valueFieldRef:w,showCheckmarkRef:y,nodePropsRef:m,handleOptionClick:x,handleOptionMouseEnter:z}=uo(fo),C=De(()=>{const{value:S}=t;return S?e.tmNode.key===S.key:!1});function f(S){const{tmNode:k}=e;k.disabled||x(S,k)}function R(S){const{tmNode:k}=e;k.disabled||z(S,k)}function F(S){const{tmNode:k}=e,{value:D}=C;k.disabled||D||z(S,k)}return{multiple:r,isGrouped:De(()=>{const{tmNode:S}=e,{parent:k}=S;return k&&k.rawNode.type==="group"}),showCheckmark:y,nodeProps:m,isPending:C,isSelected:De(()=>{const{value:S}=n,{value:k}=r;if(S===null)return!1;const D=e.tmNode.rawNode[w.value];if(k){const{value:Z}=i;return Z.has(D)}else return S===D}),labelField:l,renderLabel:c,renderOption:u,handleMouseMove:F,handleMouseEnter:R,handleClick:f}},render(){const{clsPrefix:e,tmNode:{rawNode:n},isSelected:t,isPending:r,isGrouped:i,showCheckmark:c,nodeProps:u,renderOption:l,renderLabel:w,handleClick:y,handleMouseEnter:m,handleMouseMove:x}=this,z=qt(t,e),C=w?[w(n,t),c&&z]:[ze(n[this.labelField],n,t),c&&z],f=u==null?void 0:u(n),R=s("div",Object.assign({},f,{class:[`${e}-base-select-option`,n.class,f==null?void 0:f.class,{[`${e}-base-select-option--disabled`]:n.disabled,[`${e}-base-select-option--selected`]:t,[`${e}-base-select-option--grouped`]:i,[`${e}-base-select-option--pending`]:r,[`${e}-base-select-option--show-checkmark`]:c}],style:[(f==null?void 0:f.style)||"",n.style||""],onClick:eo([y,f==null?void 0:f.onClick]),onMouseenter:eo([m,f==null?void 0:f.onMouseenter]),onMousemove:eo([x,f==null?void 0:f.onMousemove])}),s("div",{class:`${e}-base-select-option__content`},C));return n.render?n.render({node:R,option:n,selected:t}):l?l({node:R,option:n,selected:t}):R}}),xo=ce({name:"NBaseSelectGroupHeader",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0}},setup(){const{renderLabelRef:e,renderOptionRef:n,labelFieldRef:t,nodePropsRef:r}=uo(fo);return{labelField:t,nodeProps:r,renderLabel:e,renderOption:n}},render(){const{clsPrefix:e,renderLabel:n,renderOption:t,nodeProps:r,tmNode:{rawNode:i}}=this,c=r==null?void 0:r(i),u=n?n(i,!1):ze(i[this.labelField],i,!1),l=s("div",Object.assign({},c,{class:[`${e}-base-select-group-header`,c==null?void 0:c.class]}),u);return i.render?i.render({node:l,option:i}):t?t({node:l,option:i,selected:!1}):l}}),Gt=E("base-select-menu",`
 line-height: 1.5;
 outline: none;
 z-index: 0;
 position: relative;
 border-radius: var(--n-border-radius);
 transition:
 background-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 background-color: var(--n-color);
`,[E("scrollbar",`
 max-height: var(--n-height);
 `),E("virtual-list",`
 max-height: var(--n-height);
 `),E("base-select-option",`
 min-height: var(--n-option-height);
 font-size: var(--n-option-font-size);
 display: flex;
 align-items: center;
 `,[_("content",`
 z-index: 1;
 white-space: nowrap;
 text-overflow: ellipsis;
 overflow: hidden;
 `)]),E("base-select-group-header",`
 min-height: var(--n-option-height);
 font-size: .93em;
 display: flex;
 align-items: center;
 `),E("base-select-menu-option-wrapper",`
 position: relative;
 width: 100%;
 `),_("loading, empty",`
 display: flex;
 padding: 12px 32px;
 flex: 1;
 justify-content: center;
 `),_("loading",`
 color: var(--n-loading-color);
 font-size: var(--n-loading-size);
 `),_("action",`
 padding: 8px var(--n-option-padding-left);
 font-size: var(--n-option-font-size);
 transition: 
 color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 border-top: 1px solid var(--n-action-divider-color);
 color: var(--n-action-text-color);
 `),E("base-select-group-header",`
 position: relative;
 cursor: default;
 padding: var(--n-option-padding);
 color: var(--n-group-header-text-color);
 `),E("base-select-option",`
 cursor: pointer;
 position: relative;
 padding: var(--n-option-padding);
 transition:
 color .3s var(--n-bezier),
 opacity .3s var(--n-bezier);
 box-sizing: border-box;
 color: var(--n-option-text-color);
 opacity: 1;
 `,[Q("show-checkmark",`
 padding-right: calc(var(--n-option-padding-right) + 20px);
 `),re("&::before",`
 content: "";
 position: absolute;
 left: 4px;
 right: 4px;
 top: 0;
 bottom: 0;
 border-radius: var(--n-border-radius);
 transition: background-color .3s var(--n-bezier);
 `),re("&:active",`
 color: var(--n-option-text-color-pressed);
 `),Q("grouped",`
 padding-left: calc(var(--n-option-padding-left) * 1.5);
 `),Q("pending",[re("&::before",`
 background-color: var(--n-option-color-pending);
 `)]),Q("selected",`
 color: var(--n-option-text-color-active);
 `,[re("&::before",`
 background-color: var(--n-option-color-active);
 `),Q("pending",[re("&::before",`
 background-color: var(--n-option-color-active-pending);
 `)])]),Q("disabled",`
 cursor: not-allowed;
 `,[Se("selected",`
 color: var(--n-option-text-color-disabled);
 `),Q("selected",`
 opacity: var(--n-option-opacity-disabled);
 `)]),_("check",`
 font-size: 16px;
 position: absolute;
 right: calc(var(--n-option-padding-right) - 4px);
 top: calc(50% - 7px);
 color: var(--n-option-check-color);
 transition: color .3s var(--n-bezier);
 `,[To({enterScale:"0.5"})])])]),Yt=ce({name:"InternalSelectMenu",props:Object.assign(Object.assign({},he.props),{clsPrefix:{type:String,required:!0},scrollable:{type:Boolean,default:!0},treeMate:{type:Object,required:!0},multiple:Boolean,size:{type:String,default:"medium"},value:{type:[String,Number,Array],default:null},autoPending:Boolean,virtualScroll:{type:Boolean,default:!0},show:{type:Boolean,default:!0},labelField:{type:String,default:"label"},valueField:{type:String,default:"value"},loading:Boolean,focusable:Boolean,renderLabel:Function,renderOption:Function,nodeProps:Function,showCheckmark:{type:Boolean,default:!0},onMousedown:Function,onScroll:Function,onFocus:Function,onBlur:Function,onKeyup:Function,onKeydown:Function,onTabOut:Function,onMouseenter:Function,onMouseleave:Function,onResize:Function,resetMenuOnOptionsChange:{type:Boolean,default:!0},inlineThemeDisabled:Boolean,onToggle:Function}),setup(e){const n=he("InternalSelectMenu","-internal-select-menu",Gt,it,e,ne(e,"clsPrefix")),t=O(null),r=O(null),i=O(null),c=M(()=>e.treeMate.getFlattenedNodes()),u=M(()=>zt(c.value)),l=O(null);function w(){const{treeMate:a}=e;let g=null;const{value:L}=e;L===null?g=a.getFirstAvailableNode():(e.multiple?g=a.getNode((L||[])[(L||[]).length-1]):g=a.getNode(L),(!g||g.disabled)&&(g=a.getFirstAvailableNode())),v(g||null)}function y(){const{value:a}=l;a&&!e.treeMate.getNode(a.key)&&(l.value=null)}let m;Te(()=>e.show,a=>{a?m=Te(()=>e.treeMate,()=>{e.resetMenuOnOptionsChange?(e.autoPending?w():y(),so(b)):y()},{immediate:!0}):m==null||m()},{immediate:!0}),ko(()=>{m==null||m()});const x=M(()=>lo(n.value.self[X("optionHeight",e.size)])),z=M(()=>Je(n.value.self[X("padding",e.size)])),C=M(()=>e.multiple&&Array.isArray(e.value)?new Set(e.value):new Set),f=M(()=>{const a=c.value;return a&&a.length===0});function R(a){const{onToggle:g}=e;g&&g(a)}function F(a){const{onScroll:g}=e;g&&g(a)}function S(a){var g;(g=i.value)===null||g===void 0||g.sync(),F(a)}function k(){var a;(a=i.value)===null||a===void 0||a.sync()}function D(){const{value:a}=l;return a||null}function Z(a,g){g.disabled||v(g,!1)}function q(a,g){g.disabled||R(g)}function H(a){var g;je(a,"action")||(g=e.onKeyup)===null||g===void 0||g.call(e,a)}function G(a){var g;je(a,"action")||(g=e.onKeydown)===null||g===void 0||g.call(e,a)}function J(a){var g;(g=e.onMousedown)===null||g===void 0||g.call(e,a),!e.focusable&&a.preventDefault()}function le(){const{value:a}=l;a&&v(a.getNext({loop:!0}),!0)}function te(){const{value:a}=l;a&&v(a.getPrev({loop:!0}),!0)}function v(a,g=!1){l.value=a,g&&b()}function b(){var a,g;const L=l.value;if(!L)return;const ee=u.value(L.key);ee!==null&&(e.virtualScroll?(a=r.value)===null||a===void 0||a.scrollTo({index:ee}):(g=i.value)===null||g===void 0||g.scrollTo({index:ee,elSize:x.value}))}function P(a){var g,L;!((g=t.value)===null||g===void 0)&&g.contains(a.target)&&((L=e.onFocus)===null||L===void 0||L.call(e,a))}function W(a){var g,L;!((g=t.value)===null||g===void 0)&&g.contains(a.relatedTarget)||(L=e.onBlur)===null||L===void 0||L.call(e,a)}io(fo,{handleOptionMouseEnter:Z,handleOptionClick:q,valueSetRef:C,pendingTmNodeRef:l,nodePropsRef:ne(e,"nodeProps"),showCheckmarkRef:ne(e,"showCheckmark"),multipleRef:ne(e,"multiple"),valueRef:ne(e,"value"),renderLabelRef:ne(e,"renderLabel"),renderOptionRef:ne(e,"renderOption"),labelFieldRef:ne(e,"labelField"),valueFieldRef:ne(e,"valueField")}),io(Rt,t),Ie(()=>{const{value:a}=i;a&&a.sync()});const j=M(()=>{const{size:a}=e,{common:{cubicBezierEaseInOut:g},self:{height:L,borderRadius:ee,color:ae,groupHeaderTextColor:me,actionDividerColor:Ce,optionTextColorPressed:ge,optionTextColor:ve,optionTextColorDisabled:de,optionTextColorActive:ie,optionOpacityDisabled:be,optionCheckColor:fe,actionTextColor:Fe,optionColorPending:ye,optionColorActive:we,loadingColor:Oe,loadingSize:Pe,optionColorActivePending:Me,[X("optionFontSize",a)]:ke,[X("optionHeight",a)]:Re,[X("optionPadding",a)]:se}}=n.value;return{"--n-height":L,"--n-action-divider-color":Ce,"--n-action-text-color":Fe,"--n-bezier":g,"--n-border-radius":ee,"--n-color":ae,"--n-option-font-size":ke,"--n-group-header-text-color":me,"--n-option-check-color":fe,"--n-option-color-pending":ye,"--n-option-color-active":we,"--n-option-color-active-pending":Me,"--n-option-height":Re,"--n-option-opacity-disabled":be,"--n-option-text-color":ve,"--n-option-text-color-active":ie,"--n-option-text-color-disabled":de,"--n-option-text-color-pressed":ge,"--n-option-padding":se,"--n-option-padding-left":Je(se,"left"),"--n-option-padding-right":Je(se,"right"),"--n-loading-color":Oe,"--n-loading-size":Pe}}),{inlineThemeDisabled:V}=e,A=V?Be("internal-select-menu",M(()=>e.size[0]),j,e):void 0,K={selfRef:t,next:le,prev:te,getPendingTmNode:D};return Po(t,e.onResize),Object.assign({mergedTheme:n,virtualListRef:r,scrollbarRef:i,itemSize:x,padding:z,flattenedNodes:c,empty:f,virtualListContainer(){const{value:a}=r;return a==null?void 0:a.listElRef},virtualListContent(){const{value:a}=r;return a==null?void 0:a.itemsElRef},doScroll:F,handleFocusin:P,handleFocusout:W,handleKeyUp:H,handleKeyDown:G,handleMouseDown:J,handleVirtualListResize:k,handleVirtualListScroll:S,cssVars:V?void 0:j,themeClass:A==null?void 0:A.themeClass,onRender:A==null?void 0:A.onRender},K)},render(){const{$slots:e,virtualScroll:n,clsPrefix:t,mergedTheme:r,themeClass:i,onRender:c}=this;return c==null||c(),s("div",{ref:"selfRef",tabindex:this.focusable?0:-1,class:[`${t}-base-select-menu`,i,this.multiple&&`${t}-base-select-menu--multiple`],style:this.cssVars,onFocusin:this.handleFocusin,onFocusout:this.handleFocusout,onKeyup:this.handleKeyUp,onKeydown:this.handleKeyDown,onMousedown:this.handleMouseDown,onMouseenter:this.onMouseenter,onMouseleave:this.onMouseleave},this.loading?s("div",{class:`${t}-base-select-menu__loading`},s(rt,{clsPrefix:t,strokeWidth:20})):this.empty?s("div",{class:`${t}-base-select-menu__empty`,"data-empty":!0},st(e.empty,()=>[s(Ut,{theme:r.peers.Empty,themeOverrides:r.peerOverrides.Empty})])):s(at,{ref:"scrollbarRef",theme:r.peers.Scrollbar,themeOverrides:r.peerOverrides.Scrollbar,scrollable:this.scrollable,container:n?this.virtualListContainer:void 0,content:n?this.virtualListContent:void 0,onScroll:n?void 0:this.doScroll},{default:()=>n?s(Lt,{ref:"virtualListRef",class:`${t}-virtual-list`,items:this.flattenedNodes,itemSize:this.itemSize,showScrollbar:!1,paddingTop:this.padding.top,paddingBottom:this.padding.bottom,onResize:this.handleVirtualListResize,onScroll:this.handleVirtualListScroll,itemResizable:!0},{default:({item:u})=>u.isGroup?s(xo,{key:u.key,clsPrefix:t,tmNode:u}):u.ignored?null:s(wo,{clsPrefix:t,key:u.key,tmNode:u})}):s("div",{class:`${t}-base-select-menu-option-wrapper`,style:{paddingTop:this.padding.top,paddingBottom:this.padding.bottom}},this.flattenedNodes.map(u=>u.isGroup?s(xo,{key:u.key,clsPrefix:t,tmNode:u}):s(wo,{clsPrefix:t,key:u.key,tmNode:u})))}),ro(e.action,u=>u&&[s("div",{class:`${t}-base-select-menu__action`,"data-action":!0,key:"action"},u),s(Wt,{onFocus:this.onTabOut,key:"focus-detector"})]))}}),Zt=e=>{const{textColor2:n,primaryColorHover:t,primaryColorPressed:r,primaryColor:i,infoColor:c,successColor:u,warningColor:l,errorColor:w,baseColor:y,borderColor:m,opacityDisabled:x,tagColor:z,closeIconColor:C,closeIconColorHover:f,closeIconColorPressed:R,borderRadiusSmall:F,fontSizeMini:S,fontSizeTiny:k,fontSizeSmall:D,fontSizeMedium:Z,heightMini:q,heightTiny:H,heightSmall:G,heightMedium:J,closeColorHover:le,closeColorPressed:te,buttonColor2Hover:v,buttonColor2Pressed:b,fontWeightStrong:P}=e;return Object.assign(Object.assign({},dt),{closeBorderRadius:F,heightTiny:q,heightSmall:H,heightMedium:G,heightLarge:J,borderRadius:F,opacityDisabled:x,fontSizeTiny:S,fontSizeSmall:k,fontSizeMedium:D,fontSizeLarge:Z,fontWeightStrong:P,textColorCheckable:n,textColorHoverCheckable:n,textColorPressedCheckable:n,textColorChecked:y,colorCheckable:"#0000",colorHoverCheckable:v,colorPressedCheckable:b,colorChecked:i,colorCheckedHover:t,colorCheckedPressed:r,border:`1px solid ${m}`,textColor:n,color:z,colorBordered:"rgb(250, 250, 252)",closeIconColor:C,closeIconColorHover:f,closeIconColorPressed:R,closeColorHover:le,closeColorPressed:te,borderPrimary:`1px solid ${U(i,{alpha:.3})}`,textColorPrimary:i,colorPrimary:U(i,{alpha:.12}),colorBorderedPrimary:U(i,{alpha:.1}),closeIconColorPrimary:i,closeIconColorHoverPrimary:i,closeIconColorPressedPrimary:i,closeColorHoverPrimary:U(i,{alpha:.12}),closeColorPressedPrimary:U(i,{alpha:.18}),borderInfo:`1px solid ${U(c,{alpha:.3})}`,textColorInfo:c,colorInfo:U(c,{alpha:.12}),colorBorderedInfo:U(c,{alpha:.1}),closeIconColorInfo:c,closeIconColorHoverInfo:c,closeIconColorPressedInfo:c,closeColorHoverInfo:U(c,{alpha:.12}),closeColorPressedInfo:U(c,{alpha:.18}),borderSuccess:`1px solid ${U(u,{alpha:.3})}`,textColorSuccess:u,colorSuccess:U(u,{alpha:.12}),colorBorderedSuccess:U(u,{alpha:.1}),closeIconColorSuccess:u,closeIconColorHoverSuccess:u,closeIconColorPressedSuccess:u,closeColorHoverSuccess:U(u,{alpha:.12}),closeColorPressedSuccess:U(u,{alpha:.18}),borderWarning:`1px solid ${U(l,{alpha:.35})}`,textColorWarning:l,colorWarning:U(l,{alpha:.15}),colorBorderedWarning:U(l,{alpha:.12}),closeIconColorWarning:l,closeIconColorHoverWarning:l,closeIconColorPressedWarning:l,closeColorHoverWarning:U(l,{alpha:.12}),closeColorPressedWarning:U(l,{alpha:.18}),borderError:`1px solid ${U(w,{alpha:.23})}`,textColorError:w,colorError:U(w,{alpha:.1}),colorBorderedError:U(w,{alpha:.08}),closeIconColorError:w,closeIconColorHoverError:w,closeIconColorPressedError:w,closeColorHoverError:U(w,{alpha:.12}),closeColorPressedError:U(w,{alpha:.18})})},Xt={name:"Tag",common:ct,self:Zt},Qt=Xt,Jt={color:Object,type:{type:String,default:"default"},round:Boolean,size:{type:String,default:"medium"},closable:Boolean,disabled:{type:Boolean,default:void 0}},en=E("tag",`
 white-space: nowrap;
 position: relative;
 box-sizing: border-box;
 cursor: default;
 display: inline-flex;
 align-items: center;
 flex-wrap: nowrap;
 padding: var(--n-padding);
 border-radius: var(--n-border-radius);
 color: var(--n-text-color);
 background-color: var(--n-color);
 transition: 
 border-color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier),
 opacity .3s var(--n-bezier);
 line-height: 1;
 height: var(--n-height);
 font-size: var(--n-font-size);
`,[Q("strong",`
 font-weight: var(--n-font-weight-strong);
 `),_("border",`
 pointer-events: none;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 border-radius: inherit;
 border: var(--n-border);
 transition: border-color .3s var(--n-bezier);
 `),_("icon",`
 display: flex;
 margin: 0 4px 0 0;
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 font-size: var(--n-avatar-size-override);
 `),_("avatar",`
 display: flex;
 margin: 0 6px 0 0;
 `),_("close",`
 margin: var(--n-close-margin);
 transition:
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 `),Q("round",`
 padding: 0 calc(var(--n-height) / 3);
 border-radius: calc(var(--n-height) / 2);
 `,[_("icon",`
 margin: 0 4px 0 calc((var(--n-height) - 8px) / -2);
 `),_("avatar",`
 margin: 0 6px 0 calc((var(--n-height) - 8px) / -2);
 `),Q("closable",`
 padding: 0 calc(var(--n-height) / 4) 0 calc(var(--n-height) / 3);
 `)]),Q("icon, avatar",[Q("round",`
 padding: 0 calc(var(--n-height) / 3) 0 calc(var(--n-height) / 2);
 `)]),Q("disabled",`
 cursor: not-allowed !important;
 opacity: var(--n-opacity-disabled);
 `),Q("checkable",`
 cursor: pointer;
 box-shadow: none;
 color: var(--n-text-color-checkable);
 background-color: var(--n-color-checkable);
 `,[Se("disabled",[re("&:hover","background-color: var(--n-color-hover-checkable);",[Se("checked","color: var(--n-text-color-hover-checkable);")]),re("&:active","background-color: var(--n-color-pressed-checkable);",[Se("checked","color: var(--n-text-color-pressed-checkable);")])]),Q("checked",`
 color: var(--n-text-color-checked);
 background-color: var(--n-color-checked);
 `,[Se("disabled",[re("&:hover","background-color: var(--n-color-checked-hover);"),re("&:active","background-color: var(--n-color-checked-pressed);")])])])]),on=Object.assign(Object.assign(Object.assign({},he.props),Jt),{bordered:{type:Boolean,default:void 0},checked:Boolean,checkable:Boolean,strong:Boolean,triggerClickOnClose:Boolean,onClose:[Array,Function],onMouseenter:Function,onMouseleave:Function,"onUpdate:checked":Function,onUpdateChecked:Function,internalCloseFocusable:{type:Boolean,default:!0},internalCloseIsButtonTag:{type:Boolean,default:!0},onCheckedChange:Function}),tn=ht("n-tag"),to=ce({name:"Tag",props:on,setup(e){const n=O(null),{mergedBorderedRef:t,mergedClsPrefixRef:r,inlineThemeDisabled:i,mergedRtlRef:c}=co(e),u=he("Tag","-tag",en,Qt,e,r);io(tn,{roundRef:ne(e,"round")});function l(C){if(!e.disabled&&e.checkable){const{checked:f,onCheckedChange:R,onUpdateChecked:F,"onUpdate:checked":S}=e;F&&F(!f),S&&S(!f),R&&R(!f)}}function w(C){if(e.triggerClickOnClose||C.stopPropagation(),!e.disabled){const{onClose:f}=e;f&&ue(f,C)}}const y={setTextContent(C){const{value:f}=n;f&&(f.textContent=C)}},m=ut("Tag",c,r),x=M(()=>{const{type:C,size:f,color:{color:R,textColor:F}={}}=e,{common:{cubicBezierEaseInOut:S},self:{padding:k,closeMargin:D,closeMarginRtl:Z,borderRadius:q,opacityDisabled:H,textColorCheckable:G,textColorHoverCheckable:J,textColorPressedCheckable:le,textColorChecked:te,colorCheckable:v,colorHoverCheckable:b,colorPressedCheckable:P,colorChecked:W,colorCheckedHover:j,colorCheckedPressed:V,closeBorderRadius:A,fontWeightStrong:K,[X("colorBordered",C)]:a,[X("closeSize",f)]:g,[X("closeIconSize",f)]:L,[X("fontSize",f)]:ee,[X("height",f)]:ae,[X("color",C)]:me,[X("textColor",C)]:Ce,[X("border",C)]:ge,[X("closeIconColor",C)]:ve,[X("closeIconColorHover",C)]:de,[X("closeIconColorPressed",C)]:ie,[X("closeColorHover",C)]:be,[X("closeColorPressed",C)]:fe}}=u.value;return{"--n-font-weight-strong":K,"--n-avatar-size-override":`calc(${ae} - 8px)`,"--n-bezier":S,"--n-border-radius":q,"--n-border":ge,"--n-close-icon-size":L,"--n-close-color-pressed":fe,"--n-close-color-hover":be,"--n-close-border-radius":A,"--n-close-icon-color":ve,"--n-close-icon-color-hover":de,"--n-close-icon-color-pressed":ie,"--n-close-icon-color-disabled":ve,"--n-close-margin":D,"--n-close-margin-rtl":Z,"--n-close-size":g,"--n-color":R||(t.value?a:me),"--n-color-checkable":v,"--n-color-checked":W,"--n-color-checked-hover":j,"--n-color-checked-pressed":V,"--n-color-hover-checkable":b,"--n-color-pressed-checkable":P,"--n-font-size":ee,"--n-height":ae,"--n-opacity-disabled":H,"--n-padding":k,"--n-text-color":F||Ce,"--n-text-color-checkable":G,"--n-text-color-checked":te,"--n-text-color-hover-checkable":J,"--n-text-color-pressed-checkable":le}}),z=i?Be("tag",M(()=>{let C="";const{type:f,size:R,color:{color:F,textColor:S}={}}=e;return C+=f[0],C+=R[0],F&&(C+=`a${go(F)}`),S&&(C+=`b${go(S)}`),t.value&&(C+="c"),C}),x,e):void 0;return Object.assign(Object.assign({},y),{rtlEnabled:m,mergedClsPrefix:r,contentRef:n,mergedBordered:t,handleClick:l,handleCloseClick:w,cssVars:i?void 0:x,themeClass:z==null?void 0:z.themeClass,onRender:z==null?void 0:z.onRender})},render(){var e,n;const{mergedClsPrefix:t,rtlEnabled:r,closable:i,color:{borderColor:c}={},round:u,onRender:l,$slots:w}=this;l==null||l();const y=ro(w.avatar,x=>x&&s("div",{class:`${t}-tag__avatar`},x)),m=ro(w.icon,x=>x&&s("div",{class:`${t}-tag__icon`},x));return s("div",{class:[`${t}-tag`,this.themeClass,{[`${t}-tag--rtl`]:r,[`${t}-tag--strong`]:this.strong,[`${t}-tag--disabled`]:this.disabled,[`${t}-tag--checkable`]:this.checkable,[`${t}-tag--checked`]:this.checkable&&this.checked,[`${t}-tag--round`]:u,[`${t}-tag--avatar`]:y,[`${t}-tag--icon`]:m,[`${t}-tag--closable`]:i}],style:this.cssVars,onClick:this.handleClick,onMouseenter:this.onMouseenter,onMouseleave:this.onMouseleave},m||y,s("span",{class:`${t}-tag__content`,ref:"contentRef"},(n=(e=this.$slots).default)===null||n===void 0?void 0:n.call(e)),!this.checkable&&i?s(ft,{clsPrefix:t,class:`${t}-tag__close`,disabled:this.disabled,onClick:this.handleCloseClick,focusable:this.internalCloseFocusable,round:u,isButtonTag:this.internalCloseIsButtonTag,absolute:!0}):null,!this.checkable&&this.mergedBordered?s("div",{class:`${t}-tag__border`,style:{borderColor:c}}):null)}}),nn=re([E("base-selection",`
 position: relative;
 z-index: auto;
 box-shadow: none;
 width: 100%;
 max-width: 100%;
 display: inline-block;
 vertical-align: bottom;
 border-radius: var(--n-border-radius);
 min-height: var(--n-height);
 line-height: 1.5;
 font-size: var(--n-font-size);
 `,[E("base-loading",`
 color: var(--n-loading-color);
 `),E("base-selection-tags","min-height: var(--n-height);"),_("border, state-border",`
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 pointer-events: none;
 border: var(--n-border);
 border-radius: inherit;
 transition:
 box-shadow .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `),_("state-border",`
 z-index: 1;
 border-color: #0000;
 `),E("base-suffix",`
 cursor: pointer;
 position: absolute;
 top: 50%;
 transform: translateY(-50%);
 right: 10px;
 `,[_("arrow",`
 font-size: var(--n-arrow-size);
 color: var(--n-arrow-color);
 transition: color .3s var(--n-bezier);
 `)]),E("base-selection-overlay",`
 display: flex;
 align-items: center;
 white-space: nowrap;
 pointer-events: none;
 position: absolute;
 top: 0;
 right: 0;
 bottom: 0;
 left: 0;
 padding: var(--n-padding-single);
 transition: color .3s var(--n-bezier);
 `,[_("wrapper",`
 flex-basis: 0;
 flex-grow: 1;
 overflow: hidden;
 text-overflow: ellipsis;
 `)]),E("base-selection-placeholder",`
 color: var(--n-placeholder-color);
 `,[_("inner",`
 max-width: 100%;
 overflow: hidden;
 `)]),E("base-selection-tags",`
 cursor: pointer;
 outline: none;
 box-sizing: border-box;
 position: relative;
 z-index: auto;
 display: flex;
 padding: var(--n-padding-multiple);
 flex-wrap: wrap;
 align-items: center;
 width: 100%;
 vertical-align: bottom;
 background-color: var(--n-color);
 border-radius: inherit;
 transition:
 color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `),E("base-selection-label",`
 height: var(--n-height);
 display: inline-flex;
 width: 100%;
 vertical-align: bottom;
 cursor: pointer;
 outline: none;
 z-index: auto;
 box-sizing: border-box;
 position: relative;
 transition:
 color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 border-radius: inherit;
 background-color: var(--n-color);
 align-items: center;
 `,[E("base-selection-input",`
 font-size: inherit;
 line-height: inherit;
 outline: none;
 cursor: pointer;
 box-sizing: border-box;
 border:none;
 width: 100%;
 padding: var(--n-padding-single);
 background-color: #0000;
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 caret-color: var(--n-caret-color);
 `,[_("content",`
 text-overflow: ellipsis;
 overflow: hidden;
 white-space: nowrap; 
 `)]),_("render-label",`
 color: var(--n-text-color);
 `)]),Se("disabled",[re("&:hover",[_("state-border",`
 box-shadow: var(--n-box-shadow-hover);
 border: var(--n-border-hover);
 `)]),Q("focus",[_("state-border",`
 box-shadow: var(--n-box-shadow-focus);
 border: var(--n-border-focus);
 `)]),Q("active",[_("state-border",`
 box-shadow: var(--n-box-shadow-active);
 border: var(--n-border-active);
 `),E("base-selection-label","background-color: var(--n-color-active);"),E("base-selection-tags","background-color: var(--n-color-active);")])]),Q("disabled","cursor: not-allowed;",[_("arrow",`
 color: var(--n-arrow-color-disabled);
 `),E("base-selection-label",`
 cursor: not-allowed;
 background-color: var(--n-color-disabled);
 `,[E("base-selection-input",`
 cursor: not-allowed;
 color: var(--n-text-color-disabled);
 `),_("render-label",`
 color: var(--n-text-color-disabled);
 `)]),E("base-selection-tags",`
 cursor: not-allowed;
 background-color: var(--n-color-disabled);
 `),E("base-selection-placeholder",`
 cursor: not-allowed;
 color: var(--n-placeholder-color-disabled);
 `)]),E("base-selection-input-tag",`
 height: calc(var(--n-height) - 6px);
 line-height: calc(var(--n-height) - 6px);
 outline: none;
 display: none;
 position: relative;
 margin-bottom: 3px;
 max-width: 100%;
 vertical-align: bottom;
 `,[_("input",`
 font-size: inherit;
 font-family: inherit;
 min-width: 1px;
 padding: 0;
 background-color: #0000;
 outline: none;
 border: none;
 max-width: 100%;
 overflow: hidden;
 width: 1em;
 line-height: inherit;
 cursor: pointer;
 color: var(--n-text-color);
 caret-color: var(--n-caret-color);
 `),_("mirror",`
 position: absolute;
 left: 0;
 top: 0;
 white-space: pre;
 visibility: hidden;
 user-select: none;
 -webkit-user-select: none;
 opacity: 0;
 `)]),["warning","error"].map(e=>Q(`${e}-status`,[_("state-border",`border: var(--n-border-${e});`),Se("disabled",[re("&:hover",[_("state-border",`
 box-shadow: var(--n-box-shadow-hover-${e});
 border: var(--n-border-hover-${e});
 `)]),Q("active",[_("state-border",`
 box-shadow: var(--n-box-shadow-active-${e});
 border: var(--n-border-active-${e});
 `),E("base-selection-label",`background-color: var(--n-color-active-${e});`),E("base-selection-tags",`background-color: var(--n-color-active-${e});`)]),Q("focus",[_("state-border",`
 box-shadow: var(--n-box-shadow-focus-${e});
 border: var(--n-border-focus-${e});
 `)])])]))]),E("base-selection-popover",`
 margin-bottom: -3px;
 display: flex;
 flex-wrap: wrap;
 margin-right: -8px;
 `),E("base-selection-tag-wrapper",`
 max-width: 100%;
 display: inline-flex;
 padding: 0 7px 3px 0;
 `,[re("&:last-child","padding-right: 0;"),E("tag",`
 font-size: 14px;
 max-width: 100%;
 `,[_("content",`
 line-height: 1.25;
 text-overflow: ellipsis;
 overflow: hidden;
 `)])])]),ln=ce({name:"InternalSelection",props:Object.assign(Object.assign({},he.props),{clsPrefix:{type:String,required:!0},bordered:{type:Boolean,default:void 0},active:Boolean,pattern:{type:String,default:""},placeholder:String,selectedOption:{type:Object,default:null},selectedOptions:{type:Array,default:null},labelField:{type:String,default:"label"},valueField:{type:String,default:"value"},multiple:Boolean,filterable:Boolean,clearable:Boolean,disabled:Boolean,size:{type:String,default:"medium"},loading:Boolean,autofocus:Boolean,showArrow:{type:Boolean,default:!0},inputProps:Object,focused:Boolean,renderTag:Function,onKeydown:Function,onClick:Function,onBlur:Function,onFocus:Function,onDeleteOption:Function,maxTagCount:[String,Number],onClear:Function,onPatternInput:Function,onPatternFocus:Function,onPatternBlur:Function,renderLabel:Function,status:String,inlineThemeDisabled:Boolean,ignoreComposition:{type:Boolean,default:!0},onResize:Function}),setup(e){const n=O(null),t=O(null),r=O(null),i=O(null),c=O(null),u=O(null),l=O(null),w=O(null),y=O(null),m=O(null),x=O(!1),z=O(!1),C=O(!1),f=he("InternalSelection","-internal-selection",nn,vt,e,ne(e,"clsPrefix")),R=M(()=>e.clearable&&!e.disabled&&(C.value||e.active)),F=M(()=>e.selectedOption?e.renderTag?e.renderTag({option:e.selectedOption,handleClose:()=>{}}):e.renderLabel?e.renderLabel(e.selectedOption,!0):ze(e.selectedOption[e.labelField],e.selectedOption,!0):e.placeholder),S=M(()=>{const d=e.selectedOption;if(d)return d[e.labelField]}),k=M(()=>e.multiple?!!(Array.isArray(e.selectedOptions)&&e.selectedOptions.length):e.selectedOption!==null);function D(){var d;const{value:p}=n;if(p){const{value:Y}=t;Y&&(Y.style.width=`${p.offsetWidth}px`,e.maxTagCount!=="responsive"&&((d=y.value)===null||d===void 0||d.sync()))}}function Z(){const{value:d}=m;d&&(d.style.display="none")}function q(){const{value:d}=m;d&&(d.style.display="inline-block")}Te(ne(e,"active"),d=>{d||Z()}),Te(ne(e,"pattern"),()=>{e.multiple&&so(D)});function H(d){const{onFocus:p}=e;p&&p(d)}function G(d){const{onBlur:p}=e;p&&p(d)}function J(d){const{onDeleteOption:p}=e;p&&p(d)}function le(d){const{onClear:p}=e;p&&p(d)}function te(d){const{onPatternInput:p}=e;p&&p(d)}function v(d){var p;(!d.relatedTarget||!(!((p=r.value)===null||p===void 0)&&p.contains(d.relatedTarget)))&&H(d)}function b(d){var p;!((p=r.value)===null||p===void 0)&&p.contains(d.relatedTarget)||G(d)}function P(d){le(d)}function W(){C.value=!0}function j(){C.value=!1}function V(d){!e.active||!e.filterable||d.target!==t.value&&d.preventDefault()}function A(d){J(d)}function K(d){if(d.key==="Backspace"&&!a.value&&!e.pattern.length){const{selectedOptions:p}=e;p!=null&&p.length&&A(p[p.length-1])}}const a=O(!1);let g=null;function L(d){const{value:p}=n;if(p){const Y=d.target.value;p.textContent=Y,D()}e.ignoreComposition&&a.value?g=d:te(d)}function ee(){a.value=!0}function ae(){a.value=!1,e.ignoreComposition&&te(g),g=null}function me(d){var p;z.value=!0,(p=e.onPatternFocus)===null||p===void 0||p.call(e,d)}function Ce(d){var p;z.value=!1,(p=e.onPatternBlur)===null||p===void 0||p.call(e,d)}function ge(){var d,p;if(e.filterable)z.value=!1,(d=u.value)===null||d===void 0||d.blur(),(p=t.value)===null||p===void 0||p.blur();else if(e.multiple){const{value:Y}=i;Y==null||Y.blur()}else{const{value:Y}=c;Y==null||Y.blur()}}function ve(){var d,p,Y;e.filterable?(z.value=!1,(d=u.value)===null||d===void 0||d.focus()):e.multiple?(p=i.value)===null||p===void 0||p.focus():(Y=c.value)===null||Y===void 0||Y.focus()}function de(){const{value:d}=t;d&&(q(),d.focus())}function ie(){const{value:d}=t;d&&d.blur()}function be(d){const{value:p}=l;p&&p.setTextContent(`+${d}`)}function fe(){const{value:d}=w;return d}function Fe(){return t.value}let ye=null;function we(){ye!==null&&window.clearTimeout(ye)}function Oe(){e.disabled||e.active||(we(),ye=window.setTimeout(()=>{k.value&&(x.value=!0)},100))}function Pe(){we()}function Me(d){d||(we(),x.value=!1)}Te(k,d=>{d||(x.value=!1)}),Ie(()=>{gt(()=>{const d=u.value;d&&(d.tabIndex=e.disabled||z.value?-1:0)})}),Po(r,e.onResize);const{inlineThemeDisabled:ke}=e,Re=M(()=>{const{size:d}=e,{common:{cubicBezierEaseInOut:p},self:{borderRadius:Y,color:_e,placeholderColor:Ue,textColor:qe,paddingSingle:Ge,paddingMultiple:Ye,caretColor:$e,colorDisabled:Ee,textColorDisabled:Ae,placeholderColorDisabled:Ze,colorActive:Xe,boxShadowFocus:Ne,boxShadowActive:pe,boxShadowHover:o,border:h,borderFocus:T,borderHover:N,borderActive:I,arrowColor:$,arrowColorDisabled:B,loadingColor:oe,colorActiveWarning:Le,boxShadowFocusWarning:Qe,boxShadowActiveWarning:Io,boxShadowHoverWarning:Bo,borderWarning:_o,borderFocusWarning:$o,borderHoverWarning:Eo,borderActiveWarning:Ao,colorActiveError:No,boxShadowFocusError:Lo,boxShadowActiveError:Ho,boxShadowHoverError:Vo,borderError:Do,borderFocusError:Wo,borderHoverError:jo,borderActiveError:Ko,clearColor:Uo,clearColorHover:qo,clearColorPressed:Go,clearSize:Yo,arrowSize:Zo,[X("height",d)]:Xo,[X("fontSize",d)]:Qo}}=f.value;return{"--n-bezier":p,"--n-border":h,"--n-border-active":I,"--n-border-focus":T,"--n-border-hover":N,"--n-border-radius":Y,"--n-box-shadow-active":pe,"--n-box-shadow-focus":Ne,"--n-box-shadow-hover":o,"--n-caret-color":$e,"--n-color":_e,"--n-color-active":Xe,"--n-color-disabled":Ee,"--n-font-size":Qo,"--n-height":Xo,"--n-padding-single":Ge,"--n-padding-multiple":Ye,"--n-placeholder-color":Ue,"--n-placeholder-color-disabled":Ze,"--n-text-color":qe,"--n-text-color-disabled":Ae,"--n-arrow-color":$,"--n-arrow-color-disabled":B,"--n-loading-color":oe,"--n-color-active-warning":Le,"--n-box-shadow-focus-warning":Qe,"--n-box-shadow-active-warning":Io,"--n-box-shadow-hover-warning":Bo,"--n-border-warning":_o,"--n-border-focus-warning":$o,"--n-border-hover-warning":Eo,"--n-border-active-warning":Ao,"--n-color-active-error":No,"--n-box-shadow-focus-error":Lo,"--n-box-shadow-active-error":Ho,"--n-box-shadow-hover-error":Vo,"--n-border-error":Do,"--n-border-focus-error":Wo,"--n-border-hover-error":jo,"--n-border-active-error":Ko,"--n-clear-size":Yo,"--n-clear-color":Uo,"--n-clear-color-hover":qo,"--n-clear-color-pressed":Go,"--n-arrow-size":Zo}}),se=ke?Be("internal-selection",M(()=>e.size[0]),Re,e):void 0;return{mergedTheme:f,mergedClearable:R,patternInputFocused:z,filterablePlaceholder:F,label:S,selected:k,showTagsPanel:x,isComposing:a,counterRef:l,counterWrapperRef:w,patternInputMirrorRef:n,patternInputRef:t,selfRef:r,multipleElRef:i,singleElRef:c,patternInputWrapperRef:u,overflowRef:y,inputTagElRef:m,handleMouseDown:V,handleFocusin:v,handleClear:P,handleMouseEnter:W,handleMouseLeave:j,handleDeleteOption:A,handlePatternKeyDown:K,handlePatternInputInput:L,handlePatternInputBlur:Ce,handlePatternInputFocus:me,handleMouseEnterCounter:Oe,handleMouseLeaveCounter:Pe,handleFocusout:b,handleCompositionEnd:ae,handleCompositionStart:ee,onPopoverUpdateShow:Me,focus:ve,focusInput:de,blur:ge,blurInput:ie,updateCounter:be,getCounter:fe,getTail:Fe,renderLabel:e.renderLabel,cssVars:ke?void 0:Re,themeClass:se==null?void 0:se.themeClass,onRender:se==null?void 0:se.onRender}},render(){const{status:e,multiple:n,size:t,disabled:r,filterable:i,maxTagCount:c,bordered:u,clsPrefix:l,onRender:w,renderTag:y,renderLabel:m}=this;w==null||w();const x=c==="responsive",z=typeof c=="number",C=x||z,f=s(bt,null,{default:()=>s(Tt,{clsPrefix:l,loading:this.loading,showArrow:this.showArrow,showClear:this.mergedClearable&&this.selected,onClear:this.handleClear},{default:()=>{var F,S;return(S=(F=this.$slots).arrow)===null||S===void 0?void 0:S.call(F)}})});let R;if(n){const{labelField:F}=this,S=b=>s("div",{class:`${l}-base-selection-tag-wrapper`,key:b.value},y?y({option:b,handleClose:()=>{this.handleDeleteOption(b)}}):s(to,{size:t,closable:!b.disabled,disabled:r,onClose:()=>{this.handleDeleteOption(b)},internalCloseIsButtonTag:!1,internalCloseFocusable:!1},{default:()=>m?m(b,!0):ze(b[F],b,!0)})),k=()=>(z?this.selectedOptions.slice(0,c):this.selectedOptions).map(S),D=i?s("div",{class:`${l}-base-selection-input-tag`,ref:"inputTagElRef",key:"__input-tag__"},s("input",Object.assign({},this.inputProps,{ref:"patternInputRef",tabindex:-1,disabled:r,value:this.pattern,autofocus:this.autofocus,class:`${l}-base-selection-input-tag__input`,onBlur:this.handlePatternInputBlur,onFocus:this.handlePatternInputFocus,onKeydown:this.handlePatternKeyDown,onInput:this.handlePatternInputInput,onCompositionstart:this.handleCompositionStart,onCompositionend:this.handleCompositionEnd})),s("span",{ref:"patternInputMirrorRef",class:`${l}-base-selection-input-tag__mirror`},this.pattern)):null,Z=x?()=>s("div",{class:`${l}-base-selection-tag-wrapper`,ref:"counterWrapperRef"},s(to,{size:t,ref:"counterRef",onMouseenter:this.handleMouseEnterCounter,onMouseleave:this.handleMouseLeaveCounter,disabled:r})):void 0;let q;if(z){const b=this.selectedOptions.length-c;b>0&&(q=s("div",{class:`${l}-base-selection-tag-wrapper`,key:"__counter__"},s(to,{size:t,ref:"counterRef",onMouseenter:this.handleMouseEnterCounter,disabled:r},{default:()=>`+${b}`})))}const H=x?i?s(yo,{ref:"overflowRef",updateCounter:this.updateCounter,getCounter:this.getCounter,getTail:this.getTail,style:{width:"100%",display:"flex",overflow:"hidden"}},{default:k,counter:Z,tail:()=>D}):s(yo,{ref:"overflowRef",updateCounter:this.updateCounter,getCounter:this.getCounter,style:{width:"100%",display:"flex",overflow:"hidden"}},{default:k,counter:Z}):z?k().concat(q):k(),G=C?()=>s("div",{class:`${l}-base-selection-popover`},x?k():this.selectedOptions.map(S)):void 0,J=C?{show:this.showTagsPanel,trigger:"hover",overlap:!0,placement:"top",width:"trigger",onUpdateShow:this.onPopoverUpdateShow,theme:this.mergedTheme.peers.Popover,themeOverrides:this.mergedTheme.peerOverrides.Popover}:null,te=(this.selected?!1:this.active?!this.pattern&&!this.isComposing:!0)?s("div",{class:`${l}-base-selection-placeholder ${l}-base-selection-overlay`},s("div",{class:`${l}-base-selection-placeholder__inner`},this.placeholder)):null,v=i?s("div",{ref:"patternInputWrapperRef",class:`${l}-base-selection-tags`},H,x?null:D,f):s("div",{ref:"multipleElRef",class:`${l}-base-selection-tags`,tabindex:r?void 0:0},H,f);R=s(pt,null,C?s(Ft,Object.assign({},J,{scrollable:!0,style:"max-height: calc(var(--v-target-height) * 6.6);"}),{trigger:()=>v,default:G}):v,te)}else if(i){const F=this.pattern||this.isComposing,S=this.active?!F:!this.selected,k=this.active?!1:this.selected;R=s("div",{ref:"patternInputWrapperRef",class:`${l}-base-selection-label`},s("input",Object.assign({},this.inputProps,{ref:"patternInputRef",class:`${l}-base-selection-input`,value:this.active?this.pattern:"",placeholder:"",readonly:r,disabled:r,tabindex:-1,autofocus:this.autofocus,onFocus:this.handlePatternInputFocus,onBlur:this.handlePatternInputBlur,onInput:this.handlePatternInputInput,onCompositionstart:this.handleCompositionStart,onCompositionend:this.handleCompositionEnd})),k?s("div",{class:`${l}-base-selection-label__render-label ${l}-base-selection-overlay`,key:"input"},s("div",{class:`${l}-base-selection-overlay__wrapper`},y?y({option:this.selectedOption,handleClose:()=>{}}):m?m(this.selectedOption,!0):ze(this.label,this.selectedOption,!0))):null,S?s("div",{class:`${l}-base-selection-placeholder ${l}-base-selection-overlay`,key:"placeholder"},s("div",{class:`${l}-base-selection-overlay__wrapper`},this.filterablePlaceholder)):null,f)}else R=s("div",{ref:"singleElRef",class:`${l}-base-selection-label`,tabindex:this.disabled?void 0:0},this.label!==void 0?s("div",{class:`${l}-base-selection-input`,title:$t(this.label),key:"input"},s("div",{class:`${l}-base-selection-input__content`},y?y({option:this.selectedOption,handleClose:()=>{}}):m?m(this.selectedOption,!0):ze(this.label,this.selectedOption,!0))):s("div",{class:`${l}-base-selection-placeholder ${l}-base-selection-overlay`,key:"placeholder"},s("div",{class:`${l}-base-selection-placeholder__inner`},this.placeholder)),f);return s("div",{ref:"selfRef",class:[`${l}-base-selection`,this.themeClass,e&&`${l}-base-selection--${e}-status`,{[`${l}-base-selection--active`]:this.active,[`${l}-base-selection--selected`]:this.selected||this.active&&this.pattern,[`${l}-base-selection--disabled`]:this.disabled,[`${l}-base-selection--multiple`]:this.multiple,[`${l}-base-selection--focus`]:this.focused}],style:this.cssVars,onClick:this.onClick,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onKeydown:this.onKeydown,onFocusin:this.handleFocusin,onFocusout:this.handleFocusout,onMousedown:this.handleMouseDown},R,u?s("div",{class:`${l}-base-selection__border`}):null,u?s("div",{class:`${l}-base-selection__state-border`}):null)}});function Ke(e){return e.type==="group"}function Mo(e){return e.type==="ignored"}function no(e,n){try{return!!(1+n.toString().toLowerCase().indexOf(e.trim().toLowerCase()))}catch{return!1}}function rn(e,n){return{getIsGroup:Ke,getIgnored:Mo,getKey(r){return Ke(r)?r.name||r.key||"key-required":r[e]},getChildren(r){return r[n]}}}function an(e,n,t,r){if(!n)return e;function i(c){if(!Array.isArray(c))return[];const u=[];for(const l of c)if(Ke(l)){const w=i(l[r]);w.length&&u.push(Object.assign({},l,{[r]:w}))}else{if(Mo(l))continue;n(t,l)&&u.push(l)}return u}return i(e)}function sn(e,n,t){const r=new Map;return e.forEach(i=>{Ke(i)?i[t].forEach(c=>{r.set(c[n],c)}):r.set(i[n],i)}),r}const cn=re([E("select",`
 z-index: auto;
 outline: none;
 width: 100%;
 position: relative;
 `),E("select-menu",`
 margin: 4px 0;
 box-shadow: var(--n-menu-box-shadow);
 `,[To({originalTransition:"background-color .3s var(--n-bezier), box-shadow .3s var(--n-bezier)"})])]),dn=Object.assign(Object.assign({},he.props),{to:ao.propTo,bordered:{type:Boolean,default:void 0},clearable:Boolean,clearFilterAfterSelect:{type:Boolean,default:!0},options:{type:Array,default:()=>[]},defaultValue:{type:[String,Number,Array],default:null},keyboard:{type:Boolean,default:!0},value:[String,Number,Array],placeholder:String,menuProps:Object,multiple:Boolean,size:String,filterable:Boolean,disabled:{type:Boolean,default:void 0},remote:Boolean,loading:Boolean,filter:Function,placement:{type:String,default:"bottom-start"},widthMode:{type:String,default:"trigger"},tag:Boolean,onCreate:Function,fallbackOption:{type:[Function,Boolean],default:void 0},show:{type:Boolean,default:void 0},showArrow:{type:Boolean,default:!0},maxTagCount:[Number,String],consistentMenuWidth:{type:Boolean,default:!0},virtualScroll:{type:Boolean,default:!0},labelField:{type:String,default:"label"},valueField:{type:String,default:"value"},childrenField:{type:String,default:"children"},renderLabel:Function,renderOption:Function,renderTag:Function,"onUpdate:value":[Function,Array],inputProps:Object,nodeProps:Function,ignoreComposition:{type:Boolean,default:!0},showOnFocus:Boolean,onUpdateValue:[Function,Array],onBlur:[Function,Array],onClear:[Function,Array],onFocus:[Function,Array],onScroll:[Function,Array],onSearch:[Function,Array],onUpdateShow:[Function,Array],"onUpdate:show":[Function,Array],displayDirective:{type:String,default:"show"},resetMenuOnOptionsChange:{type:Boolean,default:!0},status:String,showCheckmark:{type:Boolean,default:!0},onChange:[Function,Array],items:Array}),vn=ce({name:"Select",props:dn,setup(e){const{mergedClsPrefixRef:n,mergedBorderedRef:t,namespaceRef:r,inlineThemeDisabled:i}=co(e),c=he("Select","-select",cn,xt,e,n),u=O(e.defaultValue),l=ne(e,"value"),w=po(l,u),y=O(!1),m=O(""),x=M(()=>{const{valueField:o,childrenField:h}=e,T=rn(o,h);return Bt(b.value,T)}),z=M(()=>sn(te.value,e.valueField,e.childrenField)),C=O(!1),f=po(ne(e,"show"),C),R=O(null),F=O(null),S=O(null),{localeRef:k}=Oo("Select"),D=M(()=>{var o;return(o=e.placeholder)!==null&&o!==void 0?o:k.value.placeholder}),Z=Ot(e,["items","options"]),q=[],H=O([]),G=O([]),J=O(new Map),le=M(()=>{const{fallbackOption:o}=e;if(o===void 0){const{labelField:h,valueField:T}=e;return N=>({[h]:String(N),[T]:N})}return o===!1?!1:h=>Object.assign(o(h),{value:h})}),te=M(()=>G.value.concat(H.value).concat(Z.value)),v=M(()=>{const{filter:o}=e;if(o)return o;const{labelField:h,valueField:T}=e;return(N,I)=>{if(!I)return!1;const $=I[h];if(typeof $=="string")return no(N,$);const B=I[T];return typeof B=="string"?no(N,B):typeof B=="number"?no(N,String(B)):!1}}),b=M(()=>{if(e.remote)return Z.value;{const{value:o}=te,{value:h}=m;return!h.length||!e.filterable?o:an(o,v.value,h,e.childrenField)}});function P(o){const h=e.remote,{value:T}=J,{value:N}=z,{value:I}=le,$=[];return o.forEach(B=>{if(N.has(B))$.push(N.get(B));else if(h&&T.has(B))$.push(T.get(B));else if(I){const oe=I(B);oe&&$.push(oe)}}),$}const W=M(()=>{if(e.multiple){const{value:o}=w;return Array.isArray(o)?P(o):[]}return null}),j=M(()=>{const{value:o}=w;return!e.multiple&&!Array.isArray(o)?o===null?null:P([o])[0]||null:null}),V=mt(e),{mergedSizeRef:A,mergedDisabledRef:K,mergedStatusRef:a}=V;function g(o,h){const{onChange:T,"onUpdate:value":N,onUpdateValue:I}=e,{nTriggerFormChange:$,nTriggerFormInput:B}=V;T&&ue(T,o,h),I&&ue(I,o,h),N&&ue(N,o,h),u.value=o,$(),B()}function L(o){const{onBlur:h}=e,{nTriggerFormBlur:T}=V;h&&ue(h,o),T()}function ee(){const{onClear:o}=e;o&&ue(o)}function ae(o){const{onFocus:h,showOnFocus:T}=e,{nTriggerFormFocus:N}=V;h&&ue(h,o),N(),T&&de()}function me(o){const{onSearch:h}=e;h&&ue(h,o)}function Ce(o){const{onScroll:h}=e;h&&ue(h,o)}function ge(){var o;const{remote:h,multiple:T}=e;if(h){const{value:N}=J;if(T){const{valueField:I}=e;(o=W.value)===null||o===void 0||o.forEach($=>{N.set($[I],$)})}else{const I=j.value;I&&N.set(I[e.valueField],I)}}}function ve(o){const{onUpdateShow:h,"onUpdate:show":T}=e;h&&ue(h,o),T&&ue(T,o),C.value=o}function de(){K.value||(ve(!0),C.value=!0,e.filterable&&Ae())}function ie(){ve(!1)}function be(){m.value="",G.value=q}const fe=O(!1);function Fe(){e.filterable&&(fe.value=!0)}function ye(){e.filterable&&(fe.value=!1,f.value||be())}function we(){K.value||(f.value?e.filterable?Ae():ie():de())}function Oe(o){var h,T;!((T=(h=S.value)===null||h===void 0?void 0:h.selfRef)===null||T===void 0)&&T.contains(o.relatedTarget)||(y.value=!1,L(o),ie())}function Pe(o){ae(o),y.value=!0}function Me(o){y.value=!0}function ke(o){var h;!((h=R.value)===null||h===void 0)&&h.$el.contains(o.relatedTarget)||(y.value=!1,L(o),ie())}function Re(){var o;(o=R.value)===null||o===void 0||o.focus(),ie()}function se(o){var h;f.value&&(!((h=R.value)===null||h===void 0)&&h.$el.contains(St(o))||ie())}function d(o){if(!Array.isArray(o))return[];if(le.value)return Array.from(o);{const{remote:h}=e,{value:T}=z;if(h){const{value:N}=J;return o.filter(I=>T.has(I)||N.has(I))}else return o.filter(N=>T.has(N))}}function p(o){Y(o.rawNode)}function Y(o){if(K.value)return;const{tag:h,remote:T,clearFilterAfterSelect:N,valueField:I}=e;if(h&&!T){const{value:$}=G,B=$[0]||null;if(B){const oe=H.value;oe.length?oe.push(B):H.value=[B],G.value=q}}if(T&&J.value.set(o[I],o),e.multiple){const $=d(w.value),B=$.findIndex(oe=>oe===o[I]);if(~B){if($.splice(B,1),h&&!T){const oe=_e(o[I]);~oe&&(H.value.splice(oe,1),N&&(m.value=""))}}else $.push(o[I]),N&&(m.value="");g($,P($))}else{if(h&&!T){const $=_e(o[I]);~$?H.value=[H.value[$]]:H.value=q}Ee(),ie(),g(o[I],o)}}function _e(o){return H.value.findIndex(T=>T[e.valueField]===o)}function Ue(o){f.value||de();const{value:h}=o.target;m.value=h;const{tag:T,remote:N}=e;if(me(h),T&&!N){if(!h){G.value=q;return}const{onCreate:I}=e,$=I?I(h):{[e.labelField]:h,[e.valueField]:h},{valueField:B}=e;Z.value.some(oe=>oe[B]===$[B])||H.value.some(oe=>oe[B]===$[B])?G.value=q:G.value=[$]}}function qe(o){o.stopPropagation();const{multiple:h}=e;!h&&e.filterable&&ie(),ee(),h?g([],[]):g(null,null)}function Ge(o){!je(o,"action")&&!je(o,"empty")&&o.preventDefault()}function Ye(o){Ce(o)}function $e(o){var h,T,N,I,$;if(!e.keyboard){o.preventDefault();return}switch(o.key){case" ":if(e.filterable)break;o.preventDefault();case"Enter":if(!(!((h=R.value)===null||h===void 0)&&h.isComposing)){if(f.value){const B=(T=S.value)===null||T===void 0?void 0:T.getPendingTmNode();B?p(B):e.filterable||(ie(),Ee())}else if(de(),e.tag&&fe.value){const B=G.value[0];if(B){const oe=B[e.valueField],{value:Le}=w;e.multiple&&Array.isArray(Le)&&Le.some(Qe=>Qe===oe)||Y(B)}}}o.preventDefault();break;case"ArrowUp":if(o.preventDefault(),e.loading)return;f.value&&((N=S.value)===null||N===void 0||N.prev());break;case"ArrowDown":if(o.preventDefault(),e.loading)return;f.value?(I=S.value)===null||I===void 0||I.next():de();break;case"Escape":f.value&&(kt(o),ie()),($=R.value)===null||$===void 0||$.focus();break}}function Ee(){var o;(o=R.value)===null||o===void 0||o.focus()}function Ae(){var o;(o=R.value)===null||o===void 0||o.focusInput()}function Ze(){var o;f.value&&((o=F.value)===null||o===void 0||o.syncPosition())}ge(),Te(ne(e,"options"),ge);const Xe={focus:()=>{var o;(o=R.value)===null||o===void 0||o.focus()},blur:()=>{var o;(o=R.value)===null||o===void 0||o.blur()}},Ne=M(()=>{const{self:{menuBoxShadow:o}}=c.value;return{"--n-menu-box-shadow":o}}),pe=i?Be("select",void 0,Ne,e):void 0;return Object.assign(Object.assign({},Xe),{mergedStatus:a,mergedClsPrefix:n,mergedBordered:t,namespace:r,treeMate:x,isMounted:Ct(),triggerRef:R,menuRef:S,pattern:m,uncontrolledShow:C,mergedShow:f,adjustedTo:ao(e),uncontrolledValue:u,mergedValue:w,followerRef:F,localizedPlaceholder:D,selectedOption:j,selectedOptions:W,mergedSize:A,mergedDisabled:K,focused:y,activeWithoutMenuOpen:fe,inlineThemeDisabled:i,onTriggerInputFocus:Fe,onTriggerInputBlur:ye,handleTriggerOrMenuResize:Ze,handleMenuFocus:Me,handleMenuBlur:ke,handleMenuTabOut:Re,handleTriggerClick:we,handleToggle:p,handleDeleteOption:Y,handlePatternInput:Ue,handleClear:qe,handleTriggerBlur:Oe,handleTriggerFocus:Pe,handleKeydown:$e,handleMenuAfterLeave:be,handleMenuClickOutside:se,handleMenuScroll:Ye,handleMenuKeydown:$e,handleMenuMousedown:Ge,mergedTheme:c,cssVars:i?void 0:Ne,themeClass:pe==null?void 0:pe.themeClass,onRender:pe==null?void 0:pe.onRender})},render(){return s("div",{class:`${this.mergedClsPrefix}-select`},s(Pt,null,{default:()=>[s(Mt,null,{default:()=>s(ln,{ref:"triggerRef",inlineThemeDisabled:this.inlineThemeDisabled,status:this.mergedStatus,inputProps:this.inputProps,clsPrefix:this.mergedClsPrefix,showArrow:this.showArrow,maxTagCount:this.maxTagCount,bordered:this.mergedBordered,active:this.activeWithoutMenuOpen||this.mergedShow,pattern:this.pattern,placeholder:this.localizedPlaceholder,selectedOption:this.selectedOption,selectedOptions:this.selectedOptions,multiple:this.multiple,renderTag:this.renderTag,renderLabel:this.renderLabel,filterable:this.filterable,clearable:this.clearable,disabled:this.mergedDisabled,size:this.mergedSize,theme:this.mergedTheme.peers.InternalSelection,labelField:this.labelField,valueField:this.valueField,themeOverrides:this.mergedTheme.peerOverrides.InternalSelection,loading:this.loading,focused:this.focused,onClick:this.handleTriggerClick,onDeleteOption:this.handleDeleteOption,onPatternInput:this.handlePatternInput,onClear:this.handleClear,onBlur:this.handleTriggerBlur,onFocus:this.handleTriggerFocus,onKeydown:this.handleKeydown,onPatternBlur:this.onTriggerInputBlur,onPatternFocus:this.onTriggerInputFocus,onResize:this.handleTriggerOrMenuResize,ignoreComposition:this.ignoreComposition},{arrow:()=>{var e,n;return[(n=(e=this.$slots).arrow)===null||n===void 0?void 0:n.call(e)]}})}),s(It,{ref:"followerRef",show:this.mergedShow,to:this.adjustedTo,teleportDisabled:this.adjustedTo===ao.tdkey,containerClass:this.namespace,width:this.consistentMenuWidth?"target":void 0,minWidth:"target",placement:this.placement},{default:()=>s(zo,{name:"fade-in-scale-up-transition",appear:this.isMounted,onAfterLeave:this.handleMenuAfterLeave},{default:()=>{var e,n,t;return this.mergedShow||this.displayDirective==="show"?((e=this.onRender)===null||e===void 0||e.call(this),yt(s(Yt,Object.assign({},this.menuProps,{ref:"menuRef",onResize:this.handleTriggerOrMenuResize,inlineThemeDisabled:this.inlineThemeDisabled,virtualScroll:this.consistentMenuWidth&&this.virtualScroll,class:[`${this.mergedClsPrefix}-select-menu`,this.themeClass,(n=this.menuProps)===null||n===void 0?void 0:n.class],clsPrefix:this.mergedClsPrefix,focusable:!0,labelField:this.labelField,valueField:this.valueField,autoPending:!0,nodeProps:this.nodeProps,theme:this.mergedTheme.peers.InternalSelectMenu,themeOverrides:this.mergedTheme.peerOverrides.InternalSelectMenu,treeMate:this.treeMate,multiple:this.multiple,size:"medium",renderOption:this.renderOption,renderLabel:this.renderLabel,value:this.mergedValue,style:[(t=this.menuProps)===null||t===void 0?void 0:t.style,this.cssVars],onToggle:this.handleToggle,onScroll:this.handleMenuScroll,onFocus:this.handleMenuFocus,onBlur:this.handleMenuBlur,onKeydown:this.handleMenuKeydown,onTabOut:this.handleMenuTabOut,onMousedown:this.handleMenuMousedown,show:this.mergedShow,showCheckmark:this.showCheckmark,resetMenuOnOptionsChange:this.resetMenuOnOptionsChange}),{empty:()=>{var r,i;return[(i=(r=this.$slots).empty)===null||i===void 0?void 0:i.call(r)]},action:()=>{var r,i;return[(i=(r=this.$slots).action)===null||i===void 0?void 0:i.call(r)]}}),this.displayDirective==="show"?[[wt,this.mergedShow],[bo,this.handleMenuClickOutside,void 0,{capture:!0}]]:[[bo,this.handleMenuClickOutside,void 0,{capture:!0}]])):null}})})]}))}});export{Wt as F,Ut as N,Lt as V,vn as _,to as a,Yt as b,rn as c,Jt as d,eo as m,Qt as t};
