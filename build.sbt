name := "myshop"

version := "1.0"

lazy val `myshop` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(jdbc, ehcache, ws, specs2 % Test, guice)

libraryDependencies ++= Seq(
	"com.typesafe.play" %% "play-slick" % "5.0.0",
	"com.typesafe.slick" %% "slick-codegen" % "3.3.3",
	"com.typesafe.play" %% "play-json" % "2.9.2",
	"mysql" % "mysql-connector-java" % "8.0.28",
	"com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
	"org.mindrot" % "jbcrypt" % "0.4",
	"commons-codec" % "commons-codec" % "1.15",
	"org.typelevel" %% "cats-core" % "2.7.0"
)