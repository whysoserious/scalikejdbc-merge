scalaVersion := "2.10.4"

val scalikeJdbcVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.scalikejdbc"         %% "scalikejdbc"          % scalikeJdbcVersion,
  "org.scalikejdbc"         %% "scalikejdbc-config"   % scalikeJdbcVersion,
 // "org.scalikejdbc"         %% "scalikejdbc-syntax-support-macro" % scalikeJdbcVersion,
  "com.h2database"          %  "h2"                   % "1.4.178",
  "ch.qos.logback"          %  "logback-classic"      % "1.1.2"
)
