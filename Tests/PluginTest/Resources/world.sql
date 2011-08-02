
INSERT INTO Country (id, code, name, continent, region, surfaceArea, indepYear, population, lifeExpectancy, gnp, gnpOld, localName,  governmentForm, headOfState, capitalID, code2 ) VALUES (9998, 'NLD','Netherlands',null,'Western Europe',41526.00,1581,15864000,78.3,371362.00,360478.00,'Nederland','Constitutional Monarchy','Beatrix',null,'NL');
INSERT INTO Country (id, code, name, continent, region, surfaceArea, indepYear, population, lifeExpectancy, gnp, gnpOld, localName,  governmentForm, headOfState, capitalID, code2 ) VALUES (9999,'NOR','Norway',null,'Nordic Countries',323877.00,1905,4478500,78.7,145895.00,153370.00,'Norge','Constitutional Monarchy','Harald V',null,'NO');

INSERT INTO City (id, name, countryID, distict, population)VALUES (5000,'Amsterdam',9998,'Noord-Holland',731200);
INSERT INTO City (id, name, countryID, distict, population) VALUES (6000,'Rotterdam',9998,'Zuid-Holland',593321);
INSERT INTO City (id, name, countryID, distict, population) VALUES (7000,'Haag',9998,'Zuid-Holland',440900);
INSERT INTO City (id, name, countryID, distict, population) VALUES (8000,'Utrecht',9998,'Utrecht',234323);
INSERT INTO City (id, name, countryID, distict, population) VALUES (9000,'Eindhoven',9998,'Noord-Brabant',201843);
INSERT INTO City (id, name, countryID, distict, population) VALUES (10000,'Tilburg',9998,'Noord-Brabant',193238);
INSERT INTO City (id, name, countryID, distict, population) VALUES (11000,'Groningen',9998,'Groningen',172701);
INSERT INTO City (id, name, countryID, distict, population) VALUES (12000,'Breda',9998,'Noord-Brabant',160398);
INSERT INTO City (id, name, countryID, distict, population) VALUES (13000,'Apeldoorn',9998,'Gelderland',153491);
INSERT INTO City (id, name, countryID, distict, population) VALUES (14000,'Nijmegen',9998,'Gelderland',152463);
INSERT INTO City (id, name, countryID, distict, population) VALUES (15000,'Enschede',9998,'Overijssel',149544);
INSERT INTO City (id, name, countryID, distict, population) VALUES (16000,'Haarlem',9998,'Noord-Holland',148772);
INSERT INTO City (id, name, countryID, distict, population) VALUES (17000,'Almere',9998,'Flevoland',142465);
INSERT INTO City (id, name, countryID, distict, population) VALUES (18000,'Arnhem',9998,'Gelderland',138020);
INSERT INTO City (id, name, countryID, distict, population) VALUES (19000,'Zaanstad',9998,'Noord-Holland',135621);
INSERT INTO City (id, name, countryID, distict, population) VALUES (20000,'´s-Hertogenbosch',9998,'Noord-Brabant',129170);
INSERT INTO City (id, name, countryID, distict, population) VALUES (21000,'Amersfoort',9998,'Utrecht',126270);
INSERT INTO City (id, name, countryID, distict, population) VALUES (22000,'Maastricht',9998,'Limburg',122087);
INSERT INTO City (id, name, countryID, distict, population) VALUES (23000,'Dordrecht',9998,'Zuid-Holland',119811);
INSERT INTO City (id, name, countryID, distict, population) VALUES (24000,'Leiden',9998,'Zuid-Holland',117196);
INSERT INTO City (id, name, countryID, distict, population) VALUES (25000,'Haarlemmermeer',9998,'Noord-Holland',110722);
INSERT INTO City (id, name, countryID, distict, population) VALUES (26000,'Zoetermeer',9998,'Zuid-Holland',110214);
INSERT INTO City (id, name, countryID, distict, population) VALUES (27000,'Emmen',9998,'Drenthe',105853);
INSERT INTO City (id, name, countryID, distict, population) VALUES (28000,'Zwolle',9998,'Overijssel',105819);
INSERT INTO City (id, name, countryID, distict, population) VALUES (29000,'Ede',9998,'Gelderland',101574);
INSERT INTO City (id, name, countryID, distict, population) VALUES (30000,'Delft',9998,'Zuid-Holland',95268);
INSERT INTO City (id, name, countryID, distict, population) VALUES (31000,'Heerlen',9998,'Limburg',95052);
INSERT INTO City (id, name, countryID, distict, population) VALUES (32000,'Alkmaar',9998,'Noord-Holland',92713);

INSERT INTO CountryLanguage (id, countryID, language, isOfficial, percentage) VALUES (1, 9998,'Arabic','false',0.9);
INSERT INTO CountryLanguage (id, countryID, language, isOfficial, percentage) VALUES (2, 9998,'Dutch','true',95.6);
INSERT INTO CountryLanguage (id, countryID, language, isOfficial, percentage) VALUES (3, 9998,'Fries','false',3.7);
