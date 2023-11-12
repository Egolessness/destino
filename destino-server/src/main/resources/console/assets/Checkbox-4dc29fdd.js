import{h as d,d as N,u as j,Q as E,r as F,j as U,S as se,t as P,G as ue,Y as l,g as b,b as r,P as y,e as $,bh as be,bT as he,bU as fe,H as ke,a0 as ve,k as H,dD as me,bi as ge,U as K,n as xe,cK as pe,bc as Ce,E as ye}from"./index-10b862f4.js";import{u as G}from"./Input-facf482e.js";const we=d("svg",{viewBox:"0 0 64 64",class:"check-icon"},d("path",{d:"M50.42,16.76L22.34,39.45l-8.1-11.46c-1.12-1.58-3.3-1.96-4.88-0.84c-1.58,1.12-1.95,3.3-0.84,4.88l10.26,14.51  c0.56,0.79,1.42,1.31,2.38,1.45c0.16,0.02,0.32,0.03,0.48,0.03c0.8,0,1.57-0.27,2.2-0.78l30.99-25.03c1.5-1.21,1.74-3.42,0.52-4.92  C54.13,15.78,51.93,15.55,50.42,16.76z"})),Re=d("svg",{viewBox:"0 0 100 100",class:"line-icon"},d("path",{d:"M80.2,55.5H21.4c-2.8,0-5.1-2.5-5.1-5.5l0,0c0-3,2.3-5.5,5.1-5.5h58.7c2.8,0,5.1,2.5,5.1,5.5l0,0C85.2,53.1,82.9,55.5,80.2,55.5z"})),V=ue("n-checkbox-group"),ze={min:Number,max:Number,size:String,value:Array,defaultValue:{type:Array,default:null},disabled:{type:Boolean,default:void 0},"onUpdate:value":[Function,Array],onUpdateValue:[Function,Array],onChange:[Function,Array]},$e=N({name:"CheckboxGroup",props:ze,setup(o){const{mergedClsPrefixRef:g}=j(o),x=E(o),{mergedSizeRef:w,mergedDisabledRef:S}=x,s=F(o.defaultValue),R=U(()=>o.value),u=G(R,s),a=U(()=>{var h;return((h=u.value)===null||h===void 0?void 0:h.length)||0}),c=U(()=>Array.isArray(u.value)?new Set(u.value):new Set);function A(h,n){const{nTriggerFormInput:p,nTriggerFormChange:C}=x,{onChange:f,"onUpdate:value":k,onUpdateValue:v}=o;if(Array.isArray(u.value)){const i=Array.from(u.value),M=i.findIndex(B=>B===n);h?~M||(i.push(n),v&&l(v,i,{actionType:"check",value:n}),k&&l(k,i,{actionType:"check",value:n}),p(),C(),s.value=i,f&&l(f,i)):~M&&(i.splice(M,1),v&&l(v,i,{actionType:"uncheck",value:n}),k&&l(k,i,{actionType:"uncheck",value:n}),f&&l(f,i),s.value=i,p(),C())}else h?(v&&l(v,[n],{actionType:"check",value:n}),k&&l(k,[n],{actionType:"check",value:n}),f&&l(f,[n]),s.value=[n],p(),C()):(v&&l(v,[],{actionType:"uncheck",value:n}),k&&l(k,[],{actionType:"uncheck",value:n}),f&&l(f,[]),s.value=[],p(),C())}return se(V,{checkedCountRef:a,maxRef:P(o,"max"),minRef:P(o,"min"),valueSetRef:c,disabledRef:S,mergedSizeRef:w,toggleCheckbox:A}),{mergedClsPrefix:g}},render(){return d("div",{class:`${this.mergedClsPrefix}-checkbox-group`,role:"group"},this.$slots)}}),Se=b([r("checkbox",`
 line-height: var(--n-label-line-height);
 font-size: var(--n-font-size);
 outline: none;
 cursor: pointer;
 display: inline-flex;
 flex-wrap: nowrap;
 align-items: flex-start;
 word-break: break-word;
 --n-merged-color-table: var(--n-color-table);
 `,[b("&:hover",[r("checkbox-box",[y("border",{border:"var(--n-border-checked)"})])]),b("&:focus:not(:active)",[r("checkbox-box",[y("border",`
 border: var(--n-border-focus);
 box-shadow: var(--n-box-shadow-focus);
 `)])]),$("inside-table",[r("checkbox-box",`
 background-color: var(--n-merged-color-table);
 `)]),$("checked",[r("checkbox-box",`
 background-color: var(--n-color-checked);
 `,[r("checkbox-icon",[b(".check-icon",`
 opacity: 1;
 transform: scale(1);
 `)])])]),$("indeterminate",[r("checkbox-box",[r("checkbox-icon",[b(".check-icon",`
 opacity: 0;
 transform: scale(.5);
 `),b(".line-icon",`
 opacity: 1;
 transform: scale(1);
 `)])])]),$("checked, indeterminate",[b("&:focus:not(:active)",[r("checkbox-box",[y("border",`
 border: var(--n-border-checked);
 box-shadow: var(--n-box-shadow-focus);
 `)])]),r("checkbox-box",`
 background-color: var(--n-color-checked);
 border-left: 0;
 border-top: 0;
 `,[y("border",{border:"var(--n-border-checked)"})])]),$("disabled",{cursor:"not-allowed"},[$("checked",[r("checkbox-box",`
 background-color: var(--n-color-disabled-checked);
 `,[y("border",{border:"var(--n-border-disabled-checked)"}),r("checkbox-icon",[b(".check-icon, .line-icon",{fill:"var(--n-check-mark-color-disabled-checked)"})])])]),r("checkbox-box",`
 background-color: var(--n-color-disabled);
 `,[y("border",{border:"var(--n-border-disabled)"}),r("checkbox-icon",[b(".check-icon, .line-icon",{fill:"var(--n-check-mark-color-disabled)"})])]),y("label",{color:"var(--n-text-color-disabled)"})]),r("checkbox-box-wrapper",`
 position: relative;
 width: var(--n-size);
 flex-shrink: 0;
 flex-grow: 0;
 user-select: none;
 -webkit-user-select: none;
 `),r("checkbox-box",`
 position: absolute;
 left: 0;
 top: 50%;
 transform: translateY(-50%);
 height: var(--n-size);
 width: var(--n-size);
 display: inline-block;
 box-sizing: border-box;
 border-radius: var(--n-border-radius);
 background-color: var(--n-color);
 transition: background-color 0.3s var(--n-bezier);
 `,[y("border",`
 transition:
 border-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
 border-radius: inherit;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 border: var(--n-border);
 `),r("checkbox-icon",`
 display: flex;
 align-items: center;
 justify-content: center;
 position: absolute;
 left: 1px;
 right: 1px;
 top: 1px;
 bottom: 1px;
 `,[b(".check-icon, .line-icon",`
 width: 100%;
 fill: var(--n-check-mark-color);
 opacity: 0;
 transform: scale(0.5);
 transform-origin: center;
 transition:
 fill 0.3s var(--n-bezier),
 transform 0.3s var(--n-bezier),
 opacity 0.3s var(--n-bezier),
 border-color 0.3s var(--n-bezier);
 `),be({left:"1px",top:"1px"})])]),y("label",`
 color: var(--n-text-color);
 transition: color .3s var(--n-bezier);
 user-select: none;
 -webkit-user-select: none;
 padding: var(--n-label-padding);
 font-weight: var(--n-label-font-weight);
 `,[b("&:empty",{display:"none"})])]),he(r("checkbox",`
 --n-merged-color-table: var(--n-color-table-modal);
 `)),fe(r("checkbox",`
 --n-merged-color-table: var(--n-color-table-popover);
 `))]),Te=Object.assign(Object.assign({},H.props),{size:String,checked:{type:[Boolean,String,Number],default:void 0},defaultChecked:{type:[Boolean,String,Number],default:!1},value:[String,Number],disabled:{type:Boolean,default:void 0},indeterminate:Boolean,label:String,focusable:{type:Boolean,default:!0},checkedValue:{type:[Boolean,String,Number],default:!0},uncheckedValue:{type:[Boolean,String,Number],default:!1},"onUpdate:checked":[Function,Array],onUpdateChecked:[Function,Array],privateInsideTable:Boolean,onChange:[Function,Array]}),Ae=N({name:"Checkbox",props:Te,setup(o){const g=F(null),{mergedClsPrefixRef:x,inlineThemeDisabled:w,mergedRtlRef:S}=j(o),s=E(o,{mergedSize(e){const{size:m}=o;if(m!==void 0)return m;if(a){const{value:t}=a.mergedSizeRef;if(t!==void 0)return t}if(e){const{mergedSize:t}=e;if(t!==void 0)return t.value}return"medium"},mergedDisabled(e){const{disabled:m}=o;if(m!==void 0)return m;if(a){if(a.disabledRef.value)return!0;const{maxRef:{value:t},checkedCountRef:z}=a;if(t!==void 0&&z.value>=t&&!n.value)return!0;const{minRef:{value:D}}=a;if(D!==void 0&&z.value<=D&&n.value)return!0}return e?e.disabled.value:!1}}),{mergedDisabledRef:R,mergedSizeRef:u}=s,a=ke(V,null),c=F(o.defaultChecked),A=P(o,"checked"),h=G(A,c),n=ve(()=>{if(a){const e=a.valueSetRef.value;return e&&o.value!==void 0?e.has(o.value):!1}else return h.value===o.checkedValue}),p=H("Checkbox","-checkbox",Se,me,o,x);function C(e){if(a&&o.value!==void 0)a.toggleCheckbox(!n.value,o.value);else{const{onChange:m,"onUpdate:checked":t,onUpdateChecked:z}=o,{nTriggerFormInput:D,nTriggerFormChange:I}=s,_=n.value?o.uncheckedValue:o.checkedValue;t&&l(t,_,e),z&&l(z,_,e),m&&l(m,_,e),D(),I(),c.value=_}}function f(e){R.value||C(e)}function k(e){if(!R.value)switch(e.key){case" ":case"Enter":C(e)}}function v(e){switch(e.key){case" ":e.preventDefault()}}const i={focus:()=>{var e;(e=g.value)===null||e===void 0||e.focus()},blur:()=>{var e;(e=g.value)===null||e===void 0||e.blur()}},M=ge("Checkbox",S,x),B=U(()=>{const{value:e}=u,{common:{cubicBezierEaseInOut:m},self:{borderRadius:t,color:z,colorChecked:D,colorDisabled:I,colorTableHeader:_,colorTableHeaderModal:L,colorTableHeaderPopover:O,checkMarkColor:Y,checkMarkColorDisabled:Q,border:W,borderFocus:q,borderDisabled:J,borderChecked:X,boxShadowFocus:Z,textColor:ee,textColorDisabled:oe,checkMarkColorDisabledChecked:ne,colorDisabledChecked:re,borderDisabledChecked:ce,labelPadding:ae,labelLineHeight:le,labelFontWeight:ie,[K("fontSize",e)]:te,[K("size",e)]:de}}=p.value;return{"--n-label-line-height":le,"--n-label-font-weight":ie,"--n-size":de,"--n-bezier":m,"--n-border-radius":t,"--n-border":W,"--n-border-checked":X,"--n-border-focus":q,"--n-border-disabled":J,"--n-border-disabled-checked":ce,"--n-box-shadow-focus":Z,"--n-color":z,"--n-color-checked":D,"--n-color-table":_,"--n-color-table-modal":L,"--n-color-table-popover":O,"--n-color-disabled":I,"--n-color-disabled-checked":re,"--n-text-color":ee,"--n-text-color-disabled":oe,"--n-check-mark-color":Y,"--n-check-mark-color-disabled":Q,"--n-check-mark-color-disabled-checked":ne,"--n-font-size":te,"--n-label-padding":ae}}),T=w?xe("checkbox",U(()=>u.value[0]),B,o):void 0;return Object.assign(s,i,{rtlEnabled:M,selfRef:g,mergedClsPrefix:x,mergedDisabled:R,renderedChecked:n,mergedTheme:p,labelId:pe(),handleClick:f,handleKeyUp:k,handleKeyDown:v,cssVars:w?void 0:B,themeClass:T==null?void 0:T.themeClass,onRender:T==null?void 0:T.onRender})},render(){var o;const{$slots:g,renderedChecked:x,mergedDisabled:w,indeterminate:S,privateInsideTable:s,cssVars:R,labelId:u,label:a,mergedClsPrefix:c,focusable:A,handleKeyUp:h,handleKeyDown:n,handleClick:p}=this;return(o=this.onRender)===null||o===void 0||o.call(this),d("div",{ref:"selfRef",class:[`${c}-checkbox`,this.themeClass,this.rtlEnabled&&`${c}-checkbox--rtl`,x&&`${c}-checkbox--checked`,w&&`${c}-checkbox--disabled`,S&&`${c}-checkbox--indeterminate`,s&&`${c}-checkbox--inside-table`],tabindex:w||!A?void 0:0,role:"checkbox","aria-checked":S?"mixed":x,"aria-labelledby":u,style:R,onKeyup:h,onKeydown:n,onClick:p,onMousedown:()=>{ye("selectstart",window,C=>{C.preventDefault()},{once:!0})}},d("div",{class:`${c}-checkbox-box-wrapper`}," ",d("div",{class:`${c}-checkbox-box`},d(Ce,null,{default:()=>this.indeterminate?d("div",{key:"indeterminate",class:`${c}-checkbox-icon`},Re):d("div",{key:"check",class:`${c}-checkbox-icon`},we)}),d("div",{class:`${c}-checkbox-box__border`}))),a!==null||g.default?d("span",{class:`${c}-checkbox__label`,id:u},g.default?g.default():a):null)}});export{$e as N,Ae as _};
