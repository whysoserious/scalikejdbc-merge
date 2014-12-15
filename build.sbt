scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc"         %% "scalikejdbc"          % "2.0.0",
  "org.scalikejdbc"         %% "scalikejdbc-config"   % "2.0.0",
  "com.h2database"          %  "h2"                   % "1.4.178",
  "ch.qos.logback"          %  "logback-classic"      % "1.1.2"
)
