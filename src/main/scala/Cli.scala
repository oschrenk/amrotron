import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import model.{ParsedRow, Row, Transaction}
import rules.{Addressbook, Rules, Transformer}
import ui.Formatters

import scala.io.Source

case class Config(
  input: Seq[File] = Nil,
  format: String = "default",
  showStats: Boolean = false
) {
  private val HomeDir = Paths.get(System.getProperty("user.home"))
  val home: Path = HomeDir.resolve(".amrotron")
  val rules: Path = home.resolve("rules")
  val addressbook: Path = home.resolve("addressbook")
}

object Cli extends App with LazyLogging {

  val parser = new scopt.OptionParser[Config]("amrotron") {
    head("amrotron", "1.x")

    opt[String]('f', "format")
      .action((f, c) => c.copy(format = f))

    opt[Unit]('s', "show-stats")
      .action((_, c) => c.copy(showStats = true))

    arg[File]("<file>...")
      .unbounded()
      .action( (x, c) => c.copy(input = c.input :+ x) )
      .text("input files")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>

      println(config)

      if (Files.notExists(config.home)) {
        logger.info("Creating empty rules")
        Files.createDirectories(config.home)
        Files.createFile(config.rules)
        Files.createFile(config.addressbook)
      }

      // configuration
      val rules = Rules.load(config.rules.toFile.getCanonicalFile)
      val transformer = new Transformer(rules)
      val addresses = Addressbook.load(config.addressbook.toFile.getCanonicalFile)
      val formatter = Formatters.from(config.format)

      // TODO read and parse only if success, transform and format
      // read and transform input
      val transactions = config.input.map{ file =>
        val lines = Source.fromFile(file.getCanonicalFile, "utf-8").getLines.toSeq

        // read lines
        val (rawErrors, rawRows) = Row.parse(lines)
        rawErrors.foreach(e => logger.error(s"malformed line: $e"))

        // parse details and create types
        val (parsedErrors, parsedRows) = ParsedRow.parse(rawRows)
        parsedErrors.foreach(e => logger.error(s"malformed row: $e"))

        parsedRows.map{ parsedRow =>
          transformer.apply(parsedRow)
        }
      }.flatten

      transactions.foreach { transaction =>
        println(formatter(addresses, transaction))
      }
      if (config.showStats) {
        val t = transactions
          .groupBy(_.tags)
          .map{ case (tags, trans) =>
            tags.map(t => (t, trans))
          }.flatten
        val perTag: Map[String, Seq[Transaction]] =
          t.foldLeft(Map.empty[String, Seq[Transaction]]) { (a, b) =>
            if (a.contains(b._1)) {
              a.updated(b._1, a(b._1) ++ b._2)
            } else {
              a ++ Map(b)
            }
          }
          val stats = perTag.map { case (k, v) ⇒ k → v.size }
          stats.toSeq.sortBy{ case  (k,v) => (k,v)}
            .foreach {case (k, v) => printf("%3d %s\n", v, k)}
          println("---")
          printf("%3d\n", stats.values.sum)
      }
      case None => ()
  }
}
