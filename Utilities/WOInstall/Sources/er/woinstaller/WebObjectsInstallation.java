package er.woinstaller;

import java.io.File;
import java.io.IOException;

public class WebObjectsInstallation {
  private static final String osName = System.getProperty("os.name");

  public static boolean isOSX() {
    return osName.toLowerCase().indexOf("os x") != -1;
  }

  public static boolean isWindows() {
    return osName.toLowerCase().indexOf("windows") != -1;
  }

  private File _destinationFolder;
  private File _localLibrariesFolder;
  private File _systemLibrariesFolder;

  public WebObjectsInstallation(File destinationFolder) throws IOException {
    _destinationFolder = destinationFolder;
    _localLibrariesFolder = new File(_destinationFolder, "Library");
    _systemLibrariesFolder = new File(new File(_destinationFolder, "System"), "Library");
    if (!isOSX()) {
      renameFolders();
    }
  }

  public File getLocalLibrariesFolder() {
    return _localLibrariesFolder;
  }

  public File getSystemLibrariesFolder() {
    return _systemLibrariesFolder;
  }

  /**
   * This method moves the folders which were designed for macOS
   * to the locations required for linux or windows
   * If the destination folder is /opt then
   * /opt/Library becomes /opt/Local/Library
   * /opt/System/Library becomes /opt/Library
   * /opt/System is then deleted
   * @throws IOException if folders already exist or can't move folders or can't delete empty folder /opt/System
   */
  public void renameFolders() throws IOException {
    File localFolder = new File(_destinationFolder, "Local");
    if (localFolder.exists()) {
      throw new IOException("The folder '" + localFolder + "' already exists.");
    }
    if (!localFolder.mkdirs()) {
      throw new IOException("Failed to create the directory '" + localFolder + "'.");
    }

    File localLibraryFolder = new File(localFolder, "Library");
    if (!_localLibrariesFolder.renameTo(localLibraryFolder)) {
      throw new IOException("Failed to move '" + _localLibrariesFolder + "' to '" + localLibraryFolder + "'.");
    }
    _localLibrariesFolder = localLibraryFolder;

    File libraryFolder = new File(_destinationFolder, "Library");
    if (libraryFolder.exists()) {
      throw new IOException("The folder '" + libraryFolder + "' already exists.");
    }
    if (!_systemLibrariesFolder.renameTo(libraryFolder)) {
      throw new IOException("Failed to move '" + _systemLibrariesFolder + "' to '" + libraryFolder + "'.");
    }
    // Delete empty folder /opt/System BEFORE reassigning _systemLibrariesFolder otherwise we will lose reference to it
    if (!_systemLibrariesFolder.getParentFile().delete()) {
      throw new IOException("Failed to delete '" + _systemLibrariesFolder.getParentFile() + ".");
    }
    _systemLibrariesFolder = libraryFolder;

    System.out.println("Done");
  }
}
