import{o as Le,m as xe,dk as or,i as ne,j as K,G as et,H as Q,c4 as bt,c2 as mt,c3 as wt,r as P,E as ae,F as re,a0 as ce,d as Z,S as pe,dl as yt,d$ as ir,W as Oe,e0 as it,e1 as ar,dd as lr,t as ee,p as xt,h,cb as Ct,L as sr,v as Re,e2 as dr,bS as ur,b9 as cr,b6 as fr,aZ as hr,co as pr,e3 as vr,e4 as gr,cp as br,g as O,b as E,a4 as ie,P as M,e as G,e5 as mr,u as $t,k as Ce,w as Te,n as St,T as wr,e6 as yr,X as at,c5 as xr,V as lt,dQ as st,c6 as Cr,q as $r,cC as ue,$ as At,bk as Sr,cA as dt,dj as Ar,e7 as Mr,a2 as zr,Y as V,bh as kr,cf as Mt,bc as Br,s as we,N as Ie,bd as _r,cH as Er,e8 as Pr,Q as Tr,bi as Ir,c7 as Fr,dg as Lr,cD as Or,U as Ke,bj as Nr}from"./index-10b862f4.js";import{b as Dr}from"./Space-73163714.js";function ni(e,n){let{target:t}=e;for(;t;){if(t.dataset&&t.dataset[n]!==void 0)return!0;t=t.parentElement}return!1}const Wr=/^(\d|\.)+$/,ut=/(\d|\.)+/;function je(e,{c:n=1,offset:t=0,attachPx:o=!0}={}){if(typeof e=="number"){const l=(e+t)*n;return l===0?"0":`${l}px`}else if(typeof e=="string")if(Wr.test(e)){const l=(Number(e)+t)*n;return o?l===0?"0":`${l}px`:`${l}`}else{const l=ut.exec(e);return l?e.replace(ut,String((Number(l[0])+t)*n)):e}return e}let He;function Vr(){return He===void 0&&(He=navigator.userAgent.includes("Node.js")||navigator.userAgent.includes("jsdom")),He}let ve,ye;const Kr=()=>{var e,n;ve=or?(n=(e=document)===null||e===void 0?void 0:e.fonts)===null||n===void 0?void 0:n.ready:void 0,ye=!1,ve!==void 0?ve.then(()=>{ye=!0}):ye=!0};Kr();function jr(e){if(ye)return;let n=!1;Le(()=>{ye||ve==null||ve.then(()=>{n||e()})}),xe(()=>{n=!0})}function zt(e,n){return ne(e,t=>{t!==void 0&&(n.value=t)}),K(()=>e.value===void 0?n.value:e.value)}function Hr(e,n){return K(()=>{for(const t of n)if(e[t]!==void 0)return e[t];return e[n[n.length-1]]})}const ri=et("n-internal-select-menu"),Gr=et("n-internal-select-menu-body"),kt="__disabled__";function ge(e){const n=Q(bt,null),t=Q(mt,null),o=Q(wt,null),l=Q(Gr,null),u=P();if(typeof document<"u"){u.value=document.fullscreenElement;const i=()=>{u.value=document.fullscreenElement};Le(()=>{ae("fullscreenchange",document,i)}),xe(()=>{re("fullscreenchange",document,i)})}return ce(()=>{var i;const{to:s}=e;return s!==void 0?s===!1?kt:s===!0?u.value||"body":s:n!=null&&n.value?(i=n.value.$el)!==null&&i!==void 0?i:n.value:t!=null&&t.value?t.value:o!=null&&o.value?o.value:l!=null&&l.value?l.value:s??(u.value||"body")})}ge.tdkey=kt;ge.propTo={type:[String,Object,Boolean],default:void 0};let oe=null;function Bt(){if(oe===null&&(oe=document.getElementById("v-binder-view-measurer"),oe===null)){oe=document.createElement("div"),oe.id="v-binder-view-measurer";const{style:e}=oe;e.position="fixed",e.left="0",e.right="0",e.top="0",e.bottom="0",e.pointerEvents="none",e.visibility="hidden",document.body.appendChild(oe)}return oe.getBoundingClientRect()}function Ur(e,n){const t=Bt();return{top:n,left:e,height:0,width:0,right:t.width-e,bottom:t.height-n}}function Ge(e){const n=e.getBoundingClientRect(),t=Bt();return{left:n.left-t.left,top:n.top-t.top,bottom:t.height+t.top-n.bottom,right:t.width+t.left-n.right,width:n.width,height:n.height}}function Xr(e){return e.nodeType===9?null:e.parentNode}function _t(e){if(e===null)return null;const n=Xr(e);if(n===null)return null;if(n.nodeType===9)return document;if(n.nodeType===1){const{overflow:t,overflowX:o,overflowY:l}=getComputedStyle(n);if(/(auto|scroll|overlay)/.test(t+l+o))return n}return _t(n)}const Yr=Z({name:"Binder",props:{syncTargetWithParent:Boolean,syncTarget:{type:Boolean,default:!0}},setup(e){var n;pe("VBinder",(n=yt())===null||n===void 0?void 0:n.proxy);const t=Q("VBinder",null),o=P(null),l=w=>{o.value=w,t&&e.syncTargetWithParent&&t.setTargetRef(w)};let u=[];const i=()=>{let w=o.value;for(;w=_t(w),w!==null;)u.push(w);for(const B of u)ae("scroll",B,v,!0)},s=()=>{for(const w of u)re("scroll",w,v,!0);u=[]},a=new Set,c=w=>{a.size===0&&i(),a.has(w)||a.add(w)},f=w=>{a.has(w)&&a.delete(w),a.size===0&&s()},v=()=>{Dr(p)},p=()=>{a.forEach(w=>w())},S=new Set,x=w=>{S.size===0&&ae("resize",window,_),S.has(w)||S.add(w)},$=w=>{S.has(w)&&S.delete(w),S.size===0&&re("resize",window,_)},_=()=>{S.forEach(w=>w())};return xe(()=>{re("resize",window,_),s()}),{targetRef:o,setTargetRef:l,addScrollListener:c,removeScrollListener:f,addResizeListener:x,removeResizeListener:$}},render(){return ir("binder",this.$slots)}}),qr=Yr,Zr=Z({name:"Target",setup(){const{setTargetRef:e,syncTarget:n}=Q("VBinder");return{syncTarget:n,setTargetDirective:{mounted:e,updated:e}}},render(){const{syncTarget:e,setTargetDirective:n}=this;return e?Oe(it("follower",this.$slots),[[n]]):it("follower",this.$slots)}}),he="@@mmoContext",Rr={mounted(e,{value:n}){e[he]={handler:void 0},typeof n=="function"&&(e[he].handler=n,ae("mousemoveoutside",e,n))},updated(e,{value:n}){const t=e[he];typeof n=="function"?t.handler?t.handler!==n&&(re("mousemoveoutside",e,t.handler),t.handler=n,ae("mousemoveoutside",e,n)):(e[he].handler=n,ae("mousemoveoutside",e,n)):t.handler&&(re("mousemoveoutside",e,t.handler),t.handler=void 0)},unmounted(e){const{handler:n}=e[he];n&&re("mousemoveoutside",e,n),e[he].handler=void 0}},Jr=Rr,{c:_e}=ar(),Qr="vueuc-style",Ee={top:"bottom",bottom:"top",left:"right",right:"left"},ct={start:"end",center:"center",end:"start"},Ue={top:"height",bottom:"height",left:"width",right:"width"},eo={"bottom-start":"top left",bottom:"top center","bottom-end":"top right","top-start":"bottom left",top:"bottom center","top-end":"bottom right","right-start":"top left",right:"center left","right-end":"bottom left","left-start":"top right",left:"center right","left-end":"bottom right"},to={"bottom-start":"bottom left",bottom:"bottom center","bottom-end":"bottom right","top-start":"top left",top:"top center","top-end":"top right","right-start":"top right",right:"center right","right-end":"bottom right","left-start":"top left",left:"center left","left-end":"bottom left"},no={"bottom-start":"right","bottom-end":"left","top-start":"right","top-end":"left","right-start":"bottom","right-end":"top","left-start":"bottom","left-end":"top"},ft={top:!0,bottom:!1,left:!0,right:!1},ht={top:"end",bottom:"start",left:"end",right:"start"};function ro(e,n,t,o,l,u){if(!l||u)return{placement:e,top:0,left:0};const[i,s]=e.split("-");let a=s??"center",c={top:0,left:0};const f=(S,x,$)=>{let _=0,w=0;const B=t[S]-n[x]-n[S];return B>0&&o&&($?w=ft[x]?B:-B:_=ft[x]?B:-B),{left:_,top:w}},v=i==="left"||i==="right";if(a!=="center"){const S=no[e],x=Ee[S],$=Ue[S];if(t[$]>n[$]){if(n[S]+n[$]<t[$]){const _=(t[$]-n[$])/2;n[S]<_||n[x]<_?n[S]<n[x]?(a=ct[s],c=f($,x,v)):c=f($,S,v):a="center"}}else t[$]<n[$]&&n[x]<0&&n[S]>n[x]&&(a=ct[s])}else{const S=i==="bottom"||i==="top"?"left":"top",x=Ee[S],$=Ue[S],_=(t[$]-n[$])/2;(n[S]<_||n[x]<_)&&(n[S]>n[x]?(a=ht[S],c=f($,S,v)):(a=ht[x],c=f($,x,v)))}let p=i;return n[i]<t[Ue[i]]&&n[i]<n[Ee[i]]&&(p=Ee[i]),{placement:a!=="center"?`${p}-${a}`:p,left:c.left,top:c.top}}function oo(e,n){return n?to[e]:eo[e]}function io(e,n,t,o,l,u){if(u)switch(e){case"bottom-start":return{top:`${Math.round(t.top-n.top+t.height)}px`,left:`${Math.round(t.left-n.left)}px`,transform:"translateY(-100%)"};case"bottom-end":return{top:`${Math.round(t.top-n.top+t.height)}px`,left:`${Math.round(t.left-n.left+t.width)}px`,transform:"translateX(-100%) translateY(-100%)"};case"top-start":return{top:`${Math.round(t.top-n.top)}px`,left:`${Math.round(t.left-n.left)}px`,transform:""};case"top-end":return{top:`${Math.round(t.top-n.top)}px`,left:`${Math.round(t.left-n.left+t.width)}px`,transform:"translateX(-100%)"};case"right-start":return{top:`${Math.round(t.top-n.top)}px`,left:`${Math.round(t.left-n.left+t.width)}px`,transform:"translateX(-100%)"};case"right-end":return{top:`${Math.round(t.top-n.top+t.height)}px`,left:`${Math.round(t.left-n.left+t.width)}px`,transform:"translateX(-100%) translateY(-100%)"};case"left-start":return{top:`${Math.round(t.top-n.top)}px`,left:`${Math.round(t.left-n.left)}px`,transform:""};case"left-end":return{top:`${Math.round(t.top-n.top+t.height)}px`,left:`${Math.round(t.left-n.left)}px`,transform:"translateY(-100%)"};case"top":return{top:`${Math.round(t.top-n.top)}px`,left:`${Math.round(t.left-n.left+t.width/2)}px`,transform:"translateX(-50%)"};case"right":return{top:`${Math.round(t.top-n.top+t.height/2)}px`,left:`${Math.round(t.left-n.left+t.width)}px`,transform:"translateX(-100%) translateY(-50%)"};case"left":return{top:`${Math.round(t.top-n.top+t.height/2)}px`,left:`${Math.round(t.left-n.left)}px`,transform:"translateY(-50%)"};case"bottom":default:return{top:`${Math.round(t.top-n.top+t.height)}px`,left:`${Math.round(t.left-n.left+t.width/2)}px`,transform:"translateX(-50%) translateY(-100%)"}}switch(e){case"bottom-start":return{top:`${Math.round(t.top-n.top+t.height+o)}px`,left:`${Math.round(t.left-n.left+l)}px`,transform:""};case"bottom-end":return{top:`${Math.round(t.top-n.top+t.height+o)}px`,left:`${Math.round(t.left-n.left+t.width+l)}px`,transform:"translateX(-100%)"};case"top-start":return{top:`${Math.round(t.top-n.top+o)}px`,left:`${Math.round(t.left-n.left+l)}px`,transform:"translateY(-100%)"};case"top-end":return{top:`${Math.round(t.top-n.top+o)}px`,left:`${Math.round(t.left-n.left+t.width+l)}px`,transform:"translateX(-100%) translateY(-100%)"};case"right-start":return{top:`${Math.round(t.top-n.top+o)}px`,left:`${Math.round(t.left-n.left+t.width+l)}px`,transform:""};case"right-end":return{top:`${Math.round(t.top-n.top+t.height+o)}px`,left:`${Math.round(t.left-n.left+t.width+l)}px`,transform:"translateY(-100%)"};case"left-start":return{top:`${Math.round(t.top-n.top+o)}px`,left:`${Math.round(t.left-n.left+l)}px`,transform:"translateX(-100%)"};case"left-end":return{top:`${Math.round(t.top-n.top+t.height+o)}px`,left:`${Math.round(t.left-n.left+l)}px`,transform:"translateX(-100%) translateY(-100%)"};case"top":return{top:`${Math.round(t.top-n.top+o)}px`,left:`${Math.round(t.left-n.left+t.width/2+l)}px`,transform:"translateY(-100%) translateX(-50%)"};case"right":return{top:`${Math.round(t.top-n.top+t.height/2+o)}px`,left:`${Math.round(t.left-n.left+t.width+l)}px`,transform:"translateY(-50%)"};case"left":return{top:`${Math.round(t.top-n.top+t.height/2+o)}px`,left:`${Math.round(t.left-n.left+l)}px`,transform:"translateY(-50%) translateX(-100%)"};case"bottom":default:return{top:`${Math.round(t.top-n.top+t.height+o)}px`,left:`${Math.round(t.left-n.left+t.width/2+l)}px`,transform:"translateX(-50%)"}}}const ao=_e([_e(".v-binder-follower-container",{position:"absolute",left:"0",right:"0",top:"0",height:"0",pointerEvents:"none",zIndex:"auto"}),_e(".v-binder-follower-content",{position:"absolute",zIndex:"auto"},[_e("> *",{pointerEvents:"all"})])]),lo=Z({name:"Follower",inheritAttrs:!1,props:{show:Boolean,enabled:{type:Boolean,default:void 0},placement:{type:String,default:"bottom"},syncTrigger:{type:Array,default:["resize","scroll"]},to:[String,Object],flip:{type:Boolean,default:!0},internalShift:Boolean,x:Number,y:Number,width:String,minWidth:String,containerClass:String,teleportDisabled:Boolean,zindexable:{type:Boolean,default:!0},zIndex:Number,overlap:Boolean},setup(e){const n=Q("VBinder"),t=ce(()=>e.enabled!==void 0?e.enabled:e.show),o=P(null),l=P(null),u=()=>{const{syncTrigger:p}=e;p.includes("scroll")&&n.addScrollListener(a),p.includes("resize")&&n.addResizeListener(a)},i=()=>{n.removeScrollListener(a),n.removeResizeListener(a)};Le(()=>{t.value&&(a(),u())});const s=lr();ao.mount({id:"vueuc/binder",head:!0,anchorMetaName:Qr,ssr:s}),xe(()=>{i()}),jr(()=>{t.value&&a()});const a=()=>{if(!t.value)return;const p=o.value;if(p===null)return;const S=n.targetRef,{x,y:$,overlap:_}=e,w=x!==void 0&&$!==void 0?Ur(x,$):Ge(S);p.style.setProperty("--v-target-width",`${Math.round(w.width)}px`),p.style.setProperty("--v-target-height",`${Math.round(w.height)}px`);const{width:B,minWidth:N,placement:g,internalShift:m,flip:F}=e;p.setAttribute("v-placement",g),_?p.setAttribute("v-overlap",""):p.removeAttribute("v-overlap");const{style:y}=p;B==="target"?y.width=`${w.width}px`:B!==void 0?y.width=B:y.width="",N==="target"?y.minWidth=`${w.width}px`:N!==void 0?y.minWidth=N:y.minWidth="";const k=Ge(p),z=Ge(l.value),{left:b,top:H,placement:U}=ro(g,w,k,m,F,_),X=oo(U,_),{left:A,top:T,transform:j}=io(U,z,w,H,b,_);p.setAttribute("v-placement",U),p.style.setProperty("--v-offset-left",`${Math.round(b)}px`),p.style.setProperty("--v-offset-top",`${Math.round(H)}px`),p.style.transform=`translateX(${A}) translateY(${T}) ${j}`,p.style.setProperty("--v-transform-origin",X),p.style.transformOrigin=X};ne(t,p=>{p?(u(),c()):i()});const c=()=>{Re().then(a).catch(p=>console.error(p))};["placement","x","y","internalShift","flip","width","overlap","minWidth"].forEach(p=>{ne(ee(e,p),a)}),["teleportDisabled"].forEach(p=>{ne(ee(e,p),c)}),ne(ee(e,"syncTrigger"),p=>{p.includes("resize")?n.addResizeListener(a):n.removeResizeListener(a),p.includes("scroll")?n.addScrollListener(a):n.removeScrollListener(a)});const f=xt(),v=ce(()=>{const{to:p}=e;if(p!==void 0)return p;f.value});return{VBinder:n,mergedEnabled:t,offsetContainerRef:l,followerRef:o,mergedTo:v,syncPosition:a}},render(){return h(sr,{show:this.show,to:this.mergedTo,disabled:this.teleportDisabled},{default:()=>{var e,n;const t=h("div",{class:["v-binder-follower-container",this.containerClass],ref:"offsetContainerRef"},[h("div",{class:"v-binder-follower-content",ref:"followerRef"},(n=(e=this.$slots).default)===null||n===void 0?void 0:n.call(e))]);return this.zindexable?Oe(t,[[Ct,{enabled:this.mergedEnabled,zIndex:this.zIndex}]]):t}})}});function so(e,n){var t=-1,o=dr(e)?Array(e.length):[];return ur(e,function(l,u,i){o[++t]=n(l,u,i)}),o}function uo(e,n){var t=cr(e)?fr:so;return t(e,hr(n))}function co(e){const{mergedLocaleRef:n,mergedDateLocaleRef:t}=Q(pr,null)||{},o=K(()=>{var u,i;return(i=(u=n==null?void 0:n.value)===null||u===void 0?void 0:u[e])!==null&&i!==void 0?i:vr[e]});return{dateLocaleRef:K(()=>{var u;return(u=t==null?void 0:t.value)!==null&&u!==void 0?u:gr}),localeRef:o}}const fo=Z({name:"Eye",render(){return h("svg",{xmlns:"http://www.w3.org/2000/svg",viewBox:"0 0 512 512"},h("path",{d:"M255.66 112c-77.94 0-157.89 45.11-220.83 135.33a16 16 0 0 0-.27 17.77C82.92 340.8 161.8 400 255.66 400c92.84 0 173.34-59.38 221.79-135.25a16.14 16.14 0 0 0 0-17.47C428.89 172.28 347.8 112 255.66 112z",fill:"none",stroke:"currentColor","stroke-linecap":"round","stroke-linejoin":"round","stroke-width":"32"}),h("circle",{cx:"256",cy:"256",r:"80",fill:"none",stroke:"currentColor","stroke-miterlimit":"10","stroke-width":"32"}))}}),ho=Z({name:"EyeOff",render(){return h("svg",{xmlns:"http://www.w3.org/2000/svg",viewBox:"0 0 512 512"},h("path",{d:"M432 448a15.92 15.92 0 0 1-11.31-4.69l-352-352a16 16 0 0 1 22.62-22.62l352 352A16 16 0 0 1 432 448z",fill:"currentColor"}),h("path",{d:"M255.66 384c-41.49 0-81.5-12.28-118.92-36.5c-34.07-22-64.74-53.51-88.7-91v-.08c19.94-28.57 41.78-52.73 65.24-72.21a2 2 0 0 0 .14-2.94L93.5 161.38a2 2 0 0 0-2.71-.12c-24.92 21-48.05 46.76-69.08 76.92a31.92 31.92 0 0 0-.64 35.54c26.41 41.33 60.4 76.14 98.28 100.65C162 402 207.9 416 255.66 416a239.13 239.13 0 0 0 75.8-12.58a2 2 0 0 0 .77-3.31l-21.58-21.58a4 4 0 0 0-3.83-1a204.8 204.8 0 0 1-51.16 6.47z",fill:"currentColor"}),h("path",{d:"M490.84 238.6c-26.46-40.92-60.79-75.68-99.27-100.53C349 110.55 302 96 255.66 96a227.34 227.34 0 0 0-74.89 12.83a2 2 0 0 0-.75 3.31l21.55 21.55a4 4 0 0 0 3.88 1a192.82 192.82 0 0 1 50.21-6.69c40.69 0 80.58 12.43 118.55 37c34.71 22.4 65.74 53.88 89.76 91a.13.13 0 0 1 0 .16a310.72 310.72 0 0 1-64.12 72.73a2 2 0 0 0-.15 2.95l19.9 19.89a2 2 0 0 0 2.7.13a343.49 343.49 0 0 0 68.64-78.48a32.2 32.2 0 0 0-.1-34.78z",fill:"currentColor"}),h("path",{d:"M256 160a95.88 95.88 0 0 0-21.37 2.4a2 2 0 0 0-1 3.38l112.59 112.56a2 2 0 0 0 3.38-1A96 96 0 0 0 256 160z",fill:"currentColor"}),h("path",{d:"M165.78 233.66a2 2 0 0 0-3.38 1a96 96 0 0 0 115 115a2 2 0 0 0 1-3.38z",fill:"currentColor"}))}}),po=Z({name:"ChevronDown",render(){return h("svg",{viewBox:"0 0 16 16",fill:"none",xmlns:"http://www.w3.org/2000/svg"},h("path",{d:"M3.14645 5.64645C3.34171 5.45118 3.65829 5.45118 3.85355 5.64645L8 9.79289L12.1464 5.64645C12.3417 5.45118 12.6583 5.45118 12.8536 5.64645C13.0488 5.84171 13.0488 6.15829 12.8536 6.35355L8.35355 10.8536C8.15829 11.0488 7.84171 11.0488 7.64645 10.8536L3.14645 6.35355C2.95118 6.15829 2.95118 5.84171 3.14645 5.64645Z",fill:"currentColor"}))}}),vo=br("clear",h("svg",{viewBox:"0 0 16 16",version:"1.1",xmlns:"http://www.w3.org/2000/svg"},h("g",{stroke:"none","stroke-width":"1",fill:"none","fill-rule":"evenodd"},h("g",{fill:"currentColor","fill-rule":"nonzero"},h("path",{d:"M8,2 C11.3137085,2 14,4.6862915 14,8 C14,11.3137085 11.3137085,14 8,14 C4.6862915,14 2,11.3137085 2,8 C2,4.6862915 4.6862915,2 8,2 Z M6.5343055,5.83859116 C6.33943736,5.70359511 6.07001296,5.72288026 5.89644661,5.89644661 L5.89644661,5.89644661 L5.83859116,5.9656945 C5.70359511,6.16056264 5.72288026,6.42998704 5.89644661,6.60355339 L5.89644661,6.60355339 L7.293,8 L5.89644661,9.39644661 L5.83859116,9.4656945 C5.70359511,9.66056264 5.72288026,9.92998704 5.89644661,10.1035534 L5.89644661,10.1035534 L5.9656945,10.1614088 C6.16056264,10.2964049 6.42998704,10.2771197 6.60355339,10.1035534 L6.60355339,10.1035534 L8,8.707 L9.39644661,10.1035534 L9.4656945,10.1614088 C9.66056264,10.2964049 9.92998704,10.2771197 10.1035534,10.1035534 L10.1035534,10.1035534 L10.1614088,10.0343055 C10.2964049,9.83943736 10.2771197,9.57001296 10.1035534,9.39644661 L10.1035534,9.39644661 L8.707,8 L10.1035534,6.60355339 L10.1614088,6.5343055 C10.2964049,6.33943736 10.2771197,6.07001296 10.1035534,5.89644661 L10.1035534,5.89644661 L10.0343055,5.83859116 C9.83943736,5.70359511 9.57001296,5.72288026 9.39644661,5.89644661 L9.39644661,5.89644661 L8,7.293 L6.60355339,5.89644661 Z"})))));function pt(e){return Array.isArray(e)?e:[e]}const Je={STOP:"STOP"};function Et(e,n){const t=n(e);e.children!==void 0&&t!==Je.STOP&&e.children.forEach(o=>Et(o,n))}function go(e,n={}){const{preserveGroup:t=!1}=n,o=[],l=t?i=>{i.isLeaf||(o.push(i.key),u(i.children))}:i=>{i.isLeaf||(i.isGroup||o.push(i.key),u(i.children))};function u(i){i.forEach(l)}return u(e),o}function bo(e,n){const{isLeaf:t}=e;return t!==void 0?t:!n(e)}function mo(e){return e.children}function wo(e){return e.key}function yo(){return!1}function xo(e,n){const{isLeaf:t}=e;return!(t===!1&&!Array.isArray(n(e)))}function Co(e){return e.disabled===!0}function $o(e,n){return e.isLeaf===!1&&!Array.isArray(n(e))}function Xe(e){var n;return e==null?[]:Array.isArray(e)?e:(n=e.checkedKeys)!==null&&n!==void 0?n:[]}function Ye(e){var n;return e==null||Array.isArray(e)?[]:(n=e.indeterminateKeys)!==null&&n!==void 0?n:[]}function So(e,n){const t=new Set(e);return n.forEach(o=>{t.has(o)||t.add(o)}),Array.from(t)}function Ao(e,n){const t=new Set(e);return n.forEach(o=>{t.has(o)&&t.delete(o)}),Array.from(t)}function Mo(e){return(e==null?void 0:e.type)==="group"}function oi(e){const n=new Map;return e.forEach((t,o)=>{n.set(t.key,o)}),t=>{var o;return(o=n.get(t))!==null&&o!==void 0?o:null}}class zo extends Error{constructor(){super(),this.message="SubtreeNotLoadedError: checking a subtree whose required nodes are not fully loaded."}}function ko(e,n,t,o){return Fe(n.concat(e),t,o,!1)}function Bo(e,n){const t=new Set;return e.forEach(o=>{const l=n.treeNodeMap.get(o);if(l!==void 0){let u=l.parent;for(;u!==null&&!(u.disabled||t.has(u.key));)t.add(u.key),u=u.parent}}),t}function _o(e,n,t,o){const l=Fe(n,t,o,!1),u=Fe(e,t,o,!0),i=Bo(e,t),s=[];return l.forEach(a=>{(u.has(a)||i.has(a))&&s.push(a)}),s.forEach(a=>l.delete(a)),l}function qe(e,n){const{checkedKeys:t,keysToCheck:o,keysToUncheck:l,indeterminateKeys:u,cascade:i,leafOnly:s,checkStrategy:a,allowNotLoaded:c}=e;if(!i)return o!==void 0?{checkedKeys:So(t,o),indeterminateKeys:Array.from(u)}:l!==void 0?{checkedKeys:Ao(t,l),indeterminateKeys:Array.from(u)}:{checkedKeys:Array.from(t),indeterminateKeys:Array.from(u)};const{levelTreeNodeMap:f}=n;let v;l!==void 0?v=_o(l,t,n,c):o!==void 0?v=ko(o,t,n,c):v=Fe(t,n,c,!1);const p=a==="parent",S=a==="child"||s,x=v,$=new Set,_=Math.max.apply(null,Array.from(f.keys()));for(let w=_;w>=0;w-=1){const B=w===0,N=f.get(w);for(const g of N){if(g.isLeaf)continue;const{key:m,shallowLoaded:F}=g;if(S&&F&&g.children.forEach(b=>{!b.disabled&&!b.isLeaf&&b.shallowLoaded&&x.has(b.key)&&x.delete(b.key)}),g.disabled||!F)continue;let y=!0,k=!1,z=!0;for(const b of g.children){const H=b.key;if(!b.disabled){if(z&&(z=!1),x.has(H))k=!0;else if($.has(H)){k=!0,y=!1;break}else if(y=!1,k)break}}y&&!z?(p&&g.children.forEach(b=>{!b.disabled&&x.has(b.key)&&x.delete(b.key)}),x.add(m)):k&&$.add(m),B&&S&&x.has(m)&&x.delete(m)}}return{checkedKeys:Array.from(x),indeterminateKeys:Array.from($)}}function Fe(e,n,t,o){const{treeNodeMap:l,getChildren:u}=n,i=new Set,s=new Set(e);return e.forEach(a=>{const c=l.get(a);c!==void 0&&Et(c,f=>{if(f.disabled)return Je.STOP;const{key:v}=f;if(!i.has(v)&&(i.add(v),s.add(v),$o(f.rawNode,u))){if(o)return Je.STOP;if(!t)throw new zo}})}),s}function Eo(e,{includeGroup:n=!1,includeSelf:t=!0},o){var l;const u=o.treeNodeMap;let i=e==null?null:(l=u.get(e))!==null&&l!==void 0?l:null;const s={keyPath:[],treeNodePath:[],treeNode:i};if(i!=null&&i.ignored)return s.treeNode=null,s;for(;i;)!i.ignored&&(n||!i.isGroup)&&s.treeNodePath.push(i),i=i.parent;return s.treeNodePath.reverse(),t||s.treeNodePath.pop(),s.keyPath=s.treeNodePath.map(a=>a.key),s}function Po(e){if(e.length===0)return null;const n=e[0];return n.isGroup||n.ignored||n.disabled?n.getNext():n}function To(e,n){const t=e.siblings,o=t.length,{index:l}=e;return n?t[(l+1)%o]:l===t.length-1?null:t[l+1]}function vt(e,n,{loop:t=!1,includeDisabled:o=!1}={}){const l=n==="prev"?Io:To,u={reverse:n==="prev"};let i=!1,s=null;function a(c){if(c!==null){if(c===e){if(!i)i=!0;else if(!e.disabled&&!e.isGroup){s=e;return}}else if((!c.disabled||o)&&!c.ignored&&!c.isGroup){s=c;return}if(c.isGroup){const f=tt(c,u);f!==null?s=f:a(l(c,t))}else{const f=l(c,!1);if(f!==null)a(f);else{const v=Fo(c);v!=null&&v.isGroup?a(l(v,t)):t&&a(l(c,!0))}}}}return a(e),s}function Io(e,n){const t=e.siblings,o=t.length,{index:l}=e;return n?t[(l-1+o)%o]:l===0?null:t[l-1]}function Fo(e){return e.parent}function tt(e,n={}){const{reverse:t=!1}=n,{children:o}=e;if(o){const{length:l}=o,u=t?l-1:0,i=t?-1:l,s=t?-1:1;for(let a=u;a!==i;a+=s){const c=o[a];if(!c.disabled&&!c.ignored)if(c.isGroup){const f=tt(c,n);if(f!==null)return f}else return c}}return null}const Lo={getChild(){return this.ignored?null:tt(this)},getParent(){const{parent:e}=this;return e!=null&&e.isGroup?e.getParent():e},getNext(e={}){return vt(this,"next",e)},getPrev(e={}){return vt(this,"prev",e)}};function Oo(e,n){const t=n?new Set(n):void 0,o=[];function l(u){u.forEach(i=>{o.push(i),!(i.isLeaf||!i.children||i.ignored)&&(i.isGroup||t===void 0||t.has(i.key))&&l(i.children)})}return l(e),o}function No(e,n){const t=e.key;for(;n;){if(n.key===t)return!0;n=n.parent}return!1}function Pt(e,n,t,o,l,u=null,i=0){const s=[];return e.forEach((a,c)=>{var f;const v=Object.create(o);if(v.rawNode=a,v.siblings=s,v.level=i,v.index=c,v.isFirstChild=c===0,v.isLastChild=c+1===e.length,v.parent=u,!v.ignored){const p=l(a);Array.isArray(p)&&(v.children=Pt(p,n,t,o,l,v,i+1))}s.push(v),n.set(v.key,v),t.has(i)||t.set(i,[]),(f=t.get(i))===null||f===void 0||f.push(v)}),s}function ii(e,n={}){var t;const o=new Map,l=new Map,{getDisabled:u=Co,getIgnored:i=yo,getIsGroup:s=Mo,getKey:a=wo}=n,c=(t=n.getChildren)!==null&&t!==void 0?t:mo,f=n.ignoreEmptyChildren?g=>{const m=c(g);return Array.isArray(m)?m.length?m:null:m}:c,v=Object.assign({get key(){return a(this.rawNode)},get disabled(){return u(this.rawNode)},get isGroup(){return s(this.rawNode)},get isLeaf(){return bo(this.rawNode,f)},get shallowLoaded(){return xo(this.rawNode,f)},get ignored(){return i(this.rawNode)},contains(g){return No(this,g)}},Lo),p=Pt(e,o,l,v,f);function S(g){if(g==null)return null;const m=o.get(g);return m&&!m.isGroup&&!m.ignored?m:null}function x(g){if(g==null)return null;const m=o.get(g);return m&&!m.ignored?m:null}function $(g,m){const F=x(g);return F?F.getPrev(m):null}function _(g,m){const F=x(g);return F?F.getNext(m):null}function w(g){const m=x(g);return m?m.getParent():null}function B(g){const m=x(g);return m?m.getChild():null}const N={treeNodes:p,treeNodeMap:o,levelTreeNodeMap:l,maxLevel:Math.max(...l.keys()),getChildren:f,getFlattenedNodes(g){return Oo(p,g)},getNode:S,getPrev:$,getNext:_,getParent:w,getChild:B,getFirstAvailableNode(){return Po(p)},getPath(g,m={}){return Eo(g,m,N)},getCheckedKeys(g,m={}){const{cascade:F=!0,leafOnly:y=!1,checkStrategy:k="all",allowNotLoaded:z=!1}=m;return qe({checkedKeys:Xe(g),indeterminateKeys:Ye(g),cascade:F,leafOnly:y,checkStrategy:k,allowNotLoaded:z},N)},check(g,m,F={}){const{cascade:y=!0,leafOnly:k=!1,checkStrategy:z="all",allowNotLoaded:b=!1}=F;return qe({checkedKeys:Xe(m),indeterminateKeys:Ye(m),keysToCheck:g==null?[]:pt(g),cascade:y,leafOnly:k,checkStrategy:z,allowNotLoaded:b},N)},uncheck(g,m,F={}){const{cascade:y=!0,leafOnly:k=!1,checkStrategy:z="all",allowNotLoaded:b=!1}=F;return qe({checkedKeys:Xe(m),indeterminateKeys:Ye(m),keysToUncheck:g==null?[]:pt(g),cascade:y,leafOnly:k,checkStrategy:z,allowNotLoaded:b},N)},getNonLeafKeys(g={}){return go(p,g)}};return N}const Ze={top:"bottom",bottom:"top",left:"right",right:"left"},W="var(--n-arrow-height) * 1.414",Do=O([E("popover",`
 transition:
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 position: relative;
 font-size: var(--n-font-size);
 color: var(--n-text-color);
 box-shadow: var(--n-box-shadow);
 word-break: break-word;
 `,[O(">",[E("scrollbar",`
 height: inherit;
 max-height: inherit;
 `)]),ie("raw",`
 background-color: var(--n-color);
 border-radius: var(--n-border-radius);
 `,[ie("scrollable",[ie("show-header-or-footer","padding: var(--n-padding);")])]),M("header",`
 padding: var(--n-padding);
 border-bottom: 1px solid var(--n-divider-color);
 transition: border-color .3s var(--n-bezier);
 `),M("footer",`
 padding: var(--n-padding);
 border-top: 1px solid var(--n-divider-color);
 transition: border-color .3s var(--n-bezier);
 `),G("scrollable, show-header-or-footer",[M("content",`
 padding: var(--n-padding);
 `)])]),E("popover-shared",`
 transform-origin: inherit;
 `,[E("popover-arrow-wrapper",`
 position: absolute;
 overflow: hidden;
 pointer-events: none;
 `,[E("popover-arrow",`
 transition: background-color .3s var(--n-bezier);
 position: absolute;
 display: block;
 width: calc(${W});
 height: calc(${W});
 box-shadow: 0 0 8px 0 rgba(0, 0, 0, .12);
 transform: rotate(45deg);
 background-color: var(--n-color);
 pointer-events: all;
 `)]),O("&.popover-transition-enter-from, &.popover-transition-leave-to",`
 opacity: 0;
 transform: scale(.85);
 `),O("&.popover-transition-enter-to, &.popover-transition-leave-from",`
 transform: scale(1);
 opacity: 1;
 `),O("&.popover-transition-enter-active",`
 transition:
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier),
 opacity .15s var(--n-bezier-ease-out),
 transform .15s var(--n-bezier-ease-out);
 `),O("&.popover-transition-leave-active",`
 transition:
 box-shadow .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 color .3s var(--n-bezier),
 opacity .15s var(--n-bezier-ease-in),
 transform .15s var(--n-bezier-ease-in);
 `)]),q("top-start",`
 top: calc(${W} / -2);
 left: calc(${te("top-start")} - var(--v-offset-left));
 `),q("top",`
 top: calc(${W} / -2);
 transform: translateX(calc(${W} / -2)) rotate(45deg);
 left: 50%;
 `),q("top-end",`
 top: calc(${W} / -2);
 right: calc(${te("top-end")} + var(--v-offset-left));
 `),q("bottom-start",`
 bottom: calc(${W} / -2);
 left: calc(${te("bottom-start")} - var(--v-offset-left));
 `),q("bottom",`
 bottom: calc(${W} / -2);
 transform: translateX(calc(${W} / -2)) rotate(45deg);
 left: 50%;
 `),q("bottom-end",`
 bottom: calc(${W} / -2);
 right: calc(${te("bottom-end")} + var(--v-offset-left));
 `),q("left-start",`
 left: calc(${W} / -2);
 top: calc(${te("left-start")} - var(--v-offset-top));
 `),q("left",`
 left: calc(${W} / -2);
 transform: translateY(calc(${W} / -2)) rotate(45deg);
 top: 50%;
 `),q("left-end",`
 left: calc(${W} / -2);
 bottom: calc(${te("left-end")} + var(--v-offset-top));
 `),q("right-start",`
 right: calc(${W} / -2);
 top: calc(${te("right-start")} - var(--v-offset-top));
 `),q("right",`
 right: calc(${W} / -2);
 transform: translateY(calc(${W} / -2)) rotate(45deg);
 top: 50%;
 `),q("right-end",`
 right: calc(${W} / -2);
 bottom: calc(${te("right-end")} + var(--v-offset-top));
 `),...uo({top:["right-start","left-start"],right:["top-end","bottom-end"],bottom:["right-end","left-end"],left:["top-start","bottom-start"]},(e,n)=>{const t=["right","left"].includes(n),o=t?"width":"height";return e.map(l=>{const u=l.split("-")[1]==="end",s=`calc((${`var(--v-target-${o}, 0px)`} - ${W}) / 2)`,a=te(l);return O(`[v-placement="${l}"] >`,[E("popover-shared",[G("center-arrow",[E("popover-arrow",`${n}: calc(max(${s}, ${a}) ${u?"+":"-"} var(--v-offset-${t?"left":"top"}));`)])])])})})]);function te(e){return["top","bottom"].includes(e.split("-")[0])?"var(--n-arrow-offset)":"var(--n-arrow-offset-vertical)"}function q(e,n){const t=e.split("-")[0],o=["top","bottom"].includes(t)?"height: var(--n-space-arrow);":"width: var(--n-space-arrow);";return O(`[v-placement="${e}"] >`,[E("popover-shared",`
 margin-${Ze[t]}: var(--n-space);
 `,[G("show-arrow",`
 margin-${Ze[t]}: var(--n-space-arrow);
 `),G("overlap",`
 margin: 0;
 `),mr("popover-arrow-wrapper",`
 right: 0;
 left: 0;
 top: 0;
 bottom: 0;
 ${t}: 100%;
 ${Ze[t]}: auto;
 ${o}
 `,[E("popover-arrow",n)])])])}const Tt=Object.assign(Object.assign({},Ce.props),{to:ge.propTo,show:Boolean,trigger:String,showArrow:Boolean,delay:Number,duration:Number,raw:Boolean,arrowPointToCenter:Boolean,arrowStyle:[String,Object],displayDirective:String,x:Number,y:Number,flip:Boolean,overlap:Boolean,placement:String,width:[Number,String],keepAliveOnHover:Boolean,scrollable:Boolean,contentStyle:[Object,String],headerStyle:[Object,String],footerStyle:[Object,String],internalDeactivateImmediately:Boolean,animated:Boolean,onClickoutside:Function,internalTrapFocus:Boolean,internalOnAfterLeave:Function,minWidth:Number,maxWidth:Number}),Wo=({arrowStyle:e,clsPrefix:n})=>h("div",{key:"__popover-arrow__",class:`${n}-popover-arrow-wrapper`},h("div",{class:`${n}-popover-arrow`,style:e})),Vo=Z({name:"PopoverBody",inheritAttrs:!1,props:Tt,setup(e,{slots:n,attrs:t}){const{namespaceRef:o,mergedClsPrefixRef:l,inlineThemeDisabled:u}=$t(e),i=Ce("Popover","-popover",Do,yr,e,l),s=P(null),a=Q("NPopover"),c=P(null),f=P(e.show),v=P(!1);Te(()=>{const{show:y}=e;y&&!Vr()&&!e.internalDeactivateImmediately&&(v.value=!0)});const p=K(()=>{const{trigger:y,onClickoutside:k}=e,z=[],{positionManuallyRef:{value:b}}=a;return b||(y==="click"&&!k&&z.push([at,g,void 0,{capture:!0}]),y==="hover"&&z.push([Jr,N])),k&&z.push([at,g,void 0,{capture:!0}]),(e.displayDirective==="show"||e.animated&&v.value)&&z.push([xr,e.show]),z}),S=K(()=>{const y=e.width==="trigger"?void 0:je(e.width),k=[];y&&k.push({width:y});const{maxWidth:z,minWidth:b}=e;return z&&k.push({maxWidth:je(z)}),b&&k.push({maxWidth:je(b)}),u||k.push(x.value),k}),x=K(()=>{const{common:{cubicBezierEaseInOut:y,cubicBezierEaseIn:k,cubicBezierEaseOut:z},self:{space:b,spaceArrow:H,padding:U,fontSize:X,textColor:A,dividerColor:T,color:j,boxShadow:J,borderRadius:Y,arrowHeight:R,arrowOffset:$e,arrowOffsetVertical:Ne}}=i.value;return{"--n-box-shadow":J,"--n-bezier":y,"--n-bezier-ease-in":k,"--n-bezier-ease-out":z,"--n-font-size":X,"--n-text-color":A,"--n-color":j,"--n-divider-color":T,"--n-border-radius":Y,"--n-arrow-height":R,"--n-arrow-offset":$e,"--n-arrow-offset-vertical":Ne,"--n-padding":U,"--n-space":b,"--n-space-arrow":H}}),$=u?St("popover",void 0,x,e):void 0;a.setBodyInstance({syncPosition:_}),xe(()=>{a.setBodyInstance(null)}),ne(ee(e,"show"),y=>{e.animated||(y?f.value=!0:f.value=!1)});function _(){var y;(y=s.value)===null||y===void 0||y.syncPosition()}function w(y){e.trigger==="hover"&&e.keepAliveOnHover&&e.show&&a.handleMouseEnter(y)}function B(y){e.trigger==="hover"&&e.keepAliveOnHover&&a.handleMouseLeave(y)}function N(y){e.trigger==="hover"&&!m().contains(lt(y))&&a.handleMouseMoveOutside(y)}function g(y){(e.trigger==="click"&&!m().contains(lt(y))||e.onClickoutside)&&a.handleClickOutside(y)}function m(){return a.getTriggerElement()}pe(wt,c),pe(mt,null),pe(bt,null);function F(){if($==null||$.onRender(),!(e.displayDirective==="show"||e.show||e.animated&&v.value))return null;let k;const z=a.internalRenderBodyRef.value,{value:b}=l;if(z)k=z([`${b}-popover-shared`,$==null?void 0:$.themeClass.value,e.overlap&&`${b}-popover-shared--overlap`,e.showArrow&&`${b}-popover-shared--show-arrow`,e.arrowPointToCenter&&`${b}-popover-shared--center-arrow`],c,S.value,w,B);else{const{value:H}=a.extraClassRef,{internalTrapFocus:U}=e,X=!st(n.header)||!st(n.footer),A=()=>{var T;const j=X?h(At,null,ue(n.header,R=>R?h("div",{class:`${b}-popover__header`,style:e.headerStyle},R):null),ue(n.default,R=>R?h("div",{class:`${b}-popover__content`,style:e.contentStyle},n):null),ue(n.footer,R=>R?h("div",{class:`${b}-popover__footer`,style:e.footerStyle},R):null)):e.scrollable?(T=n.default)===null||T===void 0?void 0:T.call(n):h("div",{class:`${b}-popover__content`,style:e.contentStyle},n),J=e.scrollable?h(Sr,{contentClass:X?void 0:`${b}-popover__content`,contentStyle:X?void 0:e.contentStyle},{default:()=>j}):j,Y=e.showArrow?Wo({arrowStyle:e.arrowStyle,clsPrefix:b}):null;return[J,Y]};k=h("div",$r({class:[`${b}-popover`,`${b}-popover-shared`,$==null?void 0:$.themeClass.value,H.map(T=>`${b}-${T}`),{[`${b}-popover--scrollable`]:e.scrollable,[`${b}-popover--show-header-or-footer`]:X,[`${b}-popover--raw`]:e.raw,[`${b}-popover-shared--overlap`]:e.overlap,[`${b}-popover-shared--show-arrow`]:e.showArrow,[`${b}-popover-shared--center-arrow`]:e.arrowPointToCenter}],ref:c,style:S.value,onKeydown:a.handleKeydown,onMouseenter:w,onMouseleave:B},t),U?h(Cr,{active:e.show,autoFocus:!0},{default:A}):A())}return Oe(k,p.value)}return{displayed:v,namespace:o,isMounted:a.isMountedRef,zIndex:a.zIndexRef,followerRef:s,adjustedTo:ge(e),followerEnabled:f,renderContentNode:F}},render(){return h(lo,{ref:"followerRef",zIndex:this.zIndex,show:this.show,enabled:this.followerEnabled,to:this.adjustedTo,x:this.x,y:this.y,flip:this.flip,placement:this.placement,containerClass:this.namespace,overlap:this.overlap,width:this.width==="trigger"?"target":void 0,teleportDisabled:this.adjustedTo===ge.tdkey},{default:()=>this.animated?h(wr,{name:"popover-transition",appear:this.isMounted,onEnter:()=>{this.followerEnabled=!0},onAfterLeave:()=>{var e;(e=this.internalOnAfterLeave)===null||e===void 0||e.call(this),this.followerEnabled=!1,this.displayed=!1}},{default:this.renderContentNode}):this.renderContentNode()})}}),Ko=Object.keys(Tt),jo={focus:["onFocus","onBlur"],click:["onClick"],hover:["onMouseenter","onMouseleave"],manual:[],nested:["onFocus","onBlur","onMouseenter","onMouseleave","onClick"]};function Ho(e,n,t){jo[n].forEach(o=>{e.props?e.props=Object.assign({},e.props):e.props={};const l=e.props[o],u=t[o];l?e.props[o]=(...i)=>{l(...i),u(...i)}:e.props[o]=u})}const Go={show:{type:Boolean,default:void 0},defaultShow:Boolean,showArrow:{type:Boolean,default:!0},trigger:{type:String,default:"hover"},delay:{type:Number,default:100},duration:{type:Number,default:100},raw:Boolean,placement:{type:String,default:"top"},x:Number,y:Number,arrowPointToCenter:Boolean,disabled:Boolean,getDisabled:Function,displayDirective:{type:String,default:"if"},arrowStyle:[String,Object],flip:{type:Boolean,default:!0},animated:{type:Boolean,default:!0},width:{type:[Number,String],default:void 0},overlap:Boolean,keepAliveOnHover:{type:Boolean,default:!0},zIndex:Number,to:ge.propTo,scrollable:Boolean,contentStyle:[Object,String],headerStyle:[Object,String],footerStyle:[Object,String],onClickoutside:Function,"onUpdate:show":[Function,Array],onUpdateShow:[Function,Array],internalDeactivateImmediately:Boolean,internalSyncTargetWithParent:Boolean,internalInheritedEventHandlers:{type:Array,default:()=>[]},internalTrapFocus:Boolean,internalExtraClass:{type:Array,default:()=>[]},onShow:[Function,Array],onHide:[Function,Array],arrow:{type:Boolean,default:void 0},minWidth:Number,maxWidth:Number},Uo=Object.assign(Object.assign(Object.assign({},Ce.props),Go),{internalOnAfterLeave:Function,internalRenderBody:Function}),ai=Z({name:"Popover",inheritAttrs:!1,props:Uo,__popover__:!0,setup(e){const n=xt(),t=P(null),o=K(()=>e.show),l=P(e.defaultShow),u=zt(o,l),i=ce(()=>e.disabled?!1:u.value),s=()=>{if(e.disabled)return!0;const{getDisabled:A}=e;return!!(A!=null&&A())},a=()=>s()?!1:u.value,c=Hr(e,["arrow","showArrow"]),f=K(()=>e.overlap?!1:c.value);let v=null;const p=P(null),S=P(null),x=ce(()=>e.x!==void 0&&e.y!==void 0);function $(A){const{"onUpdate:show":T,onUpdateShow:j,onShow:J,onHide:Y}=e;l.value=A,T&&V(T,A),j&&V(j,A),A&&J&&V(J,!0),A&&Y&&V(Y,!1)}function _(){v&&v.syncPosition()}function w(){const{value:A}=p;A&&(window.clearTimeout(A),p.value=null)}function B(){const{value:A}=S;A&&(window.clearTimeout(A),S.value=null)}function N(){const A=s();if(e.trigger==="focus"&&!A){if(a())return;$(!0)}}function g(){const A=s();if(e.trigger==="focus"&&!A){if(!a())return;$(!1)}}function m(){const A=s();if(e.trigger==="hover"&&!A){if(B(),p.value!==null||a())return;const T=()=>{$(!0),p.value=null},{delay:j}=e;j===0?T():p.value=window.setTimeout(T,j)}}function F(){const A=s();if(e.trigger==="hover"&&!A){if(w(),S.value!==null||!a())return;const T=()=>{$(!1),S.value=null},{duration:j}=e;j===0?T():S.value=window.setTimeout(T,j)}}function y(){F()}function k(A){var T;a()&&(e.trigger==="click"&&(w(),B(),$(!1)),(T=e.onClickoutside)===null||T===void 0||T.call(e,A))}function z(){if(e.trigger==="click"&&!s()){w(),B();const A=!a();$(A)}}function b(A){e.internalTrapFocus&&A.key==="Escape"&&(w(),B(),$(!1))}function H(A){l.value=A}function U(){var A;return(A=t.value)===null||A===void 0?void 0:A.targetRef}function X(A){v=A}return pe("NPopover",{getTriggerElement:U,handleKeydown:b,handleMouseEnter:m,handleMouseLeave:F,handleClickOutside:k,handleMouseMoveOutside:y,setBodyInstance:X,positionManuallyRef:x,isMountedRef:n,zIndexRef:ee(e,"zIndex"),extraClassRef:ee(e,"internalExtraClass"),internalRenderBodyRef:ee(e,"internalRenderBody")}),Te(()=>{u.value&&s()&&$(!1)}),{binderInstRef:t,positionManually:x,mergedShowConsideringDisabledProp:i,uncontrolledShow:l,mergedShowArrow:f,getMergedShow:a,setShow:H,handleClick:z,handleMouseEnter:m,handleMouseLeave:F,handleFocus:N,handleBlur:g,syncPosition:_}},render(){var e;const{positionManually:n,$slots:t}=this;let o,l=!1;if(!n&&(t.activator?o=dt(t,"activator"):o=dt(t,"trigger"),o)){o=Ar(o),o=o.type===Mr?h("span",[o]):o;const u={onClick:this.handleClick,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onFocus:this.handleFocus,onBlur:this.handleBlur};if(!((e=o.type)===null||e===void 0)&&e.__popover__)l=!0,o.props||(o.props={internalSyncTargetWithParent:!0,internalInheritedEventHandlers:[]}),o.props.internalSyncTargetWithParent=!0,o.props.internalInheritedEventHandlers?o.props.internalInheritedEventHandlers=[u,...o.props.internalInheritedEventHandlers]:o.props.internalInheritedEventHandlers=[u];else{const{internalInheritedEventHandlers:i}=this,s=[u,...i],a={onBlur:c=>{s.forEach(f=>{f.onBlur(c)})},onFocus:c=>{s.forEach(f=>{f.onFocus(c)})},onClick:c=>{s.forEach(f=>{f.onClick(c)})},onMouseenter:c=>{s.forEach(f=>{f.onMouseenter(c)})},onMouseleave:c=>{s.forEach(f=>{f.onMouseleave(c)})}};Ho(o,i?"nested":n?"manual":this.trigger,a)}}return h(qr,{ref:"binderInstRef",syncTarget:!l,syncTargetWithParent:this.internalSyncTargetWithParent},{default:()=>{this.mergedShowConsideringDisabledProp;const u=this.getMergedShow();return[this.internalTrapFocus&&u?Oe(h("div",{style:{position:"fixed",inset:0}}),[[Ct,{enabled:u,zIndex:this.zIndex}]]):null,n?null:h(Zr,null,{default:()=>o}),h(Vo,zr(this.$props,Ko,Object.assign(Object.assign({},this.$attrs),{showArrow:this.mergedShowArrow,show:u})),{default:()=>{var i,s;return(s=(i=this.$slots).default)===null||s===void 0?void 0:s.call(i)},header:()=>{var i,s;return(s=(i=this.$slots).header)===null||s===void 0?void 0:s.call(i)},footer:()=>{var i,s;return(s=(i=this.$slots).footer)===null||s===void 0?void 0:s.call(i)}})]}})}}),Xo=E("base-clear",`
 flex-shrink: 0;
 height: 1em;
 width: 1em;
 position: relative;
`,[O(">",[M("clear",`
 font-size: var(--n-clear-size);
 height: 1em;
 width: 1em;
 cursor: pointer;
 color: var(--n-clear-color);
 transition: color .3s var(--n-bezier);
 display: flex;
 `,[O("&:hover",`
 color: var(--n-clear-color-hover)!important;
 `),O("&:active",`
 color: var(--n-clear-color-pressed)!important;
 `)]),M("placeholder",`
 display: flex;
 `),M("clear, placeholder",`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 `,[kr({originalTransform:"translateX(-50%) translateY(-50%)",left:"50%",top:"50%"})])])]),Qe=Z({name:"BaseClear",props:{clsPrefix:{type:String,required:!0},show:Boolean,onClear:Function},setup(e){return Mt("-base-clear",Xo,ee(e,"clsPrefix")),{handleMouseDown(n){n.preventDefault()}}},render(){const{clsPrefix:e}=this;return h("div",{class:`${e}-base-clear`},h(Br,null,{default:()=>{var n,t;return this.show?h("div",{key:"dismiss",class:`${e}-base-clear__clear`,onClick:this.onClear,onMousedown:this.handleMouseDown,"data-clear":!0},we(this.$slots.icon,()=>[h(Ie,{clsPrefix:e},{default:()=>h(vo,null)})])):h("div",{key:"icon",class:`${e}-base-clear__placeholder`},(t=(n=this.$slots).placeholder)===null||t===void 0?void 0:t.call(n))}}))}}),Yo=Z({name:"InternalSelectionSuffix",props:{clsPrefix:{type:String,required:!0},showArrow:{type:Boolean,default:void 0},showClear:{type:Boolean,default:void 0},loading:{type:Boolean,default:!1},onClear:Function},setup(e,{slots:n}){return()=>{const{clsPrefix:t}=e;return h(_r,{clsPrefix:t,class:`${t}-base-suffix`,strokeWidth:24,scale:.85,show:e.loading},{default:()=>e.showArrow?h(Qe,{clsPrefix:t,show:e.showClear,onClear:e.onClear},{placeholder:()=>h(Ie,{clsPrefix:t,class:`${t}-base-suffix__arrow`},{default:()=>we(n.default,()=>[h(po,null)])})}):null})}}}),It=et("n-input");function qo(e){let n=0;for(const t of e)n++;return n}function Pe(e){return e===""||e==null}function Zo(e){const n=P(null);function t(){const{value:u}=e;if(!(u!=null&&u.focus)){l();return}const{selectionStart:i,selectionEnd:s,value:a}=u;if(i==null||s==null){l();return}n.value={start:i,end:s,beforeText:a.slice(0,i),afterText:a.slice(s)}}function o(){var u;const{value:i}=n,{value:s}=e;if(!i||!s)return;const{value:a}=s,{start:c,beforeText:f,afterText:v}=i;let p=a.length;if(a.endsWith(v))p=a.length-v.length;else if(a.startsWith(f))p=f.length;else{const S=f[c-1],x=a.indexOf(S,c-1);x!==-1&&(p=x+1)}(u=s.setSelectionRange)===null||u===void 0||u.call(s,p,p)}function l(){n.value=null}return ne(e,l),{recordCursor:t,restoreCursor:o}}const gt=Z({name:"InputWordCount",setup(e,{slots:n}){const{mergedValueRef:t,maxlengthRef:o,mergedClsPrefixRef:l,countGraphemesRef:u}=Q(It),i=K(()=>{const{value:s}=t;return s===null||Array.isArray(s)?0:(u.value||qo)(s)});return()=>{const{value:s}=o,{value:a}=t;return h("span",{class:`${l.value}-input-word-count`},Er(n.default,{value:a===null||Array.isArray(a)?"":a},()=>[s===void 0?i.value:`${i.value} / ${s}`]))}}}),Ro=E("input",`
 max-width: 100%;
 cursor: text;
 line-height: 1.5;
 z-index: auto;
 outline: none;
 box-sizing: border-box;
 position: relative;
 display: inline-flex;
 border-radius: var(--n-border-radius);
 background-color: var(--n-color);
 transition: background-color .3s var(--n-bezier);
 font-size: var(--n-font-size);
 --n-padding-vertical: calc((var(--n-height) - 1.5 * var(--n-font-size)) / 2);
`,[M("input, textarea",`
 overflow: hidden;
 flex-grow: 1;
 position: relative;
 `),M("input-el, textarea-el, input-mirror, textarea-mirror, separator, placeholder",`
 box-sizing: border-box;
 font-size: inherit;
 line-height: 1.5;
 font-family: inherit;
 border: none;
 outline: none;
 background-color: #0000;
 text-align: inherit;
 transition:
 -webkit-text-fill-color .3s var(--n-bezier),
 caret-color .3s var(--n-bezier),
 color .3s var(--n-bezier),
 text-decoration-color .3s var(--n-bezier);
 `),M("input-el, textarea-el",`
 -webkit-appearance: none;
 scrollbar-width: none;
 width: 100%;
 min-width: 0;
 text-decoration-color: var(--n-text-decoration-color);
 color: var(--n-text-color);
 caret-color: var(--n-caret-color);
 background-color: transparent;
 `,[O("&::-webkit-scrollbar, &::-webkit-scrollbar-track-piece, &::-webkit-scrollbar-thumb",`
 width: 0;
 height: 0;
 display: none;
 `),O("&::placeholder",`
 color: #0000;
 -webkit-text-fill-color: transparent !important;
 `),O("&:-webkit-autofill ~",[M("placeholder","display: none;")])]),G("round",[ie("textarea","border-radius: calc(var(--n-height) / 2);")]),M("placeholder",`
 pointer-events: none;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 overflow: hidden;
 color: var(--n-placeholder-color);
 `,[O("span",`
 width: 100%;
 display: inline-block;
 `)]),G("textarea",[M("placeholder","overflow: visible;")]),ie("autosize","width: 100%;"),G("autosize",[M("textarea-el, input-el",`
 position: absolute;
 top: 0;
 left: 0;
 height: 100%;
 `)]),E("input-wrapper",`
 overflow: hidden;
 display: inline-flex;
 flex-grow: 1;
 position: relative;
 padding-left: var(--n-padding-left);
 padding-right: var(--n-padding-right);
 `),M("input-mirror",`
 padding: 0;
 height: var(--n-height);
 line-height: var(--n-height);
 overflow: hidden;
 visibility: hidden;
 position: static;
 white-space: pre;
 pointer-events: none;
 `),M("input-el",`
 padding: 0;
 height: var(--n-height);
 line-height: var(--n-height);
 `,[O("+",[M("placeholder",`
 display: flex;
 align-items: center; 
 `)])]),ie("textarea",[M("placeholder","white-space: nowrap;")]),M("eye",`
 display: flex;
 align-items: center;
 justify-content: center;
 transition: color .3s var(--n-bezier);
 `),G("textarea","width: 100%;",[E("input-word-count",`
 position: absolute;
 right: var(--n-padding-right);
 bottom: var(--n-padding-vertical);
 `),G("resizable",[E("input-wrapper",`
 resize: vertical;
 min-height: var(--n-height);
 `)]),M("textarea-el, textarea-mirror, placeholder",`
 height: 100%;
 padding-left: 0;
 padding-right: 0;
 padding-top: var(--n-padding-vertical);
 padding-bottom: var(--n-padding-vertical);
 word-break: break-word;
 display: inline-block;
 vertical-align: bottom;
 box-sizing: border-box;
 line-height: var(--n-line-height-textarea);
 margin: 0;
 resize: none;
 white-space: pre-wrap;
 `),M("textarea-mirror",`
 width: 100%;
 pointer-events: none;
 overflow: hidden;
 visibility: hidden;
 position: static;
 white-space: pre-wrap;
 overflow-wrap: break-word;
 `)]),G("pair",[M("input-el, placeholder","text-align: center;"),M("separator",`
 display: flex;
 align-items: center;
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
 white-space: nowrap;
 `,[E("icon",`
 color: var(--n-icon-color);
 `),E("base-icon",`
 color: var(--n-icon-color);
 `)])]),G("disabled",`
 cursor: not-allowed;
 background-color: var(--n-color-disabled);
 `,[M("border","border: var(--n-border-disabled);"),M("input-el, textarea-el",`
 cursor: not-allowed;
 color: var(--n-text-color-disabled);
 text-decoration-color: var(--n-text-color-disabled);
 `),M("placeholder","color: var(--n-placeholder-color-disabled);"),M("separator","color: var(--n-text-color-disabled);",[E("icon",`
 color: var(--n-icon-color-disabled);
 `),E("base-icon",`
 color: var(--n-icon-color-disabled);
 `)]),E("input-word-count",`
 color: var(--n-count-text-color-disabled);
 `),M("suffix, prefix","color: var(--n-text-color-disabled);",[E("icon",`
 color: var(--n-icon-color-disabled);
 `),E("internal-icon",`
 color: var(--n-icon-color-disabled);
 `)])]),ie("disabled",[M("eye",`
 color: var(--n-icon-color);
 cursor: pointer;
 `,[O("&:hover",`
 color: var(--n-icon-color-hover);
 `),O("&:active",`
 color: var(--n-icon-color-pressed);
 `)]),O("&:hover",[M("state-border","border: var(--n-border-hover);")]),G("focus","background-color: var(--n-color-focus);",[M("state-border",`
 border: var(--n-border-focus);
 box-shadow: var(--n-box-shadow-focus);
 `)])]),M("border, state-border",`
 box-sizing: border-box;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 pointer-events: none;
 border-radius: inherit;
 border: var(--n-border);
 transition:
 box-shadow .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `),M("state-border",`
 border-color: #0000;
 z-index: 1;
 `),M("prefix","margin-right: 4px;"),M("suffix",`
 margin-left: 4px;
 `),M("suffix, prefix",`
 transition: color .3s var(--n-bezier);
 flex-wrap: nowrap;
 flex-shrink: 0;
 line-height: var(--n-height);
 white-space: nowrap;
 display: inline-flex;
 align-items: center;
 justify-content: center;
 color: var(--n-suffix-text-color);
 `,[E("base-loading",`
 font-size: var(--n-icon-size);
 margin: 0 2px;
 color: var(--n-loading-color);
 `),E("base-clear",`
 font-size: var(--n-icon-size);
 `,[M("placeholder",[E("base-icon",`
 transition: color .3s var(--n-bezier);
 color: var(--n-icon-color);
 font-size: var(--n-icon-size);
 `)])]),O(">",[E("icon",`
 transition: color .3s var(--n-bezier);
 color: var(--n-icon-color);
 font-size: var(--n-icon-size);
 `)]),E("base-icon",`
 font-size: var(--n-icon-size);
 `)]),E("input-word-count",`
 pointer-events: none;
 line-height: 1.5;
 font-size: .85em;
 color: var(--n-count-text-color);
 transition: color .3s var(--n-bezier);
 margin-left: 4px;
 font-variant: tabular-nums;
 `),["warning","error"].map(e=>G(`${e}-status`,[ie("disabled",[E("base-loading",`
 color: var(--n-loading-color-${e})
 `),M("input-el, textarea-el",`
 caret-color: var(--n-caret-color-${e});
 `),M("state-border",`
 border: var(--n-border-${e});
 `),O("&:hover",[M("state-border",`
 border: var(--n-border-hover-${e});
 `)]),O("&:focus",`
 background-color: var(--n-color-focus-${e});
 `,[M("state-border",`
 box-shadow: var(--n-box-shadow-focus-${e});
 border: var(--n-border-focus-${e});
 `)]),G("focus",`
 background-color: var(--n-color-focus-${e});
 `,[M("state-border",`
 box-shadow: var(--n-box-shadow-focus-${e});
 border: var(--n-border-focus-${e});
 `)])])]))]),Jo=E("input",[G("disabled",[M("input-el, textarea-el",`
 -webkit-text-fill-color: var(--n-text-color-disabled);
 `)])]),Qo=Object.assign(Object.assign({},Ce.props),{bordered:{type:Boolean,default:void 0},type:{type:String,default:"text"},placeholder:[Array,String],defaultValue:{type:[String,Array],default:null},value:[String,Array],disabled:{type:Boolean,default:void 0},size:String,rows:{type:[Number,String],default:3},round:Boolean,minlength:[String,Number],maxlength:[String,Number],clearable:Boolean,autosize:{type:[Boolean,Object],default:!1},pair:Boolean,separator:String,readonly:{type:[String,Boolean],default:!1},passivelyActivated:Boolean,showPasswordOn:String,stateful:{type:Boolean,default:!0},autofocus:Boolean,inputProps:Object,resizable:{type:Boolean,default:!0},showCount:Boolean,loading:{type:Boolean,default:void 0},allowInput:Function,renderCount:Function,onMousedown:Function,onKeydown:Function,onKeyup:Function,onInput:[Function,Array],onFocus:[Function,Array],onBlur:[Function,Array],onClick:[Function,Array],onChange:[Function,Array],onClear:[Function,Array],countGraphemes:Function,status:String,"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],textDecoration:[String,Array],attrSize:{type:Number,default:20},onInputBlur:[Function,Array],onInputFocus:[Function,Array],onDeactivate:[Function,Array],onActivate:[Function,Array],onWrapperFocus:[Function,Array],onWrapperBlur:[Function,Array],internalDeactivateOnEnter:Boolean,internalForceFocus:Boolean,internalLoadingBeforeSuffix:Boolean,showPasswordToggle:Boolean}),li=Z({name:"Input",props:Qo,setup(e){const{mergedClsPrefixRef:n,mergedBorderedRef:t,inlineThemeDisabled:o,mergedRtlRef:l}=$t(e),u=Ce("Input","-input",Ro,Or,e,n);Pr&&Mt("-input-safari",Jo,n);const i=P(null),s=P(null),a=P(null),c=P(null),f=P(null),v=P(null),p=P(null),S=Zo(p),x=P(null),{localeRef:$}=co("Input"),_=P(e.defaultValue),w=ee(e,"value"),B=zt(w,_),N=Tr(e),{mergedSizeRef:g,mergedDisabledRef:m,mergedStatusRef:F}=N,y=P(!1),k=P(!1),z=P(!1),b=P(!1);let H=null;const U=K(()=>{const{placeholder:r,pair:d}=e;return d?Array.isArray(r)?r:r===void 0?["",""]:[r,r]:r===void 0?[$.value.placeholder]:[r]}),X=K(()=>{const{value:r}=z,{value:d}=B,{value:C}=U;return!r&&(Pe(d)||Array.isArray(d)&&Pe(d[0]))&&C[0]}),A=K(()=>{const{value:r}=z,{value:d}=B,{value:C}=U;return!r&&C[1]&&(Pe(d)||Array.isArray(d)&&Pe(d[1]))}),T=ce(()=>e.internalForceFocus||y.value),j=ce(()=>{if(m.value||e.readonly||!e.clearable||!T.value&&!k.value)return!1;const{value:r}=B,{value:d}=T;return e.pair?!!(Array.isArray(r)&&(r[0]||r[1]))&&(k.value||d):!!r&&(k.value||d)}),J=K(()=>{const{showPasswordOn:r}=e;if(r)return r;if(e.showPasswordToggle)return"click"}),Y=P(!1),R=K(()=>{const{textDecoration:r}=e;return r?Array.isArray(r)?r.map(d=>({textDecoration:d})):[{textDecoration:r}]:["",""]}),$e=P(void 0),Ne=()=>{var r,d;if(e.type==="textarea"){const{autosize:C}=e;if(C&&($e.value=(d=(r=x.value)===null||r===void 0?void 0:r.$el)===null||d===void 0?void 0:d.offsetWidth),!s.value||typeof C=="boolean")return;const{paddingTop:L,paddingBottom:D,lineHeight:I}=window.getComputedStyle(s.value),le=Number(L.slice(0,-2)),se=Number(D.slice(0,-2)),de=Number(I.slice(0,-2)),{value:be}=a;if(!be)return;if(C.minRows){const me=Math.max(C.minRows,1),Ve=`${le+se+de*me}px`;be.style.minHeight=Ve}if(C.maxRows){const me=`${le+se+de*C.maxRows}px`;be.style.maxHeight=me}}},Ft=K(()=>{const{maxlength:r}=e;return r===void 0?void 0:Number(r)});Le(()=>{const{value:r}=B;Array.isArray(r)||We(r)});const Lt=yt().proxy;function Se(r){const{onUpdateValue:d,"onUpdate:value":C,onInput:L}=e,{nTriggerFormInput:D}=N;d&&V(d,r),C&&V(C,r),L&&V(L,r),_.value=r,D()}function Ae(r){const{onChange:d}=e,{nTriggerFormChange:C}=N;d&&V(d,r),_.value=r,C()}function Ot(r){const{onBlur:d}=e,{nTriggerFormBlur:C}=N;d&&V(d,r),C()}function Nt(r){const{onFocus:d}=e,{nTriggerFormFocus:C}=N;d&&V(d,r),C()}function Dt(r){const{onClear:d}=e;d&&V(d,r)}function Wt(r){const{onInputBlur:d}=e;d&&V(d,r)}function Vt(r){const{onInputFocus:d}=e;d&&V(d,r)}function Kt(){const{onDeactivate:r}=e;r&&V(r)}function jt(){const{onActivate:r}=e;r&&V(r)}function Ht(r){const{onClick:d}=e;d&&V(d,r)}function Gt(r){const{onWrapperFocus:d}=e;d&&V(d,r)}function Ut(r){const{onWrapperBlur:d}=e;d&&V(d,r)}function Xt(){z.value=!0}function Yt(r){z.value=!1,r.target===v.value?Me(r,1):Me(r,0)}function Me(r,d=0,C="input"){const L=r.target.value;if(We(L),r instanceof InputEvent&&!r.isComposing&&(z.value=!1),e.type==="textarea"){const{value:I}=x;I&&I.syncUnifiedContainer()}if(H=L,z.value)return;S.recordCursor();const D=qt(L);if(D)if(!e.pair)C==="input"?Se(L):Ae(L);else{let{value:I}=B;Array.isArray(I)?I=[I[0],I[1]]:I=["",""],I[d]=L,C==="input"?Se(I):Ae(I)}Lt.$forceUpdate(),D||Re(S.restoreCursor)}function qt(r){const{countGraphemes:d,maxlength:C,minlength:L}=e;if(d){let I;if(C!==void 0&&(I===void 0&&(I=d(r)),I>Number(C))||L!==void 0&&(I===void 0&&(I=d(r)),I<Number(C)))return!1}const{allowInput:D}=e;return typeof D=="function"?D(r):!0}function Zt(r){Wt(r),r.relatedTarget===i.value&&Kt(),r.relatedTarget!==null&&(r.relatedTarget===f.value||r.relatedTarget===v.value||r.relatedTarget===s.value)||(b.value=!1),ze(r,"blur"),p.value=null}function Rt(r,d){Vt(r),y.value=!0,b.value=!0,jt(),ze(r,"focus"),d===0?p.value=f.value:d===1?p.value=v.value:d===2&&(p.value=s.value)}function Jt(r){e.passivelyActivated&&(Ut(r),ze(r,"blur"))}function Qt(r){e.passivelyActivated&&(y.value=!0,Gt(r),ze(r,"focus"))}function ze(r,d){r.relatedTarget!==null&&(r.relatedTarget===f.value||r.relatedTarget===v.value||r.relatedTarget===s.value||r.relatedTarget===i.value)||(d==="focus"?(Nt(r),y.value=!0):d==="blur"&&(Ot(r),y.value=!1))}function en(r,d){Me(r,d,"change")}function tn(r){Ht(r)}function nn(r){Dt(r),e.pair?(Se(["",""]),Ae(["",""])):(Se(""),Ae(""))}function rn(r){const{onMousedown:d}=e;d&&d(r);const{tagName:C}=r.target;if(C!=="INPUT"&&C!=="TEXTAREA"){if(e.resizable){const{value:L}=i;if(L){const{left:D,top:I,width:le,height:se}=L.getBoundingClientRect(),de=14;if(D+le-de<r.clientX&&r.clientX<D+le&&I+se-de<r.clientY&&r.clientY<I+se)return}}r.preventDefault(),y.value||nt()}}function on(){var r;k.value=!0,e.type==="textarea"&&((r=x.value)===null||r===void 0||r.handleMouseEnterWrapper())}function an(){var r;k.value=!1,e.type==="textarea"&&((r=x.value)===null||r===void 0||r.handleMouseLeaveWrapper())}function ln(){m.value||J.value==="click"&&(Y.value=!Y.value)}function sn(r){if(m.value)return;r.preventDefault();const d=L=>{L.preventDefault(),re("mouseup",document,d)};if(ae("mouseup",document,d),J.value!=="mousedown")return;Y.value=!0;const C=()=>{Y.value=!1,re("mouseup",document,C)};ae("mouseup",document,C)}function dn(r){var d;switch((d=e.onKeydown)===null||d===void 0||d.call(e,r),r.key){case"Escape":De();break;case"Enter":un(r);break}}function un(r){var d,C;if(e.passivelyActivated){const{value:L}=b;if(L){e.internalDeactivateOnEnter&&De();return}r.preventDefault(),e.type==="textarea"?(d=s.value)===null||d===void 0||d.focus():(C=f.value)===null||C===void 0||C.focus()}}function De(){e.passivelyActivated&&(b.value=!1,Re(()=>{var r;(r=i.value)===null||r===void 0||r.focus()}))}function nt(){var r,d,C;m.value||(e.passivelyActivated?(r=i.value)===null||r===void 0||r.focus():((d=s.value)===null||d===void 0||d.focus(),(C=f.value)===null||C===void 0||C.focus()))}function cn(){var r;!((r=i.value)===null||r===void 0)&&r.contains(document.activeElement)&&document.activeElement.blur()}function fn(){var r,d;(r=s.value)===null||r===void 0||r.select(),(d=f.value)===null||d===void 0||d.select()}function hn(){m.value||(s.value?s.value.focus():f.value&&f.value.focus())}function pn(){const{value:r}=i;r!=null&&r.contains(document.activeElement)&&r!==document.activeElement&&De()}function vn(r){if(e.type==="textarea"){const{value:d}=s;d==null||d.scrollTo(r)}else{const{value:d}=f;d==null||d.scrollTo(r)}}function We(r){const{type:d,pair:C,autosize:L}=e;if(!C&&L)if(d==="textarea"){const{value:D}=a;D&&(D.textContent=(r??"")+`\r
`)}else{const{value:D}=c;D&&(r?D.textContent=r:D.innerHTML="&nbsp;")}}function gn(){Ne()}const rt=P({top:"0"});function bn(r){var d;const{scrollTop:C}=r.target;rt.value.top=`${-C}px`,(d=x.value)===null||d===void 0||d.syncUnifiedContainer()}let ke=null;Te(()=>{const{autosize:r,type:d}=e;r&&d==="textarea"?ke=ne(B,C=>{!Array.isArray(C)&&C!==H&&We(C)}):ke==null||ke()});let Be=null;Te(()=>{e.type==="textarea"?Be=ne(B,r=>{var d;!Array.isArray(r)&&r!==H&&((d=x.value)===null||d===void 0||d.syncUnifiedContainer())}):Be==null||Be()}),pe(It,{mergedValueRef:B,maxlengthRef:Ft,mergedClsPrefixRef:n,countGraphemesRef:ee(e,"countGraphemes")});const mn={wrapperElRef:i,inputElRef:f,textareaElRef:s,isCompositing:z,focus:nt,blur:cn,select:fn,deactivate:pn,activate:hn,scrollTo:vn},wn=Ir("Input",l,n),ot=K(()=>{const{value:r}=g,{common:{cubicBezierEaseInOut:d},self:{color:C,borderRadius:L,textColor:D,caretColor:I,caretColorError:le,caretColorWarning:se,textDecorationColor:de,border:be,borderDisabled:me,borderHover:Ve,borderFocus:yn,placeholderColor:xn,placeholderColorDisabled:Cn,lineHeightTextarea:$n,colorDisabled:Sn,colorFocus:An,textColorDisabled:Mn,boxShadowFocus:zn,iconSize:kn,colorFocusWarning:Bn,boxShadowFocusWarning:_n,borderWarning:En,borderFocusWarning:Pn,borderHoverWarning:Tn,colorFocusError:In,boxShadowFocusError:Fn,borderError:Ln,borderFocusError:On,borderHoverError:Nn,clearSize:Dn,clearColor:Wn,clearColorHover:Vn,clearColorPressed:Kn,iconColor:jn,iconColorDisabled:Hn,suffixTextColor:Gn,countTextColor:Un,countTextColorDisabled:Xn,iconColorHover:Yn,iconColorPressed:qn,loadingColor:Zn,loadingColorError:Rn,loadingColorWarning:Jn,[Ke("padding",r)]:Qn,[Ke("fontSize",r)]:er,[Ke("height",r)]:tr}}=u.value,{left:nr,right:rr}=Nr(Qn);return{"--n-bezier":d,"--n-count-text-color":Un,"--n-count-text-color-disabled":Xn,"--n-color":C,"--n-font-size":er,"--n-border-radius":L,"--n-height":tr,"--n-padding-left":nr,"--n-padding-right":rr,"--n-text-color":D,"--n-caret-color":I,"--n-text-decoration-color":de,"--n-border":be,"--n-border-disabled":me,"--n-border-hover":Ve,"--n-border-focus":yn,"--n-placeholder-color":xn,"--n-placeholder-color-disabled":Cn,"--n-icon-size":kn,"--n-line-height-textarea":$n,"--n-color-disabled":Sn,"--n-color-focus":An,"--n-text-color-disabled":Mn,"--n-box-shadow-focus":zn,"--n-loading-color":Zn,"--n-caret-color-warning":se,"--n-color-focus-warning":Bn,"--n-box-shadow-focus-warning":_n,"--n-border-warning":En,"--n-border-focus-warning":Pn,"--n-border-hover-warning":Tn,"--n-loading-color-warning":Jn,"--n-caret-color-error":le,"--n-color-focus-error":In,"--n-box-shadow-focus-error":Fn,"--n-border-error":Ln,"--n-border-focus-error":On,"--n-border-hover-error":Nn,"--n-loading-color-error":Rn,"--n-clear-color":Wn,"--n-clear-size":Dn,"--n-clear-color-hover":Vn,"--n-clear-color-pressed":Kn,"--n-icon-color":jn,"--n-icon-color-hover":Yn,"--n-icon-color-pressed":qn,"--n-icon-color-disabled":Hn,"--n-suffix-text-color":Gn}}),fe=o?St("input",K(()=>{const{value:r}=g;return r[0]}),ot,e):void 0;return Object.assign(Object.assign({},mn),{wrapperElRef:i,inputElRef:f,inputMirrorElRef:c,inputEl2Ref:v,textareaElRef:s,textareaMirrorElRef:a,textareaScrollbarInstRef:x,rtlEnabled:wn,uncontrolledValue:_,mergedValue:B,passwordVisible:Y,mergedPlaceholder:U,showPlaceholder1:X,showPlaceholder2:A,mergedFocus:T,isComposing:z,activated:b,showClearButton:j,mergedSize:g,mergedDisabled:m,textDecorationStyle:R,mergedClsPrefix:n,mergedBordered:t,mergedShowPasswordOn:J,placeholderStyle:rt,mergedStatus:F,textAreaScrollContainerWidth:$e,handleTextAreaScroll:bn,handleCompositionStart:Xt,handleCompositionEnd:Yt,handleInput:Me,handleInputBlur:Zt,handleInputFocus:Rt,handleWrapperBlur:Jt,handleWrapperFocus:Qt,handleMouseEnter:on,handleMouseLeave:an,handleMouseDown:rn,handleChange:en,handleClick:tn,handleClear:nn,handlePasswordToggleClick:ln,handlePasswordToggleMousedown:sn,handleWrapperKeydown:dn,handleTextAreaMirrorResize:gn,getTextareaScrollContainer:()=>s.value,mergedTheme:u,cssVars:o?void 0:ot,themeClass:fe==null?void 0:fe.themeClass,onRender:fe==null?void 0:fe.onRender})},render(){var e,n;const{mergedClsPrefix:t,mergedStatus:o,themeClass:l,type:u,countGraphemes:i,onRender:s}=this,a=this.$slots;return s==null||s(),h("div",{ref:"wrapperElRef",class:[`${t}-input`,l,o&&`${t}-input--${o}-status`,{[`${t}-input--rtl`]:this.rtlEnabled,[`${t}-input--disabled`]:this.mergedDisabled,[`${t}-input--textarea`]:u==="textarea",[`${t}-input--resizable`]:this.resizable&&!this.autosize,[`${t}-input--autosize`]:this.autosize,[`${t}-input--round`]:this.round&&u!=="textarea",[`${t}-input--pair`]:this.pair,[`${t}-input--focus`]:this.mergedFocus,[`${t}-input--stateful`]:this.stateful}],style:this.cssVars,tabindex:!this.mergedDisabled&&this.passivelyActivated&&!this.activated?0:void 0,onFocus:this.handleWrapperFocus,onBlur:this.handleWrapperBlur,onClick:this.handleClick,onMousedown:this.handleMouseDown,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onCompositionstart:this.handleCompositionStart,onCompositionend:this.handleCompositionEnd,onKeyup:this.onKeyup,onKeydown:this.handleWrapperKeydown},h("div",{class:`${t}-input-wrapper`},ue(a.prefix,c=>c&&h("div",{class:`${t}-input__prefix`},c)),u==="textarea"?h(Fr,{ref:"textareaScrollbarInstRef",class:`${t}-input__textarea`,container:this.getTextareaScrollContainer,triggerDisplayManually:!0,useUnifiedContainer:!0,internalHoistYRail:!0},{default:()=>{var c,f;const{textAreaScrollContainerWidth:v}=this,p={width:this.autosize&&v&&`${v}px`};return h(At,null,h("textarea",Object.assign({},this.inputProps,{ref:"textareaElRef",class:[`${t}-input__textarea-el`,(c=this.inputProps)===null||c===void 0?void 0:c.class],autofocus:this.autofocus,rows:Number(this.rows),placeholder:this.placeholder,value:this.mergedValue,disabled:this.mergedDisabled,maxlength:i?void 0:this.maxlength,minlength:i?void 0:this.minlength,readonly:this.readonly,tabindex:this.passivelyActivated&&!this.activated?-1:void 0,style:[this.textDecorationStyle[0],(f=this.inputProps)===null||f===void 0?void 0:f.style,p],onBlur:this.handleInputBlur,onFocus:S=>{this.handleInputFocus(S,2)},onInput:this.handleInput,onChange:this.handleChange,onScroll:this.handleTextAreaScroll})),this.showPlaceholder1?h("div",{class:`${t}-input__placeholder`,style:[this.placeholderStyle,p],key:"placeholder"},this.mergedPlaceholder[0]):null,this.autosize?h(Lr,{onResize:this.handleTextAreaMirrorResize},{default:()=>h("div",{ref:"textareaMirrorElRef",class:`${t}-input__textarea-mirror`,key:"mirror"})}):null)}}):h("div",{class:`${t}-input__input`},h("input",Object.assign({type:u==="password"&&this.mergedShowPasswordOn&&this.passwordVisible?"text":u},this.inputProps,{ref:"inputElRef",class:[`${t}-input__input-el`,(e=this.inputProps)===null||e===void 0?void 0:e.class],style:[this.textDecorationStyle[0],(n=this.inputProps)===null||n===void 0?void 0:n.style],tabindex:this.passivelyActivated&&!this.activated?-1:void 0,placeholder:this.mergedPlaceholder[0],disabled:this.mergedDisabled,maxlength:i?void 0:this.maxlength,minlength:i?void 0:this.minlength,value:Array.isArray(this.mergedValue)?this.mergedValue[0]:this.mergedValue,readonly:this.readonly,autofocus:this.autofocus,size:this.attrSize,onBlur:this.handleInputBlur,onFocus:c=>{this.handleInputFocus(c,0)},onInput:c=>{this.handleInput(c,0)},onChange:c=>{this.handleChange(c,0)}})),this.showPlaceholder1?h("div",{class:`${t}-input__placeholder`},h("span",null,this.mergedPlaceholder[0])):null,this.autosize?h("div",{class:`${t}-input__input-mirror`,key:"mirror",ref:"inputMirrorElRef"}," "):null),!this.pair&&ue(a.suffix,c=>c||this.clearable||this.showCount||this.mergedShowPasswordOn||this.loading!==void 0?h("div",{class:`${t}-input__suffix`},[ue(a["clear-icon-placeholder"],f=>(this.clearable||f)&&h(Qe,{clsPrefix:t,show:this.showClearButton,onClear:this.handleClear},{placeholder:()=>f,icon:()=>{var v,p;return(p=(v=this.$slots)["clear-icon"])===null||p===void 0?void 0:p.call(v)}})),this.internalLoadingBeforeSuffix?null:c,this.loading!==void 0?h(Yo,{clsPrefix:t,loading:this.loading,showArrow:!1,showClear:!1,style:this.cssVars}):null,this.internalLoadingBeforeSuffix?c:null,this.showCount&&this.type!=="textarea"?h(gt,null,{default:f=>{var v;return(v=a.count)===null||v===void 0?void 0:v.call(a,f)}}):null,this.mergedShowPasswordOn&&this.type==="password"?h("div",{class:`${t}-input__eye`,onMousedown:this.handlePasswordToggleMousedown,onClick:this.handlePasswordToggleClick},this.passwordVisible?we(a["password-visible-icon"],()=>[h(Ie,{clsPrefix:t},{default:()=>h(fo,null)})]):we(a["password-invisible-icon"],()=>[h(Ie,{clsPrefix:t},{default:()=>h(ho,null)})])):null]):null)),this.pair?h("span",{class:`${t}-input__separator`},we(a.separator,()=>[this.separator])):null,this.pair?h("div",{class:`${t}-input-wrapper`},h("div",{class:`${t}-input__input`},h("input",{ref:"inputEl2Ref",type:this.type,class:`${t}-input__input-el`,tabindex:this.passivelyActivated&&!this.activated?-1:void 0,placeholder:this.mergedPlaceholder[1],disabled:this.mergedDisabled,maxlength:i?void 0:this.maxlength,minlength:i?void 0:this.minlength,value:Array.isArray(this.mergedValue)?this.mergedValue[1]:void 0,readonly:this.readonly,style:this.textDecorationStyle[1],onBlur:this.handleInputBlur,onFocus:c=>{this.handleInputFocus(c,1)},onInput:c=>{this.handleInput(c,1)},onChange:c=>{this.handleChange(c,1)}}),this.showPlaceholder2?h("div",{class:`${t}-input__placeholder`},h("span",null,this.mergedPlaceholder[1])):null),ue(a.suffix,c=>(this.clearable||c)&&h("div",{class:`${t}-input__suffix`},[this.clearable&&h(Qe,{clsPrefix:t,show:this.showClearButton,onClear:this.handleClear},{icon:()=>{var f;return(f=a["clear-icon"])===null||f===void 0?void 0:f.call(a)},placeholder:()=>{var f;return(f=a["clear-icon-placeholder"])===null||f===void 0?void 0:f.call(a)}}),c]))):null,this.mergedBordered?h("div",{class:`${t}-input__border`}):null,this.mergedBordered?h("div",{class:`${t}-input__state-border`}):null,this.showCount&&u==="textarea"?h(gt,null,{default:c=>{var f;const{renderCount:v}=this;return v?v(c):(f=a.count)===null||f===void 0?void 0:f.call(a,c)}}):null)}});export{po as C,ai as N,qr as V,li as _,co as a,ge as b,Zr as c,lo as d,ii as e,je as f,Hr as g,so as h,ni as i,Oo as j,oi as k,Qr as l,_e as m,ri as n,jr as o,Go as p,Gr as q,Wo as r,Yo as s,zt as u};
