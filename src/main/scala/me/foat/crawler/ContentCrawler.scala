package me.foat.crawler

import java.net.URL
import java.util.Date
import java.text.SimpleDateFormat
import java.io._

import akka.actor.{Actor, Props, _}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scalikejdbc._


class ContentCrawler(system: ActorSystem, database: ActorRef) extends Actor {

  var content: List[Transcript] = List.empty[Transcript]
  var scraperCount = 0
  var manager: ActorRef = _



  def receive: Receive = {
    case GetText(urls, db) => {
      manager = sender
      urls.foreach(r => {
        val date = dateFromURL(r)
        val id = "Hannity-" + date.hashCode()
        database ! SaveTranscriptMeta(id, r.toString)
        scraperCount += 1
        val ref = context actorOf Props(new ContentScraper(system, database))
        ref ! ScrapeContent(r, id, db)
      })
      system.scheduler.scheduleOnce(10 seconds, database, TestQuery())
    }
    case ReturnText(t, url, id) => {
      val date = dateFromURL(url)
      val transcript = Transcript(id, date, "Hannity", t, url.toString)
      //database ! SaveTranscriptMeta(transcript)

      content = transcript :: content
      scraperCount -= 1
      if(scraperCount == 0){
        manager ! ReturnTranscripts(content)
      }
    }

    case _ =>
      print("why are we here?")
  }

  def dateFromURL(url: URL): Date = {
    val date = "\\d{4}\\/\\d{2}\\/\\d{2}".r
    val dateString = date findFirstIn url.toString
    val simpleDateFormat = new SimpleDateFormat("yyyy/mm/dd")
    simpleDateFormat.parse(dateString.get)
  }

}
