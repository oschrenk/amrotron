import java.io.File
import java.nio.file.{Files, Path, Paths}

import com.typesafe.scalalogging.LazyLogging
import model.Row
import org.parboiled2.{ErrorFormatter, _}
import rules.{DslParser, Transformer}

import scala.io.Source
import scala.util.{Failure, Success}

case class Config(
  input: Seq[File] = Nil
) {
  private val HomeDir = Paths.get(System.getProperty("user.home"))
  val home: Path = HomeDir.resolve(".amrotron")
  val rules: Path = home.resolve("rules")
}

object Cli extends App with LazyLogging {

  val parser = new scopt.OptionParser[Config]("amrotron") {
    head("amrotron", "1.x")

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
      }

      // read configuration
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

      // read and transform input
      config.input.foreach{ file =>
        val lines = Source.fromFile(file.getCanonicalFile, "utf-8").getLines.toSeq
        val (errors, parsed) = Row.parse(lines)
        errors.foreach(e => logger.error(s"malformed entry: $e"))
        parsed.map(p => transformer.apply(p)).foreach(println)
      }
    case None => ()
  }
}


