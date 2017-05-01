# AmroTron

1. parses transaction files (`*.TAB`) downloaded from ABN Amro,
2. tags these transaction based on customazible rules
3. creates csv file for import in other programs such as Excel

## Usage

1. Download transactions from ABN Amro as `TXT` file. It will generate a
`tab`-separated, like `TXT170429155733.TAB`

Start the terminal app and point to that file. A guide will ask you
for each transaction, what to do with it.

```
amrotron TXT170429155733.TAB
Mon, 3rd April:  Moved 1000,00 to John Doe (Personal) (NL85ABNA0222222222)
  suggested tags:  #personal
$ (i)gnore, (t)ag, (c)onfirm, (d)efer: c
Sat, 15th April: Paid 67,06 to Acme Inc. (NL12TRIO2222222222)
  suggested tags: #work #decductable #tax-high
$ (i)gnore, (t)ag, (c)onfirm: c
Mon, 17th April:  Moved 1000,00 to John Doe (Savings) (NL85ABNA0222222222)
  suggested tags:  #personal #saving
$ (i)gnore, (t)ag, (c)onfirm, (l)ater: c
Mon, 17th April: Paid 73,00 to "PayPal Europe S.a.r.l. et Cie S.C.A"  // 1000774523441 PAYPAL
$ (i)gnore, (e)dit, (t)ag, (d)escribe, (l)ater, (p)df: t
$ tags: work, paypal, europe
```

## Configuration

Multiple files living in `$HOME/.amrotron/

This maps IBANs to names, so that things are more readable

```
// addressbook.config
NL85ABNA0111111111=John Doe (Personal)
NL85ABNA0222222222=John Doe (Savings)
NL12TRIO0333333333=Acme Inc.
```

```
// rules.config
// autotags



