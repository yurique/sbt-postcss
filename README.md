sbt-postcss
================

[sbt-web](https://github.com/sbt/sbt-web) plugin that runs [Autoprefixer](https://github.com/ai/autoprefixer) using postcss to post-process CSS and add vendor prefixes to rules by [Can I Use](http://caniuse.com).

To use the latest version from Github, add the following to the `project/plugins.sbt` of your project:

```scala
    lazy val root = project.in(file(".")).dependsOn(sbtPostcss)
    lazy val sbtPostcss = uri("git://github.com/kotonoha/sbt-postcss")
```

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

```scala
    lazy val root = (project in file(".")).enablePlugins(SbtWeb)
```

Declare the execution order of the asset pipeline:
```scala
pipelineStages in Assets := Seq(postcss)
```


You will need to add following lines to your `package.json`:

```json
{
  "devDependencies": {
    "autoprefixer": "^6.3.7",
    "postcss-cli": "^2.5.2"
  }
}
```

To include all CSS files for post processing

```scala
includeFilter in postcss := GlobFilter("*.css")
```


### Acknowledgments

This plugin was built on [matthewrennie/sbt-autoprefixer](https://github.com/matthewrennie/sbt-autoprefixer).
