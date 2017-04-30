# History

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

