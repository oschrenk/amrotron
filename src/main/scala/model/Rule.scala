package model

sealed trait Rule
case class Iban(number: String) extends Rule
