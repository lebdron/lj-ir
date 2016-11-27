package com.innopolis.ir

import com.innopolis.ir.tfidf_db_index.TfIdfDocumentPosition
import com.innopolis.ir.tolerant_retrieval.TolerantRetrieval

/**
  * Created by mjazz on 18.11.16.
  */
trait EvalApp {
  val retrievalName: String
  val retrievalModel: Retrieval
  val tolerantRetrieval: TolerantRetrieval


  def printGreeting() = {
    println(retrievalName + ".")
  }

  /** Prints results of query. */
  def printResult(res: Set[SearchIndexPosition], count: Int): Unit = {
    res
      .toList
      .sortWith(_.asInstanceOf[TfIdfDocumentPosition].weight > _.asInstanceOf[TfIdfDocumentPosition].weight)
      .take(count)
      .foreach(println(_))

  }

  def main(args: Array[String]): Unit = {
    printGreeting()

    val file = scala.io.Source.fromFile("data/query-text")
    val text = file.mkString
    val queries = text.split("/").dropRight(1).map(_.replaceAll("[0-9]", "")).map(_.replaceAll("\n", "")).map(_.toLowerCase())

    val file1 = scala.io.Source.fromFile("data/rlv-ass")
    val text1 = file1.mkString
    val expected = text1.split("/")
      .dropRight(1)
      .map(_.replaceAll("  "," "))
      .map(_.replaceAll("   "," "))
      .map(_.replaceAll("    "," "))
      .map(_.replaceAll("     "," "))
      .map(_.replaceAll("\n[0-9]\n",""))
      .map(_.replaceAll("\n[0-9][0-9]\n",""))
      .map(_.replaceAll("\n "," "))
      .map(_.replaceAll("\n"," "))
      .map(_.replaceAll("  "," "))
      .map(_.replaceAll("   "," "))
      .map(_.replaceAll("    "," "))
      .map(_.replaceAll("     "," "))
      .map(_.dropRight(1))
      .map(_.drop(1))
      .map(_.split(" "))
      .toList
      .map(_.map(_.toInt))


    var totalPrecision = 0.0
    var queryPrecision = 0.0
    var trueRes = 0;
    var counter = 1;

    var count = 0
    for(query<- queries ) {
      var q = query
      println("QUERY #" + (count+1) +": " + q)
      println("Result:")
      try {
        q = tolerantRetrieval.replaceMissingTerms(q)
        val storage = retrievalModel.executeQuery(q)
          .toList
          .sortWith(_.asInstanceOf[TfIdfDocumentPosition].weight > _.asInstanceOf[TfIdfDocumentPosition].weight)
          .take(100)
          .toList.map(_.asInstanceOf[TfIdfDocumentPosition].file.toString)
          .map(_.replaceAll("data/docs/",""))
          .map(_.toInt)

        for (i <- 0 to 99){
          val instance = storage(i)
          if(expected(counter-1).toSet.contains(instance)){
            trueRes = trueRes + 1
            queryPrecision = queryPrecision + trueRes.toDouble/(i+1)
          }
        }


        printResult(retrievalModel.executeQuery(q), 100)

        if(queryPrecision != 0){
          queryPrecision = queryPrecision/trueRes;
          totalPrecision = totalPrecision + queryPrecision;
          println("AVERAGE PRECISION = "+queryPrecision)
          println("MEAN AVG PRECISION FOR QUREIES = "+totalPrecision.toDouble/counter.toDouble)
        }
        else {
          println("AVERAGE PRECISION = "+queryPrecision)
          println("MEAN AVG PRECISION FOR QUREIES = "+totalPrecision.toDouble/counter.toDouble)
        }
        queryPrecision = 0.0;
        trueRes = 0;
        counter = counter + 1;
        println()

      } catch {
        case e: UnsupportedOperationException => println("Wrong query: " + e)
      }
      count+=1
    }
  }

}
