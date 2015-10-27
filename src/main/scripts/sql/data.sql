USE quintuitive;

DROP TABLE IF EXISTS instrument;
CREATE TABLE IF NOT EXISTS instrument (
   id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
   provider VARCHAR(10) NOT NULL,
   symbol VARCHAR(16) NOT NULL,
   type VARCHAR(16),
   bpv DECIMAL(18,8) NOT NULL,
   tick DECIMAL(18,8) NOT NULL,
   min_move DECIMAL(18,8) NOT NULL,
   comment VARCHAR(64),
   exchange VARCHAR(32),
   contracts CHAR(12),
   roll_day TINYINT,
   roll_month TINYINT,
   bytes TINYINT,
   decimal_digits TINYINT,
   current_contract DATE,
   next_contract DATE,
   rollover_date DATE,
   trading_days INTEGER,
   days INTEGER,
   UNIQUE INDEX instrument_unique (symbol,provider))
ENGINE = InnoDB;

DROP TABLE IF EXISTS instrument_variation;
CREATE TABLE IF NOT EXISTS instrument_variation (
   id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
   provider VARCHAR(10) NOT NULL,
   symbol VARCHAR(16) NOT NULL,
   original_provider VARCHAR(10),
   original_symbol VARCHAR(16) NOT NULL,
   factor DECIMAL(18,8) NOT NULL,
   tick DECIMAL(18,8) NOT NULL,
   UNIQUE INDEX iv_unique2 (original_provider, original_symbol))
ENGINE = InnoDB;

