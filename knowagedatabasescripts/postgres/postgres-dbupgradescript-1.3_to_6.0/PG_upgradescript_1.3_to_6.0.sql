﻿ALTER TABLE SBI_META_TABLE_BC
	DROP CONSTRAINT FK_SBI_META_TABLE_BC_2;
ALTER TABLE SBI_META_TABLE_BC
	ADD CONSTRAINT FK_SBI_META_TABLE_BC_2 FOREIGN KEY (BC_ID) REFERENCES SBI_META_BC (BC_ID) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE SBI_META_BC
	DROP CONSTRAINT FK_SBI_META_BC_1;
ALTER TABLE SBI_META_BC
	ADD CONSTRAINT FK_SBI_META_BC_1 FOREIGN KEY (MODEL_ID) REFERENCES SBI_META_MODELS (ID) ON UPDATE NO ACTION ON DELETE CASCADE;
	
ALTER TABLE SBI_META_BC_ATTRIBUTE
	DROP CONSTRAINT FK_SBI_META_BC_ATTRIBUTE_1;
ALTER TABLE SBI_META_BC_ATTRIBUTE
	ADD CONSTRAINT FK_SBI_META_BC_ATTRIBUTE_1 FOREIGN KEY (BC_ID) REFERENCES SBI_META_BC (BC_ID) ON UPDATE NO ACTION ON DELETE CASCADE;	
	
ALTER TABLE SBI_META_BC_ATTRIBUTE
	DROP CONSTRAINT FK_SBI_META_BC_ATTRIBUTE_2;
ALTER TABLE SBI_META_BC_ATTRIBUTE
	ADD CONSTRAINT FK_SBI_META_BC_ATTRIBUTE_2 FOREIGN KEY (COLUMN_ID) REFERENCES SBI_META_TABLE_COLUMN (COLUMN_ID) ON UPDATE NO ACTION ON DELETE CASCADE;	
	
ALTER TABLE SBI_META_DS_BC
	DROP CONSTRAINT FK_SBI_META_DS_BC_2;
ALTER TABLE SBI_META_DS_BC
	ADD CONSTRAINT FK_SBI_META_DS_BC_2 FOREIGN KEY (BC_ID) REFERENCES SBI_META_BC (BC_ID) ON UPDATE NO ACTION ON DELETE CASCADE;
	
update SBI_ENGINES set MAIN_URL='/knowagewhatifengine/restful-services/olap/startolap' where LABEL = 'knowageolapengine';
update SBI_ENGINES set MAIN_URL='/knowagewhatifengine/restful-services/olap/startwhatif' where LABEL = 'knowagewhatifengine';

ALTER TABLE SBI_CATALOG_FUNCTION ADD COLUMN REMOTE BOOLEAN DEFAULT FALSE;
ALTER TABLE SBI_CATALOG_FUNCTION ADD COLUMN URL VARCHAR(100);
ALTER TABLE KNOWAGE.SBI_CATALOG_FUNCTION CHANGE COLUMN `SCRIPT` `SCRIPT` TEXT NULL;

ALTER TABLE SBI_OUTPUT_PARAMETER ADD COLUMN IS_USER_DEFINED BOOLEAN NULL DEFAULT FALSE;

ALTER TABLE SBI_SNAPSHOTS ADD COLUMN SCHEDULATION VARCHAR(100);
ALTER TABLE SBI_SNAPSHOTS ADD COLUMN SCHEDULER VARCHAR(100);
ALTER TABLE SBI_SNAPSHOTS ADD COLUMN SCHEDULATION_START INTEGER;
ALTER TABLE SBI_SNAPSHOTS ADD COLUMN SEQUENCE INTEGER;

CREATE TABLE SBI_ACCESSIBILITY_PREFERENCES (
	 ID INTEGER NOT NULL,
	 USER_ID INTEGER NOT NULL,
	 ENABLE_UIO BOOLEAN NOT NULL,
	 ENABLE_RROBOBRAILLE BOOLEAN NULL DEFAULT NULL,
	 ENABLE_GRAPH_SONIFICATION BOOLEAN NULL DEFAULT NULL,
	 ENABLE_VOICE BOOLEAN NULL DEFAULT NULL,
	 PREFERENCES TEXT NULL,
	 USER_IN VARCHAR(100) NULL DEFAULT NULL,
	 USER_UP VARCHAR(100) NULL DEFAULT NULL,
	 USER_DE VARCHAR(100) NULL DEFAULT NULL,
	 TIME_IN TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	 TIME_UP TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	 TIME_DE TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
	 SBI_VERSION_IN VARCHAR(10) NULL DEFAULT NULL,
	 SBI_VERSION_UP VARCHAR(10) NULL DEFAULT NULL,
	 SBI_VERSION_DE VARCHAR(10) NULL DEFAULT NULL,
	 ORGANIZATION VARCHAR(20) NULL DEFAULT NULL,
	 PRIMARY KEY (ID)
) WITHOUT OIDS;

ALTER TABLE SBI_ACCESSIBILITY_PREFERENCES ADD CONSTRAINT FK_SBI_ACCESSIBILITY_PREFERENCES_SBI_USER FOREIGN KEY (USER_ID) REFERENCES SBI_USER (ID) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE SBI_IMAGES
DROP INDEX NAME_UNIQUE,
ADD UNIQUE INDEX NAME_UNIQUE (NAME, ORGANIZATION);

-- 02.03.2017 Dragan Pirkovic 
-- changed path for chart document execution
update SBI_ENGINES set MAIN_URL='/knowagecockpitengine/api/1.0/chart/pages/execute' where LABEL = 'knowagechartengine';
ALTER TABLE SBI_CROSS_NAVIGATION ADD COLUMN TYPE INTEGER NOT NULL;