# AmroTron

1. parses transaction files (`*.TAB`) downloaded from ABN Amro,
2. tags these transaction based on customizable rules
3. creates csv file for import in other programs such as Excel

## Usage

Download transactions from ABN Amro as `TXT` file. It will generate a
`tab`-separated, like `TXT170429155733.TAB`

```
# defaults to colorized pretty-printed output
./amrotron TXT170429155733.TAB

# create csv
./amrotron --format csv TXT170429155733.TAB

# pretty-print without color
./amrotron --format no-color TXT170429155733.TAB
```

## Configuration

Configuration files live in `$HOME/.amrotron/`

### Rules

**Location:** `$HOME/.amrotron/rules`

This is what makes **amrotron** so useful. Once you identify certain patterns in your transactions you can automatically tag them. All these groceries you get at Albert Heijn can get that `groceries` tag if you want to. That gym subscription? That gets a `health` tag

**Tag every transaction**

```
tag with "work"
```

**Tag certain descriptions**. Most SEPA and Paypoint transactions carry a description. You can write a rule to check for certain content (it ignores upper and lower case)

```
# albert heijn uses a lot of different patterns
tag with "groceries" if description contains "albert heijn"
tag with "groceries" if description contains "AH 8598"
tag with "groceries" if description contains "5822AH"
tag with "food" if description contains "AH to go"
```

**Tag certain IBAN**

```
# bol.com uses NL27INGB0000026500
tag with "bol" if iban contains "NL27INGB0000026500"

# NS International uses NL40ABNA0537879099 for subscriptions
tag with "transport" if iban contains "NL40ABNA0537879099"
```

**Tag certain types of payment** Depending on your workflow, you might want to identify how your money is spent, via online transaction, if you withdrew moneya, or just know what internal fees and services ABN Amro you pay for

```
tag with "cash" if category is cashpoint
tag with "pay" if category is paypoint
tag with "fee" if category is fee
tag with "sepa" if category is sepa
```

### Address Book

If you prefer human readable descriptions over IBAN numbers, you can create an
address book at `$HOME/.amrotron/addressbook` and fill it with entries:

```
NL86INGB0002445588=Belastingdienst
DE88500700100175526303=Paypal
```
