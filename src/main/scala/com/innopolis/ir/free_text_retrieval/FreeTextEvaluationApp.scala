package com.innopolis.ir.free_text_retrieval

import com.innopolis.ir.EvalApp
import com.innopolis.ir.kgram_db_index.KGramDBIndexLoader
import com.innopolis.ir.tfidf_db_index.TfIdfDBIndexLoader
import com.innopolis.ir.token_processing.{NoTokenFilter, NoTokenProcessor, StrangeSymbolFilter}
import com.innopolis.ir.tokenization.{LanguageProcessorTokenizer, LemmatizingTokenizer, SimpleTokenizer}
import com.innopolis.ir.tolerant_retrieval.TolerantRetrieval

/**
  * Created by mjazz on 18.11.16.
  */
object FreeTextEvaluationApp extends EvalApp{


  val corpus = "data/docs"
  val tokenProcessor = new NoTokenProcessor()
  val tokenFilter = new StrangeSymbolFilter(new NoTokenFilter())
  val tokenizer = new LemmatizingTokenizer
  val indexBuilder = new TfIdfDBIndexLoader(tokenizer, tokenProcessor, tokenFilter)

  val k = 2
  val tolerantTokenizer = new LanguageProcessorTokenizer
  val kGramIndex = new KGramDBIndexLoader(k, tolerantTokenizer, tokenFilter)
  val tolerantRetrieval = new TolerantRetrieval(k, "", kGramIndex)

  override val retrievalName = "Free text retrieval evaluation"
  override val retrievalModel = new FreeTextRetrieval(corpus, indexBuilder)



}
