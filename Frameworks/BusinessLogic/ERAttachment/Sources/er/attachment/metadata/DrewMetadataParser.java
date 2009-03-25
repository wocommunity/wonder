package er.attachment.metadata;

import java.io.File;
import java.util.Iterator;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.webobjects.foundation.NSLog;

public class DrewMetadataParser implements IERMetadataParser {
  public static final String UNKNOWN_TAG = "Unknown tag";

  public DrewMetadataParser() {
  }

  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException {
    try {
      ERMetadataDirectorySet directorySet = new ERMetadataDirectorySet();
      Metadata metadata = JpegMetadataReader.readMetadata(importFile);
      Iterator directoryIter = metadata.getDirectoryIterator();
      while (directoryIter.hasNext()) {
        Directory directory = (Directory) directoryIter.next();
        ERParsedMetadataDirectory parsedMetadataDirectory = new ERParsedMetadataDirectory(directory.getName());
        DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(parsedMetadataDirectory, directory);
        directorySet.addMetadata(parsedMetadataDirectory);
      }
      return directorySet;
    }
    catch (JpegProcessingException e) {
      throw new ERMetadataParserException("Failed to parse metadata.", e);
    }
  }

  public static void fillInParsedMetadataDirectoryFromDrewMetadata(ERParsedMetadataDirectory parsedMetadataDirectory, Directory directory) {
    String directoryName = directory.getName();
    boolean isEXIF = IERMetadataDirectory.EXIF.equalsIgnoreCase(directoryName);
    Iterator tags = directory.getTagIterator();
    while (tags.hasNext()) {
      Tag tag = (Tag) tags.next();
      try {
        String tagName = tag.getTagName();
        String tagValue = tag.getDescription();
        int tagType = tag.getTagType();
        // System.out.println("ImageMagickJNIFilter.importMetadata: " + directoryName + ", " + tagName + "=" + tagValue);
        // 0x927C = Maker Note, which is a crazy binary block, but purports to be a String
        boolean isMakerNote = tagType == 0x927C && isEXIF;
        boolean isUnknown = tagName.startsWith(DrewMetadataParser.UNKNOWN_TAG);
        if (!isUnknown && !isMakerNote) {
          parsedMetadataDirectory.addMetadataEntry(new ERMetadataEntry(tagType, tagName, tagValue, ERMetadataUtils.classForTagName(directoryName, tagName)));
        }
        else {
          // NSLog.out.appendln("Skipped " + tagType + ": " + tagValue);
        }
      }
      catch (MetadataException t) {
        NSLog.out.appendln(t);
      }
    }
    /*
     if (directory.hasErrors()) {
     Iterator errors = directory.getErrors();
     while (errors.hasNext()) {
     NSLog.out.appendln("Error: " + errors.next());
     }
     }
     */
  }
}
