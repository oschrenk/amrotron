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
    val raw = """/TRTP/SEPA OVERBOEKING/IBAN/DE88500700100123456789/BIC/DEUTDEFFXXX/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/REMI/PAYPAL BEVEILIGINGSMAATREGEL/EREF/AAAAAAAAAAAAAA44 PAYPAL"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "DE88500700100123456789"
      case _ => fail()
    }
  }

  it should "parse slash sepa Incasso algemeen doorlopend" in {
    val raw = """/TRTP/SEPA Incasso algemeen doorlopend/CSID/LU96ZZZ0000000000000000012/NAME/PayPal Europe S.a.r.l. et Cie S.C.A/MARF/AAAAAAAAAAAAA/REMI/1000123456789 PAYPAL/IBAN/DE88500700100123456789/BIC/DEUTDEFFXXX/EREF/1000123456789 PP AFSCHRIJVING"""

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

  it should "parse slash sepa with broken key value" in {
    val raw = """/TRTP/SEPA OVERBOEKING/IBAN/NL12INGB0001234567/BIC/TRIONL2U/NAME/T.M. van Hasselhoff/REMI/Emoji of a party hat with confetti/EREF/TRIODOS/NL/20170403/22222222"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse slash sepa with slash inside description" in {
    val raw = """/TRTP/SEPA Incasso algemeen doorlopend/CSID/NL49IAK111111111111/NAME/IAK VOLMACHT BV/MARF/11111/REMI/FBI Verzekeringen B.V.. Zorgverzekering(en) PKTZB, SV6. Polis 1111111. Mei 2017 t/m mei 2017/IBAN/NL12INGB0001234567/BIC/ABNANL2A/EREF/11111111"""

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
    val raw = """PAKKETVERZ. POLISNR.   111111111 MAANDPREMIE 02-16"""
    Details.parse(raw).right.get match {
      case Fee(name, description) =>
        name shouldEqual "ABN AMRO Bank N.V."
        description shouldEqual "Liability Insurance"
      case _ => fail()
    }
  }

  it should "parse cashpoint" in {
    val raw = """GEA   NR:A1B111   20.02.16/21.23 POSTFOOBAR 11 (STEIN) AM,PAS111"""

    Details.parse(raw).right.get match {
      case CashPoint(_, _, description) => description shouldEqual "POSTFOOBAR 11 (STEIN) AM"
      case _ => fail()
    }
  }

  it should "parse paypoint" in {
    val raw = """BEA   NR:AB111A   12.02.14/18.35 AA Amsterdam Centrum AMS,PAS111"""

    Details.parse(raw).right.get match {
      case PayPoint(_, _, description) => description shouldEqual "AA Amsterdam Centrum AMS"
      case _ => fail()
    }
  }

  it should "parse Sepa without proper delimiter between type and first key" in {
    val raw = """SEPA Incasso algemeen doorlopend Incassant: NL49IAK222222222222  Naam: FBI VOLMACHT BV            Machtiging: 11111               Omschrijving: FBI Verzekeringen  B.V.. Zorgverzekering(en) AAAAA, SV6. Polis 1111111. Januari 201 6 t/m januari 2016              IBAN: NL12INGB0001234567"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse Sepa with double space in description" in {
    val raw = """SEPA Overboeking                 IBAN: NL12INGB0001234567        BIC: ABNANL2A                    Naam: STICHTING FOOBAR     Omschrijving: Level A Lindy Hop,  John Doe"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse descriptions that contain a colon" in {
    val raw = """SEPA Incasso algemeen doorlopend Incassant: NL12INGB0001234567  Naam: Simyo                      Machtiging: 007-M333333333      Omschrijving: Simyo:0633333333,  FACTUURNUMMER:44444444          IBAN: NL12INGB0001234567         Kenmerk: aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }
  }

  it should "parse descriptions with no space between the last value and the next key" in {
    val raw = """SEPA Incasso algemeen eenmalig   Incassant: NL51ZZZ222222210000  Naam: ATLETIEKVERENIGING PHANOS  Machtiging: 20160609-01110-00077Omschrijving: Inschrijffgeld VU  Polderloop def                  IBAN: NL12INGB0001234567         Kenmerk: 20160609-01110-00077"""

    Details.parse(raw).right.get match {
      case Sepa(_, iban, _, _, _) => iban shouldEqual "NL12INGB0001234567"
      case _ => fail()
    }

  }

}
