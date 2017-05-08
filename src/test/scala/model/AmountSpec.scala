package model

import org.scalatest.{FlatSpec, Matchers}

class AmountSpec extends FlatSpec with Matchers {

  "Amount" should "parse numbers with comma as decimal separator" in {
     Amount.parse("1,23") shouldBe BigDecimal("1.23")
  }
}
