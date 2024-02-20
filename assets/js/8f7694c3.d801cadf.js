(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[915],{3905:function(e,t,n){"use strict";n.d(t,{Zo:function(){return p},kt:function(){return m}});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var c=a.createContext({}),d=function(e){var t=a.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=d(e.components);return a.createElement(c.Provider,{value:t},e.children)},s={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},u=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,c=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),u=d(n),m=r,k=u["".concat(c,".").concat(m)]||u[m]||s[m]||i;return n?a.createElement(k,o(o({ref:t},p),{},{components:n})):a.createElement(k,o({ref:t},p))}));function m(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,o=new Array(i);o[0]=u;var l={};for(var c in t)hasOwnProperty.call(t,c)&&(l[c]=t[c]);l.originalType=e,l.mdxType="string"==typeof e?e:r,o[1]=l;for(var d=2;d<i;d++)o[d]=n[d];return a.createElement.apply(null,o)}return a.createElement.apply(null,n)}u.displayName="MDXCreateElement"},787:function(e,t,n){"use strict";n.r(t),n.d(t,{frontMatter:function(){return o},metadata:function(){return l},toc:function(){return c},default:function(){return p}});var a=n(2122),r=n(9756),i=(n(7294),n(3905)),o={sidebar_position:2,description:"Client credentials grant documentation"},l={unversionedId:"client-credentials",id:"client-credentials",isDocsHomePage:!1,title:"Client credentials grant",description:"Client credentials grant documentation",source:"@site/../mdoc/target/mdoc/client-credentials.md",sourceDirName:".",slug:"/client-credentials",permalink:"/sttp-oauth2/docs/client-credentials",editUrl:"https://github.com/ocadotechnology/sttp-oauth2/edit/main/docs/client-credentials.md",version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2,description:"Client credentials grant documentation"},sidebar:"tutorialSidebar",previous:{title:"Getting started",permalink:"/sttp-oauth2/docs/getting-started"},next:{title:"Authorization code grant",permalink:"/sttp-oauth2/docs/authorization-code"}},c=[{value:"Caching",id:"caching",children:[{value:"Cats example",id:"cats-example",children:[]}]},{value:"<code>sttp-oauth2</code> backends",id:"sttp-oauth2-backends",children:[]}],d={toc:c};function p(e){var t=e.components,n=(0,r.Z)(e,["components"]);return(0,i.kt)("wrapper",(0,a.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"ClientCredentials")," and traits ",(0,i.kt)("inlineCode",{parentName:"p"},"AccessTokenProvider")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"TokenIntrospection")," expose methods that:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"Obtain token via ",(0,i.kt)("inlineCode",{parentName:"li"},"requestToken")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"introspect")," the token for its details like ",(0,i.kt)("inlineCode",{parentName:"li"},"UserInfo"))),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'import com.ocadotechnology.sttp.oauth2.json.circe.instances._ // Or your favorite JSON implementation\n\nval accessTokenProvider = AccessTokenProvider[IO](tokenUrl, clientId, clientSecret)(backend)\nval tokenIntrospection = TokenIntrospection[IO](tokenIntrospectionUrl, clientId, clientSecret)(backend)\nval scope: Option[Scope] = Some("scope")\n\nfor {\n  token <- accessTokenProvider.requestToken(scope) // ask for token\n  response <- tokenIntrospection.introspect(token.accessToken) // check if token is valid\n} yield response.active // is the token active?\n')),(0,i.kt)("h2",{id:"caching"},"Caching"),(0,i.kt)("p",null,"Caching modules provide cached ",(0,i.kt)("inlineCode",{parentName:"p"},"AccessTokenProvider"),", which can:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"reuse the token multiple times using a cache (default cache implementation may be overridden using appropriate constructor functions)"),(0,i.kt)("li",{parentName:"ul"},"fetch a new token if the previous one expires")),(0,i.kt)("table",null,(0,i.kt)("thead",{parentName:"table"},(0,i.kt)("tr",{parentName:"thead"},(0,i.kt)("th",{parentName:"tr",align:null},"module name"),(0,i.kt)("th",{parentName:"tr",align:null},"class name"),(0,i.kt)("th",{parentName:"tr",align:null},"provided cache implementation"),(0,i.kt)("th",{parentName:"tr",align:null},"semaphore"),(0,i.kt)("th",{parentName:"tr",align:null},"notes"))),(0,i.kt)("tbody",{parentName:"table"},(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"sttp-oauth2-cache-cats")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"CachingAccessTokenProvider")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"cats-effect3"),"'s ",(0,i.kt)("inlineCode",{parentName:"td"},"Ref")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"cats-effect2"),"'s ",(0,i.kt)("inlineCode",{parentName:"td"},"Semaphore")),(0,i.kt)("td",{parentName:"tr",align:null})),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"sttp-oauth2-cache-ce2")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"CachingAccessTokenProvider")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"cats-effect2"),"'s ",(0,i.kt)("inlineCode",{parentName:"td"},"Ref")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"cats-effect2"),"'s ",(0,i.kt)("inlineCode",{parentName:"td"},"Semaphore")),(0,i.kt)("td",{parentName:"tr",align:null})),(0,i.kt)("tr",{parentName:"tbody"},(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"sttp-oauth2-cache-future")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"FutureCachingAccessTokenProvider")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"monix-execution"),"'s ",(0,i.kt)("inlineCode",{parentName:"td"},"AtomicAny")),(0,i.kt)("td",{parentName:"tr",align:null},(0,i.kt)("inlineCode",{parentName:"td"},"monix-execution"),"'s ",(0,i.kt)("inlineCode",{parentName:"td"},"AsyncSemaphore")),(0,i.kt)("td",{parentName:"tr",align:null},"It only uses submodule of whole ",(0,i.kt)("inlineCode",{parentName:"td"},"monix")," project")))),(0,i.kt)("h3",{id:"cats-example"},"Cats example"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"val delegate = AccessTokenProvider[IO](tokenUrl, clientId, clientSecret)(backend)\nCachingAccessTokenProvider.refCacheInstance[IO](delegate)\n")),(0,i.kt)("h2",{id:"sttp-oauth2-backends"},(0,i.kt)("inlineCode",{parentName:"h2"},"sttp-oauth2")," backends"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"SttpOauth2ClientCredentialsBackend")," is a ",(0,i.kt)("inlineCode",{parentName:"p"},"SttpBackend")," which sends auth bearer headers for every ",(0,i.kt)("inlineCode",{parentName:"p"},"http")," call performed with it using provided ",(0,i.kt)("inlineCode",{parentName:"p"},"AccessTokenProvider"),"."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'val scope: Option[Scope] = Some("scope") // backend will use defined scope for all requests\nval backend: SttpBackend[IO, Any] = SttpOauth2ClientCredentialsBackend[IO, Any](tokenUrl, clientId, clientSecret)(scope)(delegateBackend)\nbackend.send(request) // this will add header: Authorization: Bearer {token}\n\n')),(0,i.kt)("p",null,"In order to cache tokens, use one of the ",(0,i.kt)("inlineCode",{parentName:"p"},"AccessTokenProviders")," described in Caching section."))}p.isMDXComponent=!0}}]);