package ui

import java.time.format.DateTimeFormatter
import scala.Console.{BLUE, BOLD, GREEN, MAGENTA, RED, RESET}
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

  def from(format: String): (Transaction) => String = format match {
    case _ => pretty
  }

  private def red(s: String) = {
    s"$RESET$RED$s$RESET"
  }
  private def green(s: String) = {
    s"$RESET$GREEN$s$RESET"
  }

  private val DayFormatter = DateTimeFormatter.ofPattern("EEE, d. MMM")
  val pretty: (Transaction) => String = (t: Transaction) => {
    val direction = Direction(t.amount)
    val amount = t.amount
    val day = t.date.format(DayFormatter)
    val target = t.details match {
      case Sepa(iban, _, _, _) => iban
      case Fee(name, _) => name
      case CashPoint(_, _, description) => description
      case PayPoint(_, _, description) => description
    }
    val message = direction match {
      case Incoming => s"Got ${green(amount.toString())} from $target"
      case Outgoing => s"Paid ${red((-amount).toString())} to $target"
    }

    s"$day: $message"
  }

}
