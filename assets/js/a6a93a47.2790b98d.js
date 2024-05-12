(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[970],{3905:function(e,t,n){"use strict";n.d(t,{Zo:function(){return c},kt:function(){return m}});var o=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);t&&(o=o.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,o)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,o,r=function(e,t){if(null==e)return{};var n,o,r={},i=Object.keys(e);for(o=0;o<i.length;o++)n=i[o],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(o=0;o<i.length;o++)n=i[o],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=o.createContext({}),u=function(e){var t=o.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},c=function(e){var t=u(e.components);return o.createElement(s.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return o.createElement(o.Fragment,{},t)}},p=o.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,s=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),p=u(n),m=r,f=p["".concat(s,".").concat(m)]||p[m]||d[m]||i;return n?o.createElement(f,a(a({ref:t},c),{},{components:n})):o.createElement(f,a({ref:t},c))}));function m(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,a=new Array(i);a[0]=p;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l.mdxType="string"==typeof e?e:r,a[1]=l;for(var u=2;u<i;u++)a[u]=n[u];return o.createElement.apply(null,a)}return o.createElement.apply(null,n)}p.displayName="MDXCreateElement"},8392:function(e,t,n){"use strict";n.r(t),n.d(t,{frontMatter:function(){return a},metadata:function(){return l},toc:function(){return s},default:function(){return c}});var o=n(2122),r=n(9756),i=(n(7294),n(3905)),a={sidebar_position:5,description:"Contributing"},l={unversionedId:"contributing",id:"contributing",isDocsHomePage:!1,title:"Contributing to sttp-oauth2",description:"Contributing",source:"@site/../mdoc/target/mdoc/contributing.md",sourceDirName:".",slug:"/contributing",permalink:"/docs/contributing",editUrl:"https://github.com/polyvariant/sttp-oauth2/edit/main/docs/contributing.md",version:"current",sidebarPosition:5,frontMatter:{sidebar_position:5,description:"Contributing"},sidebar:"tutorialSidebar",previous:{title:"Password grant",permalink:"/docs/password-grant"},next:{title:"Token introspection",permalink:"/docs/token-introspection"}},s=[{value:"Working with documentation",id:"working-with-documentation",children:[{value:"Working on documentation locally using live reload",id:"working-on-documentation-locally-using-live-reload",children:[]},{value:"Documentation build workflow",id:"documentation-build-workflow",children:[]}]},{value:"Adding JSON implementations",id:"adding-json-implementations",children:[]}],u={toc:s};function c(e){var t=e.components,n=(0,r.Z)(e,["components"]);return(0,i.kt)("wrapper",(0,o.Z)({},u,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"This is an early stage project. The codebase is prone to change. All suggestions are welcome, please create an issue describing your problem before contributing."),(0,i.kt)("p",null,"Please make sure to format the code with ",(0,i.kt)("a",{parentName:"p",href:"https://scalameta.org/scalafmt/"},"scalafmt")," using ",(0,i.kt)("inlineCode",{parentName:"p"},".scalafmt.conf")," from the repository root."),(0,i.kt)("h2",{id:"working-with-documentation"},"Working with documentation"),(0,i.kt)("p",null,"The documentation is built using ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/scalameta/mdoc"},"mdoc")," combined with ",(0,i.kt)("a",{parentName:"p",href:"https://docusaurus.io/"},"docusaurus v2"),". "),(0,i.kt)("p",null,"To build the documentation make sure you have installed Node.js and Yarn according to ",(0,i.kt)("a",{parentName:"p",href:"https://docusaurus.io/docs/installation#requirements"},"docusaurus requirements"),". "),(0,i.kt)("h3",{id:"working-on-documentation-locally-using-live-reload"},"Working on documentation locally using live reload"),(0,i.kt)("p",null,"For live reload you'd preferably need two console windows open. In both you should navigate to your repository root, then:"),(0,i.kt)("p",null,"In the first terminal, launch ",(0,i.kt)("inlineCode",{parentName:"p"},"sbt")," shell and run ",(0,i.kt)("inlineCode",{parentName:"p"},"docs/mdoc --watch"),"."),(0,i.kt)("p",null,"In the second one run:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-shell"},"cd ./website\nnpx docusaurus start\n")),(0,i.kt)("p",null,"If it's your first time, remember to run ",(0,i.kt)("inlineCode",{parentName:"p"},"npm install")," in the ",(0,i.kt)("inlineCode",{parentName:"p"},"./website")," directory"),(0,i.kt)("h3",{id:"documentation-build-workflow"},"Documentation build workflow"),(0,i.kt)("p",null,"The raw documentation goes through a few steps process before the final website is created."),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"Raw data resides in ",(0,i.kt)("inlineCode",{parentName:"li"},"./docs")," directory, it follows regular ",(0,i.kt)("a",{parentName:"li",href:"https://docusaurus.io/"},"docusaurus")," rules regarding creating documentation"),(0,i.kt)("li",{parentName:"ul"},"The first step when building the documentation is to run ",(0,i.kt)("inlineCode",{parentName:"li"},"docs/mdoc/"),". This step compiles the code examples, verifying if everything makes sense and is up to date."),(0,i.kt)("li",{parentName:"ul"},"When the build finishes, the compiled documentation ends up in ",(0,i.kt)("inlineCode",{parentName:"li"},"./mdoc/target/mdoc/")),(0,i.kt)("li",{parentName:"ul"},"The last step is to build docusaurus. Docusaurus is configured to read files from ",(0,i.kt)("inlineCode",{parentName:"li"},"./mdoc/target/mdoc/")," and generate the website using regular docusaurus rules.")),(0,i.kt)("h2",{id:"adding-json-implementations"},"Adding JSON implementations"),(0,i.kt)("p",null,"When adding a JSON implementation please follow the subsequent guidelines:"),(0,i.kt)("ol",null,(0,i.kt)("li",{parentName:"ol"},"Each JSON implementation should exist in a separate module, not to introduce unwanted dependencies."),(0,i.kt)("li",{parentName:"ol"},"It should expose all necessary ",(0,i.kt)("inlineCode",{parentName:"li"},"JsonDecoder"),"s via a single import following the ",(0,i.kt)("inlineCode",{parentName:"li"},"import org.polyvariant.sttp.oauth2.json.<insert-json-library-name-here>.instances._")," convention."),(0,i.kt)("li",{parentName:"ol"},"It should make use of ",(0,i.kt)("inlineCode",{parentName:"li"},"org.polyvariant.sttp.oauth2.json.JsonSpec")," test suite to ensure correctness."),(0,i.kt)("li",{parentName:"ol"},"It should be included in the documentation (",(0,i.kt)("a",{parentName:"li",href:"/docs/json-deserialisation"},"JSON Deserialisation"),").")))}c.isMDXComponent=!0}}]);