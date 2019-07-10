// You may use this file to add plugin dependencies for sbt.

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")


// from https://github.com/databricks/spark-corenlp/blob/master/project/plugins.sbt
resolvers += "Spark Packages repo" at "https://dl.bintray.com/spark-packages/maven/"

addSbtPlugin("org.spark-packages" %% "sbt-spark-package" % "0.2.6")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.1")