/**
  * @author Yex
  */

package com.innopolis.ir.token_processing

/**
  * Filter tokens.
  */
trait TokenFilter {

  def isOk(token: String): Boolean

}
