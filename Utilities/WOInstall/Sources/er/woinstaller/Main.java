package er.woinstaller;

import java.io.File;
import java.io.IOException;

import er.woinstaller.archiver.CPIO;
import er.woinstaller.ui.ConsoleProgressMonitor;

public class Main {
  private static final String osName = System.getProperty("os.name");
  
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      showUsage();
    }

    showLicense();
    
    File destinationFolder = new File(args[1]);
    if (destinationFolder.exists()) {
      if (!destinationFolder.canWrite()) {
        throw new IOException("You do not have permission to write to the folder '" + destinationFolder + "'.");
      }
    }
    else if (!destinationFolder.mkdirs()) {
      throw new IOException("Failed to create the directory '" + destinationFolder + "'.");
    }

    WebObjectsRuntime runtime = WebObjectsRuntime.newRuntime(args[0]);
    CPIO cpio = new CPIO(runtime.getInputStream());
    cpio.setLength(runtime.getLength());
    cpio.extractTo(destinationFolder, !isWindows(), new ConsoleProgressMonitor("Extracting WebObjects Runtime"));    	
    
    if (!isOSX()) {    
      shuffleFolders(destinationFolder);
    }
    
    System.out.println("Installation Complete");
  }
  
  private static boolean isOSX() {
    return osName.toLowerCase().indexOf("os x") != -1;
  }
  
  private static boolean isWindows() {
    return osName.toLowerCase().indexOf("windows") != -1;
  }

  private static void shuffleFolders(File destinationFolder) throws IOException {
    System.out.print("Shuffling file structure for " + osName + ": ");
    File localFolder = new File(destinationFolder, "Local");
    if (localFolder.exists()) {
      throw new IOException("The folder '" + localFolder + "' already exists.");
    }
    if (!localFolder.mkdirs()) {
      throw new IOException("Failed to create the directory '" + localFolder + "'.");
    }

    File localLibraryFolder = new File(localFolder, "Library");

    File originalLibraryFolder = new File(destinationFolder, "Library");
    if (!originalLibraryFolder.renameTo(localLibraryFolder)) {
      throw new IOException("Failed to move '" + originalLibraryFolder + "' to '" + localLibraryFolder + "'.");
    }

    File originalSystemFolder = new File(destinationFolder, "System");

    File originalSystemLibraryFolder = new File(originalSystemFolder, "Library");
    File libraryFolder = new File(destinationFolder, "Library");
    if (libraryFolder.exists()) {
      throw new IOException("The folder '" + libraryFolder + "' already exists.");
    }
    if (!originalSystemLibraryFolder.renameTo(libraryFolder)) {
      throw new IOException("Failed to move '" + originalSystemLibraryFolder + "' to '" + libraryFolder + "'.");
    }

    if (!originalSystemFolder.delete()) {
      throw new IOException("Failed to delete '" + originalSystemFolder + ".");
    }
    System.out.println("Done");
  }

  private static void showUsage() {
    System.out.println("usage: java -jar WOInstaller.jar [5.3.3|5.4.3|5.5] [destinationFolder]");
    
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
        "without limitation, Sections 2, 6 and 7 of this License [1]).\n\n" +
        "For avoidance of doubt, you may not distribute the WebObjects Software on a\n" +
        "stand-alone basis, and you may not develop application programs using the\n" +
        "WebObjects Software (or any portion thereof) on any non-Apple branded\n" +
        "computer.\n\n" +
        "To view the full license agreement see the following document:\n" +
        "[1] http://images.apple.com/legal/sla/docs/xcode.pdf\n");
  }
}
