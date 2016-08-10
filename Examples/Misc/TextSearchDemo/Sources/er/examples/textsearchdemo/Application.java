package er.examples.textsearchdemo;

import er.extensions.appserver.ERXApplication;
import er.indexing.ERIndexing;

public class Application extends ERXApplication {
	
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		/* ** put your initialization code in here ** */
	}
	
	@Override
	public void didFinishLaunching() {
		super.didFinishLaunching();
		ERIndexing.indexing().loadIndexDefinitions();
	}

}
