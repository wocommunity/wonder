DELETE FROM PRIORITY;
insert into PRIORITY (ID, SORT_ORDER, DESCRIPTION) values ('crtl', 1, 'Critical');
insert into PRIORITY (ID, SORT_ORDER, DESCRIPTION) values ('high', 2, 'High');
insert into PRIORITY (ID, SORT_ORDER, DESCRIPTION) values ('medm', 3, 'Medium');
insert into PRIORITY (ID, SORT_ORDER, DESCRIPTION) values ('low ', 4, 'Low');

DELETE FROM STATE;
insert into STATE (ID, SORT_ORDER, DESCRIPTION) values ('anzl', 1, 'Analyze');
insert into STATE (ID, SORT_ORDER, DESCRIPTION) values ('buld', 2, 'Build');
insert into STATE (ID, SORT_ORDER, DESCRIPTION) values ('vrfy', 3, 'Verify');
insert into STATE (ID, SORT_ORDER, DESCRIPTION) values ('dcmt', 4, 'Document');
insert into STATE (ID, SORT_ORDER, DESCRIPTION) values ('clsd', 5, 'Closed');

DELETE FROM TEST_ITEM_STATE;
insert into TEST_ITEM_STATE (ID,  SORT_ORDER, NAME) values ('open', 1, 'Open');
insert into TEST_ITEM_STATE (ID,  SORT_ORDER, NAME) values ('bug ', 2, 'Bug');
insert into TEST_ITEM_STATE (ID,  SORT_ORDER, NAME) values ('clsd', 3, 'Closed');
insert into TEST_ITEM_STATE (ID,  SORT_ORDER, NAME) values ('rqmt', 4, 'Requirement');

DELETE FROM DIFFICULTY;
insert into DIFFICULTY (ID,  DESCRIPTION) values (1, 'Hard');
insert into DIFFICULTY (ID,  DESCRIPTION) values (2, 'Medium');
insert into DIFFICULTY (ID,  DESCRIPTION) values (3, 'Easy');

DELETE FROM PEOPLE;
insert into PEOPLE (ID, EMAIL, IS_ACTIVE, IS_ENGINEERING, IS_ADMIN, IS_CUSTOMER_SERVICE, LOGIN, NAME, PASSWORD, TEAM) values
           (1, 'admin@mydomain.com', 1, 1, 1, 0, 'admin', 'Administrator','admin', 'Core team');

DELETE FROM FRAMEW;
insert into FRAMEW (ID, NAME, ORDERING, OWNED_SINCE, USER_ID) values 
(1, "ERExtensions", 1, NULL, NULL),
(2, "ERDirectToWeb", 2, NULL, NULL),
(3, "ERExtras", 3, NULL, NULL),
(4, "BTBusinessLogic", 4, NULL, NULL),
(5, "BugTracker", 5, NULL, NULL);

DELETE FROM RELEASE;
INSERT INTO RELEASE VALUES (1,1,'0.5'),(2,1,'1.0'),(3,1,'1.1');

DELETE FROM REQ_SUB_TYPE;
insert into REQ_SUB_TYPE (REQ_SUB_TYPE_ID, SUB_TYPE_DESC) values (1, "Essential"), (2, "Important"), (3, "Useful"), (4, "Cosmetic");

DELETE FROM REQ_TYPE;
insert into REQ_TYPE (REQ_TYPE_ID, TYPE_DESCRIPTION) values (1, "Interface"), (2, "Documentation"), (3, "Backend"), (4, "Communication");
