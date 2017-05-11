package rules

import java.io.File

import scala.io.Source

object Addressbook {
  def load(file: File): Map[String, String] = {
    Source.fromFile(file, "utf-8").getLines.map { line =>
      val values =line.split("=")
      val iban = values.head.toUpperCase()
      val description = values.tail.head
      Map(iban -> description)
    }.foldLeft(Map.empty[String, String])(_ ++ _)
  }
}

