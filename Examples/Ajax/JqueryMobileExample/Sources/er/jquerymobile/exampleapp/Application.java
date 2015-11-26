package er.jquerymobile.exampleapp;

import com.webobjects.foundation.NSBundle;

import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {

	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		/* ** put your initialization code in here ** */
		setAllowsConcurrentRequestHandling(true);
	}

	@Override
	public void finishInitialization() {
		super.finishInitialization();

		NSBundle bundle = NSBundle.mainBundle();

		String file = bundle.bundlePath() + "/Contents/Resources/H2DB/Movies";
		System.setProperty("Movies.URL", "jdbc:h2:file:" + file);
	}

}
