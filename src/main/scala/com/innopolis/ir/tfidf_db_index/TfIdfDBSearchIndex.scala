/**
  * @author Yex
  */

package com.innopolis.ir.tfidf_db_index

import java.io.File

import com.innopolis.ir.{SearchIndex, SearchIndexPosition}
import slick.jdbc.PostgresProfile.api._

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/** TfIdf index with DB storage.
  * Include following tables:
  *  - term(TERM_ID: Int, TERM: String, DOC_FREQ: String)
  *  - doc(DOC_ID: Int, HTTP: String)
  *  - terms_in_documents(ID: Int, TERM_ID: Int, DOC_ID: Int, WEIGHT: Double) term - doc weighting
  */
class TfIdfDBSearchIndex extends SearchIndex {

  val bufferSize = 100000

  // vocabulary and term frequency is stored in memory for optimization
  /** Term id and document frequency. */
  case class DocFrequency(var id: Int, var freq: Int)
  val docFrequencies = scala.collection.mutable.Map[String, DocFrequency]()
  var totalDocs = 0 // total documents in corpus

  val documentIds = scala.collection.mutable.Map[String, Int]()

  /** Term table */
  class Terms(tag: Tag) extends Table[(Int, String, Int)](tag, "TERMS") {
    def id = column[Int]("TERM_ID", O.PrimaryKey)
    def term = column[String]("TERM")
    def doc_freq = column[Int]("DOC_FREQ")

    def * = (id, term, doc_freq)
  }
  val terms = TableQuery[Terms]

  class Documents(tag: Tag) extends Table[(Int, String)](tag, "DOCUMENTS") {
    def id = column[Int]("DOC_ID", O.PrimaryKey, O.AutoInc)
    def http = column[String]("HTTP")

    def * = (id, http)
  }
  val documents = TableQuery[Documents]

  class TermsInDocuments(tag: Tag) extends Table[(Int, Int, Int, Double)](tag, "TERMS_IN_DOCUMENTS") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def term_id = column[Int]("TERM_ID")
    def doc_id = column[Int]("DOC_ID")
    def weight = column[Double]("WEIGHT")

    def term = foreignKey("TERM_ID", term_id, terms)(_.id)
    def document = foreignKey("DOC_ID", doc_id, documents)(_.id)

    def * = (id, term_id, doc_id, weight)
  }
  val termsInDocuments = TableQuery[TermsInDocuments]

//  val db = Database.forConfig("db") // for SQLite
  val db = Database.forURL("jdbc:postgresql://10.90.131.189:5432/ir_db", driver = "org.postgresql.Driver",
    user = "postgres", password = "postgressecret", keepAliveConnection = true)

  def closeDB() = {
    db.close()
  }

  /** Creates tables. */
  def setup() = {
    val setup = DBIO.seq((terms.schema ++ documents.schema ++ termsInDocuments.schema).create)
    Await.result(db.run(setup), Duration.Inf)
  }

  def load() = {
    docFrequencies.clear()
    Await.result(db.run(terms.result).map(_.foreach {
      case (id, term, freq) =>
        docFrequencies += (term -> DocFrequency(id, freq))
    }), Duration.Inf)

    documentIds.clear()
    Await.result(db.run(documents.result)
      .map(_.foreach{ e =>
        documentIds.put(e._2, e._1)
      }), Duration.Inf)
    totalDocs = Await.result(db.run(documents.length.result), Duration.Inf)
  }

  def getCoordinates(termsInDoc: List[String]): Map[String, Double] = {
    // count term frequency
    val vect = termsInDoc
      .foldLeft(Map.empty[String, Int]){ (count, word) => count + (word -> (count.getOrElse(word, 0) + 1)) }
      // filter terms that are both in query and documents (q AND d)
      .filterKeys(docFrequencies.contains)

      // compute tf.idf weights
      .map { case (t, f) =>
      (t, scala.math.log10(1 + f.toDouble) * scala.math.log10(totalDocs.toDouble / docFrequencies(t).freq))
    }

    // normalize vectors
    val magnitude = scala.math.sqrt(vect.foldLeft(0.0){ case (sum, (k, v)) => sum + v * v })
    vect.map{ case (t, f) => (t, f / magnitude) }
  }

  /** Adds document and terms from the document to the tables and counts document frequency.
    * If term is already in the table, increment doc_freq.
    */
  def addDocument(document: String, termsInDoc: Set[String]) = {
    /*val req = DBIO.seq(
      documents += (0, document)
    )
    Await.result(db.run(req), Duration.Inf)*/
    documentIds.getOrElseUpdate(document, documentIds.size)

    //if term in table, increment doc_freq
    //else insert term, 1
    termsInDoc.foreach {
      term =>
        if (!docFrequencies.contains(term))
          docFrequencies += (term -> DocFrequency(docFrequencies.size, 1))
        else
          docFrequencies(term).freq += 1
    }
  }

  def commitDocuments() = {
    Await.result(db.run(documents ++= documentIds.map(e => (e._2, e._1))), Duration.Inf)
    documentIds.clear()
  }

  /** Commits vocabulary to db. */
  def commitVocabulary() = {
    val req = DBIO.seq(
      terms ++= docFrequencies.map(term => (term._2.id, term._1, term._2.freq)).toList
    )
    Await.result(db.run(req), Duration.Inf)

    //load to update id for docFrequencies
//    totalDocs = documentIds.size
//    load()
    totalDocs = Await.result(db.run(documents.length.result), Duration.Inf)
  }

//  var cachedFilenameId = "" -> 0
//  var lastId = 0
  var positionsToAdd = ListBuffer[(Int, Int, Int, Double)]()
  // caching and buffering
  override def addPosition(term: String, position: SearchIndexPosition): Unit = {
    //get term id
    val term_id = docFrequencies(term).id

    //get doc id
    val filename = position.asInstanceOf[TfIdfDocumentPosition].file.getPath
    var doc_id = 0
    /*if (cachedFilenameId._1 == filename) {
      doc_id = cachedFilenameId._2
    } else {
      doc_id = Await.result(db.run(documents.filter(_.http === filename).map(t => t.id).result.head), Duration.Inf)
      cachedFilenameId = filename -> doc_id
    }*/
    /*if (!documentIds.contains(filename)){
      documentIds.clear()
      Await.result(db.run(documents.filter(_.id > lastId).filter(_.id < lastId + bufferSize).result)
        .map(_.foreach{ e =>
          documentIds.put(e._2, e._1)
        }), Duration.Inf)
    }*/
    doc_id = documentIds(filename)

    //insert (term_id, doc_id, weight)
    val weight = position.asInstanceOf[TfIdfDocumentPosition].weight
    positionsToAdd += ((0, term_id, doc_id, weight))
//    lastId = doc_id
  }

  def commitPositions() = {
    Await.result(db.run(termsInDocuments ++= positionsToAdd), Duration.Inf)
    positionsToAdd.clear()
  }

  //returns all positions for term
  override def getPositions(term: String): Set[SearchIndexPosition] = {
    // select http, weight from terms join termsInDocuments join documents
    Await.result(
      db.run(
        (terms.filter(_.id === docFrequencies(term).id) join termsInDocuments on (_.id === _.term_id)
          join documents on (_._2.doc_id === _.id))
          .map(r => r._2.http -> r._1._2.weight)
          .result
      )
    , Duration.Inf)
    .map(r => TfIdfDocumentPosition(new File(r._1), r._2)).toSet
  }

  def getVocabularySize: Int = docFrequencies.size

  override def getAllTerms: Set[String] = {
    Set[String]()
  }

  override def getAllPositions: Set[SearchIndexPosition] = {
    Set[SearchIndexPosition]()
  }

}
