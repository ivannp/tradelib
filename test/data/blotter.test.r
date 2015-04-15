require(blotter)
require(hfpkg)

test.blotter = function(fees=0.0) {
   rm(list=ls(all=TRUE, pos=.blotter), pos=.blotter)
   # rm(list=ls(all=TRUE, pos=.instrument), pos=.instrument)
   
   es = loadXts("es.csv", format="%Y%m%d", header=F, colClasses=c("character", rep("numeric", 6)))
   colnames(es) = c("Open", "High", "Low", "Close", "Volume", "Interest")
   
   currency("USD")
   future(currency="USD",primary_id = "ES",multiplier = 50, tick_size = 0.25)
   
   initPortf(name="default_portfolio", symbols=c("ES"), initDate="2013-12-25")
   getTxns("default_portfolio", Symbol="ES")
   
   initAcct(name="default_account", initDate="2013-12-25", initEq=15000, portfolios=c("default_portfolio"))
   
   addTxn("default_portfolio", "ES", TxnDate="2014-01-02", TxnQty=1, TxnPrice=1819.50, ConMult=50, TxnFees=fees*1)
   addTxn("default_portfolio", "ES", TxnDate="2014-01-09", TxnQty=2, TxnPrice=1826, ConMult=50, TxnFees=fees*2)
   addTxn("default_portfolio", "ES", TxnDate="2014-01-16", TxnQty=-3, TxnPrice=1829.25, ConMult=50, TxnFees=fees*3)
   getTxns("default_portfolio", Symbol="ES")
   
   addTxn("default_portfolio", "ES", TxnDate="2014-01-24", TxnQty=-2, TxnPrice=1775.00, ConMult=50, TxnFees=fees*2)
   addTxn("default_portfolio", "ES", TxnDate="2014-01-31", TxnQty=3, TxnPrice=1769.50, ConMult=50, TxnFees=fees*3)
   addTxn("default_portfolio", "ES", TxnDate="2014-02-07", TxnQty=-2, TxnPrice=1786.50, ConMult=50, TxnFees=fees*2)
   addTxn("default_portfolio", "ES", TxnDate="2014-02-14", TxnQty=1, TxnPrice=1828.00, ConMult=50, TxnFees=fees*1)
   print(getTxns("default_portfolio", Symbol="ES"))
   
   pp = getPortfolio("default_portfolio")
   tt = pp[["symbols"]][["ES"]][["txn"]]["2013-12-31/2014-03-01"]
   print(tt)
   
   update.dates = "2013-12-31/"
   
   prices = Cl(es)
   colnames(prices) = c("ES")
   
   updatePortf("default_portfolio", Symbols=c("ES"), Prices=prices, Dates=update.dates)
   pp = getPortfolio("default_portfolio")
   tt = pp[["symbols"]][["ES"]][["posPL"]]["2013-12-31/2014-03-01"]
   print(tt)
   
   updateAcct(name="default_account", Dates=update.dates)
   updateEndEq(Account="default_account", Dates=update.dates)
}

