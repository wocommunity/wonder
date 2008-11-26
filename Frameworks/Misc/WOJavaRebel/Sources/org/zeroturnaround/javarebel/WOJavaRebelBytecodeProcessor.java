package org.zeroturnaround.javarebel;

import java.util.Collection;

import org.zeroturnaround.bundled.javassist.ClassPool;
import org.zeroturnaround.bundled.javassist.CtClass;
import org.zeroturnaround.bundled.javassist.CtMethod;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * WOJavaRebelBytecodeProcessor injects support for WOLips projects into
 * com.webobjects._ideservices._WOProject if support doesn't already exist
 * 
 * @author q
 *
 */
public class WOJavaRebelBytecodeProcessor extends JavassistClassBytecodeProcessor {
	public static final String IDEPATCH_CLASS = "com.webobjects._ideservices._WOProject";
	public static final String IDESUPPORT_CLASS = "com.webobjects._ideservices._IDEProjectWOLips";
	public static final String IDEPATCH_METHODNAME = "ideProjectAtPath";
	public static final String IDEPATCH_SIGNATURE = "(Ljava/lang/String;)Lcom/webobjects/_ideservices/_IDEProject;";
	public static final String IDEPATCH_CODE = 
		"Object obj = com.webobjects._ideservices._IDEProjectWOLips.wolipsProjectAtPath($1);" +
		"if (obj != null) {" +
		"  if (com.webobjects.foundation.NSLog.debugLoggingAllowedForLevelAndGroups(2, 32L)) {" +
		"    com.webobjects.foundation.NSLog.debug.appendln(\"*****Found WOLips project at \" + $1);" +
		"  }" +
		"  return obj;" +
		"}";
	
	@Override
	@SuppressWarnings("unchecked")
	public void process(ClassPool classpool, ClassLoader classloader, CtClass ctClass) throws Exception {
		Collection<String> classes = ctClass.getRefClasses();
		if (!classes.contains(IDESUPPORT_CLASS)) {
			CtMethod m = ctClass.getMethod(IDEPATCH_METHODNAME, IDEPATCH_SIGNATURE);
			m.insertBefore(IDEPATCH_CODE);
		}
	}
}
