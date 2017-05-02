package rules

import org.parboiled2._

class DslParser(val input: ParserInput) extends Parser {

  // implicitly match whitespace, to reduce boilerplate
  implicit def wspStr(s: String): Rule0 = rule {
    str(s) ~ zeroOrMore(' ')
  }

  def InputLine = rule { Expression ~ EOI }

  def Expression: Rule1[rules.Rule] = rule {
    Action ~ !' ' ~> ((tags) => rules.Rule(tags, TruePredicate())) |
    Action ~ Predicates  ~> ((tags, predicate) => rules.Rule(tags, predicate))
  }

  def Predicates: Rule1[Predicate] = rule {
    Space ~ If ~ Predicate
  }

  def Predicate: Rule1[Predicate] = rule {
    Account ~ Contains ~ Quote ~ Digits ~ Quote ~> ((number) => AccountPredicate(number))
  }

  def Action = rule {
    Tag ~ optional(With) ~ Tags
  }

  def Tags   = rule { Quote ~ Words ~ Quote }

  def Word   = rule { capture(oneOrMore(CharPredicate.Alpha)) ~> ((w) => w)}
  def Words  = rule { oneOrMore(Word).separatedBy(Comma) }
  def Digits = rule { capture(oneOrMore(CharPredicate.Digit)) ~> ((n) => n)}

  def Tag      = rule { "tag" }
  def With     = rule { "with" }

  def Quote    = rule { '"' }
  def Comma    = rule { ',' }

  def If       = rule { "if" }
  def Account  = rule { "account" }
  def Contains = rule { "contains" }
  def Is       = rule { "is" }
  def Space    = rule { oneOrMore(' ') }

}