test.blotter2 = function(fees=0.0) {
   rm(list=ls(all=TRUE, pos=.blotter), pos=.blotter)
   # rm(list=ls(all=TRUE, pos=.instrument), pos=.instrument)
   
   ES = loadXts("es.csv", format="%Y%m%d", header=F, colClasses=c("character", rep("numeric", 6)))
   index(ES) = as.POSIXct(strptime(paste(index(ES),"17"),format="%Y-%m-%d %H"))
   colnames(ES) = c("Open", "High", "Low", "Close", "Volume", "Interest")
   assign("ES", value=ES, envir=.GlobalEnv)
   
   OJ = loadXts("oj.csv", format="%Y%m%d", header=F, colClasses=c("character", rep("numeric", 6)))
   index(OJ) = as.POSIXct(strptime(paste(index(OJ),"17"),format="%Y-%m-%d %H"))
   colnames(OJ) = c("Open", "High", "Low", "Close", "Volume", "Interest")
   assign("OJ", value=OJ, envir=.GlobalEnv)
   
   AUD = loadXts("aud.csv", format="%Y%m%d", header=F, colClasses=c("character", rep("numeric", 6)))
   index(AUD) = as.POSIXct(strptime(paste(index(AUD),"17"),format="%Y-%m-%d %H"))
   colnames(AUD) = c("Open", "High", "Low", "Close", "Volume", "Interest")
   assign("AUD", value=AUD, envir=.GlobalEnv)
   
   currency("USD")
   future(currency="USD",primary_id = "ES",multiplier = 50, tick_size = 0.25)
   future(currency="USD",primary_id = "OJ",multiplier = 150, tick_size = 0.05)
   future(currency="USD",primary_id = "AUD",multiplier = 1000, tick_size = 0.01)
   
   initPortf(name="default_portfolio", symbols=c("ES","OJ","AUD"), initDate="2013-12-25")
   getTxns("default_portfolio", Symbol="ES")
   
   initAcct(name="default_account", initDate="2013-12-25", initEq=150000, portfolios=c("default_portfolio"))
   
   addTxn("default_portfolio", "ES", TxnDate="2014-01-02 11:00:01", TxnQty=1, TxnPrice=1815.75, ConMult=50, TxnFees=fees*1)
   # ES: 1; OJ: 0; AUD: 0
   addTxn("default_portfolio", "OJ", TxnDate="2014-01-03 11:00:01", TxnQty=2, TxnPrice=144.25, ConMult=150, TxnFees=fees*2)
   # ES: 1; OJ: 2; AUD: 0
   addTxn("default_portfolio", "AUD", TxnDate="2014-01-03 13:00:01", TxnQty=-2, TxnPrice=86.88, ConMult=1000, TxnFees=fees*2)
   # ES: 1; OJ: 2; AUD: -2
   addTxn("default_portfolio", "AUD", TxnDate="2014-01-06 14:00:01", TxnQty=-3, TxnPrice=86.34, ConMult=1000, TxnFees=fees*3)
   # ES: 1; OJ: 2; AUD: -5
   addTxn("default_portfolio", "ES", TxnDate="2014-01-07 14:00:02", TxnQty=-3, TxnPrice=1819.25, ConMult=50, TxnFees=fees*3)
   # ES: -2; OJ: 2; AUD: -5
   addTxn("default_portfolio", "OJ", TxnDate="2014-01-10 14:00:03", TxnQty=-2, TxnPrice=151.20, ConMult=150, TxnFees=fees*2)
   # ES: -2; OJ: 0; AUD: -5
   addTxn("default_portfolio", "AUD", TxnDate="2014-01-13 14:00:02", TxnQty=1, TxnPrice=87.71, ConMult=1000, TxnFees=fees*1)
   # ES: -2; OJ: 0; AUD: -4
   addTxn("default_portfolio", "AUD", TxnDate="2014-01-21 14:00:01", TxnQty=6, TxnPrice=84.57, ConMult=1000, TxnFees=fees*6)
   # ES: -2; OJ: 0; AUD: 2
   addTxn("default_portfolio", "OJ", TxnDate="2014-01-21 11:00:02", TxnQty=3, TxnPrice=148.35, ConMult=150, TxnFees=fees*3)
   # ES: -2; OJ: 3; AUD: 2
   addTxn("default_portfolio", "AUD", TxnDate="2014-01-23 11:00:05", TxnQty=-2, TxnPrice=85.47, ConMult=1000, TxnFees=fees*2)
   # ES: -2; OJ: 3; AUD: 0
   addTxn("default_portfolio", "ES", TxnDate="2014-01-23 11:00:05", TxnQty=-2, TxnPrice=1835.25, ConMult=50, TxnFees=fees*2)
   # ES: -4; OJ: 3; AUD: 0
   addTxn("default_portfolio", "ES", TxnDate="2014-01-27 11:00:05", TxnQty=4, TxnPrice=1761.50, ConMult=50, TxnFees=fees*4)
   # ES: 0; OJ: 3; AUD: 0
   addTxn("default_portfolio", "AUD", TxnDate="2014-01-28 11:00:05", TxnQty=-2, TxnPrice=85.13, ConMult=1000, TxnFees=fees*2)
   # ES: 0; OJ: 3; AUD: -2
   addTxn("default_portfolio", "AUD", TxnDate="2014-01-31 14:00:01", TxnQty=1, TxnPrice=84.16, ConMult=1000, TxnFees=fees*1)
   # ES: 0; OJ: 3; AUD: -1
   addTxn("default_portfolio", "AUD", TxnDate="2014-02-07 11:00:05", TxnQty=1, TxnPrice=87.01, ConMult=1000, TxnFees=fees*1)
   # ES: 0; OJ: 3; AUD: 0
   addTxn("default_portfolio", "OJ", TxnDate="2014-02-14 11:00:05", TxnQty=-3, TxnPrice=153.10, ConMult=150, TxnFees=fees*3)
   # ES: 0; OJ: 0; AUD: 0
   print(getTxns("default_portfolio", Symbol="ES"))
   
   show.dates = "2013-12-31/2014-03-01"
   
   pp = getPortfolio("default_portfolio")
   #tt = pp[["symbols"]][["ES"]][["txn"]][show.dates]
   #print(tt)
   
   update.dates = "2013-12-31/"
   
   prices = merge(Cl(ES),Cl(OJ),Cl(AUD),all=F)
   colnames(prices) = c("ES","OJ","AUD")
   
   updatePortf("default_portfolio", Symbols=c("ES","OJ","AUD"), Dates=update.dates)
   pp = getPortfolio("default_portfolio")
   #tt = pp[["symbols"]][["ES"]][["posPL"]][show.dates]
   #print(tt)
   
   updateAcct(name="default_account", Dates=update.dates)
   updateEndEq(Account="default_account", Dates=update.dates)
   
   acct = getAccount("default_account")
   print(acct$summary["2013-12-25/2014-03-03"])
}

