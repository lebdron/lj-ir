/**
  * @author Yex
  */

package com.innopolis.ir

import java.io.File

import com.innopolis.ir.tokenization.Tokenizer

import scala.collection.mutable.ListBuffer
import scala.io.Source

trait IndexBuilder {

  var tokenizer: Tokenizer

  var blackList = ListBuffer[String]()

  /** Returns an array of files in a directory. */
  def getCollection(directory: String): Array[File] = {
    getFilesRecursively(new File(directory)).filter(_.isFile).filterNot(blackList.contains)
  }

  def getFilesRecursively(f: File): Array[File] = {
    val res = f.listFiles
    res ++ res.filter(_.isDirectory).flatMap(getFilesRecursively)
  }

  /** Returns set of tokens in a file. */
  def tokenizeFile(file: File): List[String] = {
    var res = ListBuffer[String]()
    for (line <- Source.fromFile(file).getLines()) {
      res ++= tokenizer.tokenize(line)
    }
    res.toList
  }

}
