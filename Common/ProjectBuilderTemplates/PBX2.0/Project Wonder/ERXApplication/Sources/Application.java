//
// Application.java
// Project ÇPROJECTNAMEÈ
//
// Created by ÇUSERNAMEÈ on ÇDATEÈ
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;

public class Application extends ERXApplication {
    
    public static final ERXLogger log = ERXLogger.getERXLogger(Application.class);

    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        super();
        log.info("Welcome to " + this.name() + "!");
    }
    
    /**
     * This method will be called automatically when the application 
     * posts the notification <code>ApplicationDidFinishLaunching</code>. 
     * You can put your application initialization code here.
     */
    public void didFinishLaunching() {
        log.debug("didFinishLaunching called.");
        ERXBrowserFactory.factory().setBrowserClassName("Browser");
        //ERXMessageEncoding.setDefaultEncodingForAllLanguages("UTF8");
        //ERXMessageEncoding.setDefaultEncodingForAllLanguages("SJIS");
    }

}
