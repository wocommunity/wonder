-- This is the MySQL migration file to create initial database schema
-- Warning, this will DROP the database first if it already exists
-- possibly causing loss in data. But since this is a demo,
-- I don't care ... but _you_ might care.

-- You can run this script form the command line easily as follows:
-- $ mysql -u root -p < threadsdemo-01.sql

-- set strict sql mode to avoid silent column defaults when creating tables
SET SESSION sql_mode='STRICT_ALL_TABLES';

-- Drop the database if it exists
DROP DATABASE IF EXISTS threadsdemo;

-- Create user/password for the demo eomodel
grant all on threadsdemo.* to guest@localhost identified by 'password';

-- Create the database
CREATE DATABASE `threadsdemo` /*!40100 DEFAULT CHARACTER SET utf8 */;

-- Use the database
USE threadsdemo;

-- resultitem table
CREATE TABLE resultitem (
	id INT NOT NULL,
	closestfactorial BIGINT DEFAULT NULL,
	factornumber INT DEFAULT NULL,
	isfactorialprime INT NOT NULL DEFAULT '0',
	isprime INT NOT NULL DEFAULT '0',
	modificationtime DATETIME NOT NULL,
	numbertocheck BIGINT NOT NULL,
	taskinfoid INT NOT NULL,
	workflowstate ENUM('Prime Checked','Checking Factorial','Factorial Checked') NOT NULL,
	PRIMARY KEY (id),
	KEY (taskinfoid, numbertocheck)
);

-- taskinfo table
CREATE TABLE taskinfo (
	id INT NOT NULL,
	duration BIGINT NOT NULL,
	endnumber BIGINT,
	endtime DATETIME,
	startnumber BIGINT NOT NULL,
	starttime DATETIME,
	workflowstate ENUM('Processing Primes','Primes Processed','Processing Factorials','Factorials Processed') NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE EO_PK_TABLE (
	NAME CHAR(40) PRIMARY KEY, 
	PK INT
);

