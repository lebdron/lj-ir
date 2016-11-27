/**
  * @author Yex
  */

package com.innopolis.ir

/** Represents different retrieval models. */
trait Retrieval {

  def executeQuery(query: String): Set[SearchIndexPosition]

}
