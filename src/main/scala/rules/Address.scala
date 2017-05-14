package rules

import java.nio.file.{Files, Path}

import scala.io.Source

object Addressbook {
  def load(path: Path): Map[String, String] = {
    if (Files.notExists(path)) {
      Map.empty
    } else {
      val file = path.toFile.getCanonicalFile
      Source.fromFile(file, "utf-8").getLines.map { line =>
        val values =line.split("=")
        val iban = values.head.toUpperCase()
        val description = values.tail.head
        Map(iban -> description)
      }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
  }
}

