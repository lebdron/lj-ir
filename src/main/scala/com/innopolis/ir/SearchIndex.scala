/**
 * @author Yex
 */

package com.innopolis.ir

/**
  * Indexes terms to positions.
  */
trait SearchIndex {

  /** Adds entry to index.
    *
    * @param term is a key of index
    * @param position is an index entry
    */
  def addPosition(term: String, position: SearchIndexPosition): Unit

  /**
    * Searches the index for term.
    *
    * @param term is a term that is looked for
    * @return set of positions for term
    */
  def getPositions(term: String): Set[SearchIndexPosition]

  /** Returns all terms in index. */
  def getAllTerms: Set[String]

  def getAllPositions: Set[SearchIndexPosition]

}
