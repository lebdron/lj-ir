/**
  * @author Yex
  */

package com.innopolis.ir.token_processing

import scala.util.Try

/**
  * Filter numbers.
  */
class NumberFilter(parentTokenFilter: TokenFilter) extends TokenFilter {

  /**
    *
    * @param token - token itself
    * @return true if token is not number
    */
  override def isOk(token: String): Boolean = {
    !Try(token.toDouble).isSuccess && parentTokenFilter.isOk(token)
  }

}
