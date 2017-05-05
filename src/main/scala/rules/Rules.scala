package rules

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import model._

sealed trait Predicate {
  def apply(row: Row): Boolean
}
case class TruePredicate() extends Predicate {
  override def apply(row: Row): Boolean = true
}
case class AccountPredicate(number: String) extends Predicate {
  override def apply(row: Row): Boolean = row.account.contains(number)
}
case class DirectionPredicate(direction: Direction) extends Predicate {
  override def apply(row: Row): Boolean = {
    Direction.apply(Amount.parse(row.amount)).equals(direction)
  }
}
case class Rule(tags: Seq[String], predicate: Predicate)

class Transformer(rules: Seq[Rule])  {

  private val DayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  private def from(row: Row): Transaction = {
    val date= LocalDate.parse(row.date, DayFormatter)
    val account = row.account
    val amount = Amount.parse(row.amount)
    val currency = row.currency
    // TODO push parsing to front of process to catch errors early
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
