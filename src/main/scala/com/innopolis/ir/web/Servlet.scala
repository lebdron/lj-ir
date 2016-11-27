package com.innopolis.ir.web

import com.innopolis.ir.free_text_retrieval.FreeTextRetrieval
import com.innopolis.ir.kgram_db_index.KGramDBIndexLoader
import com.innopolis.ir.tfidf_db_index.{TfIdfDBIndexLoader, TfIdfDocumentPosition}
import com.innopolis.ir.token_processing.{LemmatizerTokenProcessor, NoTokenFilter, NoTokenProcessor, StrangeSymbolFilter}
import com.innopolis.ir.tokenization.{LanguageProcessorTokenizer, LemmatizingTokenizer}
import com.innopolis.ir.tolerant_retrieval.TolerantRetrieval

class Servlet extends Stack {
  val k = 2

  val tokenizer = new LemmatizingTokenizer
  val tolerantTokenizer = new LanguageProcessorTokenizer()
  val tokenFilter = new StrangeSymbolFilter(new NoTokenFilter())

  val tokenProcessor = new LemmatizerTokenProcessor(new NoTokenProcessor())

  val tfIdfIndex = new TfIdfDBIndexLoader(tokenizer, tokenProcessor, tokenFilter)

  val kGramIndex = new KGramDBIndexLoader(k, tolerantTokenizer, tokenFilter)

  val freeTextRetrieval = new FreeTextRetrieval("", tfIdfIndex)
  val tolerantRetrieval = new TolerantRetrieval(k, "", kGramIndex)

  get("/") {
    contentType = "text/html"

    var result = List[QueryResult]()
    var query = ""

    if (params.contains("query")) {
      query = params.get("query").get
      if (query.nonEmpty) {
        query = tolerantRetrieval.replaceMissingTerms(query)
        result = freeTextRetrieval.executeQuery(query).toList
          .sortWith(_.asInstanceOf[TfIdfDocumentPosition].weight > _.asInstanceOf[TfIdfDocumentPosition].weight)
          .take(10)
          .map(e => new QueryResult(e.asInstanceOf[TfIdfDocumentPosition].file))
      }
      else {
        redirect("/")
      }
    }

    jade("index", "result" -> result, "query" -> query)
  }

}
