package rules

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import model.{Details, Row, Transaction}

sealed trait Predicate {
  def apply(row: Row): Boolean
}
case class TruePredicate() extends Predicate {
  override def apply(row: Row): Boolean = true
}
case class AccountPredicate(number: String) extends Predicate {
  override def apply(row: Row): Boolean = row.account.contains(number)
}
case class Rule(tags: Seq[String], predicate: Predicate)

class Transformer(rules: Seq[Rule])  {

  private val DayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  private val NumberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY)
  private def from(row: Row): Transaction = {
    val date= LocalDate.parse(row.date, DayFormatter)
    val account = row.account
    // TODO use Dutch or how to parse BigDecimal with commas
    val amount = BigDecimal(NumberFormatter.parse(row.amount).toString)
    val currency = row.currency
    // TODO push parsing to front of process
    val details = Details.parse(row.description)
    val hash = row.hash()

    Transaction(date, account, currency, amount, details.right.get, hash, Nil)
  }
  def apply(row: Row): Transaction = {
    rules.foldLeft(from(row)) { (t, rule) =>
       if (rule.predicate.apply(row)) t.copy(tags = t.tags ++ rule.tags)  else t
    }
  }
}
