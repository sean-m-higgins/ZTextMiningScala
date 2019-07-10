name := "ZTextMining"

version := "0.2"

scalaVersion := "2.11.12"

mainClass in assembly := Some("LDAExample")

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.1",
  "org.parboiled" %% "parboiled" % "2.1.4",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  //"org.jline" %% "jline" % "3.0.2",
  "org.apache.spark" %% "spark-core" % "2.4.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.4.0",
  "com.github.scopt" %% "scopt" % "3.6.0",
  "org.json4s" %% "json4s-native" % "3.2.10",
  //"com.novocode" %% "junit-interface" % "latest.release" % Test,
  "org.scalanlp" %% "breeze" % "0.13.2", //% "provided",
  "org.scalanlp" %% "breeze-natives" % "0.13.1", //% "provided",
  "org.scalanlp" %% "breeze-viz" % "0.13.1", //% "provided",
  "com.lihaoyi" %% "ammonite-ops" % "1.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "com.johnsnowlabs.nlp" %% "spark-nlp" % "1.8.3",
  "com.github.haifengl" %% "smile-scala" % "1.5.2"
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

