package er.woinstaller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import er.woinstaller.archiver.CPIO;
import er.woinstaller.archiver.XarFile;
import er.woinstaller.io.BlockEntry;
import er.woinstaller.io.FileUtilities;
import er.woinstaller.io.MultiBlockInputStream;
import er.woinstaller.ui.IWOInstallerProgressMonitor;

public abstract class WebObjectsInstaller {
  private static URL WO533_URL;
  private static URL WO533DEV_URL;
  private static URL WO543_URL;
  private static URL WO543DEV_URL;
  private static URL WO55DEV_URL;
  
  static {
    try {
      WO533_URL = new URL("http://supportdownload.apple.com/download.info.apple.com/Apple_Support_Area/Apple_Software_Updates/Mac_OS_X/downloads/061-2998.20070215.33woU/WebObjects5.3.3Update.dmg");
      WO543_URL = new URL("http://supportdownload.apple.com/download.info.apple.com/Apple_Support_Area/Apple_Software_Updates/Mac_OS_X/downloads/061-4634.20080915.3ijd0/WebObjects543.dmg");
      WO533DEV_URL = new File("WebObjects533.dmg").toURL();
      WO543DEV_URL = new File("WebObjects543.dmg").toURL();
      WO55DEV_URL = new File("webobjectsjavadeveloper10a286.dmg").toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  public static WebObjectsInstaller newInstaller(String version) {
    WebObjectsInstaller installer;
    if ("5.3.3".equals(version)) {
      installer = wo533Installer();
    } 
    else if ("5.4.3".equals(version)) {
      installer = wo543Installer();
    }
    else if ("5.5".equals(version)) {
      installer = wo55Installer();
    }
    else if ("dev53".equals(version)) {
      installer = wo533Installer();
      installer.woDmgUrl = WO533DEV_URL;
    }
    else if ("dev54".equals(version)) {
      installer = wo543Installer();
      installer.woDmgUrl = WO543DEV_URL;
    }
    else {
      throw new IllegalArgumentException("Unknown WebObjects version '" + version + "'.");
    }
    return installer;
  }
  
  private static WebObjectsInstaller wo533Installer() {
    return new WebObjectsInstaller() {
      {
        BlockEntry entry = new BlockEntry();
        entry.offset = 11608064L;
        entry.length = 29672581L;
        blockList.add(entry);
        rawLength = 51252394L;
        woVersion = 53;
        fileLength = 42321716L;
        woDmgUrl = WO533_URL;
      } 
    };
  }
  
  private static WebObjectsInstaller wo543Installer() {
    return new WebObjectsInstaller() {
      {
        BlockEntry entry = new BlockEntry();
        entry.offset = 58556928L;
        entry.length = 107601091L;
        blockList.add(entry);
        rawLength = 153786259L;
        woVersion = 54;
        fileLength = 166167249L;
        woDmgUrl = WO543_URL;
      } 
    };
  }

  private static WebObjectsInstaller wo55Installer() {
    return new WebObjectsInstaller() {
      {
        BlockEntry entry = new BlockEntry();
        entry.offset = 70230528L;
        entry.length = 2097152L;
        blockList.add(entry);
        entry = new BlockEntry();
        entry.offset = 120369664L;
        entry.length = 129819571L;
        blockList.add(entry);
        rawLength = 173276160L;
        woVersion = 55;
        fileLength = 320623432L;
        woDmgUrl = WO55DEV_URL;
      } 
    };
  }

  protected List<BlockEntry> blockList = new ArrayList<BlockEntry>();
  protected long fileLength;
  protected long rawLength;
  protected URL woDmgUrl;
  protected int woVersion;
  
  public WebObjectsInstallation installToFolder(File destinationFolder, IWOInstallerProgressMonitor progressMonitor) throws IOException, InterruptedException {
    if (destinationFolder.exists()) {
      if (!destinationFolder.canWrite()) {
        throw new IOException("You do not have permission to write to the folder '" + destinationFolder + "'.");
      }
    }
    else if (!destinationFolder.mkdirs()) {
      throw new IOException("Failed to create the directory '" + destinationFolder + "'.");
    }

    CPIO cpio = new CPIO(getInputStream(progressMonitor));
    cpio.setLength(getLength());
    cpio.extractTo(destinationFolder, !WebObjectsInstallation.isWindows(), progressMonitor);     
    
    WebObjectsInstallation installation = new WebObjectsInstallation(destinationFolder);
    progressMonitor.done();
    return installation;
  }

  protected InputStream getInputStream(IWOInstallerProgressMonitor progressMonitor) throws IOException {
    File woDmgFile = null;
    if ("file".equals(woDmgUrl.getProtocol())) {
      try {
        woDmgFile = new File(woDmgUrl.toURI());
        if (!woDmgFile.exists()) {
          throw new IllegalStateException("The file " + woDmgFile.getName() + " was not found");
        }
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
    if (woDmgFile == null || !woDmgFile.exists() || woDmgFile.length() != fileLength) {
      woDmgFile = File.createTempFile("WebObjects.", ".dmg");
      woDmgFile.deleteOnExit();
      progressMonitor.beginTask("Downloading WebObjects ...", (int)fileLength);
      FileUtilities.writeUrlToFile(woDmgUrl, woDmgFile, progressMonitor);
    }

    InputStream woPaxGZIs;
    if (woVersion == 53) {
      woPaxGZIs = new MultiBlockInputStream(new BufferedInputStream(new FileInputStream(woDmgFile)), blockList);
    } else {
      //woVersion >= 54
      InputStream woPkgIs = new MultiBlockInputStream(new BufferedInputStream(new FileInputStream(woDmgFile)), blockList);
      XarFile xarfile = new XarFile(woPkgIs);
      woPaxGZIs = xarfile.getInputStream("Payload");
    }
    return new GZIPInputStream(woPaxGZIs);
  }
  
  protected long getLength() {
    return rawLength;
  }
  
}
