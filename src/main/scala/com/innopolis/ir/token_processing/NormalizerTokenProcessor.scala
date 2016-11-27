/**
  * @author Yex
  */

package com.innopolis.ir.token_processing

/**
  * Normilize tokens by striping endings.
  *
  * @param parentTokenProcessor - upper layer of token processor
  */
class NormalizerTokenProcessor(parentTokenProcessor: TokenProcessor) extends TokenProcessor {

  def normalize(token: String): String = {
    var res = token.stripSuffix("\'s")
    res = res.stripSuffix("\'re")
    res = res.stripSuffix("\'ve")
    res = res.stripSuffix("\'m")
    res = res.stripSuffix("\'t")
    res = res.stripSuffix("\'d")
    res = res.stripSuffix("\'")
    res
  }

  /**
    * @param token - token itself
    * @return - returns soundex term
    */
  override def processToken(token: String): String = {
    normalize(parentTokenProcessor.processToken(token))
  }

}
