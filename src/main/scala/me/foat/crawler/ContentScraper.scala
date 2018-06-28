package me.foat.crawler

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorRef, ActorSystem}
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.util.matching._
import scala.language.postfixOps
import java.io._


class ContentScraper(system: ActorSystem, database: ActorRef) extends Actor {


  val writer = new PrintWriter(new FileOutputStream(new File("/engn/articles/words-with-fox/out/test.txt"),true))


  def receive: Receive = {
    case ScrapeContent(url, id, _) =>
      println(s"scraping $url")
      val text = parsePageContent(url, id)
      sender ! ReturnText(text, url, id)

  }

  def parsePageContent(url: URL, transcriptID: String): List[Block] = {
    val link: String = url.toString
    val response = Jsoup.connect(link).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1").execute()

    val contentType: String = response.contentType
    if (contentType.startsWith("text/html")) {
      val doc = response.parse()
      var textList: List[String] = doc.getElementsByTag("p").asScala.map(e => e.text()).toList
      textList = textList.map(p => "break" + p)
      val text = textList.mkString(" ")
      var pattern = "(?<=break)([A-Z\\s,]*)(?=:)".r
      var speaker: String = ""
      var index = 0

      var textMap = textList.map(p => {
        val tempSpeaker = pattern.findFirstIn(p).getOrElse("")
        if (tempSpeaker.length > 0){
          speaker = tempSpeaker
        }
        index += 1
        val date = dateFromURL(url)
        val ID = date.hashCode() + "-" + index
        val block = Block(ID, transcriptID, speaker, index, p.replace("break","\n").replace(speaker + ":",""))
        database ! SaveBlock(block)

        block
      })
      textMap
    } else {
      Nil
    }
  }

  def dateFromURL(url: URL): Date = {
    val date = "\\d{4}\\/\\d{2}\\/\\d{2}".r
    val dateString = date findFirstIn url.toString
    val simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd")
    simpleDateFormat.parse(dateString.get)
  }
}

