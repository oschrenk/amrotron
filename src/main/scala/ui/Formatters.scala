package ui

import java.time.format.DateTimeFormatter

import model._
import rules.Taxes

import scala.Console.{GREEN, RED, RESET, CYAN}

object Formatters {

  def from(format: String): (Transaction, Map[String, String]) => String = format match {
    case "csv" => csv
    case "string" => string
    case _ => pretty
  }

  private def red(s: String) = {
    s"$RESET$RED$s$RESET"
  }
  private def green(s: String) = {
    s"$RESET$GREEN$s$RESET"
  }

  private def cyan(s: String) = {
    s"$RESET$CYAN$s$RESET"
  }


  private val DayFormatter = DateTimeFormatter.ofPattern("EEE, d. MMM")
  val pretty: (Transaction, Map[String, String]) => String = (t: Transaction, addressbook: Map[String, String]) => {
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
      case Incoming => s"Got ${green(amount.toString())}$currency from $target $tags$description"
      case Outgoing => t.details match {
        case CashPoint(_,_,_) => s"Withdrew ${red((-amount).toString())}$currency to $target $tags$description"
        case _ => s"Paid ${red((-amount).toString())} $currency to $target $tags$description"
      }
    }

    s"$day: $message"
  }

  val csv: (Transaction, Map[String, String]) => String = (t: Transaction, addressbook: Map[String, String]) => {
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

  val string: (Transaction, Map[String, String]) => String = (t: Transaction, addressbook: Map[String, String]) => {
    s"$t"
  }

}
