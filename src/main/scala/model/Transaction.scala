package model

import kantan.csv._
import kantan.csv.ops._

import scala.util.Try

import scala.util.hashing.{ MurmurHash3 => MH3 }

object Details {
  private val PointPrefix = 6
  private val NumberLength = 3 + 6
  private val NumberEnd = PointPrefix + NumberLength
  private val NumberPrefix = NumberEnd + 3
  private val DateLength = 14
  private val DateEnd = NumberPrefix + DateLength
  private val DatePrefix = DateEnd + 1

  def parse(raw: String): Either[String, Details] = {
    raw match {
      case s if s.startsWith("SEPA") => parseWhitespaceSepa(raw)
      case s if s.startsWith("/TRTP") => parseSlashSepa(raw)
      case s if s.startsWith("ABN") => parseFee(raw)
      case s if s.startsWith("GEA") => parseCashpoint(raw)
      case s if s.startsWith("BEA") => parsePaypoint(raw)
      case s => Left(s"Unknown details type: $s")
    }
  }

  private def parseWhitespaceSepa(raw: String): Either[String, Sepa] = {
    //SEPA Acceptgirobetaling          IBAN: NL86INGB1111111111        BIC: INGBNL2A                    Naam: Belastingsdienst          Betalingskenm.: 2222222222222222
    //SEPA Overboeking                 IBAN: NL12TRIO2222222222        BIC: TRIONL2U                    Naam:Het Acme        Omschrijving: Factuur 2222222
    //SEPA Periodieke overb.           IBAN: NL55ABNA2222222222        BIC: ABNANL2A                    Naam: J DOE                 Omschrijving: Savings
    Try {
      val split = raw.trim.split("\\s{2,}").map(_.trim)
      val typ = split.head.substring("SEPA ".length)
      val map = split.tail.map(_.split(":")).map(l => Map(l.head.trim -> l.tail.head.trim)).reduce(_ ++ _)
      val iban = map("IBAN")
      val bic = map("BIC")
      val name = map("Naam")
      val description = (map.get("Betalingskenm."), map.get("Omschrijving")) match {
        case (Some(a), _) => Some(a)
        case (_, Some(b)) => Some(b)
        case (None, None) => None
      }

      Right(Sepa(iban, bic, name, description))
    }.toOption.getOrElse(
      Left(s"Malformed WhitespaceSepa: $raw")
    )
  }

  private def parseSlashSepa(raw: String): Either[String, Sepa] = {
    ///TRTP/SEPA OVERBOEKING/IBAN/DE11111111111111111111/BIC/DEUTDEFFXXX/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/REMI/PAYPAL BEVEILIGINGSMAATREGEL/EREF/CCCCCCCCCCCCCCCC PAYPAL
    ///TRTP/SEPA Incasso algemeen doorlopend/CSID/LU96ZZZ0000000000000000058/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/MARF/DDDDDDDDDDDDD/REMI/1111111111111 PAYPAL/IBAN/DE11111111111111111111/BIC/DEUTDEFFXXX/EREF/1111111111111 PP AFSCHRIJVING
    ///TRTP/Acceptgirobetaling/IBAN/NL86INGB1111111111/BIC/INGBNL2A  /NAME/Belastingsdienst/REMI/ISSUER: CUR                  REF: 3333333333333333/EREF/NOTPROVIDED
    Try {
      val split = raw.substring("/TRTP/".length).split('/')
      val typ = split.head
      val map = split.tail.grouped(2).map(a => Map(a(0) -> a(1))).reduce(_ ++ _)

      val iban = map("IBAN")
      val bic = map("BIC")
      val name = map("NAME")
      val description = map.get("REMI")

      Right(Sepa(iban, bic, name, description))
    }.toOption.getOrElse(
      Left(s"Malformed SlashSepa: $raw")
    )
  }

  private def parseFee(raw: String): Either[String, Fee] = {
    //ABN AMRO Bank N.V.               Debit card                  0,60
    Try {
      val split = raw.split("\\s{2,}")
      val name = split.head
      val description = split.tail.head

      Right(Fee(name, description))
    }.toOption.getOrElse(
      Left(s"Malformed Fee: $raw")
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

  private def parsePoint(raw: String): (String, String, String) = {
    //GEA   NR:S1K222   30.03.17/21.43 POSTJESWEG 98 (STEIN) AM,PAS666
    //BEA   NR:3S999W   18.02.17/18.35 MM Amsterdam Centrum AMS,PAS666
    val number = raw.substring(PointPrefix, NumberLength)
    val date = raw.substring(NumberPrefix, DateEnd)
    val description = raw.substring(DatePrefix).split(',').head

    (number, date, description)
  }
}

sealed trait Details
case class CashPoint(number: String, date: String, description: String) extends Details
case class Fee(name: String, description: String) extends Details
case class PayPoint(number: String, date: String, description: String) extends Details
case class Sepa(iban: String, bic: String, name: String, description: Option[String]) extends Details

object Row {
  //111111111	EUR	20170103	14270,21	10000,21	20170103	-4270,00	SEPA Acceptgirobetaling          IBAN: NL86INGB1111111111        BIC: INGBNL2A                    Naam: Belastingsdienst          Betalingskenm.: 2222222222222222
  implicit val rowDecoder: RowDecoder[Row] = RowDecoder.decoder(0, 1, 2, 3, 4, 6, 7)(Row.apply)
}
case class Row(account: String, currency: String, date: String, before: String, after: String, amount: String, description: String) {
  def hash(): String = {
    val value =
      MH3.stringHash(account) +
      17* MH3.stringHash(date) +
      37 * MH3.stringHash(amount)
    value.toHexString
  }
}

object Transaction {
  def parse(line: String): Option[Transaction] = {
    val iterator = line.asCsvReader[Row](rfc.withColumnSeparator('\t').withoutHeader)
    iterator.map {
      case Success(row) =>
        val date = row.date
        val account = row.account
        val amount = row.amount
        val currency = row.currency
        println(s"before: $line")
        val details = Details.parse(row.description)
        val hash = row.hash()
        val t = Transaction(date, account, amount, currency, details.right.get, hash)

        println(s"Success: $t")
        Some(t)
      case Failure(thing) =>
        println(s"Failure: $thing")
        None
    }.toSeq.head
  }

  def print(lines: Seq[String]): Unit = {
    lines.foreach { line =>
     parse(line)
    }
  }
}

case class Transaction(date: String, account: String, currency: String, amount: String, details: Details, hash: String)
