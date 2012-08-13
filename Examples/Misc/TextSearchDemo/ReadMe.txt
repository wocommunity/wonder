TextSearchDemo

This is the demo app created for the 'Full Text Search' session at WOWODC 2010.

To run this application you need a recent (as of August 31, 2010) build of ERMoviesLogic.

The location to the main index (for the movies entity) is determined by the 'store' parameter
in the Movies.indexModel.

There are two additional indexes (for the spelling and 'did you mean' demos) that are configured
with properties at the top of the apps Properties file.

The default location for all of the indexes is /Library/Lucene/ if you do not plan on changing this
location please ensure the app has write permissions to that directory.

Using the demos
---------------

The app contains 4 demos:

	Simple Search
	Spell Check
	Did You Mean..
	Auto Suggest

Index Creation (Prime Index)
----------------------------
Each demo has a "Prime Index" button that needs to be clicked the first time the demo is run. Because the 
demos are dependent on one another, prime the indexes in the following order:

	Simple Search
	Spell Check
	Did You Mean...
	
Simple Search
-------------
Demonstrates how to create an index using the ERIndexing framework and create a query using Lucene's
QueryParser object.

Spell Check
-----------
Demonstrates how to create and use a spell check dictionary using Lucene's SpellChecker object and
a text file of common english words.

Did You Mean...
---------------
Demonstrates how to use Lucene's SpellChecker object to use another index (in this case the movies index)
as its source instead of a text file.

Auto Suggest
------------
Demonstrates how to obtain a list of the tokens in the index to use as auto complete suggestions.