INSERT INTO instrument VALUES(1,'pinnacle','AN','FUT',1000.0,0.01,10.0,'Australian Dollar','CME','HHHMMMUUUZZZ',8,0,2,2,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(2,'pinnacle','BC','FUT',1000.0,0.01,10.0,'Brent Crude Oil','ICE','FGHJKMNQUVXZ',11,1,3,2,NULL,NULL,NULL,6,10);
INSERT INTO instrument VALUES(3,'pinnacle','BG','FUT',100.0,0.25,25.0,'Brent Gasoil','ICE','FGHJKMNQUVXZ',11,1,3,2,NULL,NULL,NULL,6,10);
INSERT INTO instrument VALUES(4,'pinnacle','BN','FUT',625.0,0.01,6.25,'British Pound','CME','HHHMMMUUUZZZ',8,0,3,2,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(5,'pinnacle','CC','FUT',10.0,1.0,10.0,'Cocoa','CME','HHHKKNNUUZZZ',9,1,2,0,NULL,NULL,NULL,26,38);
INSERT INTO instrument VALUES(6,'pinnacle','CN','FUT',1000.0,0.01,10.0,'Canadian Dollar','CME','HHHMMMUUUZZZ',8,0,2,2,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(7,'pinnacle','CT','FUT',500.0,0.01,5.0,'Cotton','NYBOT','HHHKKNNZZZZZ',15,1,2,2,NULL,NULL,NULL,31,45);
INSERT INTO instrument VALUES(8,'pinnacle','DA','FUT',2000.0,0.01,20.0,'Milk','CME','FGHJKMNQUVXZ',27,1,2,2,NULL,NULL,NULL,17,25);
INSERT INTO instrument VALUES(9,'pinnacle','DX','FUT',1000.0,0.005,5.0,'US Dollar Index','NYBOT','HHHMMMUUUZZZ',8,0,3,3,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(10,'pinnacle','EN','FUT',20.0,0.25,5.0,'E-mini Nasdaq','CME','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(11,'pinnacle','ER','FUT',100.0,0.1,10.0,'E-mini Russell 2000','ICE','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(12,'pinnacle','ES','FUT',50.0,0.25,12.5,'E-mini S&P 500','CME','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(13,'pinnacle','FB','FUT',1000.0,0.0078125,7.8125,'5-Year US T-Note','CBOT','HHHMMMUUUZZZ',24,1,4,6,NULL,NULL,NULL,37,53);
INSERT INTO instrument VALUES(14,'pinnacle','FN','FUT',1250.0,0.01,12.5,'Euro','CME','HHHMMMUUUZZZ',8,0,3,3,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(15,'pinnacle','JN','FUT',1250.0,0.01,12.5,'Japanese Yen','CME','HHHMMMUUUZZZ',8,0,2,2,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(16,'pinnacle','JO','FUT',150.0,0.05,7.5,'Orange Juice','ICE','FHHKKNNUUXXF',25,1,2,2,NULL,NULL,NULL,38,54);
INSERT INTO instrument VALUES(17,'pinnacle','KC','FUT',375.0,0.05,18.75,'Coffee','ICE','HHHKKNNUUZZZ',11,1,2,2,NULL,NULL,NULL,28,40);
INSERT INTO instrument VALUES(18,'pinnacle','KW','FUT',50.0,0.25,12.5,'KC Wheat','CME','HHHKKNNUUZZZ',22,1,3,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(19,'pinnacle','LB','FUT',110.0,0.1,11.0,'Lumber','CME','FHHKKNNUUXXF',1,0,2,1,NULL,NULL,NULL,41,59);
INSERT INTO instrument VALUES(20,'pinnacle','EC','FUT',2500.0,0.0025,6.25,'Eurodollar','CME','HHHMMMUUUZZZ',22,1,3,4,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(21,'pinnacle','MD','FUT',100.0,0.1,10.0,'E-mini S&P 400','CME','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(22,'pinnacle','MP','FUT',500000.0,2.5e-05,12.5,'Mexican Peso','CME','HHHMMMUUUZZZ',8,0,3,6,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(23,'pinnacle','MW','FUT',50.0,0.25,12.5,'Wheat (Minneapolis)','MGE','HHHKKNNUUZZZ',22,1,3,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(24,'pinnacle','ND','FUT',100.0,0.25,25.0,'Nasdaq 100','CME','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(25,'pinnacle','NK','FUT',5.0,5.0,25.0,'Nikkei','CME','HHHMMMUUUZZZ',3,0,3,0,NULL,NULL,NULL,42,60);
INSERT INTO instrument VALUES(26,'pinnacle','RL','FUT',500.0,0.05,25.0,'Russell 2000','CME','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(27,'pinnacle','SB','FUT',1120.0,0.01,11.2,'Sugar','ICE','HHHKKNNVVVHH',22,1,2,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(28,'pinnacle','SN','FUT',1250.0,0.01,12.5,'Swiss Franc','CME','HHHMMMUUUZZZ',8,0,2,2,NULL,NULL,NULL,46,66);
INSERT INTO instrument VALUES(29,'pinnacle','SC','FUT',250.0,0.1,25.0,'S&P 500','CME','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(30,'pinnacle','TU','FUT',2000.0,0.0078125,15.63,'2-Year US T-Note','CBOT','HHHMMMUUUZZZ',24,1,4,7,NULL,NULL,NULL,37,53);
INSERT INTO instrument VALUES(31,'pinnacle','TY','FUT',1000.0,0.015625,15.63,'10-Year US T-Note','CBOT','HHHMMMUUUZZZ',24,1,4,6,NULL,NULL,NULL,37,53);
INSERT INTO instrument VALUES(32,'pinnacle','US','FUT',1000.0,0.03125,31.25,'30-Year US T-Bond','CBOT','HHHMMMUUUZZZ',24,1,4,5,NULL,NULL,NULL,37,53);
INSERT INTO instrument VALUES(33,'pinnacle','YM','FUT',5.0,1.0,5.0,'E-mini Dow Jones','CBOT','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(34,'pinnacle','ZD','FUT',25.0,1.0,25.0,'Dow Jones','CBOT','HHHMMMUUUZZZ',-13,0,3,2,NULL,NULL,NULL,49,69);
INSERT INTO instrument VALUES(35,'pinnacle','ZG','FUT',100.0,0.1,10.0,'Gold','COMMEX','GGJJMMQQZZZZ',22,1,2,1,NULL,NULL,NULL,14,20);
INSERT INTO instrument VALUES(36,'pinnacle','ZI','FUT',50.0,0.5,25.0,'Silver','COMMEX','HHHKKNNUUZZZ',22,1,2,1,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(37,'pinnacle','ZC','FUT',50.0,0.25,12.5,'Corn','CBOT','HHHKKNNUUZZZ',22,1,3,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(38,'pinnacle','ZS','FUT',50.0,0.25,12.5,'Soybeans','CBOT','FHHKKNNQXXXF',22,1,3,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(39,'pinnacle','ZL','FUT',600.0,0.01,6.0,'Soybean Oil','CBOT','FHHKKNNQZZZZ',22,1,2,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(40,'pinnacle','ZO','FUT',50.0,0.25,12.5,'Oats','CBOT','HHHKKNNUUZZZ',13,1,3,2,NULL,NULL,NULL,30,42);
INSERT INTO instrument VALUES(41,'pinnacle','ZM','FUT',100.0,0.1,10.0,'Soybean Meal','CBOT','FHHKKNNQZZZZ',22,1,3,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(42,'pinnacle','ZR','FUT',20.0,0.5,10.0,'Rough Rice','CBOT','FHHKKNNUUXXF',22,1,3,1,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(43,'pinnacle','ZW','FUT',50.0,0.25,12.5,'Wheat','CBOT','HHHKKNNUUZZZ',22,1,3,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(44,'pinnacle','ZU','FUT',1000.0,0.01,10.0,'Crude Oil','NYMEX','FGHJKMNQUVXZ',11,1,2,2,NULL,NULL,NULL,6,10);
INSERT INTO instrument VALUES(45,'pinnacle','ZB','FUT',420.0,0.01,4.2,'RBOB Gasoline','NYMEX','FGHJKMNQUVXZ',11,1,2,2,NULL,NULL,NULL,6,10);
INSERT INTO instrument VALUES(46,'pinnacle','ZH','FUT',420.0,0.01,4.2,'Heating Oil','NYMEX','FGHJKMNQUVXZ',11,1,2,2,NULL,NULL,NULL,6,10);
INSERT INTO instrument VALUES(47,'pinnacle','ZN','FUT',10000.0,0.001,10.0,'Natural Gas','NYMEX','FGHJKMNQUVXZ',18,1,3,3,NULL,NULL,NULL,11,17);
INSERT INTO instrument VALUES(48,'pinnacle','ZK','FUT',250.0,0.05,12.5,'Copper','COMEX','HHHKKNNUUZZZ',22,1,2,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(49,'pinnacle','ZA','FUT',100.0,0.05,5.0,'Palladium','NYMEX','HHHMMMUUUZZZ',22,1,3,2,NULL,NULL,NULL,36,52);
INSERT INTO instrument VALUES(50,'pinnacle','ZP','FUT',50.0,0.1,5.0,'Platinum','NYMEX','FJJJNNNVVVFF',22,1,2,1,NULL,NULL,NULL,56,80);
INSERT INTO instrument VALUES(51,'pinnacle','ZF','FUT',500.0,0.025,12.5,'Feeder Cattle','CME','FHHKKQQQUVXF',5,0,2,2,NULL,NULL,NULL,1,3);
INSERT INTO instrument VALUES(52,'pinnacle','ZT','FUT',400.0,0.025,10.0,'Live Cattle','CME','GGJJMMQQVVZZ',27,1,2,2,NULL,NULL,NULL,17,25);
INSERT INTO instrument VALUES(53,'pinnacle','ZZ','FUT',400.0,0.025,10.0,'Lean Hogs','CME','GGJJMMNQVVZZ',27,1,2,2,NULL,NULL,NULL,17,25);

INSERT INTO instrument VALUES(101,'csi','AD','FUT',100000.0,0.0001,10.0,'Australian Dollar','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(102,'csi','BO2','FUT',600.0,0.01,6.0,'Soybean Oil','CME','VIVIVIVVVVIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(103,'csi','BP','FUT',62500.0,0.0001,6.25,'British Pound','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(104,'csi','C2','FUT',50.0,0.25,12.5,'Corn','CME','SIVIVIVIVISV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(105,'csi','CC2','FUT',10.0,1.0,10.0,'Cocoa','CSCE','IIVIVIVIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(106,'csi','CD','FUT',100000.0,0.0001,10.0,'Canadian Dollar','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(107,'csi','CL2','FUT',1000.0,0.01,10.0,'Crude Oil','NYMEX','VVVVVVVVVVVV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(108,'csi','CT2','FUT',500.0,0.01,5.0,'Cotton','NYCE','IIVIVIVIIVIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(109,'csi','CU','FUT',125000.0,0.0001,12.5,'Euro','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(110,'csi','DX2','FUT',1000.0,0.005,5.0,'US Dollar Index','FINEX','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(111,'csi','ED','FUT',2500.0,0.0025,6.25,'Eurodollar','CME','SSVSSVSSVSSV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(112,'csi','EMD','FUT',100.0,0.1,10.0,'E-mini S&P 400','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(113,'csi','ES','FUT',50.0,0.25,12.50,'E-mini S&P 500','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(114,'csi','FC','FUT',500.0,0.025,12.50,'Feeder Cattle','CME','VIVVVIIVVVVI',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(115,'csi','FV','FUT',1000.0,0.0078125,7.8125,'5-Year US T-Note','CBOT','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(116,'csi','GC2','FUT',100.0,0.1,10.0,'Gold','COMEX','SVSVSVSVSVSV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(117,'csi','HG2','FUT',250.0,0.05,12.50,'Copper','COMEX','SSVSVSVSVVSV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(118,'csi','HO2','FUT',42000.0,0.0001,4.2,'Heating Oil','NYMEX','VVVVVVVVVVVV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(119,'csi','JY','FUT',125000.0,0.0001,12.5,'Japanese Yen','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(120,'csi','KC2','FUT',375.0,0.05,18.75,'Coffee','CSCE','IIVIVIVIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(121,'csi','KW2','FUT',50.0,0.25,12.5,'KC Wheat','CME','IIVIVIVIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(122,'csi','LB','FUT',110.0,0.1,11.0,'Lumber','CME','VIVIVIVIVIVI',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(123,'csi','LC','FUT',400.0,0.025,10.0,'Live Cattle','CME','VVVVSVSVIVSV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(124,'csi','LH','FUT',400.0,0.025,10.0,'Lean Hogs','CME','IVIVVVVVIVIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(125,'csi','MP','FUT',500000.0,0.00001,5.0,'Mexican Peso','CME','VIVSSVISVISV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(126,'csi','ND','FUT',100.0,0.25,25.0,'Nasdaq 100','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(127,'csi','NG2','FUT',10000.0,0.001,10.0,'Natural Gas','NYMEX','VVVVVVVVVVVV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(128,'csi','NK','FUT',5.0,5.0,25.0,'Nikkei','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(129,'csi','NQ','FUT',20.0,0.25,5.0,'E-mini Nasdaq','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(130,'csi','O2','FUT',50.0,0.25,12.5,'Oats','CBOT','VIVIVIVVVVIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(131,'csi','OJ2','FUT',150.0,0.05,7.5,'Orange Juice','ICE','VIVIVIVIVIVI',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(132,'csi','PA2','FUT',100.0,0.05,5.0,'Palladium','NYMEX','VVVVVVVVVVVV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(133,'csi','PL2','FUT',50.0,0.1,5.0,'Platinum','NYMEX','VIIVIIVIIVII',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(134,'csi','QM','FUT',500.0,0.025,12.5,'E-mini Crude Oil','NYMEX','VVVVVVVVVVVV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(135,'csi','RB2','FUT',42000.0,0.0001,4.2,'RBOB Gasoline','NYMEX','VVVVVVVVVVVV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(136,'csi','RR2','FUT',2000.0,0.005,10.0,'Rough Rice','CBOT','VIVIVIVIVIVI',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(137,'csi','S2','FUT',50.0,0.25,12.5,'Soybeans','CBOT','VIVIVIVVVIVI',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(138,'csi','SB2','FUT',1120.0,0.01,11.2,'Sugar','CSCE','IIVIVIVIIVII',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(139,'csi','SF','FUT',125000.0,0.0001,12.5,'Swiss Franc','CME','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(140,'csi','SI2','FUT',50.0,0.5,25.0,'Silver','COMMEX','IIVIVIVIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(141,'csi','SM2','FUT',100.0,0.1,10.0,'Soybean Meal','CBOT','VIVIVIVVVVIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(142,'csi','TF2','FUT',100.0,0.1,10.0,'E-mini Russell 2000','NYFE','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(143,'csi','TU2','FUT',2000.0,0.0078125,15.63,'2-Year US T-Note','CBOT','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(144,'csi','TY','FUT',1000.0,0.015625,15.63,'10-Year US T-Note','CBOT','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(145,'csi','US','FUT',1000.0,0.03125,31.25,'30-Year US T-Bond','CBOT','IIVIIVIIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(146,'csi','W2','FUT',50.0,0.25,12.5,'Wheat','CBOT','IIVIVIVIVIIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(147,'csi','YG','FUT',32.15,0.1,3.215,'Mini Gold','CBOT','SVSVSVIVSVIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(148,'csi','YI','FUT',10.0,0.1,1.0,'Mini Silver','CBOT','SIVSVIVIVSIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(149,'csi','YM','FUT',5.0,1.0,5.0,'E-mini Dow Jones','CBOT','SIVSVIVIVSIV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(160,'csi','AD6','FX',1.0,0.0001,0.0001,'AUDUSD','CASH',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(161,'csi','EU9','FX',1.0,0.0001,0.0001,'EURUSD','CASH',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(162,'csi','QF4','FX',1.0,0.0001,0.0001,'GBPUSD','CASH',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(163,'csi','QE2','FX',1.0,0.0001,0.0001,'USDCAD','CASH',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(164,'csi','QE9','FX',1.0,0.01,0.01,'USDJPY','CASH',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(170,'csi','SPX','IND',1.0,0.01,0.01,'S&P 500','CME',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(171,'csi','DJI','IND',1.0,0.01,0.01,'Dow Jones Industrial','CBOT',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(172,'csi','CL.CASH','CASH',1.0,0.01,0.01,'Crude Oil Cash','NYMEX',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(173,'csi','DX.IND','IND',1.0,0.01,0.01,'US Dollar Index','FINEX',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(174,'csi','GC.CASH','CASH',1.0,0.01,0.01,'Gold Cash','COMEX',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO instrument VALUES(175,'csi','TY.CASH','CASH',1.0,0.01,0.01,'10-Year US T-Note Cash','CBOT',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);

INSERT INTO instrument_variation VALUES(1,'ib','EUR','pinnacle','FN',100.0,0.0001);
INSERT INTO instrument_variation VALUES(2,'ib','KE','pinnacle','KW',1.0,0.25);
INSERT INTO instrument_variation VALUES(3,'ib','HE','pinnacle','ZZ',1.0,0.025);
INSERT INTO instrument_variation VALUES(4,'ib','LE','pinnacle','ZT',1.0,0.025);
INSERT INTO instrument_variation VALUES(5,'ib','GF','pinnacle','ZF',1.0,0.025);
INSERT INTO instrument_variation VALUES(6,'ib','KC','pinnacle','KC',100.0,0.0005);
INSERT INTO instrument_variation VALUES(7,'ib','CT','pinnacle','CT',100.0,0.0001);
INSERT INTO instrument_variation VALUES(8,'ib','OJ','pinnacle','JO',100.0,0.0005);
INSERT INTO instrument_variation VALUES(9,'ib','SB','pinnacle','SB',100.0,0.0001);
INSERT INTO instrument_variation VALUES(10,'ib','HG','pinnacle','ZK',100.0,0.0005);
INSERT INTO instrument_variation VALUES(11,'ib','SI','pinnacle','ZI',100.0,0.005);
INSERT INTO instrument_variation VALUES(12,'ib','GC','pinnacle','ZG',1.0,0.1);
INSERT INTO instrument_variation VALUES(13,'ib','PA','pinnacle','ZA',1.0,0.05);
INSERT INTO instrument_variation VALUES(14,'ib','PL','pinnacle','ZP',1.0,0.1);
INSERT INTO instrument_variation VALUES(15,'ib','CL','pinnacle','ZU',1.0,0.01);
INSERT INTO instrument_variation VALUES(16,'ib','HO','pinnacle','ZH',100.0,0.0001);
INSERT INTO instrument_variation VALUES(17,'ib','RB','pinnacle','ZB',100.0,0.0001);
INSERT INTO instrument_variation VALUES(18,'ib','NG','pinnacle','ZN',1.0,0.001);
INSERT INTO instrument_variation VALUES(19,'ib','JPY','pinnacle','JN',10000.0,0.000001);
INSERT INTO instrument_variation VALUES(20,'ib','CHF','pinnacle','SN',100.0,0.0001);
INSERT INTO instrument_variation VALUES(21,'ib','GBP','pinnacle','BN',100.0,0.0001);
INSERT INTO instrument_variation VALUES(22,'ib','CAD','pinnacle','CN',100.0,0.0001);
INSERT INTO instrument_variation VALUES(23,'ib','AUD','pinnacle','AN',100.0,0.0001);
INSERT INTO instrument_variation VALUES(24,'ib','MXP','pinnacle','MP',1.0,0.00001);
INSERT INTO instrument_variation VALUES(25,'ib','ZB','pinnacle','US',1.0,0.03125);
INSERT INTO instrument_variation VALUES(26,'ib','ZN','pinnacle','TY',1.0,0.015625);
INSERT INTO instrument_variation VALUES(27,'ib','ZF','pinnacle','FB',1.0,0.0078125);
INSERT INTO instrument_variation VALUES(28,'ib','ZT','pinnacle','TU',1.0,0.0078125);
INSERT INTO instrument_variation VALUES(29,'ib','NQ','pinnacle','EN',1.0,0.25);
INSERT INTO instrument_variation VALUES(30,'ib','TF','pinnacle','ER',1.0,0.1);
INSERT INTO instrument_variation VALUES(31,'ib','EMD','pinnacle','MD',1.0,0.1);
INSERT INTO instrument_variation VALUES(32,'ib','NKD','pinnacle','NK',1.0,5.0);

INSERT INTO instrument_variation VALUES(101,'ib','AUD','csi','AD',1.0,0.0001);
INSERT INTO instrument_variation VALUES(102,'ib','ZL','csi','BO2',1.0,0.01);
INSERT INTO instrument_variation VALUES(103,'ib','GBP','csi','BP',1.0,0.0001);
INSERT INTO instrument_variation VALUES(104,'ib','ZC','csi','C2',1.0,0.25);
INSERT INTO instrument_variation VALUES(105,'ib','CC','csi','CC2',1.0,1.0);
INSERT INTO instrument_variation VALUES(106,'ib','CAD','csi','CD',1.0,0.0001);
INSERT INTO instrument_variation VALUES(107,'ib','CL','csi','CL2',1.0,0.01);
INSERT INTO instrument_variation VALUES(108,'ib','CT','csi','CT2',100.0,0.0001);
INSERT INTO instrument_variation VALUES(109,'ib','EUR','csi','CU',1.0,0.0001);
INSERT INTO instrument_variation VALUES(110,'ib','DX','csi','DX2',1.0,0.005);
INSERT INTO instrument_variation VALUES(111,'ib','GF','csi','FC',1.0,0.025);
INSERT INTO instrument_variation VALUES(112,'ib','ZF','csi','FV',1.0,0.0078125);
INSERT INTO instrument_variation VALUES(113,'ib','GC','csi','GC2',1.0,0.1);
INSERT INTO instrument_variation VALUES(120,'ib','HE','csi','LH',1.0,0.025);
INSERT INTO instrument_variation VALUES(114,'ib','HG','csi','HG2',100.0,0.0005);
INSERT INTO instrument_variation VALUES(115,'ib','HO','csi','HO2',1.0,0.0001);
INSERT INTO instrument_variation VALUES(116,'ib','JPY','csi','JY',100.0,0.000001);
INSERT INTO instrument_variation VALUES(117,'ib','KC','csi','KC2',100.0,0.0005);
INSERT INTO instrument_variation VALUES(118,'ib','KE','csi','KW2',1.0,0.25);
INSERT INTO instrument_variation VALUES(119,'ib','LE','csi','LC',1.0,0.025);
INSERT INTO instrument_variation VALUES(121,'ib','MXP','csi','MP',1.0,0.00001);
INSERT INTO instrument_variation VALUES(122,'ib','NDX','csi','NDX',1.0,0.25);
INSERT INTO instrument_variation VALUES(123,'ib','NG','csi','NG2',1.0,0.001);
INSERT INTO instrument_variation VALUES(124,'ib','NKD','csi','NK',1.0,5.0);
INSERT INTO instrument_variation VALUES(125,'ib','ZO','csi','O2',1.0,0.25);
INSERT INTO instrument_variation VALUES(126,'ib','OJ','csi','OJ2',100.0,0.0005);
INSERT INTO instrument_variation VALUES(127,'ib','PA','csi','PA2',1.0,0.05);
INSERT INTO instrument_variation VALUES(128,'ib','PL','csi','PL2',1.0,0.1);
INSERT INTO instrument_variation VALUES(129,'ib','RB','csi','RB2',1.0,0.0001);
INSERT INTO instrument_variation VALUES(130,'ib','ZS','csi','S2',1.0,0.25);
INSERT INTO instrument_variation VALUES(131,'ib','SB','csi','SB2',100.0,0.0001);
INSERT INTO instrument_variation VALUES(132,'ib','CHF','csi','SF',1.0,0.0001);
INSERT INTO instrument_variation VALUES(133,'ib','SI','csi','SI2',100.0,0.005);
INSERT INTO instrument_variation VALUES(134,'ib','ZM','csi','SM2',1.0,0.1);
INSERT INTO instrument_variation VALUES(135,'ib','TF','csi','TF2',1.0,0.1);
INSERT INTO instrument_variation VALUES(136,'ib','ZT','csi','TU2',1.0,0.0078125);
INSERT INTO instrument_variation VALUES(137,'ib','ZN','csi','TY',1.0,0.015625);
INSERT INTO instrument_variation VALUES(138,'ib','ZB','csi','US',1.0,0.03125);
INSERT INTO instrument_variation VALUES(139,'ib','ZW','csi','W2',1.0,0.25);
INSERT INTO instrument_variation VALUES(140,'ib','YI','csi','YI',100.0,0.001);

DROP TABLE IF EXISTS holidays;
CREATE TABLE IF NOT EXISTS holidays (
	ts DATE PRIMARY KEY NOT NULL)
ENGINE = InnoDB;

DROP TABLE IF EXISTS clc_index;
CREATE TABLE IF NOT EXISTS clc_index (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	symbol VARCHAR(32) NOT NULL,
	ts DATE NOT NULL,
	type TINYINT NOT NULL,
	open DECIMAL(18,8) NOT NULL,
	high DECIMAL(18,8) NOT NULL,
	low DECIMAL(18,8) NOT NULL,
	close DECIMAL(18,8) NOT NULL,
	contract_interest BIGINT NOT NULL,
	volume BIGINT NOT NULL,
	total_interest BIGINT NOT NULL,
   INDEX ci_i1 (symbol),
   INDEX ci_i2 (ts),
   UNIQUE INDEX ci_unique (symbol,ts,type))
ENGINE = InnoDB;

DROP TABLE IF EXISTS clc_rev;
CREATE TABLE IF NOT EXISTS clc_rev (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	symbol VARCHAR(32) NOT NULL,
	ts DATE NOT NULL,
	open DECIMAL(18,8) NOT NULL,
	high DECIMAL(18,8) NOT NULL,
	low DECIMAL(18,8) NOT NULL,
	close DECIMAL(18,8) NOT NULL,
	contract_interest BIGINT NOT NULL,
	volume BIGINT NOT NULL,
	total_interest BIGINT NOT NULL,
   UNIQUE INDEX cr_unique (symbol,ts))
ENGINE = InnoDB;

DROP TABLE IF EXISTS clc_non;
CREATE TABLE IF NOT EXISTS clc_non (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	symbol VARCHAR(32) NOT NULL,
	ts DATE NOT NULL,
	open DECIMAL(18,8) NOT NULL,
	high DECIMAL(18,8) NOT NULL,
	low DECIMAL(18,8) NOT NULL,
	close DECIMAL(18,8) NOT NULL,
	contract_interest BIGINT NOT NULL,
	volume BIGINT NOT NULL,
	total_interest BIGINT NOT NULL,
   UNIQUE INDEX cn_unique (symbol,ts))
ENGINE = InnoDB;

DROP TABLE IF EXISTS clc_rad;
CREATE TABLE IF NOT EXISTS clc_rad (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	symbol VARCHAR(32) NOT NULL,
	ts DATE NOT NULL,
	open DECIMAL(18,8) NOT NULL,
	high DECIMAL(18,8) NOT NULL,
	low DECIMAL(18,8) NOT NULL,
	close DECIMAL(18,8) NOT NULL,
	contract_interest BIGINT NOT NULL,
	volume BIGINT NOT NULL,
	total_interest BIGINT NOT NULL,
   UNIQUE INDEX cn_unique (symbol,ts))
ENGINE = InnoDB;

DROP TABLE IF EXISTS csi_bars;
CREATE TABLE IF NOT EXISTS csi_bars (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
	symbol VARCHAR(32) NOT NULL,
	ts DATE NOT NULL,
	open DECIMAL(18,8) NOT NULL,
	high DECIMAL(18,8) NOT NULL,
	low DECIMAL(18,8) NOT NULL,
	close DECIMAL(18,8) NOT NULL,
   volume BIGINT NOT NULL,
	interest BIGINT NOT NULL,
   closing_bid DECIMAL(18,8),
   closing_ask DECIMAL(18,8),
   UNIQUE INDEX cb_unique (symbol,ts))
ENGINE = InnoDB;

DROP TABLE IF EXISTS symbol_mappings;
CREATE TABLE IF NOT EXISTS symbol_mappings (
	id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   portfolio VARCHAR(32) NOT NULL,
   symbol VARCHAR(32) NOT NULL,
   mapping VARCHAR(32) NOT NULL,
   UNIQUE INDEX symbol_mappings_unique (portfolio,symbol))
ENGINE = InnoDB;

DROP TABLE IF EXISTS kv;
CREATE TABLE IF NOT EXISTS kv (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   key_text VARCHAR(200),
   value_text VARCHAR(200),
   UNIQUE INDEX kv_unique(key_text))
ENGINE = InnoDB;

DROP TABLE IF EXISTS kv_bin;
CREATE TABLE IF NOT EXISTS kv_bin (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   key_text VARCHAR(200),
   value_bin BLOB,
   UNIQUE INDEX kv_bin_unique(key_text))
ENGINE = InnoDB;

DROP TABLE IF EXISTS categories;
CREATE TABLE categories (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   name VARCHAR(64) NOT NULL,
   ord INTEGER NOT NULL DEFAULT '10')
ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO categories VALUES (1,'Grains',1);
INSERT INTO categories VALUES (2,'Meats',10);
INSERT INTO categories VALUES (3,'Softs',20);
INSERT INTO categories VALUES (4,'Metals',30);
INSERT INTO categories VALUES (5,'Oils',40);
INSERT INTO categories VALUES (6,'Currencies',50);
INSERT INTO categories VALUES (7,'Bonds',60);
INSERT INTO categories VALUES (8,'Indexes',70);

DROP TABLE IF EXISTS instrument_visiable;
CREATE TABLE instrument_visiable (
  id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
  categories_id INTEGER NOT NULL,
  instrument_id INTEGER NOT NULL,
  ord INTEGER NOT NULL DEFAULT '10')
ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO instrument_visiable VALUES (64,1,40,1);
INSERT INTO instrument_visiable VALUES (65,1,37,2);
INSERT INTO instrument_visiable VALUES (66,1,38,3);
INSERT INTO instrument_visiable VALUES (67,1,41,4);
INSERT INTO instrument_visiable VALUES (68,1,39,5);
INSERT INTO instrument_visiable VALUES (69,1,43,6);
INSERT INTO instrument_visiable VALUES (70,1,18,7);
INSERT INTO instrument_visiable VALUES (71,1,42,8);
INSERT INTO instrument_visiable VALUES (73,2,53,21);
INSERT INTO instrument_visiable VALUES (74,2,52,22);
INSERT INTO instrument_visiable VALUES (75,2,51,23);
INSERT INTO instrument_visiable VALUES (76,3,17,31);
INSERT INTO instrument_visiable VALUES (77,3,7,32);
INSERT INTO instrument_visiable VALUES (78,3,5,33);
INSERT INTO instrument_visiable VALUES (79,3,16,34);
INSERT INTO instrument_visiable VALUES (80,3,27,35);
INSERT INTO instrument_visiable VALUES (81,3,19,36);
INSERT INTO instrument_visiable VALUES (82,4,48,41);
INSERT INTO instrument_visiable VALUES (83,4,36,42);
INSERT INTO instrument_visiable VALUES (84,4,35,43);
INSERT INTO instrument_visiable VALUES (85,4,49,44);
INSERT INTO instrument_visiable VALUES (86,4,50,45);
INSERT INTO instrument_visiable VALUES (87,5,44,51);
INSERT INTO instrument_visiable VALUES (88,5,46,52);
INSERT INTO instrument_visiable VALUES (89,5,45,53);
INSERT INTO instrument_visiable VALUES (90,5,47,54);
INSERT INTO instrument_visiable VALUES (91,6,14,61);
INSERT INTO instrument_visiable VALUES (92,6,15,62);
INSERT INTO instrument_visiable VALUES (93,6,28,63);
INSERT INTO instrument_visiable VALUES (94,6,4,64);
INSERT INTO instrument_visiable VALUES (95,6,9,65);
INSERT INTO instrument_visiable VALUES (96,6,6,66);
INSERT INTO instrument_visiable VALUES (97,6,1,67);
INSERT INTO instrument_visiable VALUES (98,6,22,68);
INSERT INTO instrument_visiable VALUES (99,7,32,71);
INSERT INTO instrument_visiable VALUES (100,7,31,72);
INSERT INTO instrument_visiable VALUES (101,7,13,73);
INSERT INTO instrument_visiable VALUES (102,7,30,74);
INSERT INTO instrument_visiable VALUES (103,8,12,81);
INSERT INTO instrument_visiable VALUES (104,8,10,82);
INSERT INTO instrument_visiable VALUES (105,8,11,83);
INSERT INTO instrument_visiable VALUES (106,8,21,84);
INSERT INTO instrument_visiable VALUES (107,8,33,85);
INSERT INTO instrument_visiable VALUES (108,8,25,86);

INSERT INTO instrument_visiable VALUES (201,1,130,1);
INSERT INTO instrument_visiable VALUES (202,1,104,2);
INSERT INTO instrument_visiable VALUES (203,1,137,3);
INSERT INTO instrument_visiable VALUES (204,1,141,4);
INSERT INTO instrument_visiable VALUES (205,1,102,5);
INSERT INTO instrument_visiable VALUES (206,1,146,6);
INSERT INTO instrument_visiable VALUES (207,1,121,7);
INSERT INTO instrument_visiable VALUES (208,1,136,8);

INSERT INTO instrument_visiable VALUES (209,2,124,21);
INSERT INTO instrument_visiable VALUES (210,2,123,22);
INSERT INTO instrument_visiable VALUES (211,2,114,23);

INSERT INTO instrument_visiable VALUES (212,3,120,31);
INSERT INTO instrument_visiable VALUES (213,3,108,32);
INSERT INTO instrument_visiable VALUES (214,3,105,33);
INSERT INTO instrument_visiable VALUES (215,3,131,34);
INSERT INTO instrument_visiable VALUES (216,3,138,35);
INSERT INTO instrument_visiable VALUES (217,3,122,36);

INSERT INTO instrument_visiable VALUES (218,4,117,41);
INSERT INTO instrument_visiable VALUES (219,4,140,42);
INSERT INTO instrument_visiable VALUES (220,4,116,43);
INSERT INTO instrument_visiable VALUES (221,4,132,44);
INSERT INTO instrument_visiable VALUES (222,4,133,45);

INSERT INTO instrument_visiable VALUES (223,5,107,51);
INSERT INTO instrument_visiable VALUES (224,5,118,51);
INSERT INTO instrument_visiable VALUES (225,5,135,51);
INSERT INTO instrument_visiable VALUES (226,5,127,51);

INSERT INTO instrument_visiable VALUES (227,6,109,61);
INSERT INTO instrument_visiable VALUES (228,6,119,62);
INSERT INTO instrument_visiable VALUES (229,6,103,63);
INSERT INTO instrument_visiable VALUES (230,6,139,64);
INSERT INTO instrument_visiable VALUES (231,6,110,65);
INSERT INTO instrument_visiable VALUES (232,6,106,66);
INSERT INTO instrument_visiable VALUES (233,6,101,67);
INSERT INTO instrument_visiable VALUES (234,6,125,68);

INSERT INTO instrument_visiable VALUES (235,7,145,71);
INSERT INTO instrument_visiable VALUES (236,7,144,72);
INSERT INTO instrument_visiable VALUES (237,7,115,73);
INSERT INTO instrument_visiable VALUES (238,7,143,74);
INSERT INTO instrument_visiable VALUES (239,7,111,75);

INSERT INTO instrument_visiable VALUES (240,8,113,81);
INSERT INTO instrument_visiable VALUES (241,8,129,82);
INSERT INTO instrument_visiable VALUES (242,8,142,83);
INSERT INTO instrument_visiable VALUES (243,8,112,84);
INSERT INTO instrument_visiable VALUES (244,8,149,85);
INSERT INTO instrument_visiable VALUES (245,8,128,86);

DROP TABLE IF EXISTS instrument_exchange;
CREATE TABLE instrument_exchange (
  id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
  symbol VARCHAR(16) NOT NULL,
  exchange VARCHAR(32) NOT NULL,
  UNIQUE INDEX ie_unique(symbol))
ENGINE=InnoDB;

INSERT INTO instrument_exchange VALUES (1,'ZO','ECBOT');
INSERT INTO instrument_exchange VALUES (2,'ZC','ECBOT');
INSERT INTO instrument_exchange VALUES (3,'ZS','ECBOT');
INSERT INTO instrument_exchange VALUES (4,'ZM','ECBOT');
INSERT INTO instrument_exchange VALUES (5,'ZL','ECBOT');
INSERT INTO instrument_exchange VALUES (6,'ZW','ECBOT');
INSERT INTO instrument_exchange VALUES (7,'KE','ECBOT');
INSERT INTO instrument_exchange VALUES (8,'ZR','ECBOT');

INSERT INTO instrument_exchange VALUES (9,'HE','GLOBEX');
INSERT INTO instrument_exchange VALUES (10,'LE','GLOBEX');
INSERT INTO instrument_exchange VALUES (11,'GF','GLOBEX');

INSERT INTO instrument_exchange VALUES (22,'HG','NYMEX');
INSERT INTO instrument_exchange VALUES (23,'SI','NYMEX');
INSERT INTO instrument_exchange VALUES (24,'GC','NYMEX');
INSERT INTO instrument_exchange VALUES (25,'PA','NYMEX');
INSERT INTO instrument_exchange VALUES (26,'PL','NYMEX');

INSERT INTO instrument_exchange VALUES (30,'CL','NYMEX');
INSERT INTO instrument_exchange VALUES (31,'HO','NYMEX');
INSERT INTO instrument_exchange VALUES (32,'RB','NYMEX');
INSERT INTO instrument_exchange VALUES (33,'NG','NYMEX');

INSERT INTO instrument_exchange VALUES (80,'EUR','GLOBEX');
INSERT INTO instrument_exchange VALUES (81,'JPY','GLOBEX');
INSERT INTO instrument_exchange VALUES (82,'CHF','GLOBEX');
INSERT INTO instrument_exchange VALUES (83,'GBP','GLOBEX');
INSERT INTO instrument_exchange VALUES (84,'DX','NYBOT');
INSERT INTO instrument_exchange VALUES (85,'CAD','GLOBEX');
INSERT INTO instrument_exchange VALUES (86,'AUD','GLOBEX');
INSERT INTO instrument_exchange VALUES (87,'MXP','GLOBEX');

INSERT INTO instrument_exchange VALUES (91,'ZB','ECBOT');
INSERT INTO instrument_exchange VALUES (92,'ZN','ECBOT');
INSERT INTO instrument_exchange VALUES (93,'ZF','ECBOT');
INSERT INTO instrument_exchange VALUES (94,'ZT','ECBOT');
INSERT INTO instrument_exchange VALUES (95,'GE','GLOBEX');

INSERT INTO instrument_exchange VALUES (101,'ES','GLOBEX');
INSERT INTO instrument_exchange VALUES (102,'NQ','GLOBEX');
INSERT INTO instrument_exchange VALUES (103,'TF','NYBOT');
INSERT INTO instrument_exchange VALUES (104,'EMD','GLOBEX');
INSERT INTO instrument_exchange VALUES (105,'YM','ECBOT');
INSERT INTO instrument_exchange VALUES (106,'NKD','GLOBEX');

INSERT INTO symbol_mappings VALUES (1, 'cash', 'CL2', 'CL.CASH');
INSERT INTO symbol_mappings VALUES (2, 'cash', 'DX2', 'DX.IND');
INSERT INTO symbol_mappings VALUES (3, 'cash', 'ES', 'SPX');
INSERT INTO symbol_mappings VALUES (4, 'cash', 'GC2', 'GC.CASH');
INSERT INTO symbol_mappings VALUES (5, 'cash', 'TY', 'TY.CASH');
INSERT INTO symbol_mappings VALUES (6, 'cash', 'YM', 'DJI');

DROP USER 'qboss'@'localhost';
CREATE USER 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON holidays TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON instrument TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON instrument_variation TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON clc_index TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON clc_rev TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON clc_non TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON clc_rad TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON csi_bars TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON kv TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON kv_bin TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON categories TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON instrument_visiable TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON instrument_exchange TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
GRANT DELETE,INSERT,SELECT,UPDATE ON symbol_mappings TO 'qboss'@'localhost' IDENTIFIED BY 'iddqd';
