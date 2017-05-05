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

    //SEPA Acceptgirobetaling          IBAN: NL86INGB1111111111        BIC: INGBNL2A                    Naam: Belastingsdienst          Betalingskenm.: 2222222222222222
    //SEPA Overboeking                 IBAN: NL12TRIO2222222222        BIC: TRIONL2U                    Naam:Het Acme        Omschrijving: Factuur 2222222
    //SEPA Periodieke overb.           IBAN: NL55ABNA2222222222        BIC: ABNANL2A                    Naam: J DOE                 Omschrijving: Savings
    //SEPA Incasso algemeen eenmalig   Incassant: NL51ZZZ341203710000  Naam: ATLETIEKVERENIGING PHANOS  Machtiging: 20160609-01110-00077Omschrijving: Inschrijffgeld VU  Polderloop def                  IBAN: NL59INGB0008331840         Kenmerk: 20160609-01110-00077
    //SEPA Incasso algemeen doorlopend Incassant: NL49IAK556886160000  Naam: IAK VOLMACHT BV            Machtiging: 69734               Omschrijving: IAK Verzekeringen  B.V..                           IBAN: NL94ABNA0244748977         Kenmerk: 19632308               Voor: J DOE
    //SEPA Incasso algemeen doorlopend Incassant: NL49IAK556886160000  Naam: IAK VOLMACHT BV            Machtiging: 69734               Omschrijving: IAK Verzekeringen  B.V.. Zorgverzekering(en) PKTZB, SV6. Polis 1232846. Januari 201 6 t/m januari 2016              IBAN: NL94ABNA0244748977
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
    ///TRTP/SEPA OVERBOEKING/IBAN/DE11111111111111111111/BIC/DEUTDEFFXXX/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/REMI/PAYPAL BEVEILIGINGSMAATREGEL/EREF/CCCCCCCCCCCCCCCC PAYPAL
    ///TRTP/SEPA Incasso algemeen doorlopend/CSID/LU96ZZZ0000000000000000058/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/MARF/DDDDDDDDDDDDD/REMI/1111111111111 PAYPAL/IBAN/DE11111111111111111111/BIC/DEUTDEFFXXX/EREF/1111111111111 PP AFSCHRIJVING
    ///TRTP/Acceptgirobetaling/IBAN/NL86INGB1111111111/BIC/INGBNL2A  /NAME/Belastingsdienst/REMI/ISSUER: CUR                  REF: 3333333333333333/EREF/NOTPROVIDED
    Try {
      val split = raw.substring("/TRTP/".length).split('/')
      val category = split.head
      val map = split.tail.grouped(2).map(a => Map(a(0) -> a(1))).reduce(_ ++ _)

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

  private def parseInsurance(raw: String): Either[String, Fee] = {
    //PAKKETVERZ. POLISNR.   222222222 MAANDPREMIE 02-16
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

  // 10.05.17/21.43
  private val PointDateTimeformatter = DateTimeFormatter.ofPattern("dd.MM.yy/HH.mm")
  private def parsePoint(raw: String): (String, LocalDateTime, String) = {
    //GEA   NR:S1K222   30.03.17/21.43 POSTJESWEG 98 (STEIN) AM,PAS666
    //BEA   NR:3S999W   18.02.17/18.35 MM Amsterdam Centrum AMS,PAS666
    val number = raw.substring(PointPrefix, NumberLength)
    val date = LocalDateTime.parse(raw.substring(NumberPrefix, DateEnd), PointDateTimeformatter)
    val description = raw.substring(DatePrefix).split(',').head

    (number, date, description)
  }
}

