DynaReporter: March 1999.



Installation



Make sure that: $(NEXT_ROOT)\Developer\Libraries is in your PATH. It wasn't in mine for some reason.



Do a make install on DRGrouping framework, then the WRReporting framework, then the DynaReporterJava JavaWrapper project.



Now you should be able to build the examples. There are Java and Obj-C/Script versions located in the Examples folder.



The first example you should run after installation is the DocsByExample application. It is a multi-framed app that shows functionality in action and describes how the kit works.



Note that the examples use the BusinessLogicJava.framework which should already be installed by default. One part of the DocsByExample app shows how to use a feature of Oracle databases called GROUP BY. To see that feature work, you will have to have a version of the Movies database in Oracle. However, You can still see the docs on the feature and read the simple source to get a feel how it works even if you don't see it run.



d











