package model

import java.time.LocalDate

case class Transaction(date: LocalDate, account: String, currency: String, amount: BigDecimal, details: Details, hash: String, tags: Set[String])
