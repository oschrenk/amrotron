import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import model.{ParsedRow, Row, Stats}
import rules.{Addressbook, Rules, Transformer}
import ui.Formatters

import scala.io.Source

case class Config(
  input: Seq[File] = Nil,
  format: String = "default",
  showStats: Boolean = false,
  tags: Set[String] = Set.empty
) {
  private val HomeDir = Paths.get(System.getProperty("user.home"))
  val home: Path = HomeDir.resolve(".amrotron")
  val rules: Path = home.resolve("rules")
  val addressbook: Path = home.resolve("addressbook")
}

object Cli extends App with LazyLogging {

  implicit val weekDaysRead: scopt.Read[Set[String]] =
    scopt.Read.reads(s => s.split(',').map(_.trim).toSet)

  val parser = new scopt.OptionParser[Config]("amrotron") {
    head("amrotron", "1.x")

    opt[String]('f', "format")
      .action((f, c) => c.copy(format = f))

    opt[Unit]('s', "show-stats")
      .action((_, c) => c.copy(showStats = true))

    opt[Set[String]]('t', "tags").valueName("<tag1>,<tag2>...").action( (x,c) =>
    c.copy(tags = x) ).text("tags to include")

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

      val whitelist = config.tags
      val filteredTransactions = transactions.filter{t =>
        whitelist.intersect(t.tags).nonEmpty
      }
      filteredTransactions.foreach { transaction =>
        println(formatter(addresses, transaction))
      }
      if (config.showStats) {
        val stats = Stats.from(filteredTransactions)
        stats.foreach { case (tag, count, total, average) =>
          println(s"$tag")
          println(s"  $count $total $average")
        }
      }
      case None => ()
  }
}
