//
// Application.java
// Project OdaikoJavaMailTests
//
// Created by camille on Thu Jul 04 2002
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.ERXApplication;

public class Application extends ERXApplication {


    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        super();
    }
}
