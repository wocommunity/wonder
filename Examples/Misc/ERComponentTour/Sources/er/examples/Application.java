package er.examples;

import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXDictionaryUtilities;

public class Application extends ERXApplication {

	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
        setPageRefreshOnBacktrackEnabled(true);
	}

    public NSDictionary<?,?> data() {
    	NSDictionary<?,?> dict = ERXDictionaryUtilities.dictionaryFromPropertyList("ExcelStyles", NSBundle.mainBundle());
    	return dict;
    }
}
