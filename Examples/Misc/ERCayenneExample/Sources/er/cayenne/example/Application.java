package er.cayenne.example;

import org.apache.cayenne.configuration.server.ServerRuntime;

import er.cayenne.CayenneApplication;
import er.extensions.appserver.ERXApplication;

public class Application extends CayenneApplication {
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		/* ** put your initialization code in here ** */
	}
	
	@Override
	protected ServerRuntime createRuntime() {
		return new ServerRuntime("cayenne-project.xml");
	}
	
}
