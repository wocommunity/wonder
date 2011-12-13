Running the Test Application
=============================
Running the Tests with ant
--------------------------
Navigate to Wonder root
    cd path/to/Wonder
    ant applications
    ant tests.run

Note: You may need to build applications first. The output will indicate if this is needed.
For details, see Wonder/Build/build/build/xml -> tests.run -> test.app.found

Running the Tests in Eclipse
-----------------------------
This test application is designed to automatically run tests upon launching. Testing is started in `Application.didFinishLaunching()`. To run all tests, simply launch the app in Eclipse and look at the console for errors. When testing is complete, the application will quit. You might see output something like this:


    Setting EOModels to use adaptor "Memory"
    Invoke "ant -Der.erxtest.ERXTestListener=Noisy tests.run" to see verbose output.
    
    ....................................................................................................
    ....................................................................................................
    ....................................................................................................
    ....................................................................................................
    ....................................................................................................
    ....................................................................................................
    ....................................................................................................
    .....x...x....x.x.x.......x......x.x..x.x...................................................................
    
    tests run: 798
    tests FAILED: 10
    time elapsed: 6 sec
    
    --- Test Failures ---
    Failure: test_parse_guess_r(er.chronic.ParserTest$ParserInnerTest): expected:<Thu Aug 17 13:00:00 PDT 2006> but was:<Wed Aug 16 13:00:00 PDT 2006>
    Failure: test_parse_guess_gr(er.chronic.ParserTest$ParserInnerTest): expected:<Wed Aug 16 14:00:00 PDT 2006> but was:<Wed Aug 16 11:00:00 PDT 2006>
    Failure: test_parse_guess_s_r_p(er.chronic.ParserTest$ParserInnerTest): expected:<Sat Aug 16 14:00:00 PDT 2003> but was:<Sat Aug 16 11:00:00 PDT 2003>
    Failure: test_parse_guess_p_s_r(er.chronic.ParserTest$ParserInnerTest): expected:<Wed Aug 16 17:00:00 PDT 2006> but was:<Wed Aug 16 14:00:00 PDT 2006>
    Failure: test_parse_guess_s_r_p_a(er.chronic.ParserTest$ParserInnerTest): expected:<Fri Aug 18 14:00:00 PDT 2006> but was:<Fri Aug 18 11:00:00 PDT 2006>
    Failure: test_parse_only_complete_pointers(er.chronic.ParserTest$ParserInnerTest): expected:<Wed Aug 16 11:00:00 PDT 2006> but was:<Wed Aug 16 14:00:00 PDT 2006>
    Failure: test_parse_before_now(er.chronic.ParserTest$ParserInnerTest): expected:<Wed Aug 16 11:00:00 PDT 2006> but was:<Wed Aug 16 08:00:00 PDT 2006>
    Failure: test_now(er.chronic.ParserTest$ParserInnerTest): expected:<Wed Aug 16 14:00:00 PDT 2006> but was:<Wed Aug 16 11:00:00 PDT 2006>
    Failure: test_hr_and_hrs(er.chronic.ParserTest$ParserInnerTest): expected:<Wed Aug 16 17:00:00 PDT 2006> but was:<Wed Aug 16 14:00:00 PDT 2006>
    Failure: test_fractional_times(er.chronic.ParserTest$ParserInnerTest): expected:<Wed Aug 16 17:30:00 PDT 2006> but was:<Wed Aug 16 14:30:00 PDT 2006>


Configuring MySQL Database
==========================
By default the application uses the Memory database adaptor. You can customize the test environment by adding properties to the file at
    <user.home>/Library/wobuild.properties

For example:
    wo.test.dbAccess.adaptor=MySQL
    wo.test.dbAccess.MySQL.URL=jdbc:mysql://localhost/test?capitalizeTypenames=true&useBundledJdbcInfo=true
    wo.test.dbAccess.MySQL.name=developer
    wo.test.dbAccess.MySQL.password=passw0rd