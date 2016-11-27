package com.innopolis.ir.kgram_db_index

import com.innopolis.ir.SearchIndexPosition

/**
  * Created by andrey on 11/15/16.
  */
case class KGramTermPosition(rawTerm: String) extends SearchIndexPosition{
  override def toString: String = rawTerm
}
