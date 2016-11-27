package com.innopolis.ir.kgram_db_index

import com.innopolis.ir.{SearchIndex, SearchIndexPosition}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * Created by andrey on 11/15/16.
  * raw_terms(RAW_TERM_ID: Int, RAW_TERM: String)
  * kgram(GRAM_ID: Int, GRAM: String)
  * kgrams_in_terms(ID: Int, GRAM_ID: Int, TERM_ID: Int)
  */
class KGramDBSearchIndex(k: Int) extends SearchIndex{
//  Term to ID mapping
  val rawTermIds = mutable.Map[String, Int]()

//  Gram to ID mapping
  val gramIds = mutable.Map[String, Int]()

  class RawTerms(tag: Tag) extends Table[(Int, String)](tag, "RAW_TERMS") {
    def id = column[Int]("RAW_TERM_ID", O.PrimaryKey)
    def raw_term = column[String]("RAW_TERM")

    def * = (id, raw_term)
  }
  val rawTerms = TableQuery[RawTerms]

//  Gram table
  class Grams(tag: Tag) extends Table[(Int, String)](tag, f"${k}GRAMS") {
    def id = column[Int]("GRAM_ID", O.PrimaryKey)
    def gram = column[String]("GRAM")

    override def * : ProvenShape[(Int, String)] = (id, gram)
  }
  val grams = TableQuery[Grams]

  class gramsInTerms(tag: Tag) extends Table[(Int, Int, Int)](tag, f"${k}GRAMS_IN_TERMS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def gram_id = column[Int]("GRAM_ID")
    def raw_term_id = column[Int]("RAW_TERM_ID")

    def gram = foreignKey("GRAM_ID", gram_id, grams)(_.id)
    def raw_term = foreignKey("RAW_TERM_ID", raw_term_id, rawTerms)(_.id)

    override def * : ProvenShape[(Int, Int, Int)] = (id, gram_id, raw_term_id)
  }
  val gramsInTerms = TableQuery[gramsInTerms]

  // TODO remove code duplication
  val db = Database.forURL("jdbc:postgresql://10.90.131.189:5432/ir_db", driver = "org.postgresql.Driver",
    user = "postgres", password = "postgressecret", keepAliveConnection = true)

  def closeDB() = {
    db.close()
  }

//  Create tables
  def setup() = {
    val setup = DBIO.seq((rawTerms.schema ++ grams.schema ++ gramsInTerms.schema).create)
    Await.result(db.run(setup), Duration.Inf)
  }

  def load() = {
    rawTermIds.clear()
    Await.result(db.run(rawTerms.result)
      .map(_.foreach(e => rawTermIds += (e._2 -> e._1))), Duration.Inf)
    gramIds.clear()
    Await.result(db.run(grams.result)
      .map(_.foreach(e => gramIds += (e._2 -> e._1))), Duration.Inf)
  }

  def addTerm(term: String, grams: Set[String]) = {
    rawTermIds.getOrElseUpdate(term, rawTermIds.size)
    grams.foreach(gramIds.getOrElseUpdate(_, gramIds.size))
  }

  def commitVocabulary(): Unit = {
    Await.result(db.run(rawTerms ++= rawTermIds.map(e => (e._2, e._1))), Duration.Inf)
    Await.result(db.run(grams ++= gramIds.map(e => (e._2, e._1))), Duration.Inf)
  }

  var positionsToAdd = mutable.ListBuffer[(Int, Int, Int)]()

  /** Adds entry to index.
    *
    * @param gram     is a key of index
    * @param position is an index entry
    */
  override def addPosition(gram: String, position: SearchIndexPosition): Unit = {
    val gram_id = gramIds(gram)
    val term = position.asInstanceOf[KGramTermPosition].rawTerm
    val term_id = rawTermIds(term)

    positionsToAdd += ((0, gram_id, term_id))
  }

  def commitPositions() = {
    Await.result(db.run(gramsInTerms ++= positionsToAdd), Duration.Inf)
    positionsToAdd = mutable.ListBuffer[(Int, Int, Int)]()
  }

  /**
    * Searches the index for term.
    *
    * @param gram is a gram that is looked for
    * @return set of positions for gram
    */
  override def getPositions(gram: String): Set[SearchIndexPosition] = {
    if (!gramIds.contains(gram)){
      return Set[SearchIndexPosition]()
    }
    Await.result(db.run((grams.filter(_.id === gramIds(gram))
      join gramsInTerms on (_.id === _.gram_id)
      join rawTerms on (_._2.raw_term_id === _.id))
      .map(_._2.raw_term).result), Duration.Inf)
      .map(KGramTermPosition)
      .toSet
  }

  /** Returns all terms in index. */
  override def getAllTerms: Set[String] = gramIds.keys.toSet

  override def getAllPositions: Set[SearchIndexPosition] = rawTermIds.keys.map(KGramTermPosition).toSet
}
