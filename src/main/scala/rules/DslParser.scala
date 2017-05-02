package rules

import rules.Rule
import org.parboiled2._

class DslParser(val input: ParserInput) extends Parser {

  // implicitly match whitespace, to reduce boilderplate
  implicit def wspStr(s: String): Rule0 = rule {
    str(s) ~ zeroOrMore(' ')
  }

  def InputLine = rule { Expression ~ EOI }

  def Expression: Rule1[Rule] = rule {
   Action
  }

  def Action = rule {
    Tag ~ optional(With) ~ Tags ~> ((tags) => Rule(tags, new TruePredicate()))
  }

  def Tags  = rule { Quote ~ Words ~ Quote }
  def Words = rule { oneOrMore(Word).separatedBy(Comma) }
  def Word  = rule { capture(oneOrMore(CharPredicate.Alpha)) ~> ((w) => w)}

  def Tag   = rule { "tag" }
  def With  = rule { "with" }
  def Quote = rule { '"' }
  def Comma = rule { ',' }
}

