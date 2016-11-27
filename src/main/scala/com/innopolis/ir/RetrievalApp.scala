/**
  * @author Yex
  */

package com.innopolis.ir

import scala.io.StdIn.readLine

/** Main console program for retrieval. */
trait RetrievalApp {

  val retrievalName: String
  val retrievalModel: Retrieval

  def printGreeting() = {
    println(retrievalName + ".")
  }

  /** Prints results of query. */
  def printResult(res: Set[SearchIndexPosition]): Unit = {
    res.foreach(println(_))
  }

  def main(args: Array[String]): Unit = {
    printGreeting()

    while (true) {
      print("\nInput query: ")
      val query = readLine()
      println("Result:")
      try {
        printResult(retrievalModel.executeQuery(query))
      } catch {
        case e: UnsupportedOperationException => println("Wrong query: " + e)
      }
    }
  }

}
