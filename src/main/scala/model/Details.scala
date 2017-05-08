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


  private def parsePositional(raw: String, Prefix: String, KnownKeys: List[String]): Map[String, String] = {
    val positions: List[(String, Int)] = KnownKeys.map(key => (key, raw.indexOf(key))).filter{
      case (_, pos) => pos >= 0
    }.sortBy(_._2) :+ ("end", raw.length)

    positions.sliding(2).map{ pair =>
      val left = pair.head
      val right = pair.tail.head
      // trim and delete the colon
      val key = left._1.trim.dropRight(1)
      val from = left._2 + key.length + 1
      val to = right._2
      val value = raw.substring(from, to).trim
      Map(key -> value)
    }.reduce(_ ++ _) ++ Map("category" -> raw.substring(Prefix.length, positions.head._2))
  }

  private def parseWhitespaceSepa(raw: String): Either[String, Sepa] = {

    // cat TXT170504221000.TAB | grep SEPA | grep -v TRTP | grep -v BEA | grep -v GEA | egrep -o "\w+:" | sort | uniq
    // and then manual extraction and selection, leads to
    val KnownKeys = List("BIC:", "IBAN:", "Incassant:", "Betalingskenm.:", "Kenmerk:", "Machtiging:", "Naam:", "Omschrijving:", "Voor:")
    val Prefix = "SEPA"
    Try {
      val map = parsePositional(raw, Prefix, KnownKeys)

      // TODO there should be ant trims here
      val category = map("category").trim
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
    // only manual selection
    val KnownKeys = List("/IBAN/", "/BIC/", "/NAME/", "/REMI/", "/CSID/", "/EREF/")
    val Prefix = "/TRTP/"
    Try {
      val map = parsePositional(raw, Prefix, KnownKeys)

      val category = map("category")
      // TODO get rid of that leading slash
      val iban = map("/IBAN")
      val bic = map.get("/BIC")
      val name = map("/NAME")
      val description = map.get("/REMI")

      Right(Sepa(category, iban, bic, name, description))
    }.toOption.getOrElse(
      Left(s"Malformed SlashSepa: $raw")
    )
  }

  val ABN = "ABN AMRO Bank N.V."
  private def parseFee(raw: String): Either[String, Fee] = {
    Try {
      val split = raw.split("\\s{2,}")
      val name = ABN
      val description = split.tail.head

      Right(Fee(name, description))
    }.toOption.getOrElse(
      Left(s"Malformed Fee: $raw")
    )
  }

  private def parseInsurance(raw: String): Either[String, Fee] = {
    // TODO parse insurance number, and which months
    Try {
      Right(Fee(ABN, "Liability Insurance"))
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
