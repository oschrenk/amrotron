import scala.io.Source
import java.io.File

import model.Transaction

case class Config(input: Seq[File] = Nil)

object Cli extends App {

  val parser = new scopt.OptionParser[Config]("amrotron") {
    head("amrotron", "1.x")

  arg[File]("<file>...")
    .unbounded().optional()
    .action( (x, c) => c.copy(input = c.input :+ x) )
    .text("input files")
  }

  parser.parse(args, Config()) match {
    case Some(config) =>
      config.input.foreach{ file =>
        val lines = Source.fromFile(file.getCanonicalFile, "utf-8").getLines.toSeq
        Transaction.print(lines)
      }
    case None =>
      parser.showUsage()
  }
}


