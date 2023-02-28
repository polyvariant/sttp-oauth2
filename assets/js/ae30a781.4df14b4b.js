(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[112],{3905:function(e,t,n){"use strict";n.d(t,{Zo:function(){return c},kt:function(){return h}});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var p=a.createContext({}),s=function(e){var t=a.useContext(p),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},c=function(e){var t=s(e.components);return a.createElement(p.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},u=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,p=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),u=s(n),h=r,m=u["".concat(p,".").concat(h)]||u[h]||d[h]||i;return n?a.createElement(m,o(o({ref:t},c),{},{components:n})):a.createElement(m,o({ref:t},c))}));function h(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,o=new Array(i);o[0]=u;var l={};for(var p in t)hasOwnProperty.call(t,p)&&(l[p]=t[p]);l.originalType=e,l.mdxType="string"==typeof e?e:r,o[1]=l;for(var s=2;s<i;s++)o[s]=n[s];return a.createElement.apply(null,o)}return a.createElement.apply(null,n)}u.displayName="MDXCreateElement"},1663:function(e,t,n){"use strict";n.r(t),n.d(t,{frontMatter:function(){return o},metadata:function(){return l},toc:function(){return p},default:function(){return c}});var a=n(2122),r=n(9756),i=(n(7294),n(3905)),o={sidebar_position:8,description:"Migrations"},l={unversionedId:"migrating",id:"migrating",isDocsHomePage:!1,title:"Migrating to newer versions",description:"Migrations",source:"@site/../mdoc/target/mdoc/migrating.md",sourceDirName:".",slug:"/migrating",permalink:"/sttp-oauth2/docs/migrating",editUrl:"https://github.com/ocadotechnology/sttp-oauth2/edit/main/docs/migrating.md",version:"current",sidebarPosition:8,frontMatter:{sidebar_position:8,description:"Migrations"},sidebar:"tutorialSidebar",previous:{title:"Choosing JSON deserialisation module",permalink:"/sttp-oauth2/docs/json-deserialisation"}},p=[{value:"v0.17.0-RC-1",id:"v0170-rc-1",children:[]},{value:"v0.16.0",id:"v0160",children:[]},{value:"v0.15.0",id:"v0150",children:[{value:"Breaking change in authorization code grant",id:"breaking-change-in-authorization-code-grant",children:[]}]},{value:"v0.14.0",id:"v0140",children:[]},{value:"v0.12.0",id:"v0120",children:[{value:"<code>SttpBackend</code> no more passed as implicit param",id:"sttpbackend-no-more-passed-as-implicit-param",children:[]},{value:"Split <code>ClientCredentialsProvider</code>",id:"split-clientcredentialsprovider",children:[]},{value:"Caching",id:"caching",children:[]},{value:"Apply",id:"apply",children:[]}]},{value:"v0.10.0",id:"v0100",children:[]},{value:"v0.5.0",id:"v050",children:[]}],s={toc:p};function c(e){var t=e.components,n=(0,r.Z)(e,["components"]);return(0,i.kt)("wrapper",(0,a.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"Some releases introduce breaking changes. This page aims to list those and provide migration guide."),(0,i.kt)("h2",{id:"v0170-rc-1"},(0,i.kt)("a",{parentName:"h2",href:"https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.17.0"},"v0.17.0-RC-1")),(0,i.kt)("p",null,"Significant changes were introduced due to separation of JSON deserialisation from the core. Adding a module\nwith chosen JSON implementation is now required, as is importing an associated set of ",(0,i.kt)("inlineCode",{parentName:"p"},"JsonDecoder"),"s."),(0,i.kt)("p",null,"For backwards compatibility just add ",(0,i.kt)("inlineCode",{parentName:"p"},"circe")," module:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'"com.ocadotechnology" %% "sttp-oauth2-circe" % "0.16.0"\n')),(0,i.kt)("p",null,"and a following import where you were using ",(0,i.kt)("inlineCode",{parentName:"p"},"sttp-oauth2"),":"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"import com.ocadotechnology.sttp.oauth2.json.circe.instances._\n")),(0,i.kt)("h2",{id:"v0160"},(0,i.kt)("a",{parentName:"h2",href:"https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.16.0"},"v0.16.0")),(0,i.kt)("p",null,"Minor change ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/ocadotechnology/sttp-oauth2/pull/336"},"#336")," removed implicit parameter\nof ",(0,i.kt)("inlineCode",{parentName:"p"},"cats.MonadThrow")," in some methods. As long as your code just uses these methods (doesn't override or mock\ninterfaces), you have to only solve warnings suggesting that there are unused parameters. Otherwise,\nremove ",(0,i.kt)("inlineCode",{parentName:"p"},": MonadError")," from inherited implementations."),(0,i.kt)("p",null,"Affected classes: ",(0,i.kt)("inlineCode",{parentName:"p"},"PasswordGrant"),", ",(0,i.kt)("inlineCode",{parentName:"p"},"PasswordGrantProvider"),", ",(0,i.kt)("inlineCode",{parentName:"p"},"SttpOauth2ClientCredentialsBackend"),", ",(0,i.kt)("inlineCode",{parentName:"p"},"UserInfoProvider")),(0,i.kt)("h2",{id:"v0150"},(0,i.kt)("a",{parentName:"h2",href:"https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.15.0"},"v0.15.0")),(0,i.kt)("h3",{id:"breaking-change-in-authorization-code-grant"},"Breaking change in authorization code grant"),(0,i.kt)("p",null,"In ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/ocadotechnology/sttp-oauth2/pull/273"},"#273")," we have switched from using ",(0,i.kt)("inlineCode",{parentName:"p"},"withPath")," to ",(0,i.kt)("inlineCode",{parentName:"p"},"addPath")," in ",(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCode")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCodeProvider"),"."),(0,i.kt)("p",null,"If you were instantiating ",(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCodeProvider")," or using ",(0,i.kt)("inlineCode",{parentName:"p"},"AuthorizationCode")," providing ",(0,i.kt)("inlineCode",{parentName:"p"},"baseUri")," with path included, this would strip the path. Since ",(0,i.kt)("inlineCode",{parentName:"p"},"0.15.0")," this is no longer the case. If you relied on this behavior, please remove the path from the provided URL before creating the instance."),(0,i.kt)("h2",{id:"v0140"},(0,i.kt)("a",{parentName:"h2",href:"https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.14.0"},"v0.14.0")),(0,i.kt)("p",null,"Due to Scala 3 support ",(0,i.kt)("inlineCode",{parentName:"p"},"Scope.refine")," Refined macro has been removed. Scope object now extends ",(0,i.kt)("inlineCode",{parentName:"p"},"RefinedTypeOps[Scope, String]"),".\nTo parse ",(0,i.kt)("inlineCode",{parentName:"p"},"Scope")," use ",(0,i.kt)("inlineCode",{parentName:"p"},"Scope.of")," or other functions provided by ",(0,i.kt)("inlineCode",{parentName:"p"},"RefinedTypeOps")," - ",(0,i.kt)("inlineCode",{parentName:"p"},"from"),", ",(0,i.kt)("inlineCode",{parentName:"p"},"unsafeFrom")," or ",(0,i.kt)("inlineCode",{parentName:"p"},"unapply"),". "),(0,i.kt)("p",null,"Since this version, scope is also made optional in ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/ocadotechnology/sttp-oauth2/pull/199"},"#199")," to match the ",(0,i.kt)("a",{parentName:"p",href:"https://datatracker.ietf.org/doc/html/rfc6749#section-3.3"},"spec"),"."),(0,i.kt)("h2",{id:"v0120"},(0,i.kt)("a",{parentName:"h2",href:"https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.12.0"},"v0.12.0")),(0,i.kt)("h3",{id:"sttpbackend-no-more-passed-as-implicit-param"},(0,i.kt)("inlineCode",{parentName:"h3"},"SttpBackend")," no more passed as implicit param"),(0,i.kt)("p",null,"Applying ",(0,i.kt)("inlineCode",{parentName:"p"},"sttp")," convention, not to pass ",(0,i.kt)("inlineCode",{parentName:"p"},"SttpBackend")," as implicit param, all methods that require it (like constructor of ",(0,i.kt)("inlineCode",{parentName:"p"},"ClientCredentialsProvider"),") have been changed to require this as explicit parameter."),(0,i.kt)("p",null,"Change"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"implicit val backend: SttpBackend[IO, Any] = ???\nClientCredentialsProvider.instance[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)\n")),(0,i.kt)("p",null,"into:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"val backend: SttpBackend[IO, Any] = ???\nClientCredentialsProvider[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)(backend)\n")),(0,i.kt)("h3",{id:"split-clientcredentialsprovider"},"Split ",(0,i.kt)("inlineCode",{parentName:"h3"},"ClientCredentialsProvider")),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"ClientCredentialsProvider")," has been split into ",(0,i.kt)("inlineCode",{parentName:"p"},"AccessTokenProvider")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"TokenIntrospection"),". This allows using better scoped traits without a need to provide redundant token introspection url if there is only need for requesting access tokens. "),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"ClientCredentialsProvider")," has been left as a sum of both traits for smoother migration, so in most cases no changes would be required during the migration."),(0,i.kt)("h3",{id:"caching"},"Caching"),(0,i.kt)("p",null,"In this release modules ",(0,i.kt)("inlineCode",{parentName:"p"},"oauth2-cache-xx")," have been introduced, that contain cache based ",(0,i.kt)("inlineCode",{parentName:"p"},"AccessTokenProvider")," for ",(0,i.kt)("inlineCode",{parentName:"p"},"cats-effect2")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"Future"),". This has lead to removal of ",(0,i.kt)("inlineCode",{parentName:"p"},"SttpOauth2ClientCredentialsCatsBackend")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"SttpOauth2ClientCredentialsFutureBackend"),". Instead a generic ",(0,i.kt)("inlineCode",{parentName:"p"},"SttpOauth2ClientCredentialsBackend")," should be used with a ",(0,i.kt)("inlineCode",{parentName:"p"},"AccessTokenProvider")," of your choice. "),(0,i.kt)("p",null,"To build cached ",(0,i.kt)("inlineCode",{parentName:"p"},"SttpBackend"),":"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"replace dependency of ",(0,i.kt)("inlineCode",{parentName:"li"},"sttp-oauth2-backend-xx")," with ",(0,i.kt)("inlineCode",{parentName:"li"},"sttp-oauth2-cache-xx")),(0,i.kt)("li",{parentName:"ul"},"replace creation of ",(0,i.kt)("inlineCode",{parentName:"li"},"SttpOauth2ClientCredentialsXXXBackend")," with the following example adjusted to your needs:")),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"val accessTokenProvider = AccessTokenProvider[IO](tokenUrl, clientId, clientSecret)(backend)\nCachingAccessTokenProvider.refCacheInstance[IO](accessTokenProvider).map { cachingAccessTokenProvider => \n    SttpOauth2ClientCredentialsBackend[IO, Any](cachingAccessTokenProvider)(scope)\n}\n")),(0,i.kt)("p",null,"For details please see ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/ocadotechnology/sttp-oauth2/pull/149"},"PR"),"."),(0,i.kt)("h3",{id:"apply"},"Apply"),(0,i.kt)("p",null,"In many companion objects factory methods called ",(0,i.kt)("inlineCode",{parentName:"p"},"instance")," have been replaced with ",(0,i.kt)("inlineCode",{parentName:"p"},"apply"),", so previous way of creating objects:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"ClientCredentialsProvider.instance[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)\n")),(0,i.kt)("p",null,"needs to be replaced with:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"ClientCredentialsProvider[IO](tokenUrl, tokenIntrospectionUrl, clientId, clientSecret)\n")),(0,i.kt)("h2",{id:"v0100"},(0,i.kt)("a",{parentName:"h2",href:"https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.5.0"},"v0.10.0")),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"authCodeToToken")," and ",(0,i.kt)("inlineCode",{parentName:"p"},"refreshAccessToken")," no longer return fixed token response type. Instead, they require ",(0,i.kt)("inlineCode",{parentName:"p"},"RT <: OAuth2TokenResponse.Basic: Decoder")," type parameter, that describes desired. response structure."),(0,i.kt)("p",null,"There are two matching pre-defined types provided:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"OAuth2TokenResponse")," - minimal response as described by ",(0,i.kt)("a",{parentName:"li",href:"https://datatracker.ietf.org/doc/html/rfc6749#section-5.1"},"rfc6749")),(0,i.kt)("li",{parentName:"ul"},(0,i.kt)("inlineCode",{parentName:"li"},"ExtendedOAuth2TokenResponse")," - previously known as ",(0,i.kt)("inlineCode",{parentName:"li"},"Oauth2TokenResponse"),", the previously fixed response type. Use this for backward compatiblity.")),(0,i.kt)("h2",{id:"v050"},(0,i.kt)("a",{parentName:"h2",href:"https://github.com/ocadotechnology/sttp-oauth2/releases/tag/v0.5.0"},"v0.5.0")),(0,i.kt)("p",null,"This version introduces ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/ocadotechnology/sttp-oauth2/pull/39"},"sttp3"),". Please see ",(0,i.kt)("a",{parentName:"p",href:"https://github.com/softwaremill/sttp/releases/tag/v3.0.0"},"sttp v3.0.0 release")," for migration guide."))}c.isMDXComponent=!0}}]);