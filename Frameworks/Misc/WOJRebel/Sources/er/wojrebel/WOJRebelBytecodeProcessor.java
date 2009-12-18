package er.wojrebel;

import java.util.Collection;

import org.zeroturnaround.bundled.javassist.ClassPool;
import org.zeroturnaround.bundled.javassist.CtClass;
import org.zeroturnaround.bundled.javassist.CtMethod;
import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;

/**
 * WOJRebelBytecodeProcessor injects support for WOLips projects into
 * com.webobjects._ideservices._WOProject if support doesn't already exist
 * 
 * @author q
 *
 */
public class WOJRebelBytecodeProcessor extends JavassistClassBytecodeProcessor {
	public static final String IDEPATCH_CLASS = "com.webobjects._ideservices._WOProject";
	private static final String IDESUPPORT_CLASS = "com.webobjects._ideservices._IDEProjectWOLips";
	private static final String IDEPATCH_METHODNAME = "ideProjectAtPath";
	private static final String IDEPATCH_SIGNATURE = "(Ljava/lang/String;)Lcom/webobjects/_ideservices/_IDEProject;";
	private static final String IDEPATCH_CODE = 
		"Object obj = com.webobjects._ideservices._JR_IDEProjectWOLips.wolipsProjectAtPath($1);" +
		"if (obj != null) {" +
		"  if (com.webobjects.foundation.NSLog.debugLoggingAllowedForLevelAndGroups(2, 32L)) {" +
		"    com.webobjects.foundation.NSLog.debug.appendln(\"*****Found WOLips project at \" + $1);" +
		"  }" +
		"  return obj;" +
		"}";
	
	public static final String WORKERTHREAD_CLASS = "com.webobjects.appserver._private.WOWorkerThread";
	private static final String WOJREBEL_SUPPORT = "er.wojrebel.WOJRebelSupport";
	private static final String WORKERPATCH_METHODNAME = "runOnce";
	private static final String WORKERPATCH_SIGNATURE = "()V";
	private static final String WORKERPATCH_CODE = 
	  "try {" +
	    WOJREBEL_SUPPORT + ".run();" +
	  "} catch (Exception e) {" +
	  "  e.printStackTrace();" +
	  "}";
	
	@Override
	@SuppressWarnings("unchecked")
	public void process(ClassPool classpool, ClassLoader classloader, CtClass ctClass) throws Exception {
	  if (IDEPATCH_CLASS.equals(ctClass.getName())) {
	    Collection<String> classes = ctClass.getRefClasses();
	    if (!classes.contains(IDESUPPORT_CLASS)) {
	      CtMethod m = ctClass.getMethod(IDEPATCH_METHODNAME, IDEPATCH_SIGNATURE);
	      m.insertBefore(IDEPATCH_CODE);
	    }
	  }
	  if (WORKERTHREAD_CLASS.equals(ctClass.getName())) {
	    CtMethod m = ctClass.getMethod(WORKERPATCH_METHODNAME, WORKERPATCH_SIGNATURE);
	    m.insertBefore(WORKERPATCH_CODE);
	  }
	}

}
