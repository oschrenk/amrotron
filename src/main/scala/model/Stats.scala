package model

trait Monoid[M] {
  def zero: M
  def merge(b: M): M
}

class MonoidMap[K,V](underlying: Map[K, Seq[V]]) extends Monoid[Map[K,Seq[V]]] {
  val zero = Map.empty
  def merge(right: Map[K, Seq[V]]) =
    right.foldLeft(underlying) { (acc, kv) =>
      val k = kv._1
      if (acc.contains(k)) {
        acc.updated(k, acc(k) ++ kv._2)
      } else {
        acc ++ Map(kv)
      }
    }
}

object Stats {

  implicit def augmentMap[K,V](m: Map[K,Seq[V]]): MonoidMap[K,V] = new MonoidMap(m)
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
        acc.merge(tMap)
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
