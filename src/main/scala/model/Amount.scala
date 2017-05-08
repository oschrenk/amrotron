package model

import java.text.NumberFormat
import java.util.Locale

object Amount {
  private val Netherlands = new Locale("NL", "nl")
  private val NumberFormatter = NumberFormat.getNumberInstance(Netherlands)
  def parse(s: String): BigDecimal = {
    BigDecimal(NumberFormatter.parse(s).toString)
  }
}
