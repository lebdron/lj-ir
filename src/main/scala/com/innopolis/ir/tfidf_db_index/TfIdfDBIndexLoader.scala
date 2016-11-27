/**
  * @author Yex
  */

package com.innopolis.ir.tfidf_db_index

import com.innopolis.ir.token_processing.{TokenFilter, TokenProcessor}
import com.innopolis.ir.tokenization.Tokenizer
import com.innopolis.ir.{SearchIndex, SearchIndexBuilder}

/** Build index (actually loads) from DB. */
class TfIdfDBIndexLoader(var tokenizer: Tokenizer, var tokenProcessor: TokenProcessor, var tokenFilter: TokenFilter) extends SearchIndexBuilder {

  /** Loads index from DB. */
  override def buildIndex(directory: String): SearchIndex = {
    val index = new TfIdfDBSearchIndex
    index.load()
    index
  }
}
