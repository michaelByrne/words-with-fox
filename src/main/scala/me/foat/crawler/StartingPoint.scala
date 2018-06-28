package me.foat.crawler

import akka.actor.{ActorSystem, PoisonPill, Props, Actor}

import scala.language.postfixOps


import java.net.URL
import java.util.Date


case class Transcript(id: String, date: Date, show: String, text: List[Block], url: String)
case class Block(id: String, transcriptID: String, speaker: String, index: Int, text: String)




object StartingPoint extends App {

  val system = ActorSystem()

  val manager = system.actorOf(Props(new Manager(system)))
}

class Manager(system: ActorSystem) extends Actor {
  val database = context actorOf Props(new Database(system))
  val linkCrawler = context actorOf Props(new LinkCrawler(system))
  linkCrawler ! GetURLs()

  val textCrawler = context actorOf Props(new ContentCrawler(system, database))


  database ! CreateShowTables("hannity")

  var pages = List.empty[URL]
  var text: List[Transcript] = List.empty[Transcript]

  //system.scheduler.scheduleOnce(5 seconds, database, TestQuery)

  def receive: Receive = {
    case ReturnURLs(p) =>
      pages = p
      textCrawler ! GetText(p, database)
      //system.terminate()
    case ReturnTranscripts(t) =>
      text = t
      //system.terminate()
  }
}


