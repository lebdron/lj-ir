/**
  * @author Yex
  */

package com.innopolis.ir

import com.innopolis.ir.token_processing.{TokenFilter, TokenProcessor}

/** Builds index from given collection. */
trait SearchIndexBuilder extends IndexBuilder {

  var tokenProcessor: TokenProcessor
  var tokenFilter : TokenFilter

  /**
    * Returns index built on the given corpus.
    * @param directory path to the directory with a set of documents.
    */
  def buildIndex(directory: String): SearchIndex

}
