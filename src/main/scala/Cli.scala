import scala.io.Source
import java.io.File
import java.nio.file.{Files, Paths}

import com.typesafe.scalalogging.LazyLogging
import model.Transaction

case class Config(
  input: Seq[File] = Nil
) {
  private val HomeDir = Paths.get(System.getProperty("user.home"))
  val home = HomeDir.resolve(".amrotron")
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
         Files.createDirectories(config.home)
      }

      config.input.foreach{ file =>
        val lines = Source.fromFile(file.getCanonicalFile, "utf-8").getLines.toSeq
        Transaction.print(lines)
      }
    case None => _
  }
}


