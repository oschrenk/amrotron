package model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.util.Try

sealed trait Details
case class CashPoint(number: String, date: LocalDateTime, description: String) extends Details
case class Fee(name: String, description: String) extends Details
case class PayPoint(number: String, date: LocalDateTime, description: String) extends Details
case class Sepa(category: String, iban: String, bic: Option[String], name: String, description: Option[String]) extends Details

object Details {
  private val PointPrefix = 6
  private val NumberLength = 3 + 6
  private val NumberEnd = PointPrefix + NumberLength
  private val NumberPrefix = NumberEnd + 3
  private val DateLength = 14
  private val DateEnd = NumberPrefix + DateLength
  private val DatePrefix = DateEnd + 1

  def parse(raw: String): Either[String, Details] = {
    // TODO encapsulate every parser
    raw match {
      case s if s.startsWith("SEPA") => parseWhitespaceSepa(raw)
      case s if s.startsWith("/TRTP") => parseSlashSepa(raw)
      case s if s.startsWith("ABN") => parseFee(raw)
      case s if s.startsWith("PAKKETVERZ.") => parseInsurance(raw)
      case s if s.startsWith("GEA") => parseCashpoint(raw)
      case s if s.startsWith("BEA") => parsePaypoint(raw)
      case s => Left(s"Unknown details type: $s")
    }
  }

  private def parseWhitespaceSepa(raw: String): Either[String, Sepa] = {

    def prefixAndLastWord(s: String): Seq[String] = {
      val lastWordIndex = s.lastIndexOf(' ')
      // value of last key
      if (lastWordIndex < 0) {
        List(s.trim)
      } else {
        val pre = s.substring(0, lastWordIndex)
        val lastWord = s.substring(lastWordIndex + 1)
        List(pre.trim, lastWord.trim)
      }
    }

    val Prefix = "SEPA"
    Try {
      val colonSplit = raw.split(":")
      val last = colonSplit.lastOption.get
      val headList = colonSplit.dropRight(1)
      val split: Seq[String] = headList.map(_.trim).flatMap(prefixAndLastWord) ++ List(last)

      val category = split.head.substring(Prefix.length)
      val map: Map[String, String] = split.tail.grouped(2).map(kv => Map(kv.head -> kv.tail.head)).reduce(_ ++ _)
      // TODO sometimes both are there
      val iban = map.getOrElse("IBAN", map("Incassant")).trim
      // TODO if Incassant is present, there is no BIC
      val bic = map.get("BIC")
      val name = map("Naam")
      val description = (map.get("Betalingskenm."), map.get("Omschrijving")) match {
        case (Some(a), _) => Some(a)
        case (_, Some(b)) => Some(b)
        case (None, None) => None
      }

      Right(Sepa(category, iban, bic, name, description))
    }.toOption.getOrElse(
      Left(s"Malformed WhitespaceSepa: $raw")
    )
  }

  private def parseSlashSepa(raw: String): Either[String, Sepa] = {
    val KnownKeys = Set("IBAN", "BIC", "NAME", "REMI")
    Try {
      val split = raw.substring("/TRTP/".length).split('/')
      val category = split.head
      val map: Map[String, String] = split.tail.grouped(2).map{ a =>
        val key = a(0)
        if (KnownKeys.contains(key))
          Map(a(0) -> a(1))
        else
          Map[String, String]()
      }.reduce(_ ++ _)

      val iban = map("IBAN")
      val bic = map.get("BIC")
      val name = map("NAME")
      val description = map.get("REMI")

      Right(Sepa(category, iban, bic, name, description))
    }.toOption.getOrElse(
      Left(s"Malformed SlashSepa: $raw")
    )
  }

  private def parseFee(raw: String): Either[String, Fee] = {
    Try {
      val split = raw.split("\\s{2,}")
      val name = split.head
      val description = split.tail.head

      Right(Fee(name, description))
    }.toOption.getOrElse(
      Left(s"Malformed Fee: $raw")
    )
  }

  private def parseInsurance(raw: String): Either[String, Fee] = {
    val Prefix = "PAKKETVERZ."
    // TODO parse insurance number, and which months
    Try {
      val description = raw.substring(Prefix.length)

      // TODO what insurance?
      Right(Fee("Insurance", description))
    }.toOption.getOrElse(
      Left(s"Malformed insurance: $raw")
    )
  }

  private def parseCashpoint(raw: String): Either[String, CashPoint] = {
    Try {
      val (number, date, description) = parsePoint(raw)
      Right(CashPoint(number, date, description))
    }.toOption.getOrElse(
      Left(s"Malformed Cashpoint: $raw")
    )
  }

  private def parsePaypoint(raw: String): Either[String, PayPoint] = {
    Try {
      val (number, date, description) = parsePoint(raw)
      Right(PayPoint(number, date, description))
    }.toOption.getOrElse(
      Left(s"Malformed Paypoint: $raw")
    )
  }

  private val PointDateTimeformatter = DateTimeFormatter.ofPattern("dd.MM.yy/HH.mm")
  private def parsePoint(raw: String): (String, LocalDateTime, String) = {
    val number = raw.substring(PointPrefix, NumberLength)
    val date = LocalDateTime.parse(raw.substring(NumberPrefix, DateEnd), PointDateTimeformatter)
    val description = raw.substring(DatePrefix).split(',').head

    (number, date, description)
  }
}
