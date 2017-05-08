package rules

import model.Direction
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
    Account ~ Contains ~ Quote ~ Digits ~ Quote ~> ((number) => AccountPredicate(number)) |
    Direction ~ Is ~ ActualDirection ~> ((direction) => DirectionPredicate(direction)) |
    Category ~ Is ~ ActualCategory ~> ((category) => CategoryPredicate(category)) |
    Description ~ Contains ~ Quote ~ Phrase ~ Quote ~> ((needle) => DescriptionPredicate(needle)) |
    Iban ~ Contains ~ Quote ~ Phrase ~ Quote ~> ((iban) => IbanPredicate(iban))
  }

  def ActualDirection: Rule1[Direction] = rule {
    Incoming ~> (() => model.Incoming) |
    Outgoing ~> (() => model.Outgoing)
  }

  def ActualCategory: Rule1[String] = rule {
    Sepa ~> (() => "sepa") |
    CashPoint ~> (() => "cashpoint") |
    PayPoint ~> (() => "paypoint") |
    Fee ~> (() => "fee")
  }

  def Action = rule {
    Tag ~ optional(With) ~ Tags
  }

  def Tags   = rule { Quote ~ Words ~ Quote }

  def Word   = rule { capture(oneOrMore(CharPredicate.Alpha)) ~> ((w) => w)}
  def Words  = rule { oneOrMore(Word).separatedBy(Comma ~ optional(Space)) }
  def Digits = rule { capture(oneOrMore(CharPredicate.Digit)) ~> ((n) => n)}
  def Phrase = rule { capture(oneOrMore(CharPredicate.AlphaNum ++ ' ' ++ '-' ++ '\'' ++ '&' ++ '.' ++ '_' ++ '/' ++ ':')) ~> ((n) => n)}

  def Tag      = rule { "tag" }
  def With     = rule { "with" }

  def Quote    = rule { '"' }
  def Comma    = rule { ',' }

  def If        = rule { "if" }
  def Account   = rule { "account" }

  def Direction = rule { "direction" }
  def Incoming  = rule { "incoming" }
  def Outgoing  = rule { "outgoing" }

  def Iban = rule { "iban"}
  def Category  = rule { "category"}
  def Sepa  = rule { "sepa" }
  def CashPoint  = rule { "cashpoint"}
  def PayPoint  = rule { "paypoint"}
  def Fee  = rule { "fee"}

  def Description = rule { "description" }

  def Contains = rule { "contains" }
  def Is       = rule { "is" }

  def Space    = rule { oneOrMore(' ') }
}
