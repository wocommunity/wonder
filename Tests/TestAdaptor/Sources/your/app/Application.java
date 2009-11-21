package your.app;

import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXApplication;
import er.woadaptor.ERWOAdaptor;

public class Application extends ERXApplication {
    
    public static void main(String[] argv) {
        System.setProperty(WOProperties._AdaptorKey, ERWOAdaptor.class.getName());
        System.setProperty(WOProperties._HostKey, "localhost");
        System.setProperty(WOProperties._PortKey, "8080");
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        NSLog.out.appendln("Welcome to " + name() + " !");
    }
}
