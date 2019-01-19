package com.github.vitaliihonta.mlspark

object Category {
  val Scientific: String    = "Scientific"
  val NonScientific: String = "NonScientific"
}

case class LabeledText(id: Long, category: String, text: String)
case class Article(id: Long, topic: String, text: String)
