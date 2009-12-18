package er.wojrebel;

import org.zeroturnaround.javarebel.ClassResourceSource;
import org.zeroturnaround.javarebel.ConfigurationFactory;
import org.zeroturnaround.javarebel.IntegrationFactory;
import org.zeroturnaround.javarebel.Plugin;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;
import org.zeroturnaround.javarebel.support.PackageClassFilter;


/**
 * JRebel WebObjects Integration Plugin
 * 
 * @author q
 *
 */
public class WOJRebelIntegrationPlugin implements Plugin {
  private static boolean enabled = false;
  
	public void preinit() {
    enabled = true;
	  JavassistClassBytecodeProcessor processor = new WOJRebelBytecodeProcessor();
	  IntegrationFactory.getInstance()
	    .addIntegrationProcessor(WOJRebelBytecodeProcessor.IDEPATCH_CLASS, processor);
	  IntegrationFactory.getInstance()
	    .addIntegrationProcessor(WOJRebelBytecodeProcessor.WORKERTHREAD_CLASS, processor);
		// Reduce performance penalty by excluding some common packages that will not be changing
	  if (System.getProperty("wojrebel.noexclude") == null) {
	    ConfigurationFactory.getInstance()
	    .addExcludeManagedFilter(new PackageClassFilter(new String[]{
	        "com.webobjects", "com.apple", "com.ibm", "org.apache", "javax.xml",
	        "org.w3c", "org.xml", "ognl", "org.zeroturnaround", "er" }));
	  }
	}

  public boolean checkDependencies(ClassLoader arg0, ClassResourceSource arg1) {
    return false;
  }

  public String getDescription() {
    return "WebObjects JRebel Plugin                                       \n" +
    		"If you are reloading changes to Wonder or WebObjects core packages you must set " +
    		"-Dwojrebel.noexclude to prevent these packages from being excluded.";
  }

  public String getId() {
    return "wojrebel";
  }

  public String getName() {
    return "WOJRebel";
  }

  public String getAuthor() {
    return "Quinton Dolan <qdolan@gmail.com>";
  }

  public String getWebsite() {
    return null;
  }
  
  public static boolean isEnabled() {
    return enabled;
  }
}
