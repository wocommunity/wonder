package er.attachment.metadata;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.Node;

import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.iptc.IptcReader;

public class JAIMetadataParser implements IERMetadataParser {
  public static int EXIF = 0xE1;
  public static int IPTC = 0xED;

  private static Set<String> UNWANTED = new HashSet<String>();

  static {
    JAIMetadataParser.UNWANTED.add("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader");
    JAIMetadataParser.UNWANTED.add("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriter");
  }

  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException {
    try {
      ERMetadataDirectorySet rawAssetMetadata = new ERMetadataDirectorySet();
      ImageInputStream imageInputStream = ImageIO.createImageInputStream(importFile);
      try {
        Iterator imageReadersIter = ImageIO.getImageReaders(imageInputStream);
        while (imageReadersIter.hasNext()) {
          ImageReader imageReader = (ImageReader) imageReadersIter.next();
          imageReader.setInput(imageInputStream);
          rawAssetMetadata.setWidth(imageReader.getWidth(0));
          rawAssetMetadata.setHeight(imageReader.getHeight(0));
          IIOMetadata metadata = imageReader.getImageMetadata(0);
          if (metadata != null) {
            Node metadataTree = metadata.getAsTree(metadata.getNativeMetadataFormatName());
            if (metadataTree != null) {
              IptcDirectory iptcDirectory = getIptcDirectory(metadataTree);
              if (iptcDirectory != null) {
                ERParsedMetadataDirectory iptcMetadataDirectory = new ERParsedMetadataDirectory(IERMetadataDirectory.IPTC);
                DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(iptcMetadataDirectory, iptcDirectory);
                rawAssetMetadata.addMetadata(iptcMetadataDirectory);
              }

              ExifDirectory exifDirectory = getExifDirectory(metadataTree);
              if (exifDirectory != null) {
                ERParsedMetadataDirectory exifMetadataDirectory = new ERParsedMetadataDirectory(IERMetadataDirectory.EXIF);
                DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(exifMetadataDirectory, exifDirectory);
                rawAssetMetadata.addMetadata(exifMetadataDirectory);
              }
            }
          }
        }
      }
      finally {
        imageInputStream.close();
      }
      return rawAssetMetadata;
    }
    catch (IOException e) {
      throw new ERMetadataParserException("Failed to parse metadata.", e);
    }
  }

  private ExifDirectory getExifDirectory(Node node) {
    if ("unknown".equals(node.getNodeName())) {
      if (Integer.parseInt(node.getAttributes().getNamedItem("MarkerTag").getNodeValue()) == EXIF) {
        byte[] data = (byte[]) ((IIOMetadataNode) node).getUserObject();
        return (ExifDirectory) new ExifReader(data).extract().getDirectory(ExifDirectory.class);
      }
    }

    Node child = node.getFirstChild();
    while (child != null) {
      ExifDirectory directory = getExifDirectory(child);
      if (directory != null) {
        return directory;
      }
      child = child.getNextSibling();
    }
    return null;
  }

  private IptcDirectory getIptcDirectory(Node node) {
    if ("unknown".equals(node.getNodeName())) {
      if (Integer.parseInt(node.getAttributes().getNamedItem("MarkerTag").getNodeValue()) == IPTC) {
        byte[] data = (byte[]) ((IIOMetadataNode) node).getUserObject();
        IptcDirectory iptcDirectory = (IptcDirectory) new IptcReader(data).extract().getDirectory(IptcDirectory.class);
        return iptcDirectory;
      }
    }

    Node child = node.getFirstChild();
    while (child != null) {
      IptcDirectory directory = getIptcDirectory(child);
      if (directory != null) {
        return directory;
      }
      child = child.getNextSibling();
    }
    return null;
  }

  private String suffix(File file) {
    String s = file.getName();
    int i = s.lastIndexOf('.');
    return s.substring(i + 1);
  }
}
