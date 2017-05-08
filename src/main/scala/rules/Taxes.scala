package rules

import scala.math.BigDecimal.RoundingMode

object Taxes {

  def vat(deductible: BigDecimal, rate: Int = 21): BigDecimal = {
    val factor = BigDecimal(100 + rate) / BigDecimal(100)
    val result = deductible - deductible / factor
    result.setScale(2, RoundingMode.HALF_EVEN)
  }
}
