package org.zeroturnaround.javarebel.webobjects;

import org.zeroturnaround.javarebel.ClassResourceSource;
import org.zeroturnaround.javarebel.ConfigurationFactory;
import org.zeroturnaround.javarebel.IntegrationFactory;
import org.zeroturnaround.javarebel.LoggerFactory;
import org.zeroturnaround.javarebel.Plugin;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;
import org.zeroturnaround.javarebel.support.PackageClassFilter;



/**
 * JRebel WebObjects Integration Plugin
 * 
 * @property wojrebel.noexclude
 *
 * @author qdolan
 */
public class WebObjectsPlugin implements Plugin {
  public static final String JREBEL_EVENT = "JRebelReloadEvent";
  private static boolean enabled = false;
  
	public void preinit() {
    enabled = true;
    LoggerFactory.getInstance().echo("Intitializing WOJRebel plugin");
	  JavassistClassBytecodeProcessor processor = new WebObjectsCBP();
	  IntegrationFactory.getInstance()
	    .addIntegrationProcessor(WebObjectsCBP.IDEPATCH_CLASS, processor);
	  IntegrationFactory.getInstance()
	    .addIntegrationProcessor(WebObjectsCBP.WORKERTHREAD_CLASS, processor);
		// Reduce performance penalty by excluding some common packages that will not be changing
	  if (System.getProperty("wojrebel.noexclude") == null) {
      LoggerFactory.getInstance().echo("  If you are reloading changes to Wonder or WebObjects core packages you must set \n" +
      "  -Dwojrebel.noexclude to prevent these packages from being automatically excluded.");
	    ConfigurationFactory.getInstance()
	    .addExcludeManagedFilter(new PackageClassFilter(new String[]{
	        "com.webobjects", "com.apple", "com.ibm", "org.apache", "javax.xml",
	        "org.w3c", "org.xml", "ognl", "org.zeroturnaround", "er" }));
	  }
	}

  public boolean checkDependencies(ClassLoader cl, ClassResourceSource crs) {
    return crs.getClassResource("com.webobjects._ideservices._JR_IDEProjectWOLips") != null 
              && crs.getClassResource("com.webobjects.appserver.WOApplication") != null;
  }

  public String getDescription() {
    return "WebObjects JRebel Plugin";

  }

  public String getId() {
    return "wojrebel";
  }

  public String getName() {
    return "WebObjects Plugin";
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
