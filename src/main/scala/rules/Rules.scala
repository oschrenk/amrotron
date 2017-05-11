package rules

import model.{ParsedRow, Transaction}

import java.io.File

import org.parboiled2.{ErrorFormatter, ParseError}
import scala.io.Source
import scala.util.{Failure, Success}

object Rules {

  val fmt = new ErrorFormatter(showTraces = true)
  def load(file: File): Seq[Rule] = {
    Source.fromFile(file, "utf-8").getLines.map{ line =>
      // comments
      if (line.startsWith("#") || line.trim.isEmpty) {
        None
      } else {
        val dslParser = new DslParser(line)
        dslParser.InputLine.run() match {
          case Success(rule) => Some(rule)
          case Failure(e: ParseError) =>
            println(s"Invalid expression: ${dslParser.formatError(e, fmt)}")
            throw new IllegalArgumentException("Error parsing rules")
          case Failure(e) =>
            throw new IllegalArgumentException("Error parsing rules")
        }
      }
    }.flatten.toSeq
  }
}

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
