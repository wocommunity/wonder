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

import er.woinstaller.archiver.XarFile;
import er.woinstaller.io.BlockEntry;
import er.woinstaller.io.FileUtilities;
import er.woinstaller.io.MultiBlockInputStream;
import er.woinstaller.ui.ConsoleProgressMonitor;

public abstract class WebObjectsRuntime {
  private static URL WO533_URL;
  private static URL WO533DEV_URL;
  private static URL WO543_URL;
  private static URL WO543DEV_URL;
  private static URL WO55DEV_URL;
  
  static {
    try {
      WO533_URL = new URL("http://supportdownload.apple.com/download.info.apple.com/Apple_Support_Area/Apple_Software_Updates/Mac_OS_X/downloads/061-2998.20070215.33woU/WebObjects5.3.3Update.dmg");
      WO543_URL = new URL("http://download.info.apple.com/Mac_OS_X/061-4634.20080915.3ijd0/WebObjects543.dmg");
      WO533DEV_URL = new File("WebObjects533.dmg").toURL();
      WO543DEV_URL = new File("WebObjects543.dmg").toURL();
      WO55DEV_URL = new File("webobjectsjavadeveloper10a286.dmg").toURL();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  public static WebObjectsRuntime newRuntime(String version) {
    WebObjectsRuntime runtime;
    if ("5.3.3".equals(version)) {
      runtime = wo533Runtime();
    } 
    else if ("5.4.3".equals(version)) {
      runtime = wo543Runtime();
    }
    else if ("5.5".equals(version)) {
      runtime = wo55Runtime();
    }
    else if ("dev53".equals(version)) {
      runtime = wo533Runtime();
      runtime.woDmgUrl = WO533DEV_URL;
    }
    else if ("dev54".equals(version)) {
      runtime = wo543Runtime();
      runtime.woDmgUrl = WO543DEV_URL;
    }
    else {
      throw new IllegalArgumentException("Unknown WebObjects version '" + version + "'.");
    }
    return runtime;
  }
  
  private static WebObjectsRuntime wo533Runtime() {
    return new WebObjectsRuntime() {
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
  
  private static WebObjectsRuntime wo543Runtime() {
    return new WebObjectsRuntime() {
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

  private static WebObjectsRuntime wo55Runtime() {
    return new WebObjectsRuntime() {
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
  
  public InputStream getInputStream() throws IOException {
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
      FileUtilities.writeUrlToFile(woDmgUrl, woDmgFile, new ConsoleProgressMonitor("Downloading WebObjects"));
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
  
  public long getLength() {
    return rawLength;
  }
  
}
