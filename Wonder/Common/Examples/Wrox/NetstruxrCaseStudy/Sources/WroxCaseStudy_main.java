
import java.lang.*;
import java.util.*;
import java.io.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

public class WroxCaseStudy_main  {
    /*
    Copyright (c) 1995-1997, Apple Enterprise Software, Inc.  All rights reserved.
*/
    static public int main(int argc, char argv) {
        // WOApplicationMain() will process the arguments to this application and
        // create an instance of the application. The application instance is then sent
        // the init message, followed by the run message, which puts the application
        // into its run loop, listening for requests.
        // The content of this function is documented in WOApplication.h.
        // The first argument to WOApplicationMain() is the name of the principal class
        // for the application. If you have written a custom subclass (e.g. MyApplication)
        // and you wish to use that class as your Application class, then replace the
        // string in the function call below with the new name (@"MyApplication").
        return this.WOApplicationMain("Application", argc, argv);
    }


}