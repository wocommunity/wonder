package er.woinstaller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import er.woinstaller.archiver.CPIO;
import er.woinstaller.archiver.XarFile;
import er.woinstaller.io.BlockEntry;
import er.woinstaller.io.MultiBlockInputStream;
import er.woinstaller.ui.IWOInstallerProgressMonitor;

public abstract class WebObjectsInstaller {
  private static URI WO533_URI;
  private static URI WO533DEV_URI;
  private static URI WO543_URI;
  private static URI WO543DEV_URI;
  
  static {
    try {
      WO533_URI = new URI("https://download.info.apple.com/Apple_Support_Area/Apple_Software_Updates/Mac_OS_X/downloads/061-2998.20070215.33woU/WebObjects5.3.3Update.dmg");
      WO543_URI = new URI("https://download.info.apple.com/Apple_Support_Area/Apple_Software_Updates/Mac_OS_X/downloads/061-4634.20080915.3ijd0/WebObjects543.dmg");
      WO533DEV_URI = new File("WebObjects533.dmg").toURI();
      WO543DEV_URI = new File("WebObjects543.dmg").toURI();
    } catch (URISyntaxException e) {
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
    else if ("dev53".equals(version)) {
      installer = wo533Installer();
      installer.woDmgUri = WO533DEV_URI;
    }
    else if ("dev54".equals(version)) {
      installer = wo543Installer();
      installer.woDmgUri = WO543DEV_URI;
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
        woDmgUri = WO533_URI;
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
        woDmgUri = WO543_URI;
      } 
    };
  }

  protected List<BlockEntry> blockList = new ArrayList<BlockEntry>();
  protected long fileLength;
  protected long rawLength;
  protected URI woDmgUri;
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
    if ("file".equals(woDmgUri.getScheme())) {
      woDmgFile = new File(woDmgUri);
      if (!woDmgFile.exists()) {
        throw new IllegalStateException("The file " + woDmgFile.getName() + " was not found");
      }
    }
    InputStream woDmgIs = new BufferedInputStream(woDmgFile == null ? woDmgUri.toURL().openStream() : new FileInputStream(woDmgFile));

    InputStream woPaxGZIs;
    if (woVersion == 53) {
      woPaxGZIs = new MultiBlockInputStream(woDmgIs, blockList);
    } else {
      //woVersion >= 54
      InputStream woPkgIs = new MultiBlockInputStream(woDmgIs, blockList);
      XarFile xarfile = new XarFile(woPkgIs);
      woPaxGZIs = xarfile.getInputStream("Payload");
    }
    return new GZIPInputStream(woPaxGZIs);
  }
  
  protected long getLength() {
    return rawLength;
  }
  
}
