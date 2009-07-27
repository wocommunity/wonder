package org.zeroturnaround.javarebel;

import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;
import org.zeroturnaround.javarebel.support.PackageClassFilter;


/**
 * JavaRebel WebObjects Integration Plugin
 * 
 * @author q
 *
 */
public class WOJavaRebelIntegrationPlugin implements Plugin {
	public void preinit() {
	  JavassistClassBytecodeProcessor processor = new WOJavaRebelBytecodeProcessor();
	  IntegrationFactory.getInstance()
	    .addIntegrationProcessor(WOJavaRebelBytecodeProcessor.IDEPATCH_CLASS, processor);
	  IntegrationFactory.getInstance()
	    .addIntegrationProcessor(WOJavaRebelBytecodeProcessor.WORKERTHREAD_CLASS, processor);
		// Reduce performance penalty by excluding some common packages that will not be changing
	  if (System.getProperty("wojavarebel.noexclude") == null) {
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
    return "WebObjects JavaRebel Plugin                                       \n" +
    		"If you are reloading changes to Wonder or WebObjects core packages you must set " +
    		"-Dwojavarebel.noexclude to prevent these packages from being excluded.";
  }

  public String getId() {
    return "wojavarebel";
  }

  public String getName() {
    return "WOJavaRebel";
  }

  public String getAuthor() {
    return "Quinton Dolan <qdolan@gmail.com>";
  }

  public String getWebsite() {
    return null;
  }
}
