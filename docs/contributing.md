---
sidebar_position: 5
description: Contributing
---

# Contributing to sttp-oauth2

This is an early stage project. The codebase is prone to change. All suggestions are welcome, please create an issue describing your problem before contributing.

Please make sure to format the code with [scalafmt](https://scalameta.org/scalafmt/) using `.scalafmt.conf` from the repository root.

## Working with documentation

The documentation is built using [mdoc](https://github.com/scalameta/mdoc) combined with [docusaurus v2](https://docusaurus.io/). 

To build the documentation make sure you have installed Node.js and Yarn according to [docusaurus requirements](https://docusaurus.io/docs/installation#requirements). 

### Working on documentation locally using live reload

For live reload you'd preferably need two console windows open. In both you should navigate to your repository root, then:

In the first terminal, launch `sbt` shell and run `docs/mdoc --watch`.

In the second one run:
```shell
cd ./website
npx docusaurus start
```

If it's your first time, remember to run `npm install` in the `./website` directory


### Documentation build workflow

The raw documentation goes through a few steps process before the final website is created.

- Raw data resides in `./docs` directory, it follows regular [docusaurus](https://docusaurus.io/) rules regarding creating documentation
- The first step when building the documentation is to run `docs/mdoc/`. This step compiles the code examples, verifying if everything makes sense and is up to date.
- When the build finishes, the compiled documentation ends up in `./mdoc/target/mdoc/`
- The last step is to build docusaurus. Docusaurus is configured to read files from `./mdoc/target/mdoc/` and generate the website using regular docusaurus rules.
