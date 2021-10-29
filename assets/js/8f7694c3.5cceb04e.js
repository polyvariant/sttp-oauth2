(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[915],{787:function(e,t,n){"use strict";n.r(t),n.d(t,{frontMatter:function(){return o},metadata:function(){return l},toc:function(){return d},default:function(){return c}});var a=n(2122),i=n(9756),r=(n(7294),n(3905)),o={sidebar_position:2,description:"Client credentials grant documentation"},l={unversionedId:"client-credentials",id:"client-credentials",isDocsHomePage:!1,title:"Client credentials grant",description:"Client credentials grant documentation",source:"@site/../mdoc/target/mdoc/client-credentials.md",sourceDirName:".",slug:"/client-credentials",permalink:"/sttp-oauth2/docs/client-credentials",editUrl:"https://github.com/ocadotechnology/sttp-oauth2/edit/main/docs/client-credentials.md",version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2,description:"Client credentials grant documentation"},sidebar:"tutorialSidebar",previous:{title:"Getting started",permalink:"/sttp-oauth2/docs/getting-started"},next:{title:"Authorization code grant",permalink:"/sttp-oauth2/docs/authorization-code"}},d=[{value:"<code>sttp-oauth2</code> backends",id:"sttp-oauth2-backends",children:[]}],s={toc:d};function c(e){var t=e.components,n=(0,i.Z)(e,["components"]);return(0,r.kt)("wrapper",(0,a.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"ClientCredentials")," and traits ",(0,r.kt)("inlineCode",{parentName:"p"},"AccessTokenProvider")," and ",(0,r.kt)("inlineCode",{parentName:"p"},"TokenIntrospection")," expose methods that:"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Obtain token via ",(0,r.kt)("inlineCode",{parentName:"li"},"requestToken")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"introspect")," the token for it's details like ",(0,r.kt)("inlineCode",{parentName:"li"},"UserInfo"))),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"val accessTokenProvider = AccessTokenProvider.instance[IO](tokenUrl, clientId, clientSecret)\nval tokenIntrospection = TokenIntrospection.instance[IO](tokenIntrospectionUrl, clientId, clientSecret)\n  \nfor {\n  token <- accessTokenProvider.requestToken(scope) // ask for token\n  response <- tokenIntrospection.introspect(token.accessToken) // check if token is valid\n} yield response.active // is the token active?\n")),(0,r.kt)("h2",{id:"sttp-oauth2-backends"},(0,r.kt)("inlineCode",{parentName:"h2"},"sttp-oauth2")," backends"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"provide Client Credentials Backend, which is an interceptor for another backend and which can:",(0,r.kt)("ul",{parentName:"li"},(0,r.kt)("li",{parentName:"ul"},"fetch a token using ",(0,r.kt)("inlineCode",{parentName:"li"},"AccessTokenProvider")),(0,r.kt)("li",{parentName:"ul"},"reuse the token multiple times using a cache (default cache implementation may be overridden using appropriate constructor functions)"),(0,r.kt)("li",{parentName:"ul"},"fetch a new token if the previous one expires"),(0,r.kt)("li",{parentName:"ul"},"add an Authorization header to the intercepted request")))),(0,r.kt)("p",null,"Implementations:"),(0,r.kt)("table",null,(0,r.kt)("thead",{parentName:"table"},(0,r.kt)("tr",{parentName:"thead"},(0,r.kt)("th",{parentName:"tr",align:null},"module name"),(0,r.kt)("th",{parentName:"tr",align:null},"class name"),(0,r.kt)("th",{parentName:"tr",align:null},"default cache implementation"),(0,r.kt)("th",{parentName:"tr",align:null},"semaphore"),(0,r.kt)("th",{parentName:"tr",align:null},"notes"))),(0,r.kt)("tbody",{parentName:"table"},(0,r.kt)("tr",{parentName:"tbody"},(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"sttp-oauth2-backend-cats")),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"SttpOauth2ClientCredentialsCatsBackend")),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"cats-effect"),"'s ",(0,r.kt)("inlineCode",{parentName:"td"},"Ref")),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"cats-effect"),"'s ",(0,r.kt)("inlineCode",{parentName:"td"},"Semaphore")),(0,r.kt)("td",{parentName:"tr",align:null})),(0,r.kt)("tr",{parentName:"tbody"},(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"sttp-oauth2-backend-future")),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"SttpOauth2ClientCredentialsFutureBackend")),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"monix-execution"),"'s ",(0,r.kt)("inlineCode",{parentName:"td"},"AtomicAny")),(0,r.kt)("td",{parentName:"tr",align:null},(0,r.kt)("inlineCode",{parentName:"td"},"monix-execution"),"'s ",(0,r.kt)("inlineCode",{parentName:"td"},"AsyncSemaphore")),(0,r.kt)("td",{parentName:"tr",align:null},"It only uses submodule of whole ",(0,r.kt)("inlineCode",{parentName:"td"},"monix")," project")))))}c.isMDXComponent=!0}}]);