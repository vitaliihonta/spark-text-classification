package com.github.vitaliihonta.mlspark

import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.ml._
import org.apache.spark.ml.feature.{HashingTF, RegexTokenizer}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.tuning.CrossValidator
import org.apache.spark.ml.param._
import org.apache.spark.ml.tuning.ParamGridBuilder

object Training {
  def main(args: Array[String]): Unit =
    using(SparkSession.builder().appName("text-classification").master("local[*]").getOrCreate()) { spark =>
      import spark.implicits._

      val data = List(
        LabeledText(0, Category.Scientific, "hello world"),
        LabeledText(1, Category.NonScientific, "witaj swiecie")
      ).toDS

      val articles = Seq(
        Article(0, "sci.math", "Hello, Math!"),
        Article(1, "alt.religion", "Hello, Religion!"),
        Article(2, "sci.physics", "Hello, Physics!"),
        Article(3, "sci.math", "Hello, Math Revised!"),
        Article(4, "sci.math", "Better Math"),
        Article(5, "alt.religion", "TGIF")
      ).toDS

      withPrettySep("Articles") {
        articles.show()
      }

      val topic2Label: Boolean => Double = isSci => if (isSci) 1 else 0
      val toLabel                        = udf(topic2Label)

      val labelled = articles.withColumn("label", toLabel($"topic".like("sci%"))).cache

      val Array(trainDF, testDF) = labelled.randomSplit(Array(0.75, 0.25))

      withPrettySep("Training dataset") {
        trainDF.show()
      }
      withPrettySep("Test dataset") {
        testDF.show()
      }

      val tokenizer = new RegexTokenizer()
        .setInputCol("text")
        .setOutputCol("words")

      val hashingTF = new HashingTF()
        .setInputCol(tokenizer.getOutputCol) // it does not wire transformers -- it's just a column name
        .setOutputCol("features")
        .setNumFeatures(5000)

      val lr = new LogisticRegression().setMaxIter(20).setRegParam(0.01)

      val pipeline = new Pipeline().setStages(Array(tokenizer, hashingTF, lr))

      val model = pipeline.fit(trainDF)

      val trainPredictions = model.transform(trainDF)

      withPrettySep("Train prediction") {
        trainPredictions
          .select($"id", $"topic", $"text", $"label", $"prediction")
          .show
      }

      val evaluator       = new BinaryClassificationEvaluator().setMetricName("areaUnderROC")
      val evaluatorParams = ParamMap(evaluator.metricName -> "areaUnderROC")

      val paramGrid = new ParamGridBuilder()
        .addGrid(hashingTF.numFeatures, Array(100, 1000))
        .addGrid(lr.regParam, Array(0.05, 0.2))
        .addGrid(lr.maxIter, Array(5, 10, 15))
        .build

      val cv = new CrossValidator()
        .setEstimator(pipeline)
        .setEstimatorParamMaps(paramGrid)
        .setEvaluator(evaluator)
        .setNumFolds(10)

      val cvModel = cv.fit(trainDF)

      val cvPredictions = cvModel.transform(testDF)

      withPrettySep("CV prediction") {
        cvPredictions
          .select('topic, 'text, 'prediction)
          .show
      }

      cvModel.write.overwrite.save(".model")
    }
}
