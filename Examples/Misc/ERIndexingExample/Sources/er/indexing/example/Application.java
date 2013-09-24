package er.indexing.example;

import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {
	
	public static void main(String argv[]) {
		ERXApplication.main(argv, Application.class);
	}

	@Override
	public void finishInitialization() {
	    super.finishInitialization();
        //DataCreator.main(null);
        //ERIndexing.indexing().loadIndexDefinitions();
	}
	
	public Application() {
		NSLog.out.appendln("Welcome to " + name() + " !");
	}
}
