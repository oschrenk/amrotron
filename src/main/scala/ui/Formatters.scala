package ui

import java.time.format.DateTimeFormatter
import scala.Console.{GREEN, RED, RESET}
import model._

object Direction {
  // TODO what todo with 0, is that even possible?
  def apply(n: BigDecimal): Direction = {
    if (n < 0) Outgoing else Incoming
  }
}
sealed trait Direction
case object Incoming extends Direction
case object Outgoing extends Direction

object Formatters {

  def from(format: String): (Transaction, Map[String, String]) => String = format match {
    case _ => pretty
  }

  private def red(s: String) = {
    s"$RESET$RED$s$RESET"
  }
  private def green(s: String) = {
    s"$RESET$GREEN$s$RESET"
  }

  private val DayFormatter = DateTimeFormatter.ofPattern("EEE, d. MMM")
  val pretty: (Transaction, Map[String, String]) => String = (t: Transaction, addressbook: Map[String, String]) => {
    val direction = Direction(t.amount)
    val amount = t.amount
    val day = t.date.format(DayFormatter)
    val target = t.details match {
      case Sepa(iban, _, _, _) => addressbook.getOrElse(iban.toUpperCase, iban)
      case Fee(name, _) => name
      case CashPoint(_, _, text) => text
      case PayPoint(_, _, text) => text
    }
    val currency = t.currency match {
      case s if s.equalsIgnoreCase("EUR") =>"€"
      case s => s
    }
    val description = t.details match {
      case Sepa(_, _, _, Some(text)) => s" // $text"
      case _ => ""
    }
    val message = direction match {
      case Incoming => s"Got ${green(amount.toString())}$currency from $target$description"
      case Outgoing => t.details match {
        case CashPoint(_,_,_) => s"Withdrew ${red((-amount).toString())}$currency to $target$description"
        case _ => s"Paid ${red((-amount).toString())}$currency to $target$description"
      }
    }

    s"$day: $message"
  }

}
