/**
  * @author Yex
  */

package com.innopolis.ir.tfidf_db_index

import java.io.File

import com.innopolis.ir.SearchIndexPosition

case class TfIdfDocumentPosition(file: File, weight: Double) extends SearchIndexPosition{

  override def toString: String = {
    file + " - " + weight.toString
  }

}
