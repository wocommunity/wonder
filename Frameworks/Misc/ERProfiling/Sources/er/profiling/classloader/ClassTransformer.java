package er.profiling.classloader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.gluonj.weave.Weaver;

/**
 * ClassTransformer implements ClassFileTransformer and operates in a factory
 * like capacity to ensure that each classloader has its own transformer with an
 * appropriate classpool to ensure while transforming that classes are resolved
 * using the correct classpath.
 * 
 * @author q
 * Some portions of this code are copied from GluonJ's HotSwapper class
 */
public class ClassTransformer implements ClassFileTransformer {
  private final String glueName;

  private Map<ClassLoader, ClassPool> knownClassLoaders = new HashMap<ClassLoader, ClassPool>();
  private Map<ClassLoader, ClassFileTransformer> transformers = new HashMap<ClassLoader, ClassFileTransformer>();

  public ClassTransformer(String glueName) {
    this.glueName = glueName;
  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
      throws IllegalClassFormatException {

    if (!knownClassLoaders.containsKey(loader)) {
      if (loader.getParent() == null) {
        transformers.put(loader, createTransformer(loader, null));
      } else {
        ClassPool parentPool = knownClassLoaders.get(loader.getParent());
        ClassPool pool = new ClassPool(parentPool);
        knownClassLoaders.put(loader, pool);
        transformers.put(loader, createTransformer(loader, pool));
      }
    }
    return transformers.get(loader).transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
  }

  private ClassFileTransformer createTransformer(ClassLoader loader, ClassPool pool) {
    pool.appendClassPath(new LoaderClassPath(loader));
    pool.childFirstLookup = true;
    return new WeavingClassTransformer(glueName, pool);
  }

  /**
   * The WeavingClassTransformer does the real work of transforming a class. It
   * maintains its own weaver and classpool for resolving classes with the correct
   * classpath.
   * 
   * @author q
   * 
   */
  private static class WeavingClassTransformer implements ClassFileTransformer {
    private String glueName;
    private Weaver weaver;
    private boolean stop;
    private ClassPool classPool;

    public WeavingClassTransformer(String glue, ClassPool cp) {
      glueName = glue;
      weaver = null;
      stop = false;
      classPool = cp;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classfile)
        throws IllegalClassFormatException {
      if (stop)
        return null;

      if (weaver == null) {
        try {
          if (classPool != null) {
            weaver = new Weaver(glueName, classPool, loader);
          } else {
            weaver = new Weaver(glueName, loader, true);
          }

        } catch (Throwable t) {
          t.printStackTrace();
          stop = true;
          showError("cannot read a glue: " + glueName, t);
          return null;
        }
      }

      try {
        return weaver.transform(className, classfile);
      } catch (Throwable t) {
        String msg = "cannot transform a class: " + className.replace('/', '.');
        showError(msg, t);
        return null;
      }
    }

    private void showError(String msg, Throwable e) {
      System.err.println("GluonJ Error: " + msg);
      System.err.println("  by " + e);
      e.printStackTrace(System.err);
    }
  }
}
