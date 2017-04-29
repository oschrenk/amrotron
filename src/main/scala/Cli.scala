import com.prowidesoftware.swift.model.field.Field20
import com.prowidesoftware.swift.model.field.Field61
import com.prowidesoftware.swift.model.mt.mt9xx.MT940

import scala.collection.JavaConverters._

object Cli extends App {
  val msg = """
  |{1:F01AAAABB99BSMK3513951576}
  |{2:O9400934081223BBBBAA33XXXX03592332770812230834N}
  |{4:
  |:20:0112230000000890
  |:25:SAKG800030155USD
  |:28C:255/1
  |:60F:C011223USD175768,92
  |:61:0112201223CD110,92NDIVNONREF//08 IL053309
  |/GB/2542049/SHS/312,
  |:62F:C011021USD175879,84
  |:20:NONREF
  |:25:4001400010
  |:28C:58/1
  |:60F:C140327EUR6308,75
  |:61:1403270327C3519,76NTRF50RS201403240008//2014032100037666
  |ABC DO BRASIL LTDA
  |:86:INVOICE NR. 6000012801
  |ORDPRTY : ABC DO BRASIL LTDA RUA LIBERO BADARO,293-SAO
  |PAULO BRAZIL }""".stripMargin

	val mt = MT940.parse(msg)

  println("Sender: "+mt.getSender)
  println("Receiver: "+mt.getReceiver)

  val f: Field20 = mt.getField20
  println(f.getLabel() + ": "+f.getReference)

  for (tx <- mt.getField61.asScala) {
    println("Amount: "+tx.getComponent(Field61.AMOUNT))
    println("Transaction Type: "+tx.getComponent(Field61.TRANSACTION_TYPE))
    println("Reference Acc Owner: "+tx.getComponent(Field61.REFERENCE_FOR_THE_ACCOUNT_OWNER))
  }
}

