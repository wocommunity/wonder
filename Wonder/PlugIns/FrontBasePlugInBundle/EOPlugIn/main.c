
/* 

  You must have this file in your project even though there's apparently nothing useful in it.

  This makes the bundle build correctly so that your Java PlugIn can loaded in EOModeler.

  This bundle should link against Cocoa.framework and JDBCEOAdaptor.framework.
  
  There should be one java file for each Bridged JDBC PlugIn that you want to make available in EOModeler for the Bridged JDBC Adaptor (JDBCEOAdaptor.framework).
  
  The created bundle should be named something like MyBundle.EOMplugin.  It must be installed in /Developer/EOMBundles/.  You can use a symlink if you want to keep the bundle somewhere else on your disk.
  
  Note that these Bridged JDBCPlugIns are only used by EOModeler.  The WebObjects 5 runtime needs a pure Java versions of the JDBCPlugIn.  The source code is similar but the binary is not the same.
  
  */