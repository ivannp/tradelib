CREATE TABLE IF NOT EXISTS executions(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol varchar(10) NOT NULL,
	ts DATETIME NOT NULL,
	price DECIMAL(18,8),
	quantity BIGINT NOT NULL,
	signal_name VARCHAR(64))
ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS trades(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	start DATETIME NOT NULL,
	end DATETIME NOT NULL,
	initial_position BIGINT NOT NULL,
	max_position BIGINT NOT NULL,
	num_transactions BIGINT NOT NULL,
	pnl DECIMAL(18,8) NOT NULL,
	pct_pnl DECIMAL(18,8) NOT NULL,
	tick_pnl DECIMAL(18,8) NOT NULL,
	fees DECIMAL(18,8) NOT NULL)
ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS pnls(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	ts DATETIME NOT NULL,
	pnl DECIMAL(18,8) NOT NULL,
    UNIQUE INDEX pnls_unique (strategy_id, symbol, ts))
ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS trade_summaries(
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	type VARCHAR(8) NOT NULL,
	num_trades BIGINT NOT NULL,
    gross_profits DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    gross_losses DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    profit_factor DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    average_daily_pnl DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    daily_pnl_stddev DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    sharpe_ratio DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    average_trade_pnl DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    trade_pnl_stddev DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    pct_positive DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    pct_negative DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    max_win DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    max_loss DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    average_win DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    average_loss DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    average_win_loss DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    equity_min DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    equity_max DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    max_drawdown DECIMAL(18,8) NOT NULL DEFAULT 0.0,
    UNIQUE INDEX trade_summaries_unique (strategy_id, symbol, type))
ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS strategy_positions (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	strategy_id INTEGER NOT NULL,
	symbol VARCHAR(10) NOT NULL,
	ts DATETIME NOT NULL,
	details VARCHAR(256),
    UNIQUE INDEX strategy_positions_unique (strategy_id, symbol, ts))
ENGINE InnoDB;

CREATE TABLE IF NOT EXISTS strategy_orders (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	# Link to strategy_positions
	position_id INTEGER NOT NULL,
	type TINYINT NOT NULL,
	stop_price DECIMAL(18,8),
	limit_price DECIMAL(18,8))
ENGINE InnoDB;
	  
COMMIT;