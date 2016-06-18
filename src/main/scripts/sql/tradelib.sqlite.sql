DROP TABLE IF EXISTS strategies;
CREATE TABLE IF NOT EXISTS strategies(
	id INTEGER PRIMARY KEY NOT NULL,
	name VARCHAR(20) not null,
    details TEXT);
CREATE UNIQUE INDEX strategies_unique on strategies (name);

DROP TABLE IF EXISTS strategy_history;
CREATE TABLE IF NOT EXISTS strategy_history(
	id INTEGER PRIMARY KEY NOT NULL,
	symbol varchar(10) not null,
	strategy_id INTEGER NOT NULL,
	ts DATE NOT NULL,
	live_price DECIMAL(18,8),
	live_weight DECIMAL(18,8),
	live_position INTEGER,
	established BOOLEAN,
	ma_order TINYINT DEFAULT NULL,
	price DECIMAL(18,8),
	weight DECIMAL(18,8),
	position INTEGER,
	pnl DECIMAL(18,8),
	equity DECIMAL(18,8));
CREATE INDEX sh_i1 on strategy_history (symbol, ts);
CREATE UNIQUE INDEX sh_unique on strategy_history (symbol, strategy_id, ts);

DROP TABLE IF EXISTS yahoo_daily;
CREATE TABLE IF NOT EXISTS yahoo_daily(
	id INTEGER PRIMARY KEY NOT NULL,
	symbol varchar(10) not null,
	ts DATETIME(6) NOT NULL,
	open DECIMAL(18,8),
	high DECIMAL(18,8),
	low DECIMAL(18,8),
	close DECIMAL(18,8),
	adjusted DECIMAL(18,8),
	volume BIGINT);
CREATE UNIQUE INDEX yd_unique on yahoo_daily (symbol, ts);

DROP TABLE IF EXISTS executions;
CREATE TABLE IF NOT EXISTS executions(
	id INTEGER PRIMARY KEY NOT NULL,
	strategy_id INTEGER NOT NULL,
	symbol varchar(10) NOT NULL,
	ts DATETIME(6) NOT NULL,
	price DOUBLE,
	quantity BIGINT NOT NULL,
	signal_name VARCHAR(64));

DROP TABLE IF EXISTS trades;
CREATE TABLE IF NOT EXISTS trades(
	id INTEGER PRIMARY KEY NOT NULL,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	start DATETIME(6) NOT NULL,
	end DATETIME(6) NOT NULL,
	initial_position BIGINT NOT NULL,
	max_position BIGINT NOT NULL,
	num_transactions BIGINT NOT NULL,
	pnl DOUBLE NOT NULL,
	pct_pnl DOUBLE NOT NULL,
	tick_pnl DOUBLE NOT NULL,
	fees DECIMAL(18,8) NOT NULL);

DROP TABLE IF EXISTS pnls;
CREATE TABLE IF NOT EXISTS pnls(
	id INTEGER PRIMARY KEY NOT NULL,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	ts DATETIME(6) NOT NULL,
	pnl DOUBLE NOT NULL);
CREATE UNIQUE INDEX pnls_unique on pnls (strategy_id, symbol, ts);

DROP TABLE IF EXISTS end_equity;
CREATE TABLE IF NOT EXISTS end_equity(
	id INTEGER PRIMARY KEY NOT NULL,
	strategy_id INTEGER NOT NULL,
	ts DATETIME(6) NOT NULL,
	equity DOUBLE NOT NULL);
CREATE UNIQUE INDEX end_equity_unique on end_equity (strategy_id, ts);

DROP TABLE IF EXISTS trade_summaries;
CREATE TABLE IF NOT EXISTS trade_summaries(
	id INTEGER PRIMARY KEY NOT NULL,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	type VARCHAR(8) NOT NULL,
	num_trades BIGINT NOT NULL,
    gross_profits DOUBLE DEFAULT 0.0,
    gross_losses DOUBLE DEFAULT 0.0,
    profit_factor DOUBLE DEFAULT 0.0,
    average_daily_pnl DOUBLE DEFAULT 0.0,
    daily_pnl_stddev DOUBLE DEFAULT 0.0,
    sharpe_ratio DOUBLE DEFAULT 0.0,
    average_trade_pnl DOUBLE DEFAULT 0.0,
    trade_pnl_stddev DOUBLE DEFAULT 0.0,
    pct_positive DOUBLE DEFAULT 0.0,
    pct_negative DOUBLE DEFAULT 0.0,
    max_win DOUBLE DEFAULT 0.0,
    max_loss DOUBLE DEFAULT 0.0,
    average_win DOUBLE DEFAULT 0.0,
    average_loss DOUBLE DEFAULT 0.0,
    average_win_loss DOUBLE DEFAULT 0.0,
    equity_min DOUBLE DEFAULT 0.0,
    equity_max DOUBLE DEFAULT 0.0,
    max_drawdown DOUBLE DEFAULT 0.0,
    max_drawdown_pct DOUBLE DEFAULT 0.0);
CREATE UNIQUE INDEX trade_summaries_unique on trade_summaries (strategy_id, symbol, type);

DROP TABLE IF EXISTS strategy_positions;
CREATE TABLE IF NOT EXISTS strategy_positions (
	id INTEGER PRIMARY KEY NOT NULL,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	ts DATETIME(6),
   	position DOUBLE NOT NULL,
   	last_close DECIMAL(18,8),
   	last_ts DATETIME(6) NOT NULL,
	details TEXT);
CREATE UNIQUE INDEX strategy_positions_unique on strategy_positions (strategy_id, symbol, last_ts);

DROP TABLE IF EXISTS strategy_report;
CREATE TABLE IF NOT EXISTS strategy_report (
	id INTEGER PRIMARY KEY NOT NULL,
	strategy_id INTEGER NOT NULL,
	last_date DATETIME(6) NOT NULL,
	report TEXT);
CREATE UNIQUE INDEX strategy_report_unique on strategy_report (strategy_id, last_date);