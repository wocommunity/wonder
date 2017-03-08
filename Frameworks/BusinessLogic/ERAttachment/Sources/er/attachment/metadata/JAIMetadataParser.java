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

import com.drew.lang.ByteArrayReader;
import com.drew.lang.SequentialByteArrayReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.iptc.IptcReader;

public class JAIMetadataParser implements IERMetadataParser {
  public static int EXIF = 0xE1;
  public static int IPTC = 0xED;

  private static Set<String> UNWANTED = new HashSet<>();

  static {
    JAIMetadataParser.UNWANTED.add("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader");
    JAIMetadataParser.UNWANTED.add("com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageWriter");
  }

  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException {
    try {
      ERMetadataDirectorySet rawAssetMetadata = new ERMetadataDirectorySet();
      try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(importFile)) {
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

              ExifIFD0Directory exifDirectory = getExifDirectory(metadataTree);
              if (exifDirectory != null) {
                ERParsedMetadataDirectory exifMetadataDirectory = new ERParsedMetadataDirectory(IERMetadataDirectory.EXIF);
                DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(exifMetadataDirectory, exifDirectory);
                rawAssetMetadata.addMetadata(exifMetadataDirectory);
              }
            }
          }
        }
      }
      return rawAssetMetadata;
    }
    catch (IOException e) {
      throw new ERMetadataParserException("Failed to parse metadata.", e);
    }
  }

  private ExifIFD0Directory getExifDirectory(Node node) {
    if ("unknown".equals(node.getNodeName())) {
      if (Integer.parseInt(node.getAttributes().getNamedItem("MarkerTag").getNodeValue()) == EXIF) {
    	ByteArrayReader reader = new ByteArrayReader((byte[]) ((IIOMetadataNode) node).getUserObject());
    	Metadata metadata = new Metadata();
    	new ExifReader().extract(reader, metadata);
    	return metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
      }
    }

    Node child = node.getFirstChild();
    while (child != null) {
      ExifIFD0Directory directory = getExifDirectory(child);
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
        byte[] tagBytes = (byte[]) ((IIOMetadataNode) node).getUserObject();
    	Metadata metadata = new Metadata();
    	new IptcReader().extract(new SequentialByteArrayReader(tagBytes), metadata, tagBytes.length);
        return metadata.getFirstDirectoryOfType(IptcDirectory.class);
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
}
