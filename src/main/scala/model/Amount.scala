package model

import java.text.NumberFormat
import java.util.Locale

object Amount {
  // TODO use Dutch or how to parse BigDecimal with commas
  private val NumberFormatter = NumberFormat.getNumberInstance(Locale.GERMANY)
  def parse(s: String): BigDecimal = {
    BigDecimal(NumberFormatter.parse(s).toString)

  }
}
