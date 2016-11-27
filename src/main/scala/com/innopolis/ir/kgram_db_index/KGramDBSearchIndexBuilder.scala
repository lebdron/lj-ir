package com.innopolis.ir.kgram_db_index

import com.innopolis.ir.{SearchIndex, SearchIndexBuilder}
import com.innopolis.ir.token_processing.{NoTokenProcessor, TokenFilter, TokenProcessor}
import com.innopolis.ir.tokenization.Tokenizer

/**
  * Created by andrey on 11/15/16.
  */
class KGramDBSearchIndexBuilder(k: Int, var tokenizer: Tokenizer, var tokenFilter: TokenFilter) extends SearchIndexBuilder {
  override var tokenProcessor: TokenProcessor = new NoTokenProcessor

  /**
    * Returns index built on the given corpus.
    *
    * @param directory path to the directory with a set of documents.
    */
  override def buildIndex(directory: String): SearchIndex = {

    blackList += "postIDs"
    val collection = getCollection(directory)

    val index = new KGramDBSearchIndex(k)
    index.setup()

    println("First pass")
    collection.foreach { file =>
      val termsInDoc = tokenizeFile(file).filter(tokenFilter.isOk).filter(_.nonEmpty).map(tokenProcessor.processToken).toSet
      termsInDoc.filter(_.length >= k).foreach(e => index.addTerm(e, e.sliding(k).toSet))
          /*totalDocs += 1
          if (totalDocs % 1000 == 0) {
            println(totalDocs + "\t" + index.getVocabularySize)
          }*/
    }
    println(f"Saving vocabulary and ${k}grams")
    index.commitVocabulary()
    println("Saved")

    println("Second pass")
    index.getAllPositions.foreach { t =>
      t.asInstanceOf[KGramTermPosition].rawTerm.sliding(k).toSet
        .foreach {
        (g: String) => index.addPosition(g, t)
      }
      if (index.positionsToAdd.size > 100000) {
        index.commitPositions()
      }
    }
    if (index.positionsToAdd.nonEmpty) {
      index.commitPositions()
    }
    println("Done")

    index
  }

}
