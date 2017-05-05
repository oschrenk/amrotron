# History

## MT940 vs CSV

ABN Amro supports multiple export options for transactions.

1. MT940 
2. Tab Separated Values

and other binary formats, such as PDF.

My first idea was that the MT940 was the best option for parsing, at it followed
an industry accepted protocol, namely the MT940 variation of the Swift Protocol.

Turns out though that ABN Amro has their own idea of the format and does not
include certain fields, or blocks how they are called, namely they are missing 
the `2:` and the `4:` block. It kind of makes sense since these blocks are, so it
seems to me, more for individual transactions, where you actually have to track 
the sender and receiver. In the export, that receiver or sender is always you.

So the format wasn't actually the problem. I found only one Java library that 
seemed mature enough to use. I was able to wrap ABN Amro export, and parse it. 

That library (1) though cut of the year of the date, and only gave the month and
the day of the transaction back. You could work arround it by never working with 
list of  transactions spanning over the end of the year, but it created a
feeling of unease, so I opted to base my solutions on a homegrown CSV parsing
strategy.

(1): `"com.prowidesoftware" % "pw-swift-core" % "SRU2016-7.8.5"`

## Parsing Details

Every transaction comes with it's own details and while there are only four basic
variations, there is one variation for which you always find one edge case, when
you think you found the right parsing technique.

The four variations are:

1. **Whitespace separated** Sepa or **key: text**
2. **Slash separated** 
3. **Internal Fee**
4. **Cashpoint/Paypoint**

### Whitespace separated Sepa

I tried the following approaches:

1. Splitting the text whenever I find 2 or more spaces.
2. Splitting the text whenever I find a colon using the last word before colon
3. Using a list of known separator keys

Re 1: Of course there are examples when there is only ony space between the 
last description, and the key of the next key/value pair.
Re 2: Of course there are examples when there are colon inside the descriptions

Re 3: Means that I can only have a whitelist of accepted keywords and will fail
on anything else. 

## Configuration: DSL vs JSON

I played with a few variations on my head:

My first idea was to create a JSON like structure and use a JSON parsing library
to build the appropiate objects.

For example:

```
// Tag every outgoing transaction that targets `<IBAN>`
{ account: "number",
direction: "outgoing" // negative
to: "<IBAN>",
tag: ["tag"] }
```

Or this:

```
// Tag every transaction which `<field>` contains `<text>`
{ account: "number",
direction: "outgoing",
contains: {
  field: "description"
  text: "paypal"
},
tag: ["oschrenk", "paypal", "high"]}
```

It felt restrictive, and clumsy. Since I like PEG-parsing I went for a DSL
 approach.
