
#################################################################
# Static
#################################################################

CREATE TABLE ERCSTATIC (ID NUMBER NOT NULL, KEY_ VARCHAR2(100) NOT NULL, VALUE_ VARCHAR2(1000));

ALTER TABLE ERCSTATIC ADD PRIMARY KEY (ID);

create sequence ERCSTATIC_SEQ;

#################################################################
# Mail Message
#################################################################

CREATE TABLE ERCMAIL_MESSAG (BCC_ADDR VARCHAR2(1000), CC_ADDR VARCHAR2(1000), CREATED DATE NOT NULL, DATE_SENT DATE, FROM_ADDR VARCHAR2(255) NOT NULL, ID NUMBER NOT NULL, LAST_MODIFIED DATE NOT NULL, MAIL_STATE_ID CHAR(4) NOT NULL, REPLY_TO_ADDR VARCHAR2(1000), TEXT_ CLOB, TITLE VARCHAR2(255) NOT NULL, TO_ADDR VARCHAR2(1000) NOT NULL, X_MAILER VARCHAR2(255), EXCEPTION_REASON VARCHAR2(1000), IS_READ NUMBER);

ALTER TABLE ERCMAIL_MESSAG ADD PRIMARY KEY (ID);

create sequence ERCMAIL_MESSAG_SEQ;

#################################################################
# Mail Message State
#################################################################

CREATE TABLE ERCMAIL_STATE (ID CHAR(4) NOT NULL, NAME VARCHAR2(100) NOT NULL);

ALTER TABLE ERCMAIL_STATE ADD PRIMARY KEY (ID);

#################################################################
# Mail Message Attachment
#################################################################

CREATE TABLE ERCMESSAG_ATTACH (FILE_PATH VARCHAR2(1000) NOT NULL, ID NUMBER NOT NULL, MAIL_MESSAG_ID NUMBER NOT NULL, MIME_TYPE VARCHAR2(255) NOT NULL);

ALTER TABLE ERCMESSAG_ATTACH ADD PRIMARY KEY (ID);

create sequence ERCMESSAG_ATTACH_SEQ;

#################################################################
# Preference
#################################################################

CREATE TABLE ERCPREFER (ID NUMBER NOT NULL, KEY_ VARCHAR2(100) NOT NULL, USER_ID NUMBER , VALUE_ CLOB);

ALTER TABLE ERCPREFER ADD PRIMARY KEY (ID);

create sequence ERCPREFER_SEQ;
