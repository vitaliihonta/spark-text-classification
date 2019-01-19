name := "SparkML"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= {
  val sparkV = "2.4.0"
  Seq(
    "org.apache.spark" %% "spark-sql" % sparkV,
    "org.apache.spark" %% "spark-mllib" % sparkV
  )
}