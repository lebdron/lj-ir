package com.innopolis.ir.crawler

import java.io.{File, FileWriter}
import java.net.URL
import java.nio.file.{Files, Paths}

import org.jsoup.Jsoup

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source

object LJCrawlerApp {
  val timeout = 1000 * 30
  val directory = "data/livejournal"
  val userAgent = "Bot - a.lebedev@innopolis.ru"
  val sleep = 200 // 5 actions per sec
  var processedCount = 0
  var username = ""
  var page = 0 // machine dependent
  var userIDs = mutable.HashMap[String, (String, String)]()

  var users = mutable.ListBuffer[String]()
  val numMachines = 4
  val maxUsers = 500
  val maxPosts = 1000000
  var blacklist = mutable.Set[String]()

  def main(args: Array[String]): Unit = {
    if (args.length < 1){
      println("Starting page not specified")
      return
    }

    page = args(0).toInt

    if (checkBlacklist()){
      Source.fromFile(f"$directory/blacklist")
        .getLines.foreach(blacklist += _)
    }
    if (checkUsers()){
      Source.fromFile(f"$directory/usernames")
        .getLines.foreach(users += _)
    }
    else {
      while (users.size < maxUsers) {
        users = (users ++ getTopUsers).filterNot(blacklist.contains)
        page = page + numMachines
        print(f"\rCollected ${users.size} users out of $maxUsers")
      }
      saveUsers()
    }
    while (processedCount < maxPosts) {
      username = users.head
      println(f"Processing $username")
      users = users.tail
      userIDs = mutable.HashMap[String, (String, String)]()
      if (checkPostIDs()) {
        Source.fromFile(f"$directory/$username/postIDs")
          .getLines.map(_.split(" ")).foreach(e => userIDs.put(e(0), (e(1), e(2))))
        traverse(new File(f"$directory/$username"))
      }
      else {
        retrievePostIDs()
        savePostIDs()
      }
      println(f"Have ${userIDs.size} IDs for $username")
      val ids = userIDs.keys.toArray
      var i = 0
      for (i <- ids.indices){
        try {
          retrievePost(ids(i))
          print(f"\rProcessed ${i + 1} entries for $username (last processed: ${ids(i)})")
        }
        catch {
          case e: Exception =>
            println
            println(e.getMessage)
            Thread.sleep(timeout)
        }
      }
      println
      processedCount = processedCount + userIDs.size
      println(f"Total processed: $processedCount entries")
    }
  }

  def traverse(dir: File): Unit = {
    dir.listFiles.foreach { f => if (f.isDirectory) traverse(f) else userIDs.remove(f.getName) }
  }

  def getTopUsers: List[String] = {
    val url = f"http://www.livejournal.com/ratings/users/authority/?country=noncyr&page=$page"
    Thread.sleep(sleep)
    Jsoup.connect(url).timeout(timeout).userAgent(userAgent).get
      .select("a").map(_.attr("href"))
      .filter(e => e.contains("/profile"))
      .map(url => new URL(url).getHost.split('.')(0)).toList
  }

  def retrievePost(id: String): Unit = {
    val url = f"http://$username.livejournal.com/$id.html"
    Thread.sleep(sleep)
    val data = Jsoup.connect(f"$url?format=light").userAgent(userAgent).timeout(timeout).get
    val title = data.select(".entry-title").text
    val content = data.select(".entry-content").text
    val tags = data.select(".ljtags a").map(_.text).mkString(", ")
    save(f"$username/${userIDs(id)._1}/${userIDs(id)._2}", id, f"$url\n$title\n$tags\n$content")
  }

  def save(fileDirectory: String, filename: String, text: String): Unit = {
    val path = f"$directory/$fileDirectory"
    val dir = new File(path)
    dir.mkdirs

    new FileWriter(new File(dir, filename)) {
      write(text)
      close()
    }
  }

  def retrievePostIDs() = {
    for (i <- 1999 to 2016) {
      for (j <- 1 to 12){
        try {
          val year = i.toString
          val month = "%02d".format(j)
          val url = f"http://$username.livejournal.com/$year/$month/"
          Thread.sleep(sleep)
          Jsoup.connect(url).userAgent(userAgent).timeout(timeout).get
            .select("a").map(_.attr("href"))
            .filter(e => e.contains("html") && e.contains(username))
            .map(url => new URL(url).getFile.drop(1).dropRight(5))
            .foreach(userIDs.put(_, (year, month)))
          print(f"\rRetrieved ${userIDs.size} IDs for $username ($year/$month)")
        }
        catch {
          case e: Exception =>
            println
            println(e.getMessage)
            Thread.sleep(timeout)
        }
      }
    }
    println
  }

  def savePostIDs(): Unit = {
    val path = f"$directory/$username"
    val dir = new File(path)
    dir.mkdirs

    new FileWriter(new File(dir, "postIDs")) {
      userIDs.entrySet.foreach(e => write(f"${e.getKey} ${e.getValue._1} ${e.getValue._2}\n"))
      close()
    }
  }

  def checkPostIDs(): Boolean = {
    val file = f"$directory/$username/postIDs"
    Files.exists(Paths.get(file))
  }

  def checkUsers() : Boolean = {
    val file = f"$directory/usernames"
    Files.exists(Paths.get(file)) && new File(file).length > 0
  }

  def saveUsers() : Unit = {
    val path = f"$directory"
    val dir = new File(path)
    dir.mkdirs

    new FileWriter(new File(dir, "usernames")) {
      users.foreach(e => write(f"$e\n"))
      close()
    }
  }

  def checkBlacklist(): Boolean = {
    val file = f"$directory/blacklist"
    Files.exists(Paths.get(file)) && new File(file).length > 0
  }

}
