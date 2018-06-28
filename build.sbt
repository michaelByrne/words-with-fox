name := "words-with-fox"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaV = "2.4.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "org.jsoup" % "jsoup" % "1.8+",
    "commons-validator" % "commons-validator" % "1.5+",
    "org.scalikejdbc" %% "scalikejdbc" % "3.2.2",
    "com.h2database" % "h2" % "1.4.197",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}