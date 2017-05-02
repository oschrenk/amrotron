package rules

import scala.math.BigDecimal.RoundingMode

object Taxes {

  def vat(deductable: BigDecimal, rate: Int = 21): BigDecimal = {
    val factor = BigDecimal(100 + rate) / BigDecimal(100)
    val result = deductable - deductable / factor
    // TODO is this right mode???
    result.setScale(2, RoundingMode.HALF_EVEN)
  }

}
