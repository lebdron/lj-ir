package com.innopolis.ir.web

import java.io.File

import scala.io.Source

/**
  * Created by andrey on 11/12/16.
  */
class QueryResult(file: File) {

  private val titleLength = 66

  private val lines = Source.fromFile(file).getLines()
  val url = lines.next()
  var title = if (lines.hasNext) lines.next() else ""
  val tags = if (lines.hasNext) lines.next() else ""
  val post = if (lines.hasNext) lines.next() else ""

  if (title.isEmpty && post.nonEmpty){
    title = post
  }

  if (title.length > titleLength){
    title = title.slice(0, titleLength - 3) + "..."
  }

  if (title.isEmpty) {
    title = "untitled"
  }

}
