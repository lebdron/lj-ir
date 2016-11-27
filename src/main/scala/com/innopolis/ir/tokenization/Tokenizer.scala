package com.innopolis.ir.tokenization

/**
  * Created by andrey on 11/14/16.
  */
trait Tokenizer {

  def tokenize(document: String): List[String]
}
