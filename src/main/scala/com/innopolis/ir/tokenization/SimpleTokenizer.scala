package com.innopolis.ir.tokenization

/**
  * Created by andrey on 11/14/16.
  */
class SimpleTokenizer extends Tokenizer{

  val delimeters = "[\\s\\*\\?!.,;:=\\-\"\\(\\)]"

  override def tokenize(document: String): List[String] = document
    .split(delimeters)
    .map(_.trim)
    .map(token => token.toLowerCase)
    .toList
}
