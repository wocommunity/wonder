This example requires ERMoviesLogic. Either configure the movies database for your database platform of choice
and configure ERJasperReportsExample Properties with your connection dictionary, or use MySQL
and the quickstart SQL script below which works with existing Properties file

__________________________________________________________________________________

QuickStart for MySQL
-------------------------

Script will not run if movies database already exists

Open a terminal, cd into this directory (which has the movies.sql script) and type:

$ mysql < movies.sql

Launch ERJasperReportsExample making sure you have Wonder frameworks available.


__________________________________________________________________________________

Note the script above creates a user named developer with password 'passw0rd' having access to movies database only
Properties are already configured to access this movies database on localhost

