package model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ParsedRow {

  private val DayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
  // TODO replace with scalactic good/bad
  def parse(rows: Seq[Row]): (Seq[String], Seq[ParsedRow]) = {
    // FIXME kantan csv not reading the whole file
    rows.map { row =>
      Details.parse(row.description)  match {
        case Left(error) =>
          (Seq(error), List[ParsedRow]())
        case Right(details) =>
          val account = row.account
          val currency = row.currency
          val date= LocalDate.parse(row.date, DayFormatter)
          // TODO catch NumberFormatException
          val before = Amount.parse(row.before)
          val after = Amount.parse(row.after)
          val amount = Amount.parse(row.amount)
          val hash = row.hash()
          val parsedRow = ParsedRow(account, currency, date, before, after, amount, details, hash)

          (Nil, Seq(parsedRow))
      }
    }.reduceLeft((left, right) => (left._1 ++ right._1, left._2 ++ right._2))
  }
}

case class ParsedRow(account: String, currency: String, date: LocalDate, before: BigDecimal, after: BigDecimal, amount: BigDecimal, details: Details, hash: String)
