package com.innopolis.ir.tokenization

import edu.stanford.nlp.simple.Document
import scala.collection.JavaConversions._

/**
  * Created by andrey on 11/19/16.
  */
class LemmatizingTokenizer extends Tokenizer{
  override def tokenize(document: String): List[String] = new Document(document)
    .sentences
    .flatMap(_.lemmas)
    .map(_.toLowerCase)
    .toList
}
