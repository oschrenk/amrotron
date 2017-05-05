package rules

import model.Incoming
import org.parboiled2.{ErrorFormatter, ParseError}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.{Failure, Success}

class DslParserSpec extends FlatSpec with Matchers {
  val fmt = new ErrorFormatter(showTraces = true)

  def parse(line: String): Rule = {
    val parser = new DslParser(line)
    parser.InputLine.run() match {
      case Success(rule) => rule
      case Failure(e: ParseError) =>
        println(s"Invalid expression: ${parser.formatError(e, fmt)}")
        throw new IllegalArgumentException("Error parsing rules")
      case Failure(e) =>
        throw new IllegalArgumentException("Error parsing rules")
    }
  }
  "DslParser" should "parse default tags" in {
    val input = """tag with "foo,bar""""
    parse(input) shouldEqual Rule(List("foo", "bar"), TruePredicate())
  }

  it should "parse tagging account predicates" in {
    val input = """tag with "foo" if account contains "123""""
    parse(input) shouldEqual Rule(List("foo"), AccountPredicate("123"))
  }
  it should "parse tagging direction incoming predicates" in {
    val input = """tag with "foo" if direction is incoming"""
    parse(input) shouldEqual Rule(List("foo"), DirectionPredicate(Incoming))
  }
}
