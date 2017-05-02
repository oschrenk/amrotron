package rules

import org.scalatest.{FlatSpec, Matchers}

class DslParserSpec extends FlatSpec with Matchers {

  "DslParser" should "parse default tagge" in {
    val input = """tag with "foo,bar""""
    val parser = new DslParser(input)
    parser.InputLine.run().toOption.get.tags should contain inOrderOnly ("foo", "bar")
  }
}
