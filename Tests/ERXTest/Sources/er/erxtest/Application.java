package er.erxtest;

import java.io.File;
import java.util.Properties;

import junit.textui.TestRunner;

import org.junit.runner.JUnitCore;

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

	protected boolean isLaunchingFromEclipse() {
		String classPath = System.getProperty("java.class.path");
		return classPath != null && classPath.contains("org.eclipse.osgi/bundles");
	}
	
	@Override
	public void didFinishLaunching() {

		super.didFinishLaunching();

		String adaptorName = wobuild.getProperty("wo.test.dbAccess.adaptor");
		if (adaptorName == null) adaptorName = "Memory";
		ERXTestUtilities.fixModelsForAdaptorNamed(adaptorName);
		System.out.println("Setting EOModels to use adaptor \""+adaptorName+"\"");

	    String listener = System.getProperty("er.erxtest.ERXTestListener");
		if (listener == null || listener.compareToIgnoreCase("noisy") != 0)
			System.out.println("Invoke \"ant -Der.erxtest.ERXTestListener=Noisy test\" to see verbose output.");

		if (!isLaunchingFromEclipse()) {
		    JUnitCore core = new JUnitCore();

		    if (listener != null && listener.compareToIgnoreCase("noisy") == 0)
		    	core.addListener(new ERXTestRunNoisyListener());
		    else
		    	core.addListener(new ERXTestRunQuietListener());

		    core.run(ERXTestSuite.suite());
			System.exit(0);
		}
	}
}
