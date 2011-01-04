package er.profiling;

import java.net.URLClassLoader;

import er.profiling.classloader.WeavingClassLoader;


public class PFProfilerBootstrap {
  private static boolean premainHasRun = false;
  
  public static void premain(String[] args) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl instanceof URLClassLoader && !premainHasRun) {
      premainHasRun = true;
      System.setProperty(WeavingClassLoader.GLUE_NAME_KEY, PFProfilerMixin.class.getName());
      URLClassLoader loader = (URLClassLoader) cl;
      ClassLoader newCl = new WeavingClassLoader(loader.getURLs(), loader.getParent());
      Thread.currentThread().setContextClassLoader(newCl);
    }
  }
}
