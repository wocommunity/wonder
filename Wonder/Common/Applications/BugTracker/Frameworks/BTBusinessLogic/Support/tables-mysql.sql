-- MySQL dump 10.11
--
-- Host: localhost    Database: bug
-- ------------------------------------------------------
-- Server version       5.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `BUG`
--

DROP TABLE IF EXISTS `BUG`;
CREATE TABLE `BUG` (
  `ID` int(11) NOT NULL default '0',
  `COMP_ID` int(11) NOT NULL default '0',
  `DATE_MODIFIED` datetime default NULL,
  `DATE_SUBMITTED` datetime default NULL,
  `FEATURE_REQUEST` int(11) NOT NULL default '0',
  `ORIGINATOR_ID` int(11) NOT NULL default '0',
  `PEOPLE_ID` int(11) NOT NULL default '0',
  `PR_ID` varchar(4) NOT NULL default '',
  `PREVIOUS_OWNER_ID` int(11) default NULL,
  `READ_` char(1) default NULL,
  `RELEASE_ID` int(11) NOT NULL default '0',
  `STATE_ID` varchar(4) NOT NULL default '',
  `SUBJECT` varchar(50) NOT NULL default '',
  `DESCRIPTION` blob,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `BUG_TEST_ITEM`
--

DROP TABLE IF EXISTS `BUG_TEST_ITEM`;
CREATE TABLE `BUG_TEST_ITEM` (
  `BUG_ID` int(11) NOT NULL default '0',
  `ID` int(11) NOT NULL default '0',
  PRIMARY KEY  (`BUG_ID`,`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `COMPONENT`
--

DROP TABLE IF EXISTS `COMPONENT`;
CREATE TABLE `COMPONENT` (
  `ID` int(11) NOT NULL default '0',
  `PARENT_ID` int(11) default NULL,
  `PEOPLE_ID` int(11) NOT NULL default '0',
  `DESCRIPTION` blob NOT NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `Comment`
--

DROP TABLE IF EXISTS `Comment`;
CREATE TABLE `Comment` (
  `BUG_ID` int(11) NOT NULL,
  `DATE_SUBMITTED` datetime default NULL,
  `id` int(11) NOT NULL,
  `ORIGINATOR_ID` int(11) NOT NULL,
  `DESCRIPTION` blob,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `DIFFICULTY`
--

DROP TABLE IF EXISTS `DIFFICULTY`;
CREATE TABLE `DIFFICULTY` (
  `DESCRIPTION` varchar(50) NOT NULL default '',
  `ID` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `EO_PK_TABLE`
--

DROP TABLE IF EXISTS `EO_PK_TABLE`;
CREATE TABLE `EO_PK_TABLE` (
  `NAME` char(40) NOT NULL default '',
  `PK` int(11) default NULL,
  PRIMARY KEY  (`NAME`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCHELP_TEXT`
--

DROP TABLE IF EXISTS `ERCHELP_TEXT`;
CREATE TABLE `ERCHELP_TEXT` (
  `ID` int(11) NOT NULL,
  `KEY_` varchar(100) NOT NULL,
  `VALUE_` blob
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCLOG_ENTRY`
--

DROP TABLE IF EXISTS `ERCLOG_ENTRY`;
CREATE TABLE `ERCLOG_ENTRY` (
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `ID` int(11) NOT NULL default '0',
  `TEXT_` longblob NOT NULL,
  `USER_ID` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCMAIL_MESSAG`
--

DROP TABLE IF EXISTS `ERCMAIL_MESSAG`;
CREATE TABLE `ERCMAIL_MESSAG` (
  `BCC_ADDR` blob,
  `CC_ADDR` blob,
  `CONTENT_GZIPPED` int(11) NOT NULL default '0',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `DATE_SENT` datetime default NULL,
  `EXCEPTION_REASON` blob,
  `FROM_ADDR` varchar(255) NOT NULL default '',
  `ID` int(11) NOT NULL default '0',
  `LAST_MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MAIL_STATE_ID` varchar(4) NOT NULL default '',
  `PLAIN_TEXT_` longblob,
  `plain_text_compressed` blob,
  `REPLY_TO_ADDR` blob,
  `SHOULD_ARCHIVE_SENT_MAIL` int(11) NOT NULL default '0',
  `TEXT_` longblob,
  `TEXT_COMPRESSED` blob,
  `TITLE` varchar(255) NOT NULL default '',
  `TO_ADDR` blob NOT NULL,
  `X_MAILER` varchar(255) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCMAIL_MESSAG_ARCHIVE`
--

DROP TABLE IF EXISTS `ERCMAIL_MESSAG_ARCHIVE`;
CREATE TABLE `ERCMAIL_MESSAG_ARCHIVE` (
  `BCC_ADDR` blob,
  `CC_ADDR` blob,
  `CONTENT_GZIPPED` int(11) NOT NULL default '0',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `DATE_SENT` datetime default NULL,
  `EXCEPTION_REASON` blob NOT NULL,
  `FROM_ADDR` varchar(255) NOT NULL default '',
  `ID` int(11) NOT NULL default '0',
  `IS_READ` tinyint(4) NOT NULL default '0',
  `LAST_MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MAIL_STATE_ID` varchar(4) NOT NULL default '',
  `PLAIN_TEXT_` longblob,
  `REPLY_TO_ADDR` blob,
  `SHOULD_ARCHIVE_SENT_MAIL` int(11) NOT NULL default '0',
  `TEXT_` longblob NOT NULL,
  `TITLE` varchar(255) NOT NULL default '',
  `TO_ADDR` blob NOT NULL,
  `X_MAILER` varchar(255) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCMAIL_STATE`
--

DROP TABLE IF EXISTS `ERCMAIL_STATE`;
CREATE TABLE `ERCMAIL_STATE` (
  `ID` varchar(4) NOT NULL default '',
  `NAME` varchar(100) NOT NULL default '',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCMESSAG_ATTACH`
--

DROP TABLE IF EXISTS `ERCMESSAG_ATTACH`;
CREATE TABLE `ERCMESSAG_ATTACH` (
  `FILE_PATH` blob NOT NULL,
  `ID` int(11) NOT NULL default '0',
  `MAIL_MESSAG_ID` int(11) NOT NULL default '0',
  `MIME_TYPE` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCPREFER`
--

DROP TABLE IF EXISTS `ERCPREFER`;
CREATE TABLE `ERCPREFER` (
  `ID` int(11) NOT NULL default '0',
  `KEY_` varchar(100) NOT NULL default '',
  `USER_ID` int(11) default NULL,
  `VALUE_` longblob,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `ERCSTATIC`
--

DROP TABLE IF EXISTS `ERCSTATIC`;
CREATE TABLE `ERCSTATIC` (
  `ID` int(11) NOT NULL default '0',
  `KEY_` varchar(100) NOT NULL default '',
  `VALUE_` blob,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `FRAMEW`
--

DROP TABLE IF EXISTS `FRAMEW`;
CREATE TABLE `FRAMEW` (
  `ID` int(11) NOT NULL default '0',
  `NAME` varchar(50) NOT NULL default '',
  `ORDERING` int(11) NOT NULL default '0',
  `OWNED_SINCE` datetime default NULL,
  `USER_ID` int(11) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `PEOPLE`
--

DROP TABLE IF EXISTS `PEOPLE`;
CREATE TABLE `PEOPLE` (
  `EMAIL` varchar(50) default NULL,
  `ID` int(11) NOT NULL default '0',
  `IS_ACTIVE` tinyint(4) NOT NULL default '0',
  `IS_ADMIN` tinyint(4) NOT NULL default '0',
  `IS_CUSTOMER_SERVICE` tinyint(4) NOT NULL default '0',
  `IS_ENGINEERING` tinyint(4) NOT NULL default '0',
  `LOGIN` varchar(16) NOT NULL default '',
  `NAME` varchar(50) default NULL,
  `PASSWORD` varchar(16) NOT NULL default '',
  `TEAM` varchar(16) default NULL,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `PRIORITY`
--

DROP TABLE IF EXISTS `PRIORITY`;
CREATE TABLE `PRIORITY` (
  `ID` varchar(4) NOT NULL default '',
  `SORT_ORDER` int(11) NOT NULL default '0',
  `DESCRIPTION` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `RELEASE`
--

DROP TABLE IF EXISTS `RELEASE`;
CREATE TABLE `RELEASE` (
  `ID` int(11) NOT NULL default '0',
  `IS_OPEN` tinyint(4) NOT NULL default '0',
  `NAME` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `REQUIREMENT`
--

DROP TABLE IF EXISTS `REQUIREMENT`;
CREATE TABLE `REQUIREMENT` (
  `ID` int(11) NOT NULL default '0',
  `COMP_ID` int(11) NOT NULL default '0',
  `DATE_MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `DATE_SUBMITTED` datetime NOT NULL default '0000-00-00 00:00:00',
  `DIFFICULTY_ID` int(11) default NULL,
  `FEATURE_REQUEST` int(11) NOT NULL default '0',
  `ORIGINATOR_ID` int(11) NOT NULL default '0',
  `PEOPLE_ID` int(11) NOT NULL default '0',
  `PR_ID` varchar(4) NOT NULL default '',
  `PREVIOUS_OWNER_ID` int(11) default NULL,
  `READ_` char(1) default NULL,
  `RELEASE_ID` int(11) NOT NULL default '0',
  `SUB_TYPE_ID` int(11) default NULL,
  `REQ_TYPE_ID` int(11) default NULL,
  `STATE_ID` varchar(4) NOT NULL default '',
  `SUBJECT` varchar(100) NOT NULL default '',
  `DESCRIPTION` blob,
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `REQ_SUB_TYPE`
--

DROP TABLE IF EXISTS `REQ_SUB_TYPE`;
CREATE TABLE `REQ_SUB_TYPE` (
  `REQ_SUB_TYPE_ID` int(11) NOT NULL default '0',
  `SUB_TYPE_DESC` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`REQ_SUB_TYPE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `REQ_TEST_ITEM`
--

DROP TABLE IF EXISTS `REQ_TEST_ITEM`;
CREATE TABLE `REQ_TEST_ITEM` (
  `BUG_ID` int(11) NOT NULL default '0',
  `ID` int(11) NOT NULL default '0',
  PRIMARY KEY  (`BUG_ID`,`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `REQ_TYPE`
--

DROP TABLE IF EXISTS `REQ_TYPE`;
CREATE TABLE `REQ_TYPE` (
  `REQ_TYPE_ID` int(11) NOT NULL default '0',
  `TYPE_DESCRIPTION` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`REQ_TYPE_ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `STATE`
--

DROP TABLE IF EXISTS `STATE`;
CREATE TABLE `STATE` (
  `ID` varchar(4) NOT NULL default '',
  `SORT_ORDER` int(11) NOT NULL default '0',
  `DESCRIPTION` varchar(50) NOT NULL default '',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `TEST_ITEM`
--

DROP TABLE IF EXISTS `TEST_ITEM`;
CREATE TABLE `TEST_ITEM` (
  `COMMENTS` blob,
  `MODULE_ID` int(11) NOT NULL default '0',
  `CONTROLLED` varchar(50) NOT NULL default '',
  `DATE_CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `ID` int(11) NOT NULL default '0',
  `STATE_ID` varchar(4) NOT NULL default '',
  `TESTED_BY_ID` int(11) default NULL,
  `DESCRIPTION` blob,
  `TITLE` varchar(100) NOT NULL default '',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `TEST_ITEM_STATE`
--

DROP TABLE IF EXISTS `TEST_ITEM_STATE`;
CREATE TABLE `TEST_ITEM_STATE` (
  `NAME` varchar(50) NOT NULL default '',
  `ID` varchar(4) NOT NULL default '',
  `SORT_ORDER` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2007-05-16 21:50:53
