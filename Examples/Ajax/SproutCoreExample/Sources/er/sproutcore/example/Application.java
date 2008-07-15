package er.sproutcore.example;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXPatcher;
import er.sproutcore.example.components.sample.Collections;

public class Application extends ERXApplication {
    
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}
	
	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		ERXPatcher.setClassForName(Collections.class, "Collections");
	}
}
