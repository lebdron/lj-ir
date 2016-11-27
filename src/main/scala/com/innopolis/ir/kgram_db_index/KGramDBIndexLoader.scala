package com.innopolis.ir.kgram_db_index

import com.innopolis.ir.{SearchIndex, SearchIndexBuilder}
import com.innopolis.ir.token_processing.{NoTokenProcessor, TokenFilter, TokenProcessor}
import com.innopolis.ir.tokenization.Tokenizer

/**
  * Created by andrey on 11/15/16.
  */
class KGramDBIndexLoader(k: Int, var tokenizer: Tokenizer, var tokenFilter: TokenFilter) extends SearchIndexBuilder {
  override var tokenProcessor: TokenProcessor = new NoTokenProcessor

  /**
    * Returns index built on the given corpus.
    *
    * @param directory path to the directory with a set of documents.
    */
  override def buildIndex(directory: String): SearchIndex = {
    val index = new KGramDBSearchIndex(k)
    index.load()
    index
  }

}
