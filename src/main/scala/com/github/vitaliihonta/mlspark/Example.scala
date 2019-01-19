package com.github.vitaliihonta.mlspark

import org.apache.spark.sql._
import org.apache.spark.ml.tuning.CrossValidatorModel

object Example {
  def main(args: Array[String]): Unit =
    using(SparkSession.builder().appName("text-classification").master("local[*]").getOrCreate()) { spark =>
      import spark.implicits._

      val cvModel = CrossValidatorModel.load(".model")
      val articles = Seq(
        Article(0, "sci.scala", "Thinking functional"),
        Article(1, "alt.religion", "Bible"),
        Article(2, "sci.physics", "Black holes and revelations"),
        Article(3, "sci.math", "Category Theory"),
        Article(4, "sci.math", "Set Theory"),
        Article(5, "alt.religion", "Bible 2")
      ).toDS

      val withPredictions = cvModel.transform(articles)

      withPrettySep("Predictions") {
        withPredictions.show()
      }
    }
}
