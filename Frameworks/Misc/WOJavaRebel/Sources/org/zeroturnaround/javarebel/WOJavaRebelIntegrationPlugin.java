package org.zeroturnaround.javarebel;

import org.zeroturnaround.javarebel.IntegrationFactory;
import org.zeroturnaround.javarebel.Plugin;
import org.zeroturnaround.javarebel.support.PackageClassFilter;

/**
 * JavaRebel WebObjects Integration Plugin
 * 
 * @author q
 *
 */
public class WOJavaRebelIntegrationPlugin implements Plugin {
	public void preinit() {
	    IntegrationFactory.getInstance()
	      .addIntegrationProcessor(WOJavaRebelBytecodeProcessor.IDEPATCH_CLASS, new WOJavaRebelBytecodeProcessor());
		// Reduce performance penalty by excluding packages that will not be changing
		ConfigurationFactory.getInstance()
			.addExcludeManagedFilter(new PackageClassFilter(new String[]{
				"com.webobjects", "com.apple", "org.apache", "javax.xml",
				"org.w3c", "org.xml", "ognl", "org.zeroturnaround" }));
	}

  public boolean checkDependencies(ClassLoader arg0, ClassResourceSource arg1) {
    return false;
  }
}
