(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[478],{3905:function(e,t,n){"use strict";n.d(t,{Zo:function(){return s},kt:function(){return f}});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function u(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var c=r.createContext({}),p=function(e){var t=r.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},s=function(e){var t=p(e.components);return r.createElement(c.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},l=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,i=e.originalType,c=e.parentName,s=u(e,["components","mdxType","originalType","parentName"]),l=p(n),f=o,m=l["".concat(c,".").concat(f)]||l[f]||d[f]||i;return n?r.createElement(m,a(a({ref:t},s),{},{components:n})):r.createElement(m,a({ref:t},s))}));function f(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=n.length,a=new Array(i);a[0]=l;var u={};for(var c in t)hasOwnProperty.call(t,c)&&(u[c]=t[c]);u.originalType=e,u.mdxType="string"==typeof e?e:o,a[1]=u;for(var p=2;p<i;p++)a[p]=n[p];return r.createElement.apply(null,a)}return r.createElement.apply(null,n)}l.displayName="MDXCreateElement"},8735:function(e,t,n){"use strict";n.r(t),n.d(t,{frontMatter:function(){return a},metadata:function(){return u},toc:function(){return c},default:function(){return s}});var r=n(2122),o=n(9756),i=(n(7294),n(3905)),a={sidebar_position:3,description:"Authorization code grant documentation"},u={unversionedId:"authorization-code",id:"authorization-code",isDocsHomePage:!1,title:"Authorization code grant",description:"Authorization code grant documentation",source:"@site/../mdoc/target/mdoc/authorization-code.md",sourceDirName:".",slug:"/authorization-code",permalink:"/sttp-oauth2/docs/authorization-code",editUrl:"https://github.com/ocadotechnology/sttp-oauth2/edit/main/docs/authorization-code.md",version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3,description:"Authorization code grant documentation"},sidebar:"tutorialSidebar",previous:{title:"Client credentials grant",permalink:"/sttp-oauth2/docs/client-credentials"},next:{title:"Password grant",permalink:"/sttp-oauth2/docs/password-grant"}},c=[{value:"Methods",id:"methods",children:[]},{value:"Token types",id:"token-types",children:[]},{value:"Configuration",id:"configuration",children:[]}],p={toc:c};function s(e){var t=e.components,n=(0,o.Z)(e,["components"]);return(0,i.kt)("wrapper",(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("h2",{id:"methods"},"Methods"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCode")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCodeProvider")," - provide functionality for: "),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"generating ",(0,i.kt)("em",{parentName:"li"},"login")," and ",(0,i.kt)("em",{parentName:"li"},"logout")," redirect links,"),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"authCodeToToken")," for converting authorization code to token,"),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"refreshAccessToken")," for performing a token refresh request")),(0,i.kt)("h2",{id:"token-types"},"Token types"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"authCodeToToken")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"refreshAccessToken")," require ",(0,i.kt)("inlineCode",{parentName:"p"},"RT <: OAuth2TokenResponse.Basic: Decoder")," type parameter, that describes desired. response structure. You can use ",(0,i.kt)("inlineCode",{parentName:"p"},"OAuth2TokenResponse"),", ",(0,i.kt)("inlineCode",{parentName:"p"},"ExtendedOAuth2TokenResponse")," or roll your own type that matches the type bounds."),(0,i.kt)("h2",{id:"configuration"},"Configuration"),(0,i.kt)("p",null,"OAuth2 doesn't precisely define urls for used for the process. Those differ by provider.\n",(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCodeProvider.Config")," provides a structure for configuring the endpoints.\nFor login with GitHub you can use ",(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCodeProvider.Config.GitHub"),". Feel free to issue a PR if you want any other well-known provider supported."))}s.isMDXComponent=!0}}]);