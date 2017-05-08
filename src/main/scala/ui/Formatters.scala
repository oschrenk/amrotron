package ui

import java.time.format.DateTimeFormatter

import model._
import rules.Taxes

import scala.Console.{GREEN, RED, RESET, CYAN}

trait Colorizer {
  def red(s: String): String
  def green(s: String): String
  def cyan(s: String): String
}
class Rainbow extends Colorizer {
  def red(s: String): String = s"$RESET$RED$s$RESET"
  def green(s: String): String =  s"$RESET$GREEN$s$RESET"
  def cyan(s: String): String =  s"$RESET$CYAN$s$RESET"
}
class Bland extends Colorizer {
  def red(s: String): String = s
  def green(s: String): String = s
  def cyan(s: String): String = s
}

object Formatters {

  lazy val rainbow = new Rainbow
  lazy val bland = new Bland

  def from(format: String): (Map[String, String], Transaction) => String = format match {
    case "csv" => csv
    case "string" => string
    case "no-color" => pretty(bland)
    case _ => pretty(rainbow)
  }



  private val DayFormatter = DateTimeFormatter.ofPattern("EEE, d. MMM")
  val pretty: Colorizer => ((Map[String, String], Transaction) => String) = (c: Colorizer) => (addressbook: Map[String, String], t: Transaction) => {
    val direction = Direction(t.amount)
    val amount = t.amount
    val day = t.date.format(DayFormatter)
    val target = t.details match {
      case Sepa(_, iban, _, _, _) => addressbook.getOrElse(iban.toUpperCase, iban)
      case Fee(name, _) => name
      case CashPoint(_, _, text) => text
      case PayPoint(_, _, text) => text
    }
    val currency = t.currency match {
      case s if s.equalsIgnoreCase("EUR") =>"€"
      case s => s
    }
    val description = t.details match {
      case Sepa(_, _, _, _, Some(text)) => s" // $text"
      case Fee(_, d) => s" // $d"
      case _ => ""
    }
    val tags = t.tags.map(tag => c.cyan(s"#$tag")).mkString(" ")
    val message = direction match {
      case Incoming => s"Got ${c.green(amount.toString())}$currency from $target $tags$description"
      case Outgoing => t.details match {
        case CashPoint(_,_,_) => s"Withdrew ${c.red((-amount).toString())}$currency to $target $tags$description"
        case _ => s"Paid ${c.red((-amount).toString())} $currency to $target $tags$description"
      }
    }

    s"$day: $message"
  }

  val csv: (Map[String, String], Transaction) => String = (addressbook: Map[String, String], t: Transaction) => {
    def quote(s: String) = { "\"" + s + "\"" }
    val account = quote(t.account)
    val amount = quote(t.amount.toString())
    // TODO depends on tags
    val deductable = quote(t.amount.toString())
    // TODO depends on tags
    val tax = quote(Taxes.vat(t.amount).toString())
    val tags = quote(t.tags.mkString(","))

    s"$account,$amount,$deductable,$tax,$tags"
  }

  val string: (Map[String, String], Transaction) => String = (addressbook: Map[String, String], t: Transaction) => {
    s"$t"
  }

}
