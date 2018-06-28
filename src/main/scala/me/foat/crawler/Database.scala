package me.foat.crawler

import akka.actor.{Actor, Props, _}

import scalikejdbc._

class Database(system: ActorSystem) extends Actor {
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:~/hannity-words", "user", "pass")

  implicit val session = AutoSession

  def receive: Receive = {
    case CreateShowTables(show) =>

      sql"""
         drop table if exists blocks
         """.execute.apply()

      sql"""
         drop table if exists transcripts
         """.execute.apply()

      sql"""
        create table transcripts (
          id varchar(64) not null primary key,
          show varchar,
          url varchar
          )
      """.execute.apply()

      sql"""
        create table blocks (
          id varchar(64) not null primary key,
          tid varchar(64),
          speaker varchar,
          text clob,
          foreign key (tid) references transcripts(id)
          )
      """.execute.apply()

      print("creating new table for " + show + "\n")

    case SaveTranscriptMeta(tid, turl) =>
        sql"""
          insert into transcripts (id, url) values (${tid}, ${turl})
        """.update.apply()
      print("saving transcript meta for " + tid.toString)

    case SaveBlock(block) =>
      //print("saving block")
      sql"""
        insert into blocks (id, tid, speaker, text) values (${block.id}, ${block.transcriptID}, ${block.speaker}, ${block.text})
      """.update.apply()

    case TestQuery() =>
      val testResult = sql"""
        select top 1 * from blocks
        """.map(rs => rs.string("id")).first.apply()
      print(testResult)
  }
}
