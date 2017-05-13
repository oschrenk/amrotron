package model

object Stats {
  def from(transactions: Seq[Transaction]) = {

    val groupedTransactions = transactions
      .foldLeft(Map.empty[String, Seq[Transaction]]) { (acc, t) =>
        val tags = t.tags
        // build map of single transaction
        val tMap = if (tags.isEmpty) {
          Map("" -> Seq(t))
        } else {
          tags.map(tag => (tag, Seq(t))).toMap
        }

        // merge single transaction map into accumulator map
        tMap.foldLeft(acc) { (l, kv) =>
          val k = kv._1
          if (l.contains(k)) {
            l.updated(k, l(k) ++ kv._2)
          } else {
            l ++ Map(kv)
          }
        }
      }

      val stats = groupedTransactions.toSeq.map { case (k,v) =>
        val tag = k
        val count = v.length
        val total = v.map(_.amount).sum
        val average = total / count
        (tag, count, total, average)
      }.sortBy { case (_, count, total, _) => total }
      stats
  }
}
