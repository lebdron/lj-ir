/**
  * @author Yex
  */

package com.innopolis.ir.tfidf_db_index

import com.innopolis.ir.{SearchIndex, SearchIndexBuilder}
import com.innopolis.ir.token_processing.{TokenFilter, TokenProcessor}
import com.innopolis.ir.tokenization.Tokenizer

class TfIdfDBSearchIndexBuilder(var tokenizer: Tokenizer, var tokenProcessor: TokenProcessor, var tokenFilter: TokenFilter) extends SearchIndexBuilder {

  override def buildIndex(directory: String): SearchIndex = {

    blackList += "postIDs"
    val collection = getCollection(directory)

    val index = new TfIdfDBSearchIndex
    index.setup()

    println("First pass")
    // first pass - get docs frequencies
    println("#docs | vocabulary.size")
    var totalDocs = 0 // total documents to see the Heap's law
    collection.foreach { file =>
      val termsInDoc = tokenizeFile(file).filter(tokenFilter.isOk).filter(_.nonEmpty).map(tokenProcessor.processToken).toSet
      if (termsInDoc.nonEmpty) {
        index.addDocument(file.getPath, termsInDoc)
        totalDocs += 1
        if (totalDocs % 1000 == 0) {
          println(totalDocs + "\t" + index.getVocabularySize)
        }
      }
      if (index.documentIds.size > index.bufferSize){
        index.commitDocuments()
      }
    }
    if (index.documentIds.nonEmpty){
      index.commitDocuments()
    }
    println("Saving vocabulary...")
    println(index.docFrequencies.size)
    index.commitVocabulary()
    println("Vocabulary is saved")

    println("Second pass")
    //second pass - weighting
    index.totalDocs = collection.length
    collection.foreach { file =>
      // add to index
      index.getCoordinates(tokenizeFile(file).filter(tokenFilter.isOk).filter(_.nonEmpty).map(tokenProcessor.processToken))
        .foreach {
        case (t, v) => index.addPosition(t, TfIdfDocumentPosition(file, v))
      }
      if (index.positionsToAdd.size > index.bufferSize) {
        index.commitPositions()
      }
    }
    if (index.positionsToAdd.nonEmpty) {
      index.commitPositions()
    }
    println("Index construction completed")

    index
  }
}
