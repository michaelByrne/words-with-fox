package me.foat.crawler

import java.net.URL

import akka.actor.{Actor, ActorRef, ActorSystem}
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

/**
  * @author Foat Akhmadeev
  *         17/01/16
  */
class LinkScraper(system: ActorSystem) extends Actor {
  val urlValidator = new UrlValidator()

  def receive: Receive = {
    case ScrapeLinks(url) =>
      println(s"scraping $url")
      val links: List[URL] = parseRiverURL(url)
      sender() ! ReturnURLs(links)
  }

  def parseRiverURL(url: URL): List[URL] = {
    val link: String = url.toString
    val response = Jsoup.connect(link).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1").execute()

    val contentType: String = response.contentType
    if (contentType.startsWith("text/html")) {
      val doc = response.parse()
      val links: List[URL] = doc.getElementsByTag("a").asScala.map(e => e.attr("href"))
        .filter(s => s.contains("transcript"))
        .map(link => new URL("http://foxnews.com" + link)).toList
      links
    } else {
      List.empty[URL]
    }
  }
}

