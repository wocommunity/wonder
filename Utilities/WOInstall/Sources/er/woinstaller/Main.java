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
    System.out.println("usage: java -jar WOInstaller.jar [5.3.3|5.4.3] [destinationFolder]");
    
    System.out.println("\nExample:");
    System.out.println("WO 5.4.3 on Windows");
    System.out.println("       java -jar WOInstaller.jar 5.4.3 C:\\Apple");
    
    System.out.println();
    System.out.println("WO 5.3.3 on OS X (in alternate folder)");
    System.out.println("       java -jar WOInstaller.jar 5.3.3 /opt");
    System.exit(1);
  }
}
