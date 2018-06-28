package me.foat.crawler

import java.net.URL
import java.util.Calendar

import org.apache.commons.validator.routines.UrlValidator

import akka.actor.{Actor, Props, _}
import akka.pattern.{ask, pipe}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import org.jsoup.Jsoup

import scala.collection.JavaConverters._

import com.typesafe.config._

class LinkCrawler(system: ActorSystem) extends Actor {
  val base = ConfigFactory.load().getString("hannity.base")
  val urlValidator = new UrlValidator()
  var transcriptURLs: List[URL] = List.empty[URL]
  var manager: ActorRef = _

  var scraperCount = 0

  //context.setReceiveTimeout(10 seconds)



  def receive: Receive = {
    case GetURLs() =>
      manager = sender
      val riverLinks = generateRiverURLs(base, 1209600000, 0)
      riverLinks.foreach(l => {
        val linkScraper = context actorOf Props(new LinkScraper(system))
        context.watch(linkScraper)
        linkScraper ! ScrapeLinks(l)
        scraperCount += 1
      })
    case ReturnURLs(pages) =>
      transcriptURLs = pages ::: transcriptURLs
      scraperCount -= 1

      if(scraperCount == 0){
        print("scrapers are home")
        manager ! ReturnURLs(transcriptURLs.distinct)
      }
//    case Terminated(_) =>
//      scraperCount -= 1
  }

  def generateRiverURLs(base: URL, increment: Long, maxPages: Int): List[URL] = {
    var timeStamp = System.currentTimeMillis
    var URLs = List.empty[URL]
    for (i <- 0 to maxPages) {
      timeStamp -= increment
      URLs = new URL(base + s"$timeStamp.html") :: URLs
    }
    URLs
  }

}