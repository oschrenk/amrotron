package rules

import model.{ParsedRow, Transaction}

case class Rule(tags: Seq[String], predicate: Predicate)

class Transformer(rules: Seq[Rule])  {

  private def from(row: ParsedRow): Transaction = {
    val date = row.date
    val account = row.account
    val amount = row.amount
    val currency = row.currency
    val details = row.details
    val hash = row.hash
    Transaction(date, account, currency, amount, details, hash, Nil)
  }
  def apply(row: ParsedRow): Transaction = {
    rules.foldLeft(from(row)) { (t, rule) =>
       if (rule.predicate.apply(row)) t.copy(tags = t.tags ++ rule.tags)  else t
    }
  }
}
