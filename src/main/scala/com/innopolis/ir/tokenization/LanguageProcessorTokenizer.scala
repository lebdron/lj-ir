package com.innopolis.ir.tokenization

import edu.stanford.nlp.simple.Document

import scala.collection.JavaConversions._

/**
  * Created by andrey on 11/14/16.
  */
class LanguageProcessorTokenizer extends Tokenizer{
  override def tokenize(document: String): List[String] = new Document(document)
    .sentences
    .flatMap(_.words)
    .map(_.toLowerCase)
    .toList
}