test.blotter1 = function(fees=0.0) {
   rm(list=ls(all=TRUE, pos=.blotter), pos=.blotter)
   # rm(list=ls(all=TRUE, pos=.instrument), pos=.instrument)
   
   es = xts(read.zoo(file="es.csv", format="%Y%m%d", header=F, colClasses=c("character", rep("numeric", 6)), sep=","))
   colnames(es) = c("Open", "High", "Low", "Close", "Volume", "Interest")
   
   currency("USD")
   future(currency="USD",primary_id = "ES",multiplier = 50, tick_size = 0.25)
   
   initPortf(name="default_portfolio", symbols=c("ES"), initDate="2013-12-31")
   
   initAcct(name="default_account", initDate="2013-12-30", initEq=15000, portfolios=c("default_portfolio"))
   
   addTxn("default_portfolio", "ES", TxnDate="2014-01-02", TxnQty=1, TxnPrice=1819.50, ConMult=50, TxnFees=fees*1)
   addTxn("default_portfolio", "ES", TxnDate="2014-01-09", TxnQty=2, TxnPrice=1826, ConMult=50, TxnFees=fees*2)
   addTxn("default_portfolio", "ES", TxnDate="2014-01-16", TxnQty=-3, TxnPrice=1829.25, ConMult=50, TxnFees=fees*3)
   
   print(getTxns("default_portfolio", Symbol="ES"))
   
   dates = "2013-12-25/2014-01-01"
   
   pp = getPortfolio("default_portfolio")
   tt = pp[["symbols"]][["ES"]][["txn"]][dates]
   print(tt)
   
   prices = Cl(es)
   colnames(prices) = c("ES")
   
   updatePortf("default_portfolio", Symbols=c("ES"), Prices=prices)
   pp = getPortfolio("default_portfolio")
   tt = pp[["symbols"]][["ES"]][["posPL"]][dates]
   print(tt)
   
   updateAcct(name="default_account")
   aa = getAccount("default_account")
   print(aa$summary[dates])
}

