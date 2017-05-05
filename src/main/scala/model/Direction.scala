package model

object Direction {
  // TODO what todo with 0, is that even possible?
  def apply(n: BigDecimal): Direction = {
    if (n < 0) Outgoing else Incoming
  }
}
sealed trait Direction
case object Incoming extends Direction
case object Outgoing extends Direction

