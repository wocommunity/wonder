
ERMovies - a simple ER-D2W WO application that uses the Movies database, access via JDBC to a Derby database.

I have put the two jar files that Derby needs in the Libraries directory, here in the project. I do know if this is what
everyone will want, but I do not see better at the moment.

The database is created the first time one launches the app, in the executable's home directory. It should not get
deleted after that. I do not think the code in Application.java is the best way to do this. I am open to suggestions.

TODO:

1) some smart prototypes for Derby need to be added. Right now, I set the model to use EOJDBCPrototypes and everything
seems to work fine. But then I have not added the binary data, such as the pictures. And I have not exercised very much
of the database. It is just ERMovies. More sophisticated examples can be done later.

2) use of the Migration stuff for installing and re-installing the database. This stuff certainly looks cool and if
someone else wants to set it up as a "best use" example, that would be great. I can do so when I learn more about it.

ray@ganymede.org
