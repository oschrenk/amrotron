import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import model.{ParsedRow, Row}
import rules.{Rules, Transformer}
import ui.Formatters

import scala.io.Source

case class Config(
  input: Seq[File] = Nil,
  format: String = "default"
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

    arg[File]("<file>...")
      .unbounded()
      .action( (x, c) => c.copy(input = c.input :+ x) )
      .text("input files")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      if (Files.notExists(config.home)) {
        logger.info("Creating empty rules")
        Files.createDirectories(config.home)
        Files.createFile(config.rules)
        Files.createFile(config.addressbook)
      }

      // read rules
      val rules = Rules.load(config.rules.toFile.getCanonicalFile)
      val transformer = new Transformer(rules)

      // read addressbook
      val posssibleAddresses = Source.fromFile(config.addressbook.toFile.getCanonicalFile, "utf-8").getLines.map { line =>
        val values =line.split("=")
        val iban = values.head.toUpperCase()
        val description = values.tail.head
        Map(iban -> description)
      }
      val addresses: Map[String, String]  =
        if (posssibleAddresses.nonEmpty) posssibleAddresses.reduce(_ ++ _)  else Map.empty
      logger.info(s"${addresses.size} address book entries")

      // TODO read and parse only if success, transform and format
      // read and transform input
      config.input.foreach{ file =>
        val lines = Source.fromFile(file.getCanonicalFile, "utf-8").getLines.toSeq

        // read lines
        val (rawErrors, rawRows) = Row.parse(lines)
        rawErrors.foreach(e => logger.error(s"malformed line: $e"))

        // parse details and create types
        val (parsedErrors, parsedRows) = ParsedRow.parse(rawRows)
        parsedErrors.foreach(e => logger.error(s"malformed row: $e"))

        // transform
        val formatter = Formatters.from(config.format)
        parsedRows.map{ parsedRow =>
          transformer.apply(parsedRow)
        }.foreach{transaction =>
          println(formatter(addresses, transaction))
        }
      }
    case None => ()
  }
}
