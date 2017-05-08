package model

object Direction {
  def apply(n: BigDecimal): Direction = {
    if (n < 0) Outgoing else Incoming
  }
}
sealed trait Direction
case object Incoming extends Direction
case object Outgoing extends Direction

