name := "mxtcash"
scalaVersion := "2.13.1"
herokuAppName in Compile := "mxtcash"

enablePlugins(JavaAppPackaging)

val zioV     = "1.0.0-RC17"
val zioCatsV = "2.0.0.0-RC10"
val http4sV  = "0.21.1"
val slf4jV   = "1.7.30"

libraryDependencies ++= Seq(
  "dev.zio"      %% "zio"                 % zioV,
  "dev.zio"      %% "zio-interop-cats"    % zioCatsV,
  "org.http4s"   %% "http4s-blaze-server" % http4sV,
  "org.http4s"   %% "http4s-dsl"          % http4sV,
  "org.slf4j"    % "slf4j-simple"         % slf4jV
)

libraryDependencies ++= Seq(
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)
