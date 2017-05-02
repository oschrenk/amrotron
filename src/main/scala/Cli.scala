import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import model.Row
import org.parboiled2.{ErrorFormatter, _}
import rules.{DslParser, Transformer}
import ui.Formatters

import scala.io.Source
import scala.util.{Failure, Success}

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
      val fmt = new ErrorFormatter(showTraces = true)
      val rules = Source.fromFile(config.rules.toFile.getCanonicalFile, "utf-8").getLines.map{ line =>
        val dslParser = new DslParser(line)
        dslParser.InputLine.run() match {
          case Success(rule) => rule
          case Failure(e: ParseError) =>
            println(s"Invalid expression: ${dslParser.formatError(e, fmt)}")
            throw new IllegalArgumentException("Error parsing rules")
          case Failure(e) =>
            throw new IllegalArgumentException("Error parsing rules")
        }
      }.toSeq
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

      // print config
      println(config)

      // read and transform input
      config.input.foreach{ file =>
        val lines = Source.fromFile(file.getCanonicalFile, "utf-8").getLines.toSeq
        val (errors, parsed) = Row.parse(lines)
        errors.foreach(e => logger.error(s"malformed entry: $e"))
        val formatter = Formatters.from(config.format)
        parsed.map(p => transformer.apply(p)).foreach(t => println(formatter(t, addresses)))
      }
    case None => ()
  }
}


