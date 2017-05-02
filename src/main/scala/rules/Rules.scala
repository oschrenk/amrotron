package rules

import model.{Details, Row, Transaction}

sealed trait Predicate {
  def apply(row: Row): Boolean
}
class TruePredicate() extends Predicate {
  override def apply(row: Row): Boolean = true
}
class AccountPredicate(number: String) extends Predicate {
  override def apply(row: Row): Boolean = row.account.contains(number)
}
case class Rule(tags: Seq[String], predicate: Predicate)

class Transformer(rules: Seq[Rule])  {

  private def from(row: Row): Transaction = {
    val date = row.date
    val account = row.account
    val amount = row.amount
    val currency = row.currency
    // TODO push parsing to front of process
    val details = Details.parse(row.description)
    val hash = row.hash()

    Transaction(date, account, amount, currency, details.right.get, hash, Nil)
  }
  def apply(row: Row): Transaction = {
    rules.foldLeft(from(row)) { (t, rule) =>
       if (rule.predicate.apply(row)) t.copy(tags = t.tags ++ rule.tags)  else t
    }
  }
}
