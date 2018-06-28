package me.foat.crawler

import java.net.URL

import akka.actor.{Actor, Props, _}

case class Start(url: URL)
case class Index(url: URL, content: Content)
case class Content(title: String, meta: String, urls: List[URL])
case class IndexFinished(url: URL, urls: List[URL])
case class ScrapFailure(url: URL, reason: Throwable)

case class GetURLs()
case class ReturnURLs(urls: List[URL])
case class GetText(pages: List[URL], db: ActorRef)
case class ReturnText(text: List[Block], url: URL, transcriptID: String)
case class ReturnTranscripts(transcripts: List[Transcript])

case class ScrapeLinks(url: URL)
case class ScrapeContent(url: URL, id: String, db: ActorRef)

case class CreateShowTables(show: String)
case class SaveBlock(block: Block)
case class SaveTranscriptMeta(id: String, url: String)
case class TestQuery()
