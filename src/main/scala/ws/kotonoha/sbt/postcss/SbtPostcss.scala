package ws.kotonoha.sbt.postcss

import sbt._
import sbt.Keys._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.jse.{SbtJsEngine, SbtJsTask}

object Import {

  val postcss = TaskKey[Pipeline.Stage]("postcss", "Parse CSS and adds vendor prefixes to rules by Can I Use")

  object PostcssKeys {
    val buildDir = SettingKey[File]("postcss-build-dir", "Where autoprefixer will read from.")
    val autoprefixer = SettingKey[Seq[String]]("autoprefixer", "Options for autoprefixer")
  }

}

object SbtPostcss extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsEngine.autoImport.JsEngineKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport._
  import PostcssKeys._

  override def projectSettings = Seq(
    buildDir := (resourceManaged in postcss).value / "build" / "postcss",
    excludeFilter in postcss := HiddenFileFilter,
    includeFilter in postcss := GlobFilter("*.css"),
    resourceManaged in postcss := webTarget.value / postcss.key.label,
    postcss := runPostcss.dependsOn(WebKeys.nodeModules in Assets).value,
    autoprefixer := Seq("--use", "autoprefixer", "--cascade", "--browsers", "> 1%")
  )

  private def runPostcss: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>

      val include = (includeFilter in postcss).value
      val exclude = (excludeFilter in postcss).value
      val autoprefixerMappings = mappings.filter(f => !f._1.isDirectory && include.accept(f._1) && !exclude.accept(f._1))


      val cacheDirectory = streams.value.cacheDirectory / postcss.key.label

      val groups = autoprefixerMappings.map { case (f, p) =>
          val idx = p.lastIndexOf(Path.sep)
          val key = if (idx < 1) "" else p.substring(0, idx)
          f -> key
      }.toMap


      val runUpdate = FileFunction.cached(cacheDirectory, FilesInfo.hash) {
        inputFiles =>
          streams.value.log.info(s"Processing CSS from ${inputFiles.size} files")

          val grouped = inputFiles.groupBy(groups)

          for ((key, items) <- grouped) {
            val inputFileArgs = items.map(_.getPath)

            val allArgs = autoprefixer.value ++
              Seq("--dir", (buildDir.value / key).getPath) ++
              inputFileArgs

            streams.value.log.info(allArgs.mkString(" "))

            SbtJsTask.executeJs(
              state = state.value,
              engineType = (engineType in postcss).value,
              command = (command in postcss).value,
              nodeModules = (nodeModuleDirectories in Assets).value.map(_.getPath),
              shellSource = (nodeModuleDirectories in Assets).value.last / "postcss-cli" / "bin" / "postcss",
              args = allArgs,
              timeout = (timeoutPerSource in postcss).value * autoprefixerMappings.size
            )
          }

          buildDir.value.***.get.filter(!_.isDirectory).toSet
      }

      val autoPrefixedMappings = runUpdate(autoprefixerMappings.map(_._1).toSet).pair(relativeTo(buildDir.value))
      (mappings.toSet -- autoprefixerMappings ++ autoPrefixedMappings).toSeq
  }

}
