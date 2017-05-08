package rules

import model._

sealed trait Predicate {
  def apply(row: ParsedRow): Boolean
}
case class TruePredicate() extends Predicate {
  override def apply(row: ParsedRow): Boolean = true
}
case class AccountPredicate(number: String) extends Predicate {
  override def apply(row: ParsedRow): Boolean = row.account.contains(number)
}
case class DirectionPredicate(direction: Direction) extends Predicate {
  override def apply(row: ParsedRow): Boolean = {
    Direction.apply(row.amount).equals(direction)
  }
}
case class CategoryPredicate(category: String) extends Predicate {
  override def apply(row: ParsedRow): Boolean = {
    row.details match {
      case _: Sepa => "sepa".equals(category)
      case _: CashPoint => "cashpoint".equals(category)
      case _: PayPoint => "paypoint".equals(category)
      case _: Fee => "fee".equals(category)
    }
  }
}
case class DescriptionPredicate(needle: String) extends Predicate {
  override def apply(row: ParsedRow): Boolean = {
    val description = row.details match {
      case Sepa(_, _, _, _, Some(d)) => d
      case CashPoint(_, _, d) => d
      case PayPoint(_, _, d) => d
      case Fee(_, d) => d
      case _ => ""
    }
    description.toLowerCase().contains(needle.toLowerCase)
  }
}

case class IbanPredicate(iban: String) extends Predicate {
  override def apply(row: ParsedRow): Boolean = {
    row.details match {
      case Sepa(_, i, _, _, _) => i.toLowerCase.contains(iban.toLowerCase)
      case _ => false
    }
  }
}
