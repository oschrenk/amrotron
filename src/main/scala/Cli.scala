import java.io.File
import java.io.FileNotFoundException
import java.nio.file.{Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import model.{ParsedRow, Row, Stats, Transaction}
import rules.{Addressbook, Rules, Transformer}
import ui.Formatters

import scala.io.Source
import scala.util.{Failure, Success, Try}

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

      // configuration
      val rules = Rules.load(config.rules)
      val transformer = new Transformer(rules)
      val addresses = Addressbook.load(config.addressbook)
      val formatter = Formatters.from(config.format)

      // TODO read and parse only if success, transform and format
      // read and transform input
      val transactions = config.input.map{ file =>
        loadTransactions(transformer, file) match {
          case Success(t) => t
          case Failure(e: FileNotFoundException) =>
            System.err.println(s"File $file not found.")
            Seq.empty
          case Failure(e) =>
            System.err.println(s"Error: $e")
            Seq.empty
        }
      }.flatten
      val filteredTransactions = transactions.filter{t =>
        config.tags.intersect(t.tags).nonEmpty
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

  def loadTransactions(transformer: Transformer, file: File): Try[Seq[Transaction]] = {
    Try{
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
    }
  }
}
