package er.divalite.example;

import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        NSLog.out.appendln("Welcome to " + this.name() + " !");
        /* ** put your initialization code in here ** */
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
    }
}
