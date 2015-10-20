-- -----------------------------------
-- Athena Database script
-- Important: Execute this script after creating the SpagoBI Tables
-- ------------------------------------


-- tables for different athena product types
CREATE TABLE `SBI_PRODUCT_TYPE` (
	`PRODUCT_TYPE_ID` INT(11) NOT NULL,
	`LABEL` VARCHAR(40) NOT NULL,
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
	PRIMARY KEY(`PRODUCT_TYPE_ID`),
	UNIQUE INDEX `XAK1SBI_PRODUCT_TYPE` (`LABEL`, `ORGANIZATION`)
)
COLLATE='LATIN1_SWEDISH_CI'
ENGINE=INNODB
;

-- mapping table between organizations (tenants) and product types
CREATE TABLE `SBI_ORGANIZATION_PRODUCT_TYPE` (
	`PRODUCT_TYPE_ID` INT(11) NOT NULL,
	`ORGANIZATION_ID` INT(11) NOT NULL,
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
	PRIMARY KEY(`PRODUCT_TYPE_ID`, `ORGANIZATION_ID`),
	INDEX `FK_ORGANIZATION_3` (`ORGANIZATION_ID`),
	CONSTRAINT `FK_PRODUCT_TYPE_1` FOREIGN KEY (`PRODUCT_TYPE_ID`) REFERENCES `SBI_PRODUCT_TYPE` (`PRODUCT_TYPE_ID`) ON UPDATE NO ACTION ON DELETE NO ACTION,
	CONSTRAINT `FK_ORGANIZATION_3` FOREIGN KEY (`ORGANIZATION_ID`) REFERENCES `SBI_ORGANIZATIONS` (`ID`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='LATIN1_SWEDISH_CI'
ENGINE=INNODB
; 

-- modify sbi_user_func with product_id fk 
ALTER TABLE `SBI_USER_FUNC`
	ADD COLUMN `PRODUCT_TYPE_ID` INT(11) NOT NULL AFTER `DESCRIPTION`,
	ADD CONSTRAINT `FK_PRODUCT_TYPE` FOREIGN KEY (`PRODUCT_TYPE_ID`) REFERENCES `SBI_PRODUCT_TYPE` (`PRODUCT_TYPE_ID`);
	
-- modify sbi_authorization with product_id fk
ALTER TABLE `SBI_AUTHORIZATIONS`
	ADD COLUMN `PRODUCT_TYPE_ID` INT(11) NOT NULL AFTER `NAME`,
	ADD CONSTRAINT `FK2_PRODUCT_TYPE` FOREIGN KEY (`PRODUCT_TYPE_ID`) REFERENCES `SBI_PRODUCT_TYPE` (`PRODUCT_TYPE_ID`);


-- create: GLOSSARY tables
CREATE TABLE `SBI_GL_WORD` (
	`WORD_ID` Integer NOT NULL ,
	`WORD` VARCHAR (100),
	`DESCR` VARCHAR (500),
	`FORMULA` VARCHAR (500),
	`STATE` INT(11) DEFAULT NULL,
	`CATEGORY` INT(11) DEFAULT NULL,
	
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
	PRIMARY KEY(`WORD_ID`),
	KEY `STATE_IDX` (`STATE`),
 	KEY `CATEGORY_IDX` (`CATEGORY`),
  	CONSTRAINT `CATEGORY` FOREIGN KEY (`CATEGORY`) REFERENCES `SBI_DOMAINS` (`VALUE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  	CONSTRAINT `STATE` FOREIGN KEY (`STATE`) REFERENCES `SBI_DOMAINS` (`VALUE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ;

CREATE TABLE `SBI_GL_ATTRIBUTES` (
	`ATTRIBUTE_ID` Integer NOT NULL ,
	`ATTRIBUTE_CD` VARCHAR (30),
	`ATTRIBUTE_NM` VARCHAR (100),
	`ATTRIBUTE_DS` VARCHAR (500),
	`MANDATORY_FL` Integer,
	`ATTRIBUTES_TYPE` VARCHAR (50),
	`DOMAIN` VARCHAR (500),
	`FORMAT` VARCHAR (30),
	`DISPLAY_TP` VARCHAR (30),
	`ATTRIBUTES_ORDER` VARCHAR (30),
	
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
PRIMARY KEY(`ATTRIBUTE_ID`) 
);

CREATE TABLE `SBI_GL_WORD_ATTR` (
	`WORD_ID` Integer NOT NULL ,
	`ATTRIBUTE_ID` Integer NOT NULL ,
	`ATTR_VALUE` VARCHAR (500),
	`ATTR_ORDER` Integer,
	
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
PRIMARY KEY(`WORD_ID`,`ATTRIBUTE_ID`) 
);

CREATE TABLE `SBI_GL_REFERENCES` (
	`WORD_ID` Integer NOT NULL ,
	`REF_WORD_ID` Integer NOT NULL ,
	`REFERENCES_ORDER` Integer,
	
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
PRIMARY KEY(`WORD_ID`,`REF_WORD_ID`) 
);

CREATE TABLE `SBI_GL_GLOSSARY` (
	`GLOSSARY_ID` Integer NOT NULL ,
	`GLOSSARY_CD` VARCHAR (30),
	`GLOSSARY_NM` VARCHAR (100),
	`GLOSSARY_DS` VARCHAR (500),
	
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
PRIMARY KEY(`GLOSSARY_ID`) 
);

CREATE TABLE `SBI_GL_CONTENTS` (
	`CONTENT_ID` Integer NOT NULL ,
	`GLOSSARY_ID` Integer NOT NULL ,
	`PARENT_ID` Integer,
	`CONTENT_CD` VARCHAR (30),
	`CONTENT_NM` VARCHAR (100),
	`CONTENT_DS` VARCHAR (500),
	`DEPTH` Integer,
	
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
PRIMARY KEY(`CONTENT_ID`) 
) 
;

CREATE TABLE `SBI_GL_WLIST` (
	`CONTENT_ID` Integer NOT NULL ,
	`WORD_ID` Integer NOT NULL ,
	`WORD_ORDER` Integer,
	
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
PRIMARY KEY(`CONTENT_ID`,`WORD_ID`) 
);

CREATE TABLE `SBI_PRODUCT_TYPE_ENGINE` (
	`PRODUCT_TYPE_ID` INT(11) NOT NULL,
	`ENGINE_ID` INT(11) NOT NULL,
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) DEFAULT NULL,
	`USER_DE` VARCHAR(100) DEFAULT NULL,
	`TIME_IN` TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) DEFAULT NULL,
	`META_VERSION` VARCHAR(100) DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) DEFAULT NULL,
	PRIMARY KEY (`PRODUCT_TYPE_ID`, `ENGINE_ID`)
);

CREATE TABLE `SBI_IMAGES` (
  `IMAGE_ID` int(11) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `CONTENT` mediumblob NOT NULL,
  `CONTENT_ICO` blob,
  `USER_IN` varchar(100) DEFAULT NULL,
  `USER_UP` varchar(100) DEFAULT NULL,
  `USER_DE` varchar(100) DEFAULT NULL,
  `TIME_IN` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIME_UP` timestamp NULL DEFAULT NULL,
  `TIME_DE` timestamp NULL DEFAULT NULL,
  `SBI_VERSION_IN` varchar(10) DEFAULT NULL,
  `SBI_VERSION_UP` varchar(10) DEFAULT NULL,
  `SBI_VERSION_DE` varchar(10) DEFAULT NULL,
  `META_VERSION` varchar(100) DEFAULT NULL,
  `ORGANIZATION` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`IMAGE_ID`),
  UNIQUE KEY `NAME_UNIQUE` (`NAME`)
);


CREATE TABLE `SBI_GL_DOCWLIST` (
	`WORD_ID` INT(11) NOT NULL,
	`BIOBJ_ID` INT(11) NOT NULL,
	`USER_IN` VARCHAR(100) NOT NULL,
	`USER_UP` VARCHAR(100) NULL DEFAULT NULL,
	`USER_DE` VARCHAR(100) NULL DEFAULT NULL,
	`TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`TIME_UP` TIMESTAMP NULL DEFAULT NULL,
	`TIME_DE` TIMESTAMP NULL DEFAULT NULL,
	`SBI_VERSION_IN` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_UP` VARCHAR(10) NULL DEFAULT NULL,
	`SBI_VERSION_DE` VARCHAR(10) NULL DEFAULT NULL,
	`META_VERSION` VARCHAR(100) NULL DEFAULT NULL,
	`ORGANIZATION` VARCHAR(20) NULL DEFAULT NULL,
  PRIMARY KEY (`BIOBJ_ID`, `WORD_ID`),
  INDEX `WORD_ID_idx` (`WORD_ID` ASC),
  CONSTRAINT `WORD_ID`
   FOREIGN KEY (`WORD_ID`)
   REFERENCES `SBI_GL_WORD` (`WORD_ID`)
   ON DELETE NO ACTION
   ON UPDATE NO ACTION,
  CONSTRAINT `DOCUMENT_ID`
   FOREIGN KEY (`BIOBJ_ID`)
   REFERENCES `SBI_OBJECTS` (`BIOBJ_ID`)
   ON DELETE NO ACTION
   ON UPDATE NO ACTION
);

CREATE TABLE `SBI_GL_DATASETWLIST` (
   `WORD_ID` INT(11) NOT NULL,
   `DS_ID` INT(11) NOT NULL,
   `COLUMN_NAME` VARCHAR(100) NOT NULL,
   `USER_IN` VARCHAR(100) NOT NULL,
   `USER_UP` VARCHAR(100) DEFAULT NULL,
   `USER_DE` VARCHAR(100) DEFAULT NULL,
   `TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   `TIME_UP` TIMESTAMP NULL DEFAULT NULL,
   `TIME_DE` TIMESTAMP NULL DEFAULT NULL,
   `SBI_VERSION_IN` VARCHAR(10) DEFAULT NULL,
   `SBI_VERSION_UP` VARCHAR(10) DEFAULT NULL,
   `SBI_VERSION_DE` VARCHAR(10) DEFAULT NULL,
   `META_VERSION` VARCHAR(100) DEFAULT NULL,
   `ORGANIZATION` VARCHAR(20) DEFAULT NULL,
   `VERSION_NUM` INT(11) DEFAULT NULL,
   PRIMARY KEY (`WORD_ID`,`DS_ID`,`COLUMN_NAME`),
   KEY `DATASET_IDX` (`DS_ID`),
   KEY `ORGANIZATION_IDX` (`ORGANIZATION`),
   CONSTRAINT `DATASET` FOREIGN KEY (`DS_ID`) REFERENCES `SBI_DATA_SET` (`DS_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
   CONSTRAINT `WORD` FOREIGN KEY (`WORD_ID`) REFERENCES `SBI_GL_WORD` (`WORD_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
 ) ;

CREATE TABLE `SBI_GL_TABLE` (
  `TABLE_ID` int(11) NOT NULL,
  `LABEL` varchar(100) NOT NULL, `USER_IN` varchar(100) DEFAULT NULL,
  `USER_UP` varchar(100) DEFAULT NULL,
  `USER_DE` varchar(100) DEFAULT NULL,
  `TIME_IN` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIME_UP` timestamp NULL DEFAULT NULL,
  `TIME_DE` timestamp NULL DEFAULT NULL,
  `SBI_VERSION_IN` varchar(10) DEFAULT NULL,
  `SBI_VERSION_UP` varchar(10) DEFAULT NULL,
  `SBI_VERSION_DE` varchar(10) DEFAULT NULL,
  `META_VERSION` varchar(100) DEFAULT NULL,
  `ORGANIZATION` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`TABLE_ID`)
);

CREATE TABLE `SBI_GL_BNESS_CLS` (
  `BC_ID` int(11) NOT NULL AUTO_INCREMENT,
  `DATAMART_NAME` varchar(100) NOT NULL,
  `UNIQUE_IDENTIFIER` varchar(100) NOT NULL,
  `USER_IN` varchar(100) DEFAULT NULL,
  `USER_UP` varchar(100) DEFAULT NULL,
  `USER_DE` varchar(100) DEFAULT NULL,
  `TIME_IN` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIME_UP` timestamp NULL DEFAULT NULL,
  `TIME_DE` timestamp NULL DEFAULT NULL,
  `SBI_VERSION_IN` varchar(10) DEFAULT NULL,
  `SBI_VERSION_UP` varchar(10) DEFAULT NULL,
  `SBI_VERSION_DE` varchar(10) DEFAULT NULL,
  `META_VERSION` varchar(100) DEFAULT NULL,
  `ORGANIZATION` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`BC_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




CREATE TABLE `SBI_GL_BNESS_CLS_WLIST` (
  `WORD_ID` int(11) NOT NULL,
  `BC_ID` int(11) NOT NULL,
  `COLUMN_NAME` varchar(100) DEFAULT NULL,
  `USER_IN` varchar(100) NOT NULL,
  `USER_UP` varchar(100) DEFAULT NULL,
  `USER_DE` varchar(100) DEFAULT NULL,
  `TIME_IN` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIME_UP` timestamp NULL DEFAULT NULL,
  `TIME_DE` timestamp NULL DEFAULT NULL,
  `SBI_VERSION_IN` varchar(10) DEFAULT NULL,
  `SBI_VERSION_UP` varchar(10) DEFAULT NULL,
  `SBI_VERSION_DE` varchar(10) DEFAULT NULL,
  `META_VERSION` varchar(100) DEFAULT NULL,
  `ORGANIZATION` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`BC_ID`,`WORD_ID`),
  KEY `WORDID` (`WORD_ID`),
  CONSTRAINT `BCID` FOREIGN KEY (`BC_ID`) REFERENCES `SBI_GL_BNESS_CLS` (`BC_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `WORDID` FOREIGN KEY (`WORD_ID`) REFERENCES `SBI_GL_WORD` (`WORD_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `SBI_GL_TABLE_WLIST` (
  `WORD_ID` int(11) NOT NULL,
  `TABLE_ID` int(11) NOT NULL,
  `USER_IN` varchar(100) NOT NULL,
  `USER_UP` varchar(100) DEFAULT NULL,
  `USER_DE` varchar(100) DEFAULT NULL,
  `TIME_IN` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIME_UP` timestamp NULL DEFAULT NULL,
  `TIME_DE` timestamp NULL DEFAULT NULL,
  `SBI_VERSION_IN` varchar(10) DEFAULT NULL,
  `SBI_VERSION_UP` varchar(10) DEFAULT NULL,
  `SBI_VERSION_DE` varchar(10) DEFAULT NULL,
  `META_VERSION` varchar(100) DEFAULT NULL,
  `ORGANIZATION` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`TABLE_ID`,`WORD_ID`),
   CONSTRAINT `TABLEID` FOREIGN KEY (`TABLE_ID`) REFERENCES `SBI_GL_TABLE` (`TABLE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `WORDIDT` FOREIGN KEY (`WORD_ID`) REFERENCES `SBI_GL_WORD` (`WORD_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `SBI_WS_EVENT` (
  `ID` INT(11) NOT NULL,
  `EVENT_NAME` VARCHAR(80) NOT NULL,
  `IP_COME_FROM` VARCHAR(15) NULL,
  `INCOMING_DATE` DATETIME NULL,
  `TAKE_CHARGE_DATE` DATETIME NULL,
  `USER_IN` VARCHAR(100) NOT NULL,
  `USER_UP` VARCHAR(100) DEFAULT NULL,
  `USER_DE` VARCHAR(100) DEFAULT NULL,
  `TIME_IN` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIME_UP` TIMESTAMP NULL DEFAULT NULL,
  `TIME_DE` TIMESTAMP NULL DEFAULT NULL,
  `SBI_VERSION_IN` VARCHAR(10) DEFAULT NULL,
  `SBI_VERSION_UP` VARCHAR(10) DEFAULT NULL,
  `SBI_VERSION_DE` VARCHAR(10) DEFAULT NULL,
  `META_VERSION` VARCHAR(100) DEFAULT NULL,
  `ORGANIZATION` VARCHAR(20) DEFAULT NULL,
  PRIMARY KEY (`id`));
  
CREATE TABLE `SBI_TIMESPAN` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(45) NOT NULL,
  `TYPE` varchar(45) NOT NULL,
  `CATEGORY` varchar(45) DEFAULT NULL,
  `STATIC_FILTER` tinyint(1) DEFAULT 0,
  `DEFINITION` varchar(4000) DEFAULT NULL,
  `USER_IN` varchar(100) NOT NULL,
  `USER_UP` varchar(100) DEFAULT NULL,
  `USER_DE` varchar(100) DEFAULT NULL,
  `TIME_IN` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `TIME_UP` timestamp NULL DEFAULT NULL,
  `TIME_DE` timestamp NULL DEFAULT NULL,
  `SBI_VERSION_IN` varchar(10) DEFAULT NULL,
  `SBI_VERSION_UP` varchar(10) DEFAULT NULL,
  `SBI_VERSION_DE` varchar(10) DEFAULT NULL,
  `META_VERSION` varchar(100) DEFAULT NULL,
  `ORGANIZATION` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
);

-- Create Foreign keys section

ALTER TABLE `SBI_GL_WORD_ATTR` ADD FOREIGN KEY (`WORD_ID`) REFERENCES `SBI_GL_WORD` (`WORD_ID`);

ALTER TABLE `SBI_GL_REFERENCES` ADD FOREIGN KEY (`WORD_ID`) REFERENCES `SBI_GL_WORD` (`WORD_ID`);

ALTER TABLE `SBI_GL_REFERENCES` ADD FOREIGN KEY (`REF_WORD_ID`) REFERENCES `SBI_GL_WORD` (`WORD_ID`);

ALTER TABLE `SBI_GL_WLIST` ADD FOREIGN KEY (`WORD_ID`) REFERENCES `SBI_GL_WORD` (`WORD_ID`);

ALTER TABLE `SBI_GL_WORD_ATTR` ADD FOREIGN KEY (`ATTRIBUTE_ID`) REFERENCES `SBI_GL_ATTRIBUTES` (`ATTRIBUTE_ID`);

ALTER TABLE `SBI_GL_CONTENTS` ADD FOREIGN KEY (`GLOSSARY_ID`) REFERENCES `SBI_GL_GLOSSARY` (`GLOSSARY_ID`);

ALTER TABLE `SBI_GL_CONTENTS` ADD FOREIGN KEY (`PARENT_ID`) REFERENCES `SBI_GL_CONTENTS` (`CONTENT_ID`);

ALTER TABLE `SBI_GL_WLIST` ADD FOREIGN KEY (`CONTENT_ID`) REFERENCES `SBI_GL_CONTENTS` (`CONTENT_ID`);
	
ALTER TABLE `SBI_PRODUCT_TYPE_ENGINE` ADD CONSTRAINT `FK_PRODUCT_TYPE_2` FOREIGN KEY (`PRODUCT_TYPE_ID`) REFERENCES `SBI_PRODUCT_TYPE` (`PRODUCT_TYPE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE `SBI_PRODUCT_TYPE_ENGINE` ADD CONSTRAINT `FK_ENGINE_2` FOREIGN KEY (`ENGINE_ID`) REFERENCES `SBI_ENGINES` (`ENGINE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
