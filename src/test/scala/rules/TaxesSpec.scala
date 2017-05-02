package rules

import org.scalatest.{FlatSpec, Matchers}

class TaxesSpec extends FlatSpec with Matchers {

  "Taxes" should "calculate taxes from deductable value" in {
    Taxes.vat(121) shouldEqual 21
  }

}
