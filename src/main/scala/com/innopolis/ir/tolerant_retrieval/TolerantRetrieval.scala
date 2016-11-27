package com.innopolis.ir.tolerant_retrieval

import com.innopolis.ir.kgram_db_index.{KGramDBSearchIndex, KGramTermPosition}
import com.innopolis.ir.{Retrieval, SearchIndexBuilder, SearchIndexPosition}

import scala.collection.mutable

/**
  * Created by andrey on 11/18/16.
  */
class TolerantRetrieval(k: Int, corpus: String, indexBuilder: SearchIndexBuilder) extends Retrieval{

  val tokenizer = indexBuilder.tokenizer

  // defines how tokens are processed in indexBuilder and queries
  val tokenProcessor = indexBuilder.tokenProcessor
  val tokenFilter = indexBuilder.tokenFilter

  var index: KGramDBSearchIndex = indexBuilder.buildIndex(corpus).asInstanceOf[KGramDBSearchIndex]

  override def executeQuery(query: String): Set[SearchIndexPosition] = {
    var resultSet = Set[SearchIndexPosition]()
    val grams = query.sliding(k).toSet

    grams.foreach(resultSet ++= index.getPositions(_))
    resultSet.map(_.asInstanceOf[KGramTermPosition])
      .map(e => (e.rawTerm, (e.rawTerm.sliding(k).toSet & grams).size.toDouble
        / (e.rawTerm.sliding(k).toSet | grams).size))
      .map(e => TolerantSearchPosition(e._1, e._2))
  }

  def replaceMissingTerms(query: String): String = {
    var tokens = tokenizer.tokenize(query).filter(tokenFilter.isOk)
    var result = mutable.ListBuffer[String]()
    tokens.foreach {
      e => if (e.length > 1 && !index.rawTermIds.keySet.contains(e)){
        result += executeQuery(e).toList.sortWith(_.asInstanceOf[TolerantSearchPosition].score > _.asInstanceOf[TolerantSearchPosition].score)
          .head.asInstanceOf[TolerantSearchPosition].term
      }
      else {
        result += e
      }
    }
    result.mkString(" ")
  }
}
