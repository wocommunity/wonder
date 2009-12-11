package er.erxtest;

import java.io.File;
import java.util.Properties;

import junit.textui.TestRunner;
import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;

public class Application extends ERXApplication {

	public static void main(String argv[]) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		setAllowsConcurrentRequestHandling(true);
		setAutoOpenInBrowser(false);
	}

	public static Properties wobuild;
	
	static {
		String path = System.getProperty("user.home")+File.separator+"Library"+File.separator+"wobuild.properties";
		try {
			wobuild = ERXProperties.propertiesFromFile(new File(path));
		} catch (java.io.IOException e) {
			System.err.println("Cannot read properties file at \""+path+"\"");
			wobuild = new Properties();
		}
	}

	@Override
	public void didFinishLaunching() {

		super.didFinishLaunching();

		String adaptorName = wobuild.getProperty("wo.test.dbAccess.adaptor");
		if (adaptorName == null) adaptorName = "Memory";
		System.out.println("Setting EOModels to use adaptor \""+adaptorName+"\"");

		ERXTestUtilities.fixModelsForAdaptorNamed(adaptorName);

		TestRunner.run(ERXTestSuite.suite());
		System.exit(0);
	}
}
