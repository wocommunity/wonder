
CayenneModeler (which comes with Cayenne) includes a tool to convert EOModels to Cayenne models: create a new project and then choose Tools -> "Import EOModel". 
I suggest trying that first. In my experience it didn't work well because the prototypes in ERPrototypes were not resolved.

The CayenneConverter class in this framework will allow you to convert an EOModel to a Cayenne model.

To use it just add the framework to your build path and then add this line to you application's constructor (replace MyModel with the name of your model):

new er.cayenne.CayenneConverter().run(EOModelGroup.defaultGroup().modelNamed("MyModel"));

Run your WO app.
This will create a Cayenne DataMap file (called MyModel.map.xml) in the root of your Sources folder. 
To use it you will need to run CayenneModeler and create a new project.
Then give a name to the DataDomain (top-level) node that is created in the new project
Then choose File -> Import DataMap and select the .map.xml file that was generated.

The converter does not copy the connection dictionary from your model - you will need to re-enter that information by creating a DataNode using CayenneModeler.

The converter attempts to convert qualifiers for any fetch specifications you've defined in your model, but this should be considered just a best attempt, not guaranteed to be correct.

