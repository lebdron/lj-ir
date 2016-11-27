package com.innopolis.ir.token_processing

import edu.stanford.nlp.simple.Sentence

import scala.collection.JavaConversions._

/**
  * Created by andrey on 11/14/16.
  */
class LemmatizerTokenProcessor(parentTokenProcessor: TokenProcessor) extends TokenProcessor {
  /**
    * Process token.
    *
    * @param token - token itself
    * @return - returns processed token
    */
  override def processToken(token: String): String = new Sentence(token)
    .lemmas
    .head
}
