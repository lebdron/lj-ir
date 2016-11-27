import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.earldouglas.xwp.JettyPlugin
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object LjirBuild extends Build {
  val Organization = "com.innopolis"
  val Name = "lj-ir"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.11.8"
  val ScalatraVersion = "2.5.0"

  lazy val project = Project (
    "lj-ir",
    file("."),
    settings = ScalatraPlugin.scalatraSettings ++ scalateSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "ch.qos.logback" % "logback-classic" % "1.1.5" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "9.2.15.v20160210" % "container",
        "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
        "com.typesafe.slick" %% "slick" % "3.2.0-M1",
        "org.postgresql" % "postgresql" % "9.4.1212",
        "com.google.protobuf" % "protobuf-java" % "2.6.1",
        "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0",
        "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models",
        "edu.stanford.nlp" % "stanford-corenlp" % "3.6.0" classifier "models-english",
        "org.jsoup" % "jsoup" % "1.10.1"
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      },
      javaOptions ++= Seq(
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
      )
    )
  ).enablePlugins(JettyPlugin)
}