# > pp$symbols$ES$txn
#                     Txn.Qty Txn.Price Txn.Value Txn.Avg.Cost Pos.Qty Pos.Avg.Cost Gross.Txn.Realized.PL Txn.Fees
# 1950-01-01 00:00:00       0      0.00       0.0         0.00       0        0.000                   0.0        0
# 2014-01-02 00:00:00       1   1819.50   90975.0      1819.50       1     1819.500                   0.0        0
# 2014-01-09 00:00:00       2   1826.00  182600.0      1826.00       3     1823.833                   0.0        0
# 2014-01-16 00:00:00      -3   1829.25 -274387.5      1829.25       0        0.000                 812.5        0
# 2014-01-24 00:00:00      -2   1775.00 -177500.0      1775.00      -2     1775.000                   0.0        0
# 2014-01-31 00:00:00       2   1769.50  176950.0      1769.50       0        0.000                 550.0        0
# 2014-01-31 00:00:00       1   1769.50   88475.0      1769.50       1     1769.500                   0.0        0
# 2014-02-07 00:00:00      -1   1786.50  -89325.0      1786.50       0        0.000                 850.0        0
# 2014-02-07 00:00:00      -1   1786.50  -89325.0      1786.50      -1     1786.500                   0.0        0
# 2014-02-14 00:00:00       1   1828.00   91400.0      1828.00       0        0.000               -2075.0        0
#                     Net.Txn.Realized.PL Con.Mult
# 1950-01-01 00:00:00                 0.0        0
# 2014-01-02 00:00:00                 0.0       50
# 2014-01-09 00:00:00                 0.0       50
# 2014-01-16 00:00:00               812.5       50
# 2014-01-24 00:00:00                 0.0       50
# 2014-01-31 00:00:00               550.0       50
# 2014-01-31 00:00:00                 0.0       50
# 2014-02-07 00:00:00               850.0       50
# 2014-02-07 00:00:00                 0.0       50
# 2014-02-14 00:00:00             -2075.0       50

## Realistic trading via blotter on the SPY.
##
## @param data an xts object having at least a Close column and a weight (indicator) column
## @param init.equity the initial equity
## @param init.date the initial date to initialize the account - must be before the data start and/or start.date
## @param start.date the start trading date
## @param end.date the last trading date
##
## @code
##    spy = load.xts("spy.csv")
##    ind = load.xts("ind.csv")   
##    data = merge(spy, ind, all=F)
##    dd = blotter.trade(data, start.date="2014-12-01")
## @endcode
trade.spy = function(data, init.equity=100000, init.date="1990-01-01", start.date=NULL, end.date=NULL) {
   rm(list=ls(all=TRUE, pos=.blotter), pos=.blotter)
   
   stopifnot(is.xts(data))

   # Set currency and instruments
   currency("USD")
   stock("SPY",currency="USD",multiplier=1)

   # Set up a portfolio object and an account object in blotter
   initPortf(name='default', symbols=c("SPY"), initDate=init.date)
   initAcct(name='default', portfolios='default', initDate=init.date, initEq=init.equity)
   verbose = TRUE
   
   data.index = index(data)
   data.close = Cl(data)
   data.indicator = as.numeric(data[,NCOL(data)])
   data.nrow = NROW(data)
   
   # Determine the starting index
   if(!missing(start.date) && !is.null(start.date))
   {
      start.index = data.nrow - NROW(index(data[paste(sep="", start.date, "/")])) + 1
   }
   else
   {
      start.index = 1
   }
   
   # Determine the ending index
   if(missing(end.date) || is.null(end.date))
   {
      last.index = data.nrow
   }
   else
   {
      last.index = NROW(index(data[paste(sep="", "/", end.date)]))
   }
   
   if(start.index > last.index)
   {
      return(NULL)
   }
   
   result = matrix(0, nrow=data.nrow, ncol=6)
   
   for(ii in start.index:last.index) {
      current.date = data.index[ii]
      
      equity = getEndEq(Account='default', current.date)
      result[ii,1] = equity
      
      close.price = as.numeric(data.close[ii,])
      current.pos = getPosQty(Portfolio='default', Symbol="SPY", Date=current.date)
      result[ii,2] = close.price
      result[ii,3] = current.pos
      current.sign = sign(current.pos)
      
      new.weight = data.indicator[ii]
      new.sign = sign(new.weight)
      new.pos = as.numeric(trunc(new.weight*equity/close.price))
      quantity = new.pos - current.pos
      result[ii,4] = new.weight
      result[ii,5] = new.pos
      result[ii,6] = quantity
      
      if(quantity != 0) {
         addTxn('default', 'SPY', TxnDate=current.date, TxnQty=quantity, TxnPrice=close.price)
      }
      
      # Calculate P&L and resulting equity with blotter
      updatePortf(Portfolio='default', Dates=current.date)
      updateAcct(name='default', Dates=current.date)
      updateEndEq(Account='default', Dates=current.date)
   }
   
   result = data.frame(result)
   colnames(result) = c("equity","close","current.pos","new.weight","new.pos","quantity")
   return(result)
   
   # return(getAccount('default')$summary$End.Eq)
}