package model

import org.scalatest.{FlatSpec, Matchers}

class RowSpec extends FlatSpec with Matchers {

  "Row" should "parse single line" in {
    val row = """123456789	EUR	20170103	24000,50	20000,50	20170103	-4000,00	SEPA Acceptgirobetaling          IBAN: NL12INGB0001234567        BIC: INGBNL2A                    Naam: Belastingsdienst          Betalingskenm.: 2222222222222222"""
    Row.parse(row).right.get.account shouldEqual "123456789"
  }
}