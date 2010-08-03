INSERT INTO director (MOVIE_ID,TALENT_ID)
VALUES
	(101,88),
	(103,91),
	(111,99),
	(113,107),
	(114,109),
	(115,114),
	(118,117),
	(118,118),
	(119,127),
	(120,138),
	(121,151),
	(121,156),
	(124,165),
	(125,164),
	(126,207),
	(127,210),
	(128,216),
	(129,33),
	(129,218),
	(130,228),
	(131,232),
	(132,237),
	(133,237),
	(135,91),
	(136,251),
	(137,251),
	(138,218),
	(140,277),
	(141,251),
	(142,285),
	(143,0),
	(144,8),
	(145,10),
	(146,15),
	(147,20),
	(148,27),
	(149,32),
	(150,216),
	(151,43),
	(152,49),
	(153,60),
	(154,63),
	(157,49),
	(158,97),
	(159,15),
	(160,407),
	(162,421),
	(164,426),
	(165,432),
	(166,435),
	(169,435),
	(170,0),
	(171,454),
	(172,457),
	(174,464),
	(175,554),
	(176,49),
	(177,479),
	(178,482),
	(179,484),
	(180,491),
	(181,494),
	(182,457),
	(183,501),
	(184,504),
	(185,508),
	(186,508),
	(187,15),
	(188,522),
	(189,527),
	(190,482),
	(191,457),
	(192,534),
	(193,285),
	(194,543),
	(196,552),
	(197,557),
	(198,553),
	(199,560),
	(200,563),
	(203,574),
	(205,590),
	(205,593),
	(205,623),
	(206,593),
	(206,596),
	(207,593),
	(207,617),
	(215,627);
	

INSERT INTO movie (CATEGORY,DATE_RELEASED,MOVIE_ID,POSTER_NAME,RATED,REVENUE,STUDIO_ID,TITLE,TRAILER_NAME)
VALUES
	('Action','1977-12-29 20:00:00',101,NULL,'PG',600000.0000,12,'Star Wars',NULL),
	('Action','1981-12-27 08:00:00',102,NULL,'G',14400000.0000,7,'Raiders of the Lost Ark',NULL),
	('Action','1972-12-28 20:00:00',103,NULL,'R',0.0000,7,'The Godfather',NULL),
	('Drama','1955-12-28 20:00:00',111,NULL,'G',11200000.0000,9,'Casablanca',NULL),
	('Film-noir','1931-12-28 20:00:00',113,NULL,'G',200000.0000,11,'M',NULL),
	('Drama','1962-12-28 20:00:00',114,NULL,'PG',400000.0000,7,'Lolita',NULL),
	('Western','1967-12-28 20:00:00',115,NULL,'PG',0.0000,12,'Hombre',NULL),
	('Comedy','1983-12-29 20:00:00',118,NULL,'PG',9900000.0000,41,'Strange Brew',NULL),
	('Thriller','1954-01-02 20:00:00',119,NULL,NULL,200000.0000,13,'Les Diaboliques',NULL),
	('Comedy','1982-01-02 20:00:00',120,NULL,NULL,NULL,53,'Le père noël est une ordure',NULL),
	('Surreal','1991-01-02 20:00:00',121,NULL,'R',NULL,54,'Delicatessen',NULL),
	('Surreal','1991-01-03 20:00:00',124,NULL,NULL,500000.0000,7,'Bis ans Ende der Welt [Until the End of the World]',NULL),
	('Drama','1986-01-03 20:00:00',125,NULL,'PG',1200000.0000,14,'Hoosiers',NULL),
	('Documentary','1994-01-03 20:00:00',126,NULL,'PG-13',600000.0000,15,'Hoop Dreams',NULL),
	('Romantic','1990-01-03 20:00:00',127,NULL,NULL,NULL,55,'La discrète',NULL),
	('Thriller','1988-01-03 20:00:00',128,NULL,'R',700000.0000,43,'Frantic',NULL),
	('Drama','1986-01-03 08:00:00',129,NULL,'R',200000.0000,51,'7.2 le Matin [Betty Blues]',NULL),
	('Thriller','1986-01-03 20:00:00',130,NULL,'R',900000.0000,8,'Manhunter',NULL),
	('Action','1988-01-03 20:00:00',131,NULL,'R',1000000.0000,16,'Midnight Run',NULL),
	('Film-noir','1994-01-03 20:00:00',132,NULL,'R',800000.0000,18,'The Last Seduction',NULL),
	('Film-noir','1992-01-03 20:00:00',133,NULL,'R',700000.0000,19,'Red Rock West',NULL),
	('Drama','1979-01-03 20:00:00',135,NULL,'R',1334000.0000,20,'Apocalypse Now',NULL),
	('Surreal','1988-01-03 20:00:00',136,NULL,NULL,800000.0000,12,'Le grand bleu',NULL),
	('Cult','1985-01-03 20:00:00',137,NULL,'R',0.0000,21,'Subway',NULL),
	('Drama','1980-01-03 20:00:00',138,NULL,'R',1000000.0000,22,'Diva',NULL),
	('Drama','1982-01-03 20:00:00',140,NULL,'R',200000.0000,8,'Diner',NULL),
	('Thriller','1990-01-03 20:00:00',141,NULL,'R',900000.0000,21,'Nikita [La femme nikita]',NULL),
	('Action','1990-01-03 20:00:00',142,NULL,'R',2021000.0000,9,'GoodFellas',NULL),
	('Musical','1980-01-03 20:00:00',143,NULL,'R',700000.0000,16,'The Blues Brothers',NULL),
	('Comedy','1989-01-03 20:00:00',144,NULL,'PG',1000000.0000,16,'Uncle Buck',NULL),
	('Comedy','1993-01-03 20:00:00',145,NULL,'PG',200000.0000,23,'Cool Runnings',NULL),
	('Sci-Fi','1982-01-03 20:00:00',146,NULL,'R',400000.0000,9,'Blade Runner',NULL),
	('Film-noir','1989-01-03 20:00:00',147,NULL,'R',4400000.0000,24,'Heathers',NULL),
	('Documentary','1989-01-03 20:00:00',148,NULL,'R',600000.0000,9,'Roger & Me',NULL),
	('Comedy','1985-01-03 20:00:00',149,NULL,'PG',0.0000,16,'Fletch',NULL),
	('Detective','1974-01-03 20:00:00',150,NULL,'R',500000.0000,7,'Chinatown',NULL),
	('Action','1987-01-03 20:00:00',151,NULL,'R',1390000.0000,7,'The Untouchables',NULL),
	('Romantic Comedy','1989-01-03 20:00:00',152,NULL,'R',26400000.0000,11,'When Harry met Sally',NULL),
	('Drama','1995-04-14 07:00:00',153,NULL,'R',4310000.0000,25,'Priest',NULL),
	('Drama',NULL,154,NULL,'G',600000.0000,11,'Little Women',NULL),
	('Drama','1987-01-04 20:00:00',155,NULL,'PG',800000.0000,9,'Empire of the Sun',NULL),
	('Comedy','1988-10-13 07:00:00',157,NULL,'PG',0.0000,20,'The Princess Bride',NULL),
	('Animated','1995-11-22 08:00:00',158,NULL,'G',500000.0000,23,'Toy Story',NULL),
	('Horror','1979-10-25 07:00:00',159,NULL,'R',11200000.0000,12,'Alien',NULL),
	('Action','1985-04-24 08:00:00',160,NULL,'R',0.0000,26,'The Terminator',NULL),
	('Comedy','1982-11-11 08:00:00',162,NULL,'R',600000.0000,11,'Tootsie',NULL),
	('Film-noir','1994-11-11 08:00:00',164,NULL,'R',47700000.0000,25,'Pulp Fiction',NULL),
	('Drama','1993-11-11 08:00:00',165,NULL,'R',700000.0000,28,'The Piano',NULL),
	('Action','1993-11-11 08:00:00',166,NULL,'PG-13',400000.0000,16,'Jurassic Park',NULL),
	('Drama','1993-11-11 08:00:00',169,NULL,'R',900000.0000,16,'Schindler\'s List',NULL),
	('Comedy','1978-11-11 08:00:00',170,NULL,'R',800000.0000,16,'National Lampoon\'s Animal House',NULL),
	('Drama','1981-11-11 08:00:00',171,NULL,'PG',500000.0000,29,'My Dinner With Andre',NULL),
	('Drama','1986-11-11 08:00:00',172,NULL,'R',500000.0000,30,'My Beautiful Laundrette',NULL),
	('Drama','1983-11-11 08:00:00',174,NULL,'PG',700000.0000,31,'Entre Nous',NULL),
	('Drama','1971-11-11 08:00:00',175,NULL,'R',400000.0000,11,'The Last Picture Show',NULL),
	('Drama','1986-11-11 08:00:00',176,NULL,'R',200000.0000,43,'Stand By Me',NULL),
	('Drama','1991-11-11 08:00:00',177,NULL,'R',8800000.0000,32,'My Own Private Idaho',NULL),
	('Drama','1986-11-11 08:00:00',178,NULL,'R',0.0000,33,'Blue Velvet',NULL),
	('Comedy','1974-11-11 08:00:00',179,NULL,'R',213000.0000,34,'Amarcord',NULL),
	('Action','1990-11-11 08:00:00',180,NULL,'R',0.0000,35,'Total Recall',NULL),
	('Thriller','1983-11-11 08:00:00',181,NULL,'PG',800000.0000,8,'The Year of Living Dangerously',NULL),
	('Drama','1990-11-11 08:00:00',182,NULL,'R',100000.0000,9,'The Grifters',NULL),
	('Drama','1973-11-11 08:00:00',183,NULL,'R',100000.0000,13,'Cries and Whispers',NULL),
	('Drama','1985-11-11 08:00:00',184,NULL,'R',200000.0000,36,'Desert Hearts',NULL),
	('Comedy','1986-11-11 08:00:00',185,NULL,'R',600000.0000,37,'She\'s Gotta Have It',NULL),
	('Comedy','1989-11-11 08:00:00',186,NULL,'R',700000.0000,37,'Do the Right Thing',NULL),
	('Drama','1991-11-11 08:00:00',187,NULL,'R',800000.0000,38,'Thelma & Louise',NULL),
	('Comedy','1992-11-11 08:00:00',188,NULL,'R',500000.0000,8,'The Player',NULL),
	('Drama','1971-11-11 08:00:00',189,NULL,'R',500000.0000,39,'Klute',NULL),
	('Surreal','1976-11-11 08:00:00',190,NULL,NULL,400000.0000,43,'Eraserhead',NULL),
	('Drama','1987-11-11 08:00:00',191,NULL,'R',700000.0000,42,'Prick Up Your Ears',NULL),
	('Drama','1969-11-11 08:00:00',192,NULL,'PG',500000.0000,12,'The Prime of Miss Jean Brodie',NULL),
	('Drama','1976-11-11 08:00:00',193,NULL,'R',100000.0000,11,'Taxi Driver',NULL),
	('Comedy','1974-11-11 08:00:00',194,NULL,'PG',900000.0000,12,'Young Frankenstein',NULL),
	('Thriller','1949-11-11 08:00:00',196,NULL,'PG',1000000.0000,41,'Third Man, The',NULL),
	('Thriller','1946-11-11 08:00:00',197,NULL,NULL,900000.0000,40,'Notorious',NULL),
	('Drama','1941-11-11 08:00:00',198,NULL,NULL,100000.0000,40,'Citizen Kane',NULL),
	('Comedy','1940-11-11 08:00:00',199,NULL,NULL,1000000.0000,8,'The Philadelphia Story',NULL),
	('Western','1969-11-11 08:00:00',200,NULL,NULL,800000.0000,37,'Once Upon a Time in the West',NULL),
	('Drama','1980-01-05 20:00:00',202,NULL,'R',1000000.0000,27,'Bad Timing',NULL),
	('Sci-Fi','1958-11-11 08:00:00',203,NULL,NULL,200000.0000,50,'Plan 9 from Outer Space',NULL),
	('Surreal','1996-01-24 20:00:00',205,NULL,'G',600000.0000,52,'EOF Next Generation',NULL),
	('Drama','1999-08-22 07:00:00',206,NULL,'PG',8000000.0000,52,'Star WOB',NULL),
	('Action','1999-08-22 07:00:00',207,NULL,'G',8000000.0000,52,'WOF The Next Big Thing',NULL),
	('Comedy','1999-08-11 07:00:00',210,NULL,NULL,6000000.0000,56,'The Big Lebowsky',NULL),
	('Drama','1995-02-01 08:00:00',211,NULL,NULL,0.0000,57,'Brazil',NULL),
	('Drama','1999-02-19 08:00:00',214,NULL,NULL,NULL,16,'October Sky',NULL),
	('Drama','1993-09-08 07:00:00',215,NULL,NULL,500000.0000,58,'Blue (Three Colors)',NULL),
	(NULL,NULL,216,NULL,NULL,NULL,12,'Test',NULL);


INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (101, 'Luke Skywalker', 86);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (101, 'Han Solo', 87);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (101, 'Princess Leia', 120);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (102, 'Indiana Jones', 87);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (102, 'Marion Ravenwood', 121);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (103, 'Michel', 90);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (103, 'Michels Wife', 92);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (103, 'The Godfather', 93);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (111, 'Rick Blaine', 100);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (111, 'Ilsa Lund', 101);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (111, 'Senor Ferrari', 102);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (111, 'Ugarte', 103);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (113, 'Franz Becker', 103);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (114, 'Humbert Humbert', 110);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (114, 'Lolita Haze', 111);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (114, 'Charlotte Haze', 112);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (114, 'Rex', 113);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (115, 'John Russell', 115);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (118, 'Bob McKenzie', 117);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (118, 'Doug McKenzie', 118);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (118, 'Brewmeister Smith', 119);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (119, 'Inspecteur Ficher', 122);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (119, 'Michel Delasalle', 123);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (119, 'Nicole Horner', 124);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (119, 'Christina Delasalle', 125);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (119, 'Mr Raymond', 126);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Thérèse', 137);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Mme Musquin', 139);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Pierre Mortez', 140);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Katia', 141);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Preskovitch', 142);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Felix', 143);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Zezette', 144);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Pharmacien', 145);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Mr Leblé', 146);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (120, 'Mme Leblé', 147);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Mlle Plusse', 148);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Mme Tapioca', 149);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Marcel Tapioca', 150);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Robert Kube', 152);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Georges Interligator', 153);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Aurore Interligator', 154);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Julie Clapet', 155);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Clapet', 157);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (121, 'Louison', 158);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Henry Farber', 119);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Maisie', 152);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Hotel guest', 166);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Eugene Fitzpatrick', 167);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Bernie', 168);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Heidi', 169);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Policeman', 170);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Maid', 171);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Ronda', 172);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Woman in street car', 173);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Elsa Farber', 174);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Narcotics agent', 175);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Lydia', 176);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Makiko', 177);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Old man Alfred', 178);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Phillip Winter', 179);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Burt', 180);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Mechanic', 181);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Claire Tourneur', 182);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Hotel guest', 183);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'David', 184);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Karl', 185);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Nora Oliveira', 186);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Irina Farber', 187);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Peter', 188);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Sam Farber', 189);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Receptionist', 190);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Chico Remy', 192);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Truck driver', 193);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Astronaut', 194);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Raymond Monnet', 195);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Krasikova', 196);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Mario', 197);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Ned', 198);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Mrs Mori', 199);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Edith Farber', 200);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Doctor', 201);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Custodian', 202);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Anton Farber', 203);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (124, 'Buzzer', 204);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (125, 'Coach Norman Dale', 161);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (125, 'Myra Fleener', 162);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (125, 'Shooter', 163);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (126, 'Arthur Agee', 205);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (126, 'William Gates', 206);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (126, 'Isiah Thomas', 208);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (127, 'Solange', 209);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (127, 'Jean', 211);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (127, 'Antoine', 212);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (127, 'Catherine', 213);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (128, 'Richard Walker', 87);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (128, 'Sondra Walker', 214);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (128, 'Michelle', 215);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (129, 'Zorg', 217);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (129, 'Bob', 219);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (129, 'Betty', 220);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (129, 'Annie', 221);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (129, 'Lisa', 222);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (129, 'Eddy', 223);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (130, 'Hannibal Lecter (2)', 224);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (130, 'Hannibal Lecter (1)', 225);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (130, 'Jack Crawford', 226);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (130, 'Will Graham', 227);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (131, 'Jimmy Serrano', 226);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (131, 'Eddie Moscone', 229);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (131, 'Jack Walsh', 230);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (131, 'Marvin Dorfler', 231);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (131, 'Alonzo Mosely', 233);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (131, 'Jonathan Mardukas', 234);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (132, 'Bridget Gregory', 235);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (132, 'Clay Gregory', 236);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (132, 'Mike Swale', 238);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (133, 'Lyle', 163);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (133, 'Wayne', 239);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (133, 'Michael', 240);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (133, 'Suzanne', 241);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (135, 'Colonel', 87);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (135, 'Colonel Walter Kurtz', 93);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (135, 'Clean', 245);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (135, 'Lieutenant Colonel Kilgore', 246);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (135, 'Captain Benjamin L. Willard', 247);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (136, 'Alfredo', 248);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (136, 'Jacques Mayol', 249);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (136, 'Enzo Molinari', 250);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (136, 'Johanna', 252);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'The Roller', 217);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Drummer', 250);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Batman', 253);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Florist', 254);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Commisaire Gesberg', 255);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Helena', 256);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Fred', 257);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Big Negro', 258);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (137, 'Robin', 259);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (138, 'Cynthia Hawkins', 260);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Beth', 271);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Barbara', 272);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Bagel', 273);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Boogie', 274);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Fenwick', 275);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Modell', 276);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Billy', 278);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Eddie', 279);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (140, 'Shrevie', 280);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Old teacher', 200);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Nikitas boyfriend', 217);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Coyotte', 248);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Rico', 281);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Zap', 282);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Nikita', 283);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Bob', 284);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (141, 'Le Nettoyeur', 620);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (142, 'James Conway', 230);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (142, 'Joe Buddha', 286);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (142, 'Henry Hill', 287);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (142, 'Tommy DeVito', 288);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (142, 'Paul Cicero', 289);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (142, 'Karen Hill', 290);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Curtis', 1);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Chic Lady', 2);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Mystery Woman', 120);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Soul Food Cafe Owner', 291);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Reverend Cleophus James', 292);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Soloist', 293);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Burton Mercer', 295);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Waiter', 296);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Joliet Jake', 297);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Elwood', 298);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Ray', 299);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (143, 'Cook County Clerk', 435);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (144, 'Maisy Russell', 3);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (144, 'Buck Russell', 295);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (145, 'Irv', 295);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (146, 'Deckard', 622);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (149, 'Fletch', 610);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (150, 'Man with Knife', 216);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (151, 'Al Capone', 230);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (152, 'Marie', 120);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (157, 'Inigo Montoya', 86);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (157, 'The Albino', 87);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (157, 'The King', 88);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (157, 'The Assistant Brute', 90);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (157, 'Yellin', 91);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (157, 'Valerie', 92);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (157, 'The Ancient Booer', 93);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (159, 'Ripley', 99);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (159, 'Parker', 233);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (159, 'Ash', 400);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (159, 'Kane', 401);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (159, 'Lambert', 402);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (159, 'Brett', 403);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (160, 'Sarah Connor', 404);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (160, 'Kyle Reese', 406);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (160, 'Terminator', 606);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (162, 'Michael', 417);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (162, 'Roommate', 418);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (162, 'Ron', 419);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (162, 'Sandy', 420);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (162, 'Julie', 422);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (164, 'Jules', 286);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (164, 'Pumpkin', 425);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (164, 'Butch', 427);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (164, 'Winston Wolf', 428);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (164, 'Honey Bunny', 429);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (164, 'Mia', 430);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (164, 'Vincent Vega', 431);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (165, 'Stewart', 167);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (165, 'Baines', 428);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (165, 'Ada', 433);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (165, 'Fiona', 434);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (166, 'Doctor Alan Grant', 167);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (166, 'Dr. Ellie Sattler', 436);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (166, 'John Hammond', 437);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (166, 'Ian Malcolm', 438);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (169, 'Oskar Schindler', 447);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (169, 'Amon Goeth', 448);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (169, 'Itzhak Stern', 449);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (169, 'Helen Hirsch', 450);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (170, 'Katy', 121);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (170, 'Bluto', 297);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (170, 'Larry Kroger', 451);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (170, 'Jennings', 452);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (170, 'Kent Dorfman', 453);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (171, 'Andre', 455);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (172, 'Johnny', 594);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (172, 'Omar', 628);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (174, 'Lena', 462);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (174, 'Madeleine', 463);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (175, 'Sam the Lion', 466);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (175, 'Duane', 467);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (175, 'Jacy Farrow', 468);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (175, 'Sonny', 469);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (175, 'Ruth Popper', 470);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (175, 'Lois Farrow', 471);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (176, 'Denny Lachance', 472);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (176, 'Teddy Duchamp', 473);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (176, 'Chris Chambers', 474);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (176, 'Gordie Lachance', 475);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (176, 'Vern Tassio', 476);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (176, 'Ace Merrill', 477);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (177, 'Mike Waters', 474);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (177, 'Scott Favor', 478);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (178, 'Frank Booth', 163);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (178, 'Sandy Williams', 436);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (178, 'Jeffrey Beaumont', 480);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (178, 'Dorothy Vallens', 481);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (179, 'Titta', 483);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (179, 'The Mother', 485);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (179, 'Gradisca', 486);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (180, 'Quaid', 405);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (180, 'Melina', 487);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (180, 'Lori', 488);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (180, 'Cohaagen', 489);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (180, 'Richter', 490);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (181, 'Jill Bryant', 99);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (181, 'Billy Kwan', 492);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (181, 'Guy Hamilton', 493);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (182, 'Roy Dillon', 472);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (182, 'Myra Langtry', 495);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (182, 'Lilly Dillon', 496);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (183, 'Anna', 497);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (183, 'Agnes', 498);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (183, 'Karin', 499);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (183, 'Maria', 500);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (184, 'Frances', 502);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (184, 'Vivian Bell', 503);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (184, 'Cay Rivers', 505);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (185, 'Greer Childs', 506);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (185, 'Jamie Overstreet', 507);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (185, 'Mars Blackmon', 508);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (185, 'Nola Darling', 509);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Mister Senor Love Daddy', 286);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Mookie', 508);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Mother Sister', 510);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Da Mayor', 511);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Sal', 512);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Jade', 513);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Tina', 514);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Buggin Out', 515);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (186, 'Vito', 516);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (187, 'Hal Slocumb', 428);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (187, 'J.D.', 517);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (187, 'Darryl', 518);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (187, 'Jimmy', 519);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (188, 'June Gudmundsdottir', 520);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (188, 'Walter Stuckel', 521);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (188, 'Detective Susan Avery', 523);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (188, 'Larry Levy', 524);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (188, 'Griffin Mill', 525);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (189, 'John Klute', 452);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (189, 'Bree Daniels', 526);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (190, 'Henry Spencer', 528);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (190, 'Mary X', 529);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (191, 'Peggy Ramsay', 530);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (191, 'Joe Orton', 531);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (191, 'Kenneth Halliwell', 532);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (192, 'Teddy Lloyd', 533);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (192, 'Jean Brodie', 535);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (192, 'Sandy', 536);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (193, 'Travis Bickle', 230);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (193, 'Sport', 428);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (193, 'Betsy', 468);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (193, 'Wizard', 537);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (193, 'Iris', 538);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (193, 'Tom', 539);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (194, 'Inga', 420);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (194, 'Frau Bluecher', 470);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (194, 'The Monster', 537);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (194, 'Igor', 540);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (194, 'Dr. Frankenstein', 541);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (194, 'Elizabeth', 542);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (196, 'Major Calloway', 549);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (196, 'Anna Schmidt', 550);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (196, 'Holly Martins', 551);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (196, 'Harry Lime', 553);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (197, 'Alice Huberman', 101);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (197, 'T.R. Devlin', 555);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (197, 'Alexander Sebastian', 556);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (198, 'Jed Leland', 551);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (198, 'Charles Foster Kane', 553);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (198, 'Mrs. Kane', 558);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (198, 'Susan Alexander', 559);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (199, 'C.K. Dexter Haven', 555);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (199, 'Macaulay (Mike) Connor', 561);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (199, 'Tracy Lord', 562);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (200, 'Frank', 564);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (200, 'Cheyenne', 565);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (200, 'Harmonica', 566);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (200, 'Jill McBain', 567);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (202, NULL, 570);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (203, 'Jeff Trent', 571);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (203, 'Paula Trent', 572);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (203, 'Inspector Clay', 573);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (203, 'Vampire Girl', 575);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (203, 'Ghoul Man', 576);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Jussac', 594);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Constance Bonacieux', 600);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Planchet', 602);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'De Treville', 604);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Aramis', 605);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Grimaud', 607);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Biscarrat', 615);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Porthos', 619);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Queen Anne', 621);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Bonacieux', 622);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'DArtagnan', 624);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (205, 'Athos', 625);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Queen Ameldala', 592);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'OBen-WOB Kenobi', 595);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Qui-Gon Gautier', 596);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Dr. Beverly Crusher', 597);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Lando Calrawdonian', 598);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Tom Solo', 599);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Liubacca', 601);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Grand Moff Justin', 604);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Darth Sanchez', 611);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'JP-D2', 612);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (206, 'Jar Jar Jirman', 619);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Winston', 603);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Public Defender', 606);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Max Cherry', 608);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Melanie Ralston', 609);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Wanda', 610);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Ordell Robbie', 613);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Beaumont Livingston', 614);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Louis Gara', 616);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'Jackie Brown', 618);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (207, 'The Judge', 620);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (210, 'The Dude', 618);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (211, 'Sam', 595);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (211, 'Archibald Tuttle', 599);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (214, 'Homer Hickam', 619);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (215, 'Julie', 603);
INSERT INTO `movierole` (`MOVIE_ID`, `ROLE_NAME`, `TALENT_ID`) VALUES (215, 'Olivier', 626);



INSERT INTO PlotSummary (MOVIE_ID,SUMMARY)
VALUES
	(2,NULL),
	(3,NULL),
	(4,NULL),
	(5,NULL),
	(7,NULL),
	(8,NULL),
	(101,'Dan: This was most certainly a G movie. Right? Craig: Sorry, man.  I checked.  It was PG. Bruce: Lucas personally wanted it rated PG because he feared that viewers wouldn\'t go to a G rated action film.'),
	(102,'Very exciting!'),
	(103,'The Godfather kills everyone and rules the world.'),
	(111,NULL),
	(113,'The story of a german town caught in the grip of a child molester/murderer. '),
	(114,'Middle-aged novelist Humbert Humbert rents a room in Charlotte Haze\'s house after he falls passionately in love with her daughter Lolita. There are three slight problems, though - one, Charlotte is madly in love with him (unrequited, needless to say); two, Lolita is only fourteen; three, there\'s a very peculiar figure by the name of Clare Quilty who keeps popping up in the most unexpected places (and sporting equally unexpected accents), who seems to have a similarly unhealthy interest in Lolita.'),
	(115,NULL),
	(118,NULL),
	(119,'A scary french cult movie.'),
	(120,'A Christmas at SOS Amitié (a french \"suicide hotline\" equivalent)'),
	(121,NULL),
	(124,NULL),
	(125,'The small town of Hickory, Indiana brings in outsider Coach Norman Dale to lead their outmatched high school basketball team against the rest of the state\'s best.'),
	(126,'The film follows two young  black basketball players in Chicago as they leave grammar school and enter high school with high expectations for their futures in the world of college and professional basketball.'),
	(127,NULL),
	(128,'Dr. Richard Walker and his wive arrive in Paris for a working vacation. But Mrs. Walker disappears almost immediately, leaving Dr. Walker desparately trying to track her down without any french language skills or much help from the french police or the US embassy. Eventually he finds Michelle, the smuggler who also must discover who is behind the abduction.'),
	(129,NULL),
	(130,'Will Graham is brought out of retirement to pursue an elusive serial killer. In order to find the killer, Graham must return to Dr. Hannibal Lector, the serial killer he captured after almost losing his mind and life.'),
	(131,'Bounty hunter Jack Walsh sets out to bring bail jumper Jonathan Mardukas back to Los Angeles for trial. But after finding him, Walsh discovers that the \"Duke\" was a mafia accountant who donated millions of dollars to charities after embezzling it from the mafia.'),
	(132,'Bridget Gregory steals drug money from her husband and tries to hid in upstate New York. When her lawyer tells her to blend in and get a boyfriend, things get interesting.'),
	(133,'When Michael arrives in the town of Red Rock out of money and down on his luck, Wayne mistakes him for the hit man he hired to kill his wife. But soon Michael meets Wayne\'s wife and the real hit man arrives and things heat up.'),
	(135,'Copola\'s movie is based on Conrad\'s Heart of Darkness set in Vietnam and Cambodia. Captain Benjamin Willard is set on a secret mission to \"terminate\" the renegade Colonel Kurtz who is waging an effective but independent war against the North Vietnemese.'),
	(136,NULL),
	(137,NULL),
	(138,NULL),
	(140,'The movie tracks the lives of 5 friends who regulary meet at the Diner to share stories and figure out where their lives are heading.'),
	(141,NULL),
	(142,'Based on the true story of Henry Hill. The film tracks the rise of Hill, James Conway, and Tommy DeVito as they work their way up in the mafia.'),
	(143,'After finishing his sentence for armed robbery, Jake is release from prison only to discover that the county is going to close the catholic school he and Elwood attended as children. Both set out to recreate \"the band\" in an attempt to raise enough money to pay the taxes for the school.'),
	(144,'When Mom and Dad have to leave town, bachelor at large, Uncle Buck, comes to take care of the children.'),
	(145,'Roughly based on the story behind the 1988 Jamamican bobsled team. After being denied places on the summer olympic team as a sprinter, Leon recruits three others and the reluctant coach Irv to join him on the team. Along the way the team encounters many skeptics and discovers the colored past of the coach.'),
	(146,'Deckard is charged with tracking down and destroying a group of escaped replicants. Of course these replicants are the latest models that are getting all the more human, making Deckard\'s job all the tougher and raises questions about the role of these machines. Very Sci-Fi.'),
	(147,'The ruling clique at Westerberg High is running along smoothly until new comer J.D. (for James Dean?) arrives and convinces Veronica that some mischief would make things much more interesting. When Veronica discovers \"mischief\" involves bumping off other students and staging elaborate suicide frame-ups things get dark.'),
	(148,'Michael Moore tracks the demise of his home town of Flint Michigan. His ultimate goal is to direct his questions to Roger Smith, the president of General Motors.'),
	(149,'Irwin Fletcher is an investigative reporter searching for the story behind the drug trade on the beach. But he soon finds himself drawn into exposing a separate con, that shares some curious players as the drug trade.'),
	(150,'Jake Gittes is hired for the apparently simple task of documenting the extra-marital activities of her husband. But after encountering a few exceptionally unhelpful and down right violent people, Gittes starts to unravel a maze of corruption and deceit.'),
	(151,'Loosely based on the true story of Elliot Ness. Frustrated by the penetration of corruption in Chicago, Ness puts together a small independent Treasury officers dedicated to the destruction of the Chicago Mob. Good scene stressing the importance of team work.'),
	(152,'Harry Burns and Sally Albright gradually become friends over the course of several years and failed partners. Eventually the sleep together and wrecks the friendship. But eventually they get back together and everyone lives happily ever after.'),
	(153,'An idealistic young priest must deal with the poverty of his new parish, the lax attitude of his fellow pastor, a young girl being abused by her father, and his own shame.'),
	(154,'Based on the novel by Louisa May Alcott.'),
	(155,'An epic-scale World War II film about a privileged boy captured by the Japanese in Shanghai.'),
	(157,NULL),
	(158,'Toys come to life when the humans are away.  Classic buddy flick in which a spaceman toy (Buzz Lightyear) arrives at a birthday party and threatens to displace the cowboy (Woody) as the child\'s favorite toy.  The two rivals get lost and eventually become friends, working together to find their way home.'),
	(159,NULL),
	(160,NULL),
	(161,NULL),
	(162,NULL),
	(164,NULL),
	(165,NULL),
	(166,NULL),
	(169,NULL),
	(170,NULL),
	(171,NULL),
	(172,NULL),
	(174,NULL),
	(175,NULL),
	(176,NULL),
	(177,NULL),
	(178,NULL),
	(179,NULL),
	(180,NULL),
	(181,NULL),
	(182,NULL),
	(183,NULL),
	(184,NULL),
	(185,NULL),
	(186,NULL),
	(187,NULL),
	(188,NULL),
	(189,NULL),
	(190,NULL),
	(191,NULL),
	(192,NULL),
	(193,NULL),
	(194,NULL),
	(196,NULL),
	(197,NULL),
	(198,NULL),
	(199,NULL),
	(200,NULL),
	(202,NULL),
	(203,NULL),
	(205,'The EOF product attempts to go where no database product has gone before!'),
	(206,'The WOB Team takes WebObjects tools where they have never gone before!'),
	(207,'The WOF Team takes you where no application server product has gone before!'),
	(210,NULL),
	(211,NULL),
	(214,NULL),
	(215,'Julie loses her husband, Patrice, a well-known composer, and their young daughter, Anna, in an auto accident and must begin life anew. She decides to become anonymous and totally independent, and she deliberately cuts herself off from everything that she has possessed so abundantly before. Olivier, Patrice\'s young assistant, has always loved Julie. To try and bring her out of isolation, he decides to finish the \"Concerto for Europe,\" that had been left unfinished at the  composer\'s death.'),
	(216,NULL);


INSERT INTO studio (BUDGET,NAME,STUDIO_ID)
VALUES
	(0.0000,'New Line Cinema',2),
	(5000000.0000,'Paramount Pictures',7),
	(17000000.0000,'MGM',8),
	(100000000.0000,'Warner Brothers',9),
	(22000000.0000,'Columbia Pictures',11),
	(100000000.0000,'20th Century Fox',12),
	(200000.0000,'Filmsonor',13),
	(11000000.0000,'Hemdale Film Corporation',14),
	(70000.0000,'Kartemquin Films',15),
	(23000000.0000,'Universal Pictures',16),
	(11000000.0000,'ITC Films',18),
	(1000000.0000,'Propaganda Films',19),
	(21000000.0000,'Zoetrope Studios',20),
	(22000000.0000,'Gaumont International',21),
	(0.0000,'Greenwich Film Production',22),
	(4000000.0000,'Walt Disney Productions',23),
	(52000000.0000,'New World Entertainment',24),
	(56000000.0000,'Miramax Films',25),
	(7000000.0000,'Pacific Western',26),
	(8000000.0000,'Rank Film Productions',27),
	(9000000.0000,'CiBy 2000',28),
	(10000000.0000,'The Andre Company',29),
	(11000000.0000,'Working Title Films',30),
	(26000000.0000,'Alexandre Films',31),
	(0.0000,'Test',32),
	(4000000.0000,'De Laurentis',33),
	(55000000.0000,'F.C. Produzioni',34),
	(6000000.0000,'Carolco Pictures',35),
	(7000000.0000,'Samuel Goldwyn Company',36),
	(81000000.0000,'40 Acres and a Mule Filmworks',37),
	(9000000.0000,'Avenue Pictures',38),
	(10000000.0000,'Cineplex Odeon',39),
	(1000000.0000,'RKO Radio Pictures',40),
	(2000.0000,'British Lion Film Corp.',41),
	(0.0000,'Civilhand Zenith',42),
	(4000000.0000,'AFI',43),
	(19778543.0000,'ZZ Studios',50),
	(1000000.0000,'Cargo Films',51),
	(0.0000,'Apple Computer, Inc.',52),
	(0.0000,'Trinacra Films',53),
	(0.0000,'Sofinergie Films',54),
	(0.0000,'France 3 Cinéma',55),
	(0.0000,'Polygram Filmed Entertainment',56),
	(0.0000,'Embassy International Pictures',57),
	(0.0000,'Basilisk Communications',58);


INSERT INTO talent (FIRST_NAME,LAST_NAME,TALENT_ID)
VALUES
	('Cab','Calloway',1),
	('Twiggy','Lawson',2),
	('John','Landis',3),
	('Mark','Hammil',86),
	('Harrison','Ford',87),
	('George','Lucas',88),
	('Al','Pacino',90),
	('Francis Ford','Copola',91),
	('Dianne','Keaton',92),
	('Marlon','Brando',93),
	('Michael','Curtiz',99),
	('Humphrey','Bogart',100),
	('Ingrid','Bergman',101),
	('Sidney','Greenstreet',102),
	('Peter','Lorre',103),
	('Fritz','Lang',107),
	('Stanley','Kubrick',109),
	('James','Mason',110),
	('Sue','Lyon',111),
	('Shelley','Winters',112),
	('Peter','Sellers',113),
	('Martin','Ritt',114),
	('Paul','Newman',115),
	('Rick','Moranis',117),
	('Dave','Thomas',118),
	('Max','Von Sydow',119),
	('Carrie','Fisher',120),
	('Karen','Allen',121),
	('Charles','Vanel',122),
	('Paul','Meurisse',123),
	('Simone','Signoret',124),
	('Vera','Clouzot',125),
	('Michel','Serrault',126),
	('Henri-Georges','Clouzot',127),
	('Anémone','',137),
	('Jean-Marie','Poiré',138),
	('Josianne','Balasko',139),
	('Thierry','Lhermitte',140),
	('Christian','Clavier',141),
	('Bruno','Moynot',142),
	('Gérard','Jugnot',143),
	('Marie-Anne','Chazel',144),
	('Jacques','François',145),
	('Martin','Lamotte',146),
	('Claire','Magnin',147),
	('Karin','Viard',148),
	('Anne-Marie','Pisani',149),
	('Ticky','Holgado',150),
	('Marc','Caro',151),
	('Justine','Saunders',152),
	('Jean-François','Perrier',153),
	('Silvie','Laguna',154),
	('Marie-Laure','Dougnac',155),
	('Jean-Pierre','Jeunet',156),
	('Jean-Claude','Dreyfus',157),
	('Dominique','Pinon',158),
	('Gene','Hackman',161),
	('Barbara','Hershey',162),
	('Dennis','Hopper',163),
	('David','Anspaugh',164),
	('Wim','Wenders',165),
	('Hiroshi','Kanbe',166),
	('Sam','Neil',167),
	('Allen','Garfield',168),
	('Lauren','Graham',169),
	('Fred','Walsh',170),
	('Miwako','Fujitani',171),
	('Rhoda','Roberts',172),
	('Amalia','Rodriguez',173),
	('Lois','Chiles',174),
	('Alec','Jason',175),
	('Kylie','Belling',176),
	('Adelle','Lutz',177),
	('Alfred','Lynch',178),
	('Ruediger','Vogler',179),
	('Ernie','Dingo',180),
	('Jean-Charles','Dumay',181),
	('Solveig','Dommartin',182),
	('Yugi','Ogata',183),
	('David','Gulpilil',184),
	('Paul','Livingston',185),
	('Susan','Leith',186),
	('Christine','Osterlein',187),
	('Jimmy','Little',188),
	('William','Hurt',189),
	('Diogo','Doria',190),
	('Chick','Ortega',192),
	('Zhang','Jinzan',193),
	('Detlef','Winterberg',194),
	('Eddy','Mitchell',195),
	('Elena','Smirnova',196),
	('Pietro','Falcone',197),
	('Bart','Willoughby',198),
	('Kuniko','Miyake',199),
	('Jeanne','Moreau',200),
	('Enzo','Turrin',201),
	('Naoto','Takenaka',202),
	('Ernest','Berk',203),
	('Chalie','McMahon',204),
	('Arthur','Agee',205),
	('William','Gates',206),
	('Steve','James',207),
	('Isiah','Thomas',208),
	('Marie','Bunel',209),
	('Christian','Vincent',210),
	('Maurice','Garrel',211),
	('Fabrice','Luchini',212),
	('Judith','Henry',213),
	('Betty','Buckley',214),
	('Emmanuelle','Seigner',215),
	('Roman','Polanski',216),
	('Jean-Hugues','Anglade',217),
	('Jean-Jacques','Beineix',218),
	('Jacques','Mathou',219),
	('Béatrice','Dalle',220),
	('Clémentine','Célarié',221),
	('Consuelo','De Haviland',222),
	('Gérard','Darmont',223),
	('Dan','Butler',224),
	('Brian','Cox',225),
	('Dennis','Farina',226),
	('William','Petersen',227),
	('Michael','Mann',228),
	('Joe','Pantoliano',229),
	('Robert','De Niro',230),
	('John','Ashton',231),
	('Martin','Brest',232),
	('Yaphet','Kotto',233),
	('Charles','Grodin',234),
	('Linda','Fiorentino',235),
	('Bill','Pullman',236),
	('John','Dahl',237),
	('Peter','Berg',238),
	('J.T.','Walsh',239),
	('Nicolas','Cage',240),
	('Lara Flynn','Boyle',241),
	('Laurence','Fishburne',245),
	('Robert','Duvall',246),
	('Martin','Sheen',247),
	('Patrick','Fontana',248),
	('Jean-Marc','Barr',249),
	('Jean','Reno',250),
	('Luc','Besson',251),
	('Rosanna','Arquette',252),
	('Jean-Pierre','Bacri',253),
	('Richard','Bohringer',254),
	('Michel','Galabru',255),
	('Isabelle','Adjani',256),
	('Christophe','Lambert',257),
	('Jimmy','Blanche',258),
	('Jean-Claude','Lecas',259),
	('Wilhelmenia','Fernandez',260),
	('Ellen','Barkin',271),
	('Kathyrn','Dowling',272),
	('Michael','Tucker',273),
	('Mickey','Rourke',274),
	('Kevin','Bacon',275),
	('Paul','Reiser',276),
	('Barry','Levinson',277),
	('Timothy','Daly',278),
	('Steve','Guttenberg',279),
	('Daniel','Stern',280),
	('Marc','Duret',281),
	('Alain','Cathière',282),
	('Anne','Parillaud',283),
	('Tchéky','Karyo',284),
	('Martin','Scorsese',285),
	('Samuel L.','Jackson',286),
	('Ray','Liotta',287),
	('Joe','Pesci',288),
	('Paul','Sorvino',289),
	('Lorraine','Bracco',290),
	('Aretha','Franklin',291),
	('James','Brown',292),
	('Chaka','Kahn',293),
	('John','Candy',295),
	('Paul','Reubens',296),
	('John','Belushi',297),
	('Dan','Aykroyd',298),
	('Ray','Charles',299),
	('Gaby','Hoffmann',303),
	('Jay D.','Underwood',304),
	('Jean Louisa','Kelley',305),
	('Macaulay','Culkin',306),
	('Amy','Madigan',307),
	('John','Hughes',308),
	('Derice','Bannock',309),
	('Jon','Turteltaub',310),
	('Doug E.','Doug',311),
	('Rawle D.','Lewis',312),
	('Malik','Yoba',313),
	('Rutger','Hauer',314),
	('Ridley','Scott',315),
	('Sean','Young',316),
	('Daryl','Hannah',317),
	('Edward James','Olmos',318),
	('Michael Emmet','Walsh',319),
	('Michael','Lehmann',320),
	('Shannen','Doherty',321),
	('Lisanne','Falk',322),
	('Christian','Slater',323),
	('Winona','Ryder',324),
	('Kim','Walker',325),
	('Pat','Boone',326),
	('Michael','Moore',327),
	('Bob','Eubanks',328),
	('Roger','Smith',329),
	('Chevy','Chase',330),
	('George','Wendt',331),
	('Michael','Ritchie',332),
	('Kareem','Abdul-Jabbar',333),
	('Dana','Wheeler-Nicholson',334),
	('Chick','Hearn',335),
	('Tim','Matheson',336),
	('Joe Don','Baker',337),
	('Geena','Davis',338),
	('Faye','Dunaway',339),
	('Perry','Lopez',340),
	('Jack','Nicholson',341),
	('John','Houston',342),
	('Brian','DePalma',343),
	('Kevin','Costner',344),
	('Sean','Connery',345),
	('Charles Martin','Smith',346),
	('Andy','Garcia',347),
	('Bruno','Kirby',348),
	('Rob','Reiner',349),
	('Meg','Ryan',350),
	('Billy','Crystal',351),
	('Robert','Pugh',352),
	('Robert','Carlyle',353),
	('James','Ellis',354),
	('Christine','Tremarco',355),
	('Tom','Wilkinson',356),
	('Cathy','Tyson',357),
	('Matyelok','Gibbs',358),
	('Linus','Roache',359),
	('Antonia','Bird',360),
	('Lesley','Sharp',361),
	('Christian','Bale',362),
	('Gillian','Armstrong',363),
	('Susan','Sarandon',364),
	('Miranda','Richardson',365),
	('John','Malkovich',366),
	('Cary','Elwes',377),
	('Fred','Savage',378),
	('Robin','Wright',379),
	('Andre the Giant','Andre the Giant',380),
	('Peter','Falk',381),
	('Christopher','Guest',382),
	('Chris','Sarandon',383),
	('Peter','Cook',384),
	('Wallace','Shawn',385),
	('Mandy','Patinkin',386),
	('Mel','Smith',387),
	('Willoughby','Gray',388),
	('Anne','Dyson',389),
	('Paul','Badger',390),
	('Malcolm','Storry',391),
	('Carol','Kane',392),
	('Margery','Mason',393),
	('Betsy','Brantley',394),
	('Tom','Hanks',395),
	('Tim','Allen',396),
	('John','Lasseter',397),
	('Tom','Skerritt',398),
	('Sigourney','Weaver',399),
	('Ian','Holm',400),
	('John','Hurt',401),
	('Veronica','Cartwright',402),
	('Harry Dean','Stanton',403),
	('Linda','Hamilton',404),
	('Arnold','Schwarzenegger',405),
	('Michael','Biehn',406),
	('James','Cameron',407),
	('Pat','Thompson',408),
	('Sonia','Kruger',409),
	('Tara','Morice',410),
	('Gia','Carides',411),
	('Antonio','Vargas',412),
	('Bill','Hunter',413),
	('Peter','Whitford',414),
	('Barry','Otto',415),
	('Paul','Mercurio',416),
	('Dustin','Hoffman',417),
	('Bill','Murray',418),
	('Dabney','Coleman',419),
	('Teri','Garr',420),
	('Sydney','Pollack',421),
	('Jessica','Lange',422),
	('Tim','Roth',425),
	('Quentin','Tarantino',426),
	('Bruce','Willis',427),
	('Harvey','Keitel',428),
	('Amanda','Plummer',429),
	('Uma','Thurman',430),
	('John','Travolta',431),
	('Jane','Campion',432),
	('Holly','Hunter',433),
	('Anna','Paquin',434),
	('Steven','Spielberg',435),
	('Laura','Dern',436),
	('Richard','Attenborough',437),
	('Jeff','Goldblum',438),
	('Liam','Neeson',447),
	('Ralph','Fiennes',448),
	('Ben','Kingsley',449),
	('Embeth','Davidtz',450),
	('Tom','Hulce',451),
	('Donald','Sutherland',452),
	('Stephen','Furst',453),
	('Louis','Malle',454),
	('Andre','Gregory',455),
	('Daniel','Day-Lewis',456),
	('Stephen','Frears',457),
	('Gordon','Warnecke',458),
	('Isabelle','Huppert',462),
	('Miou-Miou','',463),
	('Diane','Kurys',464),
	('Nick','Castle',465),
	('Ben','Johnson',466),
	('Jeff','Bridges',467),
	('Cybill','Shepherd',468),
	('Timothy','Bottoms',469),
	('Cloris','Leachman',470),
	('Ellen','Burstyn',471),
	('John','Cusack',472),
	('Corey','Feldman',473),
	('River','Pheonix',474),
	('Wil','Wheaton',475),
	('Jerry','O\'Connell',476),
	('Kiefer','Sutherland',477),
	('Keanu','Reeves',478),
	('Gus','Van Sant Jr.',479),
	('Kyle','MacLachlan',480),
	('Isabella','Rossellini',481),
	('David','Lynch',482),
	('Bruno','Zanin',483),
	('Federico','Fellini',484),
	('Pupella','Maggio',485),
	('Magali','Noel',486),
	('Rachel','Ticotin',487),
	('Sharon','Stone',488),
	('Ronny','Cox',489),
	('Michael','Ironside',490),
	('Paul','Verhoeven',491),
	('Linda','Hunt',492),
	('Mel','Gibson',493),
	('Peter','Weir',494),
	('Annette','Bening',495),
	('Anjelica','Huston',496),
	('Kari','Sylwan',497),
	('Harriet','Andersson',498),
	('Ingrid','Thulin',499),
	('Liv','Ullmann',500),
	('Ingmar','Bergman',501),
	('Audra','Lindley',502),
	('Helen','Shaver',503),
	('Donna','Deitch',504),
	('Patricia','Charbonneau',505),
	('John Canada','Terrell',506),
	('Tommy Redmond','Hicks',507),
	('Spike','Lee',508),
	('Tracy Camilla','Johns',509),
	('Ruby','Dee',510),
	('Ozzie','Davis',511),
	('Danny','Aiello',512),
	('Joie','Lee',513),
	('Rosie','Perez',514),
	('Giancarlo','Esposito',515),
	('Richard','Edson',516),
	('Brad','Pitt',517),
	('Christopher','McDonald',518),
	('Michael','Madsen',519),
	('Greta','Scacchi',520),
	('Fred','Ward',521),
	('Robert','Altman',522),
	('Whoopi','Goldberg',523),
	('Peter','Gallagher',524),
	('Tim','Robbins',525),
	('Jane','Fonda',526),
	('Alan J.','Pakula',527),
	('Jack','Nance',528),
	('Charlotte','Stewart',529),
	('Vanessa','Redgrave',530),
	('Gary','Oldman',531),
	('Alfred','Molina',532),
	('Robert','Stephens',533),
	('Ronald','Neame',534),
	('Maggie','Smith',535),
	('Pamela','Franklin',536),
	('Peter','Boyle',537),
	('Jodie','Foster',538),
	('Albert','Brooks',539),
	('Marty','Feldman',540),
	('Gene','Wilder',541),
	('Madeline','Kahn',542),
	('Mel','Brooks',543),
	('Trevor','Howard',549),
	('Alida','Valli',550),
	('Joseph','Cotten',551),
	('Carol','Reed',552),
	('Orson','Welles',553),
	('Peter','Bogdanovich',554),
	('Cary','Grant',555),
	('Claude','Rains',556),
	('Alfred','Hitchcock',557),
	('Agnes','Moorehead',558),
	('Dorothy','Comingore',559),
	('George','Cukor',560),
	('James','Stewart',561),
	('Katharine','Hepburn',562),
	('Sergio','Leone',563),
	('Henry','Fonda',564),
	('Jason','Robards',565),
	('Charles','Bronson',566),
	('Claudia','Cardinale',567),
	('Nicholas','Roeg',569),
	('Theresa','Russell',570),
	('Gregory','Walcott',571),
	('Mona','McKinnon',572),
	('Tor','Johnson',573),
	('Edward D.','Wood Jr.',574),
	('Vampira','',575),
	('Bela','Lugosi',576),
	('Amel','Mohamed',592),
	('Toni','Trujillo-Vian',593),
	('Kelly','Hawk',594),
	('Ben','Haller',595),
	('Patrice','Gautier',596),
	('Umarani','Varadarajan',597),
	('Michael','Rawdon',598),
	('Tom','Naughton',599),
	('Kelly','Toshach',600),
	('Clif','Liu',601),
	('Daryl','Lee',602),
	('Melissa','Turner',603),
	('Justin','Gareau',604),
	('Ray','Kiddy',605),
	('Michael','Leong',606),
	('Andreas','Wendker',607),
	('Michael','Kaye',608),
	('Ron','Lue-Sang',609),
	('R.D.','Willhoite',610),
	('Miguel','Sanchez',611),
	('J.P.','Garcia',612),
	('Kory','Hansen',613),
	('Greg','Wilson',614),
	('Ben','Trumbull',615),
	('Francois','Jouaux',616),
	('Andy','Belk',617),
	('Chuck','Fleming',618),
	('Stan','Jirman',619),
	('Stephane','Lunati',620),
	('Cyndie','Homuth',621),
	('Steve','Miner',622),
	('Bruce','Arthur',623),
	('Rory','Lydon',624),
	('Eric','Noyau',625),
	('Benóit','Régent',626),
	('Krzysztof','Kieslowski',627),
	('Brian','Kusler',628);


INSERT INTO talentphoto (PHOTO,TALENT_ID)
VALUES
	(NULL,1),
	(NULL,2),
	(NULL,3),
	(NULL,86),
	(NULL,87),
	(NULL,88),
	(NULL,90),
	(NULL,91),
	(NULL,92),
	(NULL,93),
	(NULL,99),
	(NULL,100),
	(NULL,101),
	(NULL,102),
	(NULL,103),
	(NULL,107),
	(NULL,109),
	(NULL,110),
	(NULL,111),
	(NULL,112),
	(NULL,113),
	(NULL,114),
	(NULL,115),
	(NULL,117),
	(NULL,118),
	(NULL,119),
	(NULL,120),
	(NULL,121),
	(NULL,122),
	(NULL,123),
	(NULL,124),
	(NULL,125),
	(NULL,126),
	(NULL,127),
	(NULL,137),
	(NULL,138),
	(NULL,139),
	(NULL,140),
	(NULL,141),
	(NULL,142),
	(NULL,143),
	(NULL,144),
	(NULL,145),
	(NULL,146),
	(NULL,147),
	(NULL,148),
	(NULL,149),
	(NULL,150),
	(NULL,151),
	(NULL,152),
	(NULL,153),
	(NULL,154),
	(NULL,155),
	(NULL,156),
	(NULL,157),
	(NULL,158),
	(NULL,161),
	(NULL,162),
	(NULL,163),
	(NULL,164),
	(NULL,165),
	(NULL,166),
	(NULL,167),
	(NULL,168),
	(NULL,169),
	(NULL,170),
	(NULL,171),
	(NULL,172),
	(NULL,173),
	(NULL,174),
	(NULL,175),
	(NULL,176),
	(NULL,177),
	(NULL,178),
	(NULL,179),
	(NULL,180),
	(NULL,181),
	(NULL,182),
	(NULL,183),
	(NULL,184),
	(NULL,185),
	(NULL,186),
	(NULL,187),
	(NULL,188),
	(NULL,189),
	(NULL,190),
	(NULL,192),
	(NULL,193),
	(NULL,194),
	(NULL,195),
	(NULL,196),
	(NULL,197),
	(NULL,198),
	(NULL,199),
	(NULL,200),
	(NULL,201),
	(NULL,202),
	(NULL,203),
	(NULL,204),
	(NULL,205),
	(NULL,206),
	(NULL,207),
	(NULL,208),
	(NULL,209),
	(NULL,210),
	(NULL,211),
	(NULL,212),
	(NULL,213),
	(NULL,214),
	(NULL,215),
	(NULL,216),
	(NULL,217),
	(NULL,218),
	(NULL,219),
	(NULL,220),
	(NULL,221),
	(NULL,222),
	(NULL,223),
	(NULL,224),
	(NULL,225),
	(NULL,226),
	(NULL,227),
	(NULL,228),
	(NULL,229),
	(NULL,230),
	(NULL,231),
	(NULL,232),
	(NULL,233),
	(NULL,234),
	(NULL,235),
	(NULL,236),
	(NULL,237),
	(NULL,238),
	(NULL,239),
	(NULL,240),
	(NULL,241),
	(NULL,245),
	(NULL,246),
	(NULL,247),
	(NULL,248),
	(NULL,249),
	(NULL,250),
	(NULL,251),
	(NULL,252),
	(NULL,253),
	(NULL,254),
	(NULL,255),
	(NULL,256),
	(NULL,257),
	(NULL,258),
	(NULL,259),
	(NULL,260),
	(NULL,271),
	(NULL,272),
	(NULL,273),
	(NULL,274),
	(NULL,275),
	(NULL,276),
	(NULL,277),
	(NULL,278),
	(NULL,279),
	(NULL,280),
	(NULL,281),
	(NULL,282),
	(NULL,283),
	(NULL,284),
	(NULL,285),
	(NULL,286),
	(NULL,287),
	(NULL,288),
	(NULL,289),
	(NULL,290),
	(NULL,291),
	(NULL,292),
	(NULL,293),
	(NULL,295),
	(NULL,296),
	(NULL,297),
	(NULL,298),
	(NULL,299),
	(NULL,300),
	(NULL,301),
	(NULL,302),
	(NULL,303),
	(NULL,304),
	(NULL,305),
	(NULL,306),
	(NULL,307),
	(NULL,308),
	(NULL,309),
	(NULL,310),
	(NULL,311),
	(NULL,312),
	(NULL,313),
	(NULL,314),
	(NULL,315),
	(NULL,316),
	(NULL,317),
	(NULL,318),
	(NULL,319),
	(NULL,320),
	(NULL,321),
	(NULL,322),
	(NULL,323),
	(NULL,324),
	(NULL,325),
	(NULL,326),
	(NULL,327),
	(NULL,328),
	(NULL,329),
	(NULL,330),
	(NULL,331),
	(NULL,332),
	(NULL,333),
	(NULL,334),
	(NULL,335),
	(NULL,336),
	(NULL,337),
	(NULL,338),
	(NULL,339),
	(NULL,340),
	(NULL,341),
	(NULL,342),
	(NULL,343),
	(NULL,344),
	(NULL,345),
	(NULL,346),
	(NULL,347),
	(NULL,348),
	(NULL,349),
	(NULL,350),
	(NULL,351),
	(NULL,352),
	(NULL,353),
	(NULL,354),
	(NULL,355),
	(NULL,356),
	(NULL,357),
	(NULL,358),
	(NULL,359),
	(NULL,360),
	(NULL,361),
	(NULL,362),
	(NULL,363),
	(NULL,364),
	(NULL,365),
	(NULL,366),
	(NULL,377),
	(NULL,378),
	(NULL,379),
	(NULL,380),
	(NULL,381),
	(NULL,382),
	(NULL,383),
	(NULL,384),
	(NULL,385),
	(NULL,386),
	(NULL,387),
	(NULL,388),
	(NULL,389),
	(NULL,390),
	(NULL,391),
	(NULL,392),
	(NULL,393),
	(NULL,394),
	(NULL,395),
	(NULL,396),
	(NULL,397),
	(NULL,398),
	(NULL,399),
	(NULL,400),
	(NULL,401),
	(NULL,402),
	(NULL,403),
	(NULL,404),
	(NULL,405),
	(NULL,406),
	(NULL,407),
	(NULL,408),
	(NULL,409),
	(NULL,410),
	(NULL,411),
	(NULL,412),
	(NULL,413),
	(NULL,414),
	(NULL,415),
	(NULL,416),
	(NULL,417),
	(NULL,418),
	(NULL,419),
	(NULL,420),
	(NULL,421),
	(NULL,422),
	(NULL,425),
	(NULL,426),
	(NULL,427),
	(NULL,428),
	(NULL,429),
	(NULL,430),
	(NULL,431),
	(NULL,432),
	(NULL,433),
	(NULL,434),
	(NULL,435),
	(NULL,436),
	(NULL,437),
	(NULL,438),
	(NULL,447),
	(NULL,448),
	(NULL,449),
	(NULL,450),
	(NULL,451),
	(NULL,452),
	(NULL,453),
	(NULL,454),
	(NULL,455),
	(NULL,456),
	(NULL,457),
	(NULL,458),
	(NULL,462),
	(NULL,463),
	(NULL,464),
	(NULL,465),
	(NULL,466),
	(NULL,467),
	(NULL,468),
	(NULL,469),
	(NULL,470),
	(NULL,471),
	(NULL,472),
	(NULL,473),
	(NULL,474),
	(NULL,475),
	(NULL,476),
	(NULL,477),
	(NULL,478),
	(NULL,479),
	(NULL,480),
	(NULL,481),
	(NULL,482),
	(NULL,483),
	(NULL,484),
	(NULL,485),
	(NULL,486),
	(NULL,487),
	(NULL,488),
	(NULL,489),
	(NULL,490),
	(NULL,491),
	(NULL,492),
	(NULL,493),
	(NULL,494),
	(NULL,495),
	(NULL,496),
	(NULL,497),
	(NULL,498),
	(NULL,499),
	(NULL,500),
	(NULL,501),
	(NULL,502),
	(NULL,503),
	(NULL,504),
	(NULL,505),
	(NULL,506),
	(NULL,507),
	(NULL,508),
	(NULL,509),
	(NULL,510),
	(NULL,511),
	(NULL,512),
	(NULL,513),
	(NULL,514),
	(NULL,515),
	(NULL,516),
	(NULL,517),
	(NULL,518),
	(NULL,519),
	(NULL,520),
	(NULL,521),
	(NULL,522),
	(NULL,523),
	(NULL,524),
	(NULL,525),
	(NULL,526),
	(NULL,527),
	(NULL,528),
	(NULL,529),
	(NULL,530),
	(NULL,531),
	(NULL,532),
	(NULL,533),
	(NULL,534),
	(NULL,535),
	(NULL,536),
	(NULL,537),
	(NULL,538),
	(NULL,539),
	(NULL,540),
	(NULL,541),
	(NULL,542),
	(NULL,543),
	(NULL,549),
	(NULL,550),
	(NULL,551),
	(NULL,552),
	(NULL,553),
	(NULL,554),
	(NULL,555),
	(NULL,556),
	(NULL,557),
	(NULL,558),
	(NULL,559),
	(NULL,560),
	(NULL,561),
	(NULL,562),
	(NULL,563),
	(NULL,564),
	(NULL,565),
	(NULL,566),
	(NULL,567),
	(NULL,569),
	(NULL,570),
	(NULL,571),
	(NULL,572),
	(NULL,573),
	(NULL,574),
	(NULL,575),
	(NULL,576),
	(NULL,591),
	(NULL,592),
	(NULL,593),
	(NULL,594),
	(NULL,595),
	(NULL,596),
	(NULL,597),
	(NULL,598),
	(NULL,599),
	(NULL,600),
	(NULL,601),
	(NULL,602),
	(NULL,603),
	(NULL,604),
	(NULL,605),
	(NULL,606),
	(NULL,607),
	(NULL,608),
	(NULL,609),
	(NULL,610),
	(NULL,611),
	(NULL,612),
	(NULL,613),
	(NULL,614),
	(NULL,615),
	(NULL,616),
	(NULL,617),
	(NULL,618),
	(NULL,619),
	(NULL,620),
	(NULL,621),
	(NULL,622),
	(NULL,623),
	(NULL,624),
	(NULL,625),
	(NULL,626),
	(NULL,627),
	(NULL,628);


INSERT INTO `voting` (`MOVIE_ID`,`NUMBER_OF_VOTES`,`RUNNING_AVERAGE`)
VALUES
	(101,24,4.74278),
	(102,24,4.78974),
	(103,25,4.8367),
	(111,27,5.21236),
	(113,27,5.30628),
	(114,27,5.35324),
	(115,28,5.4002),
	(118,28,5.54107),
	(119,28,5.58803),
	(120,29,5.63499),
	(121,29,5.68194),
	(124,0,5.82282),
	(125,0,5.86978),
	(126,0,5.91674),
	(127,0,5.96369),
	(128,1,6.01065),
	(129,1,6.05761),
	(130,1,6.10457),
	(131,1,6.15153),
	(132,1,6.19848),
	(133,2,6.24544),
	(135,2,6.33936),
	(136,2,6.38632),
	(137,3,6.43328),
	(138,3,6.48023),
	(140,3,6.57415),
	(141,4,6.62111),
	(142,4,6.66807),
	(143,4,6.71502),
	(144,4,6.76198),
	(145,5,6.80894),
	(146,5,6.8559),
	(147,5,6.90286),
	(148,5,6.94982),
	(149,5,6.99677),
	(150,6,7.04373),
	(151,6,7.09069),
	(152,6,7.13765),
	(153,6,7.18461),
	(154,7,7.23156),
	(155,7,7.27852),
	(157,7,7.37244),
	(158,8,7.4194),
	(159,8,7.46636),
	(160,8,7.51331),
	(162,9,7.60723),
	(164,9,7.70115),
	(165,9,7.74811),
	(166,9,7.79506),
	(169,40,7.93594),
	(170,40,7.9829),
	(171,41,8.02985),
	(172,41,8.07681),
	(174,41,8.17073),
	(175,42,8.21769),
	(176,42,8.26465),
	(177,42,8.3116),
	(178,42,8.35856),
	(179,43,8.40552),
	(180,43,8.45248),
	(181,43,8.49944),
	(182,43,8.5464),
	(183,43,8.59335),
	(184,44,8.64031),
	(185,44,8.68727),
	(186,44,8.73423),
	(187,44,8.78119),
	(188,45,8.82815),
	(189,45,8.8751),
	(190,45,8.92206),
	(191,45,8.96902),
	(192,46,9.01598),
	(193,46,9.06294),
	(194,46,9.10989),
	(196,47,9.20381),
	(197,47,9.25077),
	(198,47,9.29773),
	(199,47,9.34468),
	(200,47,9.39164),
	(202,48,9.48556),
	(203,48,9.53252),
	(205,49,9.62643),
	(206,NULL,NULL),
	(207,NULL,NULL),
	(210,NULL,NULL),
	(211,NULL,NULL),
	(214,NULL,NULL),
	(215,NULL,NULL),
	(216,NULL,NULL);