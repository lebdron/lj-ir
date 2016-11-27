/**
  * @author Yex
  */

package com.innopolis.ir.token_processing

class NoTokenFilter extends TokenFilter {

  /**
    * @param token - token itself
    * @return - returns true
    */
  override def isOk(token: String): Boolean = {
    true
  }

}
