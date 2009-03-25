package er.attachment.metadata;

import java.io.File;

public class ImageIOMetadataParser implements IERMetadataParser {
  static {
    System.loadLibrary("ImageIOMetadataParser");
  }

  public ERMetadataDirectorySet parseMetadata(File importFile) {
    //System.out.println("ImageIOMetadataParser.parseMetadata: " + importFile);
    ERMetadataDirectorySet metadataDirectorySet = new ERMetadataDirectorySet();
    parseMetadata0(metadataDirectorySet, importFile.getAbsolutePath());
    //System.out.println();
    return metadataDirectorySet;
  }

  protected void addMetadataEntry(ERMetadataDirectorySet metadataDirectorySet, String directoryName, String name, String value) {
    String internalDirectoryName;
    int type = -1;
    if ("{Exif}".equals(directoryName)) {
      internalDirectoryName = IERMetadataDirectory.EXIF;
      type = ERMetadataUtils.typeForExifTagName(name);
    }
    else if ("{IPTC}".equals(directoryName)) {
      internalDirectoryName = IERMetadataDirectory.IPTC;
      type = ERMetadataUtils.typeForIptcTagName(name);
    }
    else if ("{TIFF}".equals(directoryName)) {
      internalDirectoryName = IERMetadataDirectory.EXIF;
      type = ERMetadataUtils.typeForExifTagName(name);
    }
    else if ("{PDF}".equals(directoryName)) {
      internalDirectoryName = IERMetadataDirectory.PDF;
      type = ERMetadataUtils.typeForPdfTagName(name);
    }
    else {
      internalDirectoryName = directoryName;
      //System.out.println("ImageIOMetadataParser.addMetadataEntry: OTHER " + type + ", " + name + ", " + value);
    }
    ERParsedMetadataDirectory metadataDirectory = metadataDirectorySet.getDirectoryNamed(internalDirectoryName);
    if (metadataDirectory == null) {
      metadataDirectory = new ERParsedMetadataDirectory(internalDirectoryName);
      metadataDirectorySet.addMetadata(metadataDirectory);
    }
    if (type == -1) {
      type = name.hashCode();
    }
    metadataDirectory.addMetadataEntry(new ERMetadataEntry(type, name, value, ERMetadataUtils.classForTagName(internalDirectoryName, name)));
  }

  private native void parseMetadata0(ERMetadataDirectorySet metadataDirectorySet, String filePath);
}
