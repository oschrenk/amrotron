package rules

import model.{Details, Row, Transaction}

sealed trait Rule {
  val tags: Seq[String]
  def applies(row: Row): Boolean
}
case class AccountRule(number: String, tags: Seq[String]) extends Rule {
  override def applies(row: Row): Boolean = row.account.contains(number)
}

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
       if (rule.applies(row)) t.copy(tags = t.tags ++ rule.tags)  else t
    }
  }
}
