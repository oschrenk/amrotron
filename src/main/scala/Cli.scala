import kantan.csv._
import kantan.csv.ops._

final case class Transaction(account: String, currency: String, before: String, after: String, amount: String, description: String)

object Cli extends App {

  implicit val transactionDecoder: RowDecoder[Transaction] = RowDecoder.decoder(1, 2, 3, 4, 5, 6)(Transaction.apply)

  val tsv = """111111111	EUR	20170103	14270,21	10000,21	20170103	-4270,00	SEPA Acceptgirobetaling          IBAN: NL86INGB1111111111        BIC: INGBNL2A                    Naam: Belastingsdienst          Betalingskenm.: 2222222222222222"""
  val reader = tsv.asCsvReader[Transaction](rfc.withColumnSeparator('\t').withoutHeader)
  reader.foreach(println)

}

