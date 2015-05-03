# SHOW WARNINGS;

# DROP TABLE IF EXISTS strategies;
CREATE TABLE IF NOT EXISTS strategies(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	name varchar(20) not null,
    UNIQUE KEY strategies_unique (name))
ENGINE InnoDB;

# DROP TABLE IF EXISTS strategy_history;
CREATE TABLE IF NOT EXISTS strategy_history(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
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
	equity DECIMAL(18,8),
    INDEX sh_i1 (symbol, ts),
    UNIQUE KEY sh_unique (symbol, strategy_id, ts))
ENGINE InnoDB;

SELECT * FROM strategies;

CREATE TABLE IF NOT EXISTS yahoo_daily(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	symbol varchar(10) not null,
	ts DATETIME NOT NULL,
	open DECIMAL(18,8),
	high DECIMAL(18,8),
	low DECIMAL(18,8),
	close DECIMAL(18,8),
	adjusted DECIMAL(18,8),
	volume BIGINT,
    UNIQUE KEY yd_unique (symbol, ts))
ENGINE InnoDB;

INSERT INTO strategies values (1, 'ARMA/GARCH');

CREATE TABLE IF NOT EXISTS executions(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol varchar(10) NOT NULL,
	ts DATETIME NOT NULL,
	price DOUBLE,
	quantity BIGINT NOT NULL,
	signal VARCHAR(64))
ENGINE InnoDB;

# DROP TABLE IF EXISTS trade_stats;
# DROP TABLE IF EXISTS trades;
CREATE TABLE IF NOT EXISTS trades(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	start DATETIME NOT NULL,
	end DATETIME NOT NULL,
	initial_position BIGINT NOT NULL,
	max_position BIGINT NOT NULL,
	num_transactions BIGINT NOT NULL,
	pnl DOUBLE NOT NULL,
	pct_pnl DOUBLE NOT NULL,
	tick_pnl DOUBLE NOT NULL,
	fees DECIMAL(18,8) NOT NULL)
ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS pnls(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	ts DATETIME NOT NULL,
	pnl DOUBLE NOT NULL,
    UNIQUE KEY pnls_unique (strategy_id, symbol, ts))
ENGINE InnoDB;

DROP TABLE IF EXISTS end_equity;
CREATE TABLE IF NOT EXISTS end_equity(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	ts DATETIME NOT NULL,
	equity DOUBLE NOT NULL,
    UNIQUE KEY end_equity_unique (strategy_id, ts))
ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS trade_summaries(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
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
    UNIQUE KEY trade_summaries_unique (strategy_id, symbol, type))
ENGINE InnoDB;

DROP TABLE IF EXISTS strategy_positions;
CREATE TABLE IF NOT EXISTS strategy_positions (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	ts DATETIME NOT NULL,
   position DOUBLE NOT NULL,
   last_close DECIMAL(18,8),
   last_ts DATETIME,
	details TEXT,
    UNIQUE INDEX strategy_positions_unique (strategy_id, symbol, ts))
ENGINE InnoDB;

GRANT DELETE,INSERT,SELECT,UPDATE ON strategies TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON strategy_history TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON yahoo_daily TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON executions TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON trades TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON pnls TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON end_equity TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON trade_summaries TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON strategy_positions TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';

COMMIT;