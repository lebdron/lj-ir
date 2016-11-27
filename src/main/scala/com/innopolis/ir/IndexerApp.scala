package com.innopolis.ir

import com.innopolis.ir.free_text_retrieval.FreeTextRetrieval
import com.innopolis.ir.kgram_db_index.KGramDBSearchIndexBuilder
import com.innopolis.ir.tfidf_db_index.TfIdfDBSearchIndexBuilder
import com.innopolis.ir.token_processing._
import com.innopolis.ir.tokenization.{LanguageProcessorTokenizer, LemmatizingTokenizer}
import com.innopolis.ir.tolerant_retrieval.TolerantRetrieval

/**
  * Created by andrey on 11/18/16.
  */
object IndexerApp {
  val corpus = "data/lj"
  val tokenProcessor = new NoTokenProcessor()
  val tokenFilter = new StrangeSymbolFilter(new NoTokenFilter())
  val tokenizer = new LemmatizingTokenizer
  val tolerantTokenizer = new LanguageProcessorTokenizer
  val indexBuilder = new TfIdfDBSearchIndexBuilder(tokenizer, tokenProcessor, tokenFilter)
  val tolerantIndexBuilder = new KGramDBSearchIndexBuilder(2, tolerantTokenizer, tokenFilter)

  val retrievalName = "Free text retrieval"

  def main(args: Array[String]): Unit = {
    new FreeTextRetrieval(corpus, indexBuilder)
    new TolerantRetrieval(2, corpus, tolerantIndexBuilder)
  }
}
