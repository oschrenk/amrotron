package rules

import java.time.LocalDate

import model.{ParsedRow, Sepa}
import org.scalatest.{FlatSpec, Matchers}

class TransformerSpec extends FlatSpec with Matchers {

  "Transformer" should "apply single rule" in {
    val sepa = Sepa("category", "NL123", None, "Acme", Some("description"))
    val row = ParsedRow("123", "EUR", LocalDate.of(2017, 5, 8), 20, 10, 10, sepa, "fakeHash")
    val predicate = IbanPredicate("NL12")
    val rule = rules.Rule(List("foo"), predicate)
    val transfomer = new Transformer(List(rule))
    val t = transfomer.apply(row)
    t.tags shouldBe Set("foo")
  }

}
