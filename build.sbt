name := "mxtcash"
scalaVersion := "2.13.1"
herokuAppName in Compile := "mxtcash"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % "0.21.1",
  "org.http4s" %% "http4s-dsl" % "0.21.1",
  "dev.zio" %% "zio" % "1.0.0-RC17",
  "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10"
)
