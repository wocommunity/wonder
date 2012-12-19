package er.woinstaller;

import java.io.File;

import er.woinstaller.ui.ConsoleProgressMonitor;

public class Main {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      showUsage();
    }

    showLicense();
    WebObjectsInstaller.newInstaller(args[0]).installToFolder(new File(args[1]), new ConsoleProgressMonitor());
    
    System.out.println("Installation Complete");
  }

  private static void showUsage() {
    System.out.println("usage: java -jar WOInstaller.jar [5.3.3|5.4.3] [destinationFolder]");
    
    System.out.println("\nExample:");
    System.out.println("WO 5.4.3 on Windows");
    System.out.println("       java -jar WOInstaller.jar 5.4.3 C:\\Apple");
    
    System.out.println();
    System.out.println("WO 5.3.3 on OS X (in alternate folder)");
    System.out.println("       java -jar WOInstaller.jar 5.3.3 /opt");
    System.exit(1);
  }
  
  private static void showLicense() {
    /* Include an extract of the WO portion of the 5.4.3 license agreement.
     * Not ideal as 5.3.3 is slightly different, but it is better than nothing. */
    System.out.println("WebObjects License Agreement extract:\n\n" + 
        "Subject to the terms and conditions of this License, you may incorporate the\n" +
        "WebObjects Software included in the Developer Software into application\n" +
        "programs (both client and server) that you develop on an Apple-branded\n" +
        "computer. You may also reproduce and distribute the WebObjects Software\n" +
        "unmodified, in binary form only, on any platform but solely as incorporated\n" +
        "into such application programs and only for use by end-users under terms that\n" +
        "are at least as restrictive of those set forth in this License (including,\n" +
        "without limitation, Sections 2, 6 and 7 of this License).\n\n" +
        "For avoidance of doubt, you may not distribute the WebObjects Software on a\n" +
        "stand-alone basis, and you may not develop application programs using the\n" +
        "WebObjects Software (or any portion thereof) on any non-Apple branded\n" +
        "computer.\n\n");
  }
}
