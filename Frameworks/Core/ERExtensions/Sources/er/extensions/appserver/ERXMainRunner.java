package er.extensions.appserver;

import java.lang.reflect.Method;

import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation._NSUtilities;

/**
 * ERXMainRunner makes it a little easier to run a main method 
 * that requires a full application context.  You can setup an
 * Eclipse run profile for the WOApplication "ERXMainRunner"
 * with the command line parameter:
 * <p>
 * -mainClass MyMainClass
 * <p>
 * or
 * <p>
 * -mainClass MyMainClass -mainMethod main2
 * <p>
 * And it will run an application, call the main method you
 * passed in, and then System.exit. Provide an empty mainClass string if you 
 * handle everything in your startup.
 *  
 * @author mschrag (inspired by Anjo :) )
 */
public class ERXMainRunner extends ERXApplication {
	public static String[] _args;

	public static void main(String[] args) {
		_args = args;
		ERXApplication.main(args, ERXMainRunner.class);
	}

	public ERXMainRunner() {
		setAutoOpenInBrowser(false);
	}
	
	@Override
	public void didFinishLaunching() {
		super.didFinishLaunching();
		try {
			String mainClassName = null;
			String mainMethodName = "main";
			NSMutableArray<String> argsArray = new NSMutableArray<>();
			if (_args != null) {
				for (int i = 0; i < _args.length; i++) {
					if ("-mainClass".equalsIgnoreCase(_args[i])) {
						mainClassName = _args[++i];
					}
					else if ("-mainMethod".equalsIgnoreCase(_args[i])) {
						mainMethodName = _args[++i];
					}
					else {
						argsArray.addObject(_args[i]);
					}
				}
			}
			if (mainClassName == null) {
				throw new RuntimeException("You must pass in -mainClass <classname>");
			}
			if(mainClassName.length() != 0) {
				Class mainClass = _NSUtilities.classWithName(mainClassName);
				Method mainMethod = mainClass.getMethod(mainMethodName, new Class[] { String[].class });
				String[] args = argsArray.toArray(new String[argsArray.count()]);
				mainMethod.invoke(null, new Object[] { args });
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			System.exit(0);
		}
	}
}
