name := "mxtcash"
version := "0.0.1"
scalaVersion := "2.13.1"
herokuAppName in Compile := "mxtcash"
herokuJdkVersion in Compile := "11"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerBaseImage := "openjdk:jre"
dockerRepository := Some("maxticorn")

val zioV     = "1.0.0-RC18-2"
val zioCatsV = "2.0.0.0-RC12"
val http4sV  = "0.21.1"
val tsecV    = "0.2.0"
val doobieV  = "0.8.8"
val slf4jV   = "1.7.30"

libraryDependencies ++= Seq(
  "dev.zio"            %% "zio"                 % zioV,
  "dev.zio"            %% "zio-interop-cats"    % zioCatsV,
  "org.http4s"         %% "http4s-blaze-server" % http4sV,
  "org.http4s"         %% "http4s-dsl"          % http4sV,
  "org.http4s"         %% "http4s-circe"        % http4sV,
  "io.github.jmcardon" %% "tsec-http4s"         % tsecV,
  "org.tpolecat"       %% "doobie-core"         % doobieV,
  "org.tpolecat"       %% "doobie-h2"           % doobieV,
  "org.slf4j"          % "slf4j-simple"         % slf4jV
)

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
