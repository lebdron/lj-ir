package com.innopolis.ir.tolerant_retrieval

import com.innopolis.ir.SearchIndexPosition

/**
  * Created by andrey on 11/18/16.
  */
case class TolerantSearchPosition(term: String, score: Double) extends SearchIndexPosition{
  override def toString: String = f"$term - $score"
}
