package model

import org.scalatest._

class DetailsSpec extends FlatSpec with Matchers {

  "Details" should "parse whitespace sepa Acceptgirobetaling with Betalingskenm." in {
    val raw = """SEPA Acceptgirobetaling          IBAN: NL12INGB0001234567        BIC: INGBNL2A                    Naam: Belastingsdienst          Betalingskenm.: 1234567890123456"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse whitespace sepa Acceptgirobetaling without description" in {
    val raw = """SEPA Overboeking                 IBAN: NL12INGB0001234567        BIC: ABNANL2A                    Naam: J DOE"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse whitespace sepa Overboeking" in {
    val raw = """SEPA Overboeking                 IBAN: NL12INGB0001234567        BIC: TRIONL2U                    Naam: Het Acme        Omschrijving: Factuur 1234567"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse whitespace sepa Periodieke overb." in {
    val raw = """SEPA Periodieke overb.           IBAN: NL12INGB0001234567        BIC: ABNANL2A                    Naam: J DOE                 Omschrijving: Savings"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse slash sepa overboeking" in {
    val raw = """/TRTP/SEPA OVERBOEKING/IBAN/DE88500700100123456789/BIC/DEUTDEFFXXX/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/REMI/PAYPAL BEVEILIGINGSMAATREGEL/EREF/CCCCCCCCCCCCCCCC PAYPAL"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "DE88500700100123456789"
      case _ => fail()
    }
  }

  it should "parse slash sepa Incasso algemeen doorlopend" in {
    val raw = """/TRTP/SEPA Incasso algemeen doorlopend/CSID/LU96ZZZ0000000000000000012/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/MARF/DDDDDDDDDDDDD/REMI/1000123456789 PAYPAL/IBAN/DE88500700100123456789/BIC/DEUTDEFFXXX/EREF/1000123456789 PP AFSCHRIJVING"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "DE88500700100123456789"
      case _ => fail()
    }
  }

  it should "parse slash sepa Acceptgirobetaling" in {
    val raw = """/TRTP/Acceptgirobetaling/IBAN/NL12INGB0001234567/BIC/INGBNL2A  /NAME/Belastingsdienst/REMI/ISSUER: CUR                  REF: 1234567890123456/EREF/NOTPROVIDED"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse fees" in {
    val raw = """ABN AMRO Bank N.V.               Debit card                  0,60"""

    Details.parse(raw).right.get match {
      case Fee(_, description) => description shouldEqual "Debit card"
      case _ => fail()
    }
  }

  it should "parse insurances" in {
    val raw = """PAKKETVERZ. POLISNR.   222222222 MAANDPREMIE 02-16"""
    Details.parse(raw).right.get match {
      case Fee(name, _) => name shouldEqual "Insurance"
      case _ => fail()
    }
  }

  it should "parse cashpoint" in {
    val raw = """GEA   NR:A1B123   10.05.17/21.43 POSTJESWEG 98 (STEIN) AM,PAS666"""

    Details.parse(raw).right.get match {
      case CashPoint(_, _, description) => description shouldEqual "POSTJESWEG 98 (STEIN) AM"
      case _ => fail()
    }
  }

  it should "parse paypoint" in {
    val raw = """BEA   NR:AB123A   18.02.17/18.35 MM Amsterdam Centrum AMS,PAS666"""

    Details.parse(raw).right.get match {
      case PayPoint(_, _, description) => description shouldEqual "MM Amsterdam Centrum AMS"
      case _ => fail()
    }
  }

  it should "parse Sepa without proper delimiter between type and first key" in {
    val raw = """SEPA Incasso algemeen doorlopend Incassant: NL49IAK556886160000  Naam: IAK VOLMACHT BV            Machtiging: 69734               Omschrijving: IAK Verzekeringen  B.V.. Zorgverzekering(en) PKTZB, SV6. Polis 1232846. Januari 201 6 t/m januari 2016              IBAN: NL12INGB0001234567"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse Sepa with double space in description" in {
    val raw = """SEPA Overboeking                 IBAN: NL12INGB0001234567        BIC: ABNANL2A                    Naam: STICHTING SWINGSTREET     Omschrijving: Level A Lindy Hop,  Oliver Schrenk"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

}
