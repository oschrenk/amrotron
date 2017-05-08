package rules

import java.time.LocalDate

import model.{ParsedRow, Sepa}
import org.scalatest.{FlatSpec, Matchers}

class PredicateSpec extends FlatSpec with Matchers {

  "IbanPredicate" should "filter on partial ibans" in {
    val sepa = Sepa("category", "NL123", None, "Acme", Some("description"))
    val row = ParsedRow("123", "EUR", LocalDate.of(2017, 5, 8), 20, 10, 10, sepa, "fakeHash")
    IbanPredicate("NL12").apply(row) shouldBe true
  }
}
