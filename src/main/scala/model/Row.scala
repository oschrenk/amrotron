package model

import kantan.csv._
import kantan.csv.ops._

import scala.util.hashing.{MurmurHash3 => MH3}

object Row {
  implicit val rowDecoder: RowDecoder[Row] = RowDecoder.decoder(0, 1, 2, 3, 4, 6, 7)(Row.apply)

  def parse(line: String): Either[ReadError, Row] = {
    val iterator = line.asCsvReader[Row](rfc.withCellSeparator('\t').withoutHeader)
    val result = iterator.toSeq.head
    result match {
      case Success(row) => Right(row)
      case Failure(fault) => Left(fault)
    }
  }
  def parse(lines: Seq[String]): (Seq[ReadError], Seq[Row]) = {
    // FIXME kantan csv not reading the whole file
    lines.map { line =>
      parse(line) match {
        case Left(error) => (Seq(error), Nil)
        case Right(row) =>  (Nil, Seq(row))
      }
    }.reduceLeft((left, right) => (left._1 ++ right._1, left._2 ++ right._2))
  }
}

case class Row(account: String, currency: String, date: String, before: String, after: String, amount: String, description: String) {
  def hash(): String = {
    val value =
      MH3.stringHash(account) +
        17 * MH3.stringHash(date) +
        37 * MH3.stringHash(amount)
    value.toHexString
  }
}
