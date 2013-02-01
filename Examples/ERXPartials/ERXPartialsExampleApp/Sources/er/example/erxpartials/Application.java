package er.example.erxpartials;

import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.navigation.ERXNavigationManager;

public class Application extends ERXApplication {
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        NSLog.out.appendln("Welcome to " + name() + " !");
        /* ** put your initialization code in here ** */
    }
    
	@Override
	public void finishInitialization()
	{
		super.finishInitialization();

		// Setup main navigation
		ERXNavigationManager.manager().configureNavigation();

	}
}
