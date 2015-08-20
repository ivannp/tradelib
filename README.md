# Tradelib
A Java Framework for Backtesting and Trading

## History
Tradelib has been used in real life, for backtesting and for signal
generation, since early 2015.

It has been mostly used to support futures trading. Thus, stocks and
ETFs shouldn't be a problem, but work is needed to support Forex,
especially when USD is not the quote currency.

## Requirements
Tradelib uses MySQL 5.6.4 or higher. It would work with older versions,
but slight changes are needed to the table definition files.

Maven is the build tool.

The code is Java (Java 8 or higher), thus, it should work pretty much everywhere.

## Challange
Tradelib is a framework, it is not straightforward to use. Currently there
aren't any examples. I have implemented a lot of strategies on top of it,
but, I am unwilling (and, frankly, lazy) to open source any of them. Hence,
a challange for you: if you have a strategy which you don't mind sharing,
and it is relatively straightforward,
I may be willing to implement it in Tradelib and to use it as an example.
