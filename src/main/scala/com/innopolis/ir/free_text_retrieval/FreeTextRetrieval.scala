/**
  * @author Yex
  */

package com.innopolis.ir.free_text_retrieval

import java.io.File

import com.innopolis.ir.{Retrieval, SearchIndexBuilder, SearchIndexPosition}
import com.innopolis.ir.tfidf_db_index.{TfIdfDBSearchIndex, TfIdfDocumentPosition}

/** Handles free text queries. */
class FreeTextRetrieval(corpus: String, indexBuilder: SearchIndexBuilder) extends Retrieval {

  val tokenizer = indexBuilder.tokenizer

  // defines how tokens are processed in indexBuilder and queries
  val tokenProcessor = indexBuilder.tokenProcessor
  val tokenFilter = indexBuilder.tokenFilter

  var index: TfIdfDBSearchIndex = indexBuilder.buildIndex(corpus).asInstanceOf[TfIdfDBSearchIndex]

  def executeQuery(query: String): Set[SearchIndexPosition] = {
    val queryTokens = tokenizer.tokenize(query).filter(tokenFilter.isOk).map(tokenProcessor.processToken)

    val scores = scala.collection.mutable.Map[File, Double]()
    // compute VSM coordinates for the query
    index.getCoordinates(queryTokens)
      .filter(t => t._2 != 0.0)
      .foreach(
        t => index.getPositions(t._1).foreach(
          pos => {
            val file = pos.asInstanceOf[TfIdfDocumentPosition].file
            val weight = pos.asInstanceOf[TfIdfDocumentPosition].weight * t._2
            if (scores.contains(file))
              scores(file) += weight
            else
              scores += (file -> weight)
          }
        )
      )

    var res = Set[SearchIndexPosition]()
    scores.foreach( t => res += TfIdfDocumentPosition(t._1, t._2) )
    res
  }

}
