import{r as F,i as de,aT as Re,dT as Ce,dU as Ie,p as Ke,H as W,dV as _e,G,d as K,h as a,b as N,e as P,g as D,n as Z,u as fe,dW as $e,k as w,q as he,l as Oe,Q as ve,v as ae,I as le,J as B,cf as be,W as H,a7 as X,_ as ze,$ as De,a0 as Te,T as je,a5 as J,a6 as Ae,cg as Fe,ce as Be,dX as Me,bv as Le,f as Ee,ac as ce,R as z,j as He,t as I,a3 as re,aa as Ue,bD as Ve,dJ as me,dY as qe,X as A}from"./index-14be5eda.js";import{h as ue,c as We}from"./Input-3ed2969b.js";function Ge(e){return n=>{n?e.value=n.$el:e.value=null}}function Xe(e,n,i){if(!n)return e;const r=F(e.value);let t=null;return de(e,o=>{t!==null&&window.clearTimeout(t),o===!0?i&&!i.value?r.value=!0:t=window.setTimeout(()=>{r.value=!0},n):r.value=!1}),r}function Je(e={},n){const i=Re({ctrl:!1,command:!1,win:!1,shift:!1,tab:!1}),{keydown:r,keyup:t}=e,o=d=>{switch(d.key){case"Control":i.ctrl=!0;break;case"Meta":i.command=!0,i.win=!0;break;case"Shift":i.shift=!0;break;case"Tab":i.tab=!0;break}r!==void 0&&Object.keys(r).forEach(y=>{if(y!==d.key)return;const h=r[y];if(typeof h=="function")h(d);else{const{stop:g=!1,prevent:S=!1}=h;g&&d.stopPropagation(),S&&d.preventDefault(),h.handler(d)}})},s=d=>{switch(d.key){case"Control":i.ctrl=!1;break;case"Meta":i.command=!1,i.win=!1;break;case"Shift":i.shift=!1;break;case"Tab":i.tab=!1;break}t!==void 0&&Object.keys(t).forEach(y=>{if(y!==d.key)return;const h=t[y];if(typeof h=="function")h(d);else{const{stop:g=!1,prevent:S=!1}=h;g&&d.stopPropagation(),S&&d.preventDefault(),h.handler(d)}})},u=()=>{(n===void 0||n.value)&&(G("keydown",document,o),G("keyup",document,s)),n!==void 0&&de(n,d=>{d?(G("keydown",document,o),G("keyup",document,s)):(W("keydown",document,o),W("keyup",document,s))})};return Ce()?(Ie(u),Ke(()=>{(n===void 0||n.value)&&(W("keydown",document,o),W("keyup",document,s))})):u(),_e(i)}const Ze=K({name:"ChevronRight",render(){return a("svg",{viewBox:"0 0 16 16",fill:"none",xmlns:"http://www.w3.org/2000/svg"},a("path",{d:"M5.64645 3.14645C5.45118 3.34171 5.45118 3.65829 5.64645 3.85355L9.79289 8L5.64645 12.1464C5.45118 12.3417 5.45118 12.6583 5.64645 12.8536C5.84171 13.0488 6.15829 13.0488 6.35355 12.8536L10.8536 8.35355C11.0488 8.15829 11.0488 7.84171 10.8536 7.64645L6.35355 3.14645C6.15829 2.95118 5.84171 2.95118 5.64645 3.14645Z",fill:"currentColor"}))}}),we=K({name:"DropdownDivider",props:{clsPrefix:{type:String,required:!0}},render(){return a("div",{class:`${this.clsPrefix}-dropdown-divider`})}}),Qe=N("icon",`
 height: 1em;
 width: 1em;
 line-height: 1em;
 text-align: center;
 display: inline-block;
 position: relative;
 fill: currentColor;
 transform: translateZ(0);
`,[P("color-transition",{transition:"color .3s var(--n-bezier)"}),P("depth",{color:"var(--n-color)"},[D("svg",{opacity:"var(--n-opacity)",transition:"opacity .3s var(--n-bezier)"})]),D("svg",{height:"1em",width:"1em"})]),Ye=Object.assign(Object.assign({},Z.props),{depth:[String,Number],size:[Number,String],color:String,component:Object}),eo=K({_n_icon__:!0,name:"Icon",inheritAttrs:!1,props:Ye,setup(e){const{mergedClsPrefixRef:n,inlineThemeDisabled:i}=fe(e),r=Z("Icon","-icon",Qe,$e,e,n),t=w(()=>{const{depth:s}=e,{common:{cubicBezierEaseInOut:u},self:d}=r.value;if(s!==void 0){const{color:y,[`opacity${s}Depth`]:h}=d;return{"--n-bezier":u,"--n-color":y,"--n-opacity":h}}return{"--n-bezier":u,"--n-color":"","--n-opacity":""}}),o=i?he("icon",w(()=>`${e.depth||"d"}`),t,e):void 0;return{mergedClsPrefix:n,mergedStyle:w(()=>{const{size:s,color:u}=e;return{fontSize:Oe(s),color:u}}),cssVars:i?void 0:t,themeClass:o==null?void 0:o.themeClass,onRender:o==null?void 0:o.onRender}},render(){var e;const{$parent:n,depth:i,mergedClsPrefix:r,component:t,onRender:o,themeClass:s}=this;return!((e=n==null?void 0:n.$options)===null||e===void 0)&&e._n_icon__&&ve("icon","don't wrap `n-icon` inside `n-icon`"),o==null||o(),a("i",ae(this.$attrs,{role:"img",class:[`${r}-icon`,s,{[`${r}-icon--depth`]:i,[`${r}-icon--color-transition`]:i!==void 0}],style:[this.cssVars,this.mergedStyle]}),t?a(t):this.$slots)}}),se=le("n-dropdown-menu"),Q=le("n-dropdown"),pe=le("n-dropdown-option");function ie(e,n){return e.type==="submenu"||e.type===void 0&&e[n]!==void 0}function oo(e){return e.type==="group"}function ye(e){return e.type==="divider"}function no(e){return e.type==="render"}const ge=K({name:"DropdownOption",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0},parentKey:{type:[String,Number],default:null},placement:{type:String,default:"right-start"},props:Object,scrollable:Boolean},setup(e){const n=B(Q),{hoverKeyRef:i,keyboardKeyRef:r,lastToggledSubmenuKeyRef:t,pendingKeyPathRef:o,activeKeyPathRef:s,animatedRef:u,mergedShowRef:d,renderLabelRef:y,renderIconRef:h,labelFieldRef:g,childrenFieldRef:S,renderOptionRef:R,nodePropsRef:_,menuPropsRef:M}=n,x=B(pe,null),$=B(se),U=B(be),Y=w(()=>e.tmNode.rawNode),V=w(()=>{const{value:c}=S;return ie(e.tmNode.rawNode,c)}),ee=w(()=>{const{disabled:c}=e.tmNode;return c}),oe=w(()=>{if(!V.value)return!1;const{key:c,disabled:m}=e.tmNode;if(m)return!1;const{value:C}=i,{value:T}=r,{value:te}=t,{value:j}=o;return C!==null?j.includes(c):T!==null?j.includes(c)&&j[j.length-1]!==c:te!==null?j.includes(c):!1}),ne=w(()=>r.value===null&&!u.value),q=Xe(oe,300,ne),L=w(()=>!!(x!=null&&x.enteringSubmenuRef.value)),E=F(!1);H(pe,{enteringSubmenuRef:E});function O(){E.value=!0}function l(){E.value=!1}function b(){const{parentKey:c,tmNode:m}=e;m.disabled||d.value&&(t.value=c,r.value=null,i.value=m.key)}function f(){const{tmNode:c}=e;c.disabled||d.value&&i.value!==c.key&&b()}function p(c){if(e.tmNode.disabled||!d.value)return;const{relatedTarget:m}=c;m&&!ue({target:m},"dropdownOption")&&!ue({target:m},"scrollbarRail")&&(i.value=null)}function k(){const{value:c}=V,{tmNode:m}=e;d.value&&!c&&!m.disabled&&(n.doSelect(m.key,m.rawNode),n.doUpdateShow(!1))}return{labelField:g,renderLabel:y,renderIcon:h,siblingHasIcon:$.showIconRef,siblingHasSubmenu:$.hasSubmenuRef,menuProps:M,popoverBody:U,animated:u,mergedShowSubmenu:w(()=>q.value&&!L.value),rawNode:Y,hasSubmenu:V,pending:X(()=>{const{value:c}=o,{key:m}=e.tmNode;return c.includes(m)}),childActive:X(()=>{const{value:c}=s,{key:m}=e.tmNode,C=c.findIndex(T=>m===T);return C===-1?!1:C<c.length-1}),active:X(()=>{const{value:c}=s,{key:m}=e.tmNode,C=c.findIndex(T=>m===T);return C===-1?!1:C===c.length-1}),mergedDisabled:ee,renderOption:R,nodeProps:_,handleClick:k,handleMouseMove:f,handleMouseEnter:b,handleMouseLeave:p,handleSubmenuBeforeEnter:O,handleSubmenuAfterEnter:l}},render(){var e,n;const{animated:i,rawNode:r,mergedShowSubmenu:t,clsPrefix:o,siblingHasIcon:s,siblingHasSubmenu:u,renderLabel:d,renderIcon:y,renderOption:h,nodeProps:g,props:S,scrollable:R}=this;let _=null;if(t){const U=(e=this.menuProps)===null||e===void 0?void 0:e.call(this,r,r.children);_=a(xe,Object.assign({},U,{clsPrefix:o,scrollable:this.scrollable,tmNodes:this.tmNode.children,parentKey:this.tmNode.key}))}const M={class:[`${o}-dropdown-option-body`,this.pending&&`${o}-dropdown-option-body--pending`,this.active&&`${o}-dropdown-option-body--active`,this.childActive&&`${o}-dropdown-option-body--child-active`,this.mergedDisabled&&`${o}-dropdown-option-body--disabled`],onMousemove:this.handleMouseMove,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onClick:this.handleClick},x=g==null?void 0:g(r),$=a("div",Object.assign({class:[`${o}-dropdown-option`,x==null?void 0:x.class],"data-dropdown-option":!0},x),a("div",ae(M,S),[a("div",{class:[`${o}-dropdown-option-body__prefix`,s&&`${o}-dropdown-option-body__prefix--show-icon`]},[y?y(r):J(r.icon)]),a("div",{"data-dropdown-option":!0,class:`${o}-dropdown-option-body__label`},d?d(r):J((n=r[this.labelField])!==null&&n!==void 0?n:r.title)),a("div",{"data-dropdown-option":!0,class:[`${o}-dropdown-option-body__suffix`,u&&`${o}-dropdown-option-body__suffix--has-submenu`]},this.hasSubmenu?a(eo,null,{default:()=>a(Ze,null)}):null)]),this.hasSubmenu?a(ze,null,{default:()=>[a(De,null,{default:()=>a("div",{class:`${o}-dropdown-offset-container`},a(Te,{show:this.mergedShowSubmenu,placement:this.placement,to:R&&this.popoverBody||void 0,teleportDisabled:!R},{default:()=>a("div",{class:`${o}-dropdown-menu-wrapper`},i?a(je,{onBeforeEnter:this.handleSubmenuBeforeEnter,onAfterEnter:this.handleSubmenuAfterEnter,name:"fade-in-scale-up-transition",appear:!0},{default:()=>_}):_)}))})]}):null);return h?h({node:$,option:r}):$}}),to=K({name:"DropdownGroupHeader",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0}},setup(){const{showIconRef:e,hasSubmenuRef:n}=B(se),{renderLabelRef:i,labelFieldRef:r,nodePropsRef:t,renderOptionRef:o}=B(Q);return{labelField:r,showIcon:e,hasSubmenu:n,renderLabel:i,nodeProps:t,renderOption:o}},render(){var e;const{clsPrefix:n,hasSubmenu:i,showIcon:r,nodeProps:t,renderLabel:o,renderOption:s}=this,{rawNode:u}=this.tmNode,d=a("div",Object.assign({class:`${n}-dropdown-option`},t==null?void 0:t(u)),a("div",{class:`${n}-dropdown-option-body ${n}-dropdown-option-body--group`},a("div",{"data-dropdown-option":!0,class:[`${n}-dropdown-option-body__prefix`,r&&`${n}-dropdown-option-body__prefix--show-icon`]},J(u.icon)),a("div",{class:`${n}-dropdown-option-body__label`,"data-dropdown-option":!0},o?o(u):J((e=u.title)!==null&&e!==void 0?e:u[this.labelField])),a("div",{class:[`${n}-dropdown-option-body__suffix`,i&&`${n}-dropdown-option-body__suffix--has-submenu`],"data-dropdown-option":!0})));return s?s({node:d,option:u}):d}}),ro=K({name:"NDropdownGroup",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0},parentKey:{type:[String,Number],default:null}},render(){const{tmNode:e,parentKey:n,clsPrefix:i}=this,{children:r}=e;return a(Ae,null,a(to,{clsPrefix:i,tmNode:e,key:e.key}),r==null?void 0:r.map(t=>{const{rawNode:o}=t;return o.show===!1?null:ye(o)?a(we,{clsPrefix:i,key:t.key}):t.isGroup?(ve("dropdown","`group` node is not allowed to be put in `group` node."),null):a(ge,{clsPrefix:i,tmNode:t,parentKey:n,key:t.key})}))}}),io=K({name:"DropdownRenderOption",props:{tmNode:{type:Object,required:!0}},render(){const{rawNode:{render:e,props:n}}=this.tmNode;return a("div",n,[e==null?void 0:e()])}}),xe=K({name:"DropdownMenu",props:{scrollable:Boolean,showArrow:Boolean,arrowStyle:[String,Object],clsPrefix:{type:String,required:!0},tmNodes:{type:Array,default:()=>[]},parentKey:{type:[String,Number],default:null}},setup(e){const{renderIconRef:n,childrenFieldRef:i}=B(Q);H(se,{showIconRef:w(()=>{const t=n.value;return e.tmNodes.some(o=>{var s;if(o.isGroup)return(s=o.children)===null||s===void 0?void 0:s.some(({rawNode:d})=>t?t(d):d.icon);const{rawNode:u}=o;return t?t(u):u.icon})}),hasSubmenuRef:w(()=>{const{value:t}=i;return e.tmNodes.some(o=>{var s;if(o.isGroup)return(s=o.children)===null||s===void 0?void 0:s.some(({rawNode:d})=>ie(d,t));const{rawNode:u}=o;return ie(u,t)})})});const r=F(null);return H(Fe,null),H(Be,null),H(be,r),{bodyRef:r}},render(){const{parentKey:e,clsPrefix:n,scrollable:i}=this,r=this.tmNodes.map(t=>{const{rawNode:o}=t;return o.show===!1?null:no(o)?a(io,{tmNode:t,key:t.key}):ye(o)?a(we,{clsPrefix:n,key:t.key}):oo(o)?a(ro,{clsPrefix:n,tmNode:t,parentKey:e,key:t.key}):a(ge,{clsPrefix:n,tmNode:t,parentKey:e,key:t.key,props:o.props,scrollable:i})});return a("div",{class:[`${n}-dropdown-menu`,i&&`${n}-dropdown-menu--scrollable`],ref:"bodyRef"},i?a(Le,{contentClass:`${n}-dropdown-menu__content`},{default:()=>r}):r,this.showArrow?Me({clsPrefix:n,arrowStyle:this.arrowStyle}):null)}}),ao=N("dropdown-menu",`
 transform-origin: var(--v-transform-origin);
 background-color: var(--n-color);
 border-radius: var(--n-border-radius);
 box-shadow: var(--n-box-shadow);
 position: relative;
 transition:
 background-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
`,[Ee(),N("dropdown-option",`
 position: relative;
 `,[D("a",`
 text-decoration: none;
 color: inherit;
 outline: none;
 `,[D("&::before",`
 content: "";
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 `)]),N("dropdown-option-body",`
 display: flex;
 cursor: pointer;
 position: relative;
 height: var(--n-option-height);
 line-height: var(--n-option-height);
 font-size: var(--n-font-size);
 color: var(--n-option-text-color);
 transition: color .3s var(--n-bezier);
 `,[D("&::before",`
 content: "";
 position: absolute;
 top: 0;
 bottom: 0;
 left: 4px;
 right: 4px;
 transition: background-color .3s var(--n-bezier);
 border-radius: var(--n-border-radius);
 `),ce("disabled",[P("pending",`
 color: var(--n-option-text-color-hover);
 `,[z("prefix, suffix",`
 color: var(--n-option-text-color-hover);
 `),D("&::before","background-color: var(--n-option-color-hover);")]),P("active",`
 color: var(--n-option-text-color-active);
 `,[z("prefix, suffix",`
 color: var(--n-option-text-color-active);
 `),D("&::before","background-color: var(--n-option-color-active);")]),P("child-active",`
 color: var(--n-option-text-color-child-active);
 `,[z("prefix, suffix",`
 color: var(--n-option-text-color-child-active);
 `)])]),P("disabled",`
 cursor: not-allowed;
 opacity: var(--n-option-opacity-disabled);
 `),P("group",`
 font-size: calc(var(--n-font-size) - 1px);
 color: var(--n-group-header-text-color);
 `,[z("prefix",`
 width: calc(var(--n-option-prefix-width) / 2);
 `,[P("show-icon",`
 width: calc(var(--n-option-icon-prefix-width) / 2);
 `)])]),z("prefix",`
 width: var(--n-option-prefix-width);
 display: flex;
 justify-content: center;
 align-items: center;
 color: var(--n-prefix-color);
 transition: color .3s var(--n-bezier);
 z-index: 1;
 `,[P("show-icon",`
 width: var(--n-option-icon-prefix-width);
 `),N("icon",`
 font-size: var(--n-option-icon-size);
 `)]),z("label",`
 white-space: nowrap;
 flex: 1;
 z-index: 1;
 `),z("suffix",`
 box-sizing: border-box;
 flex-grow: 0;
 flex-shrink: 0;
 display: flex;
 justify-content: flex-end;
 align-items: center;
 min-width: var(--n-option-suffix-width);
 padding: 0 8px;
 transition: color .3s var(--n-bezier);
 color: var(--n-suffix-color);
 z-index: 1;
 `,[P("has-submenu",`
 width: var(--n-option-icon-suffix-width);
 `),N("icon",`
 font-size: var(--n-option-icon-size);
 `)]),N("dropdown-menu","pointer-events: all;")]),N("dropdown-offset-container",`
 pointer-events: none;
 position: absolute;
 left: 0;
 right: 0;
 top: -4px;
 bottom: -4px;
 `)]),N("dropdown-divider",`
 transition: background-color .3s var(--n-bezier);
 background-color: var(--n-divider-color);
 height: 1px;
 margin: 4px 0;
 `),N("dropdown-menu-wrapper",`
 transform-origin: var(--v-transform-origin);
 width: fit-content;
 `),D(">",[N("scrollbar",`
 height: inherit;
 max-height: inherit;
 `)]),ce("scrollable",`
 padding: var(--n-padding);
 `),P("scrollable",[z("content",`
 padding: var(--n-padding);
 `)])]),lo={animated:{type:Boolean,default:!0},keyboard:{type:Boolean,default:!0},size:{type:String,default:"medium"},inverted:Boolean,placement:{type:String,default:"bottom"},onSelect:[Function,Array],options:{type:Array,default:()=>[]},menuProps:Function,showArrow:Boolean,renderLabel:Function,renderIcon:Function,renderOption:Function,nodeProps:Function,labelField:{type:String,default:"label"},keyField:{type:String,default:"key"},childrenField:{type:String,default:"children"},value:[String,Number]},so=Object.keys(me),co=Object.assign(Object.assign(Object.assign({},me),lo),Z.props),fo=K({name:"Dropdown",inheritAttrs:!1,props:co,setup(e){const n=F(!1),i=He(I(e,"show"),n),r=w(()=>{const{keyField:l,childrenField:b}=e;return We(e.options,{getKey(f){return f[l]},getDisabled(f){return f.disabled===!0},getIgnored(f){return f.type==="divider"||f.type==="render"},getChildren(f){return f[b]}})}),t=w(()=>r.value.treeNodes),o=F(null),s=F(null),u=F(null),d=w(()=>{var l,b,f;return(f=(b=(l=o.value)!==null&&l!==void 0?l:s.value)!==null&&b!==void 0?b:u.value)!==null&&f!==void 0?f:null}),y=w(()=>r.value.getPath(d.value).keyPath),h=w(()=>r.value.getPath(e.value).keyPath),g=X(()=>e.keyboard&&i.value);Je({keydown:{ArrowUp:{prevent:!0,handler:ee},ArrowRight:{prevent:!0,handler:V},ArrowDown:{prevent:!0,handler:oe},ArrowLeft:{prevent:!0,handler:Y},Enter:{prevent:!0,handler:ne},Escape:U}},g);const{mergedClsPrefixRef:S,inlineThemeDisabled:R}=fe(e),_=Z("Dropdown","-dropdown",ao,qe,e,S);H(Q,{labelFieldRef:I(e,"labelField"),childrenFieldRef:I(e,"childrenField"),renderLabelRef:I(e,"renderLabel"),renderIconRef:I(e,"renderIcon"),hoverKeyRef:o,keyboardKeyRef:s,lastToggledSubmenuKeyRef:u,pendingKeyPathRef:y,activeKeyPathRef:h,animatedRef:I(e,"animated"),mergedShowRef:i,nodePropsRef:I(e,"nodeProps"),renderOptionRef:I(e,"renderOption"),menuPropsRef:I(e,"menuProps"),doSelect:M,doUpdateShow:x}),de(i,l=>{!e.animated&&!l&&$()});function M(l,b){const{onSelect:f}=e;f&&re(f,l,b)}function x(l){const{"onUpdate:show":b,onUpdateShow:f}=e;b&&re(b,l),f&&re(f,l),n.value=l}function $(){o.value=null,s.value=null,u.value=null}function U(){x(!1)}function Y(){L("left")}function V(){L("right")}function ee(){L("up")}function oe(){L("down")}function ne(){const l=q();l!=null&&l.isLeaf&&i.value&&(M(l.key,l.rawNode),x(!1))}function q(){var l;const{value:b}=r,{value:f}=d;return!b||f===null?null:(l=b.getNode(f))!==null&&l!==void 0?l:null}function L(l){const{value:b}=d,{value:{getFirstAvailableNode:f}}=r;let p=null;if(b===null){const k=f();k!==null&&(p=k.key)}else{const k=q();if(k){let c;switch(l){case"down":c=k.getNext();break;case"up":c=k.getPrev();break;case"right":c=k.getChild();break;case"left":c=k.getParent();break}c&&(p=c.key)}}p!==null&&(o.value=null,s.value=p)}const E=w(()=>{const{size:l,inverted:b}=e,{common:{cubicBezierEaseInOut:f},self:p}=_.value,{padding:k,dividerColor:c,borderRadius:m,optionOpacityDisabled:C,[A("optionIconSuffixWidth",l)]:T,[A("optionSuffixWidth",l)]:te,[A("optionIconPrefixWidth",l)]:j,[A("optionPrefixWidth",l)]:Se,[A("fontSize",l)]:ke,[A("optionHeight",l)]:Ne,[A("optionIconSize",l)]:Pe}=p,v={"--n-bezier":f,"--n-font-size":ke,"--n-padding":k,"--n-border-radius":m,"--n-option-height":Ne,"--n-option-prefix-width":Se,"--n-option-icon-prefix-width":j,"--n-option-suffix-width":te,"--n-option-icon-suffix-width":T,"--n-option-icon-size":Pe,"--n-divider-color":c,"--n-option-opacity-disabled":C};return b?(v["--n-color"]=p.colorInverted,v["--n-option-color-hover"]=p.optionColorHoverInverted,v["--n-option-color-active"]=p.optionColorActiveInverted,v["--n-option-text-color"]=p.optionTextColorInverted,v["--n-option-text-color-hover"]=p.optionTextColorHoverInverted,v["--n-option-text-color-active"]=p.optionTextColorActiveInverted,v["--n-option-text-color-child-active"]=p.optionTextColorChildActiveInverted,v["--n-prefix-color"]=p.prefixColorInverted,v["--n-suffix-color"]=p.suffixColorInverted,v["--n-group-header-text-color"]=p.groupHeaderTextColorInverted):(v["--n-color"]=p.color,v["--n-option-color-hover"]=p.optionColorHover,v["--n-option-color-active"]=p.optionColorActive,v["--n-option-text-color"]=p.optionTextColor,v["--n-option-text-color-hover"]=p.optionTextColorHover,v["--n-option-text-color-active"]=p.optionTextColorActive,v["--n-option-text-color-child-active"]=p.optionTextColorChildActive,v["--n-prefix-color"]=p.prefixColor,v["--n-suffix-color"]=p.suffixColor,v["--n-group-header-text-color"]=p.groupHeaderTextColor),v}),O=R?he("dropdown",w(()=>`${e.size[0]}${e.inverted?"i":""}`),E,e):void 0;return{mergedClsPrefix:S,mergedTheme:_,tmNodes:t,mergedShow:i,handleAfterLeave:()=>{e.animated&&$()},doUpdateShow:x,cssVars:R?void 0:E,themeClass:O==null?void 0:O.themeClass,onRender:O==null?void 0:O.onRender}},render(){const e=(r,t,o,s,u)=>{var d;const{mergedClsPrefix:y,menuProps:h}=this;(d=this.onRender)===null||d===void 0||d.call(this);const g=(h==null?void 0:h(void 0,this.tmNodes.map(R=>R.rawNode)))||{},S={ref:Ge(t),class:[r,`${y}-dropdown`,this.themeClass],clsPrefix:y,tmNodes:this.tmNodes,style:[o,this.cssVars],showArrow:this.showArrow,arrowStyle:this.arrowStyle,scrollable:this.scrollable,onMouseenter:s,onMouseleave:u};return a(xe,ae(this.$attrs,S,g))},{mergedTheme:n}=this,i={show:this.mergedShow,theme:n.peers.Popover,themeOverrides:n.peerOverrides.Popover,internalOnAfterLeave:this.handleAfterLeave,internalRenderBody:e,onUpdateShow:this.doUpdateShow,"onUpdate:show":void 0};return a(Ve,Object.assign({},Ue(this.$props,so),i),{trigger:()=>{var r,t;return(t=(r=this.$slots).default)===null||t===void 0?void 0:t.call(r)}})}});export{Ze as C,fo as N,eo as a,Ge as c,Je as u};
