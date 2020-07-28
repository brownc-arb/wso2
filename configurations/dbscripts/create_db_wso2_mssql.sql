USE master;
CREATE DATABASE wso2db_amconfig;
CREATE DATABASE wso2db_am;
CREATE DATABASE wso2db_um;
CREATE DATABASE wso2db_gov;
CREATE DATABASE wso2db_ammb;
CREATE DATABASE wso2db_amanalyticsconfig;
CREATE DATABASE wso2db_amevent;
CREATE DATABASE wso2db_amprocessed;
CREATE DATABASE wso2db_amstat;
CREATE DATABASE wso2db_bps;
CREATE DATABASE wso2db_bpsconfig;
CREATE DATABASE wso2db_iamconfig;
CREATE DATABASE wso2db_obconsent;
CREATE DATABASE wso2db_obpsu;
--CREATE DATABASE wso2db_geolocation;

-- Custom user-store database
CREATE DATABASE wso2db_psu;

CREATE LOGIN wso2user_amconfig WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_am WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_um WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_gov WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_ammb WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_amanalyticsconfig WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_amevent WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_amprocessed WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_amstat WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_bps WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_bpsconfig WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_iamconfig WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_obconsent WITH PASSWORD = 'ZAQ!@WSX123';
CREATE LOGIN wso2user_obpsu WITH PASSWORD = 'ZAQ!@WSX123';
-- CREATE LOGIN wso2user_geolocation WITH PASSWORD = 'ZAQ!@WSX123';

-- Custom user-store database login
CREATE LOGIN wso2user_psu WITH PASSWORD = 'ZAQ!@WSX123';

USE wso2db_amconfig;
CREATE USER wso2user_amconfig FOR LOGIN wso2user_amconfig WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_amconfig';

USE wso2db_am;
CREATE USER wso2user_am FOR LOGIN wso2user_am WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_am';

USE wso2db_um;
CREATE USER wso2user_um FOR LOGIN wso2user_um WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_um';

USE wso2db_gov;
CREATE USER wso2user_gov FOR LOGIN wso2user_gov WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_gov';

USE wso2db_ammb;
CREATE USER wso2user_ammb FOR LOGIN wso2user_ammb WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_ammb';

USE wso2db_amanalyticsconfig;
CREATE USER wso2user_amanalyticsconfig FOR LOGIN wso2user_amanalyticsconfig WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_amanalyticsconfig';

USE wso2db_amevent;
CREATE USER wso2user_amevent FOR LOGIN wso2user_amevent WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_amevent';

USE wso2db_amprocessed;
CREATE USER wso2user_amprocessed FOR LOGIN wso2user_amprocessed WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_amprocessed';

USE wso2db_amstat;
CREATE USER wso2user_amstat FOR LOGIN wso2user_amstat WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_amstat';

USE wso2db_bps;
CREATE USER wso2user_bps FOR LOGIN wso2user_bps WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_bps';

USE wso2db_bpsconfig;
CREATE USER wso2user_bpsconfig FOR LOGIN wso2user_bpsconfig WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_bpsconfig';

USE wso2db_iamconfig;
CREATE USER wso2user_iamconfig FOR LOGIN wso2user_iamconfig WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_iamconfig';

USE wso2db_obconsent;
CREATE USER wso2user_obconsent FOR LOGIN wso2user_obconsent WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_obconsent';

USE wso2db_obpsu;
CREATE USER wso2user_obpsu FOR LOGIN wso2user_obpsu WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_obpsu';

--USE wso2db_geolocation;
--CREATE USER wso2user_geolocation FOR LOGIN wso2user_geolocation WITH DEFAULT_SCHEMA = dbo;
--EXEC sp_addrolemember 'db_owner', 'wso2user_geolocation';

-- Custom user-store database user
USE wso2db_psu;
CREATE USER wso2user_psu FOR LOGIN wso2user_psu WITH DEFAULT_SCHEMA = dbo;
EXEC sp_addrolemember 'db_owner', 'wso2user_psu';
