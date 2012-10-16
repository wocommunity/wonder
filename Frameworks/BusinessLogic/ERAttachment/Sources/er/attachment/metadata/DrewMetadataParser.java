package er.attachment.metadata;

import java.io.File;
import java.io.IOException;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

public class DrewMetadataParser implements IERMetadataParser {
  public static final String UNKNOWN_TAG = "Unknown tag";

  public DrewMetadataParser() {
  }

  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException {
    try {
      ERMetadataDirectorySet directorySet = new ERMetadataDirectorySet();
      Metadata metadata = JpegMetadataReader.readMetadata(importFile);
      for (Directory directory : metadata.getDirectories()) {
        ERParsedMetadataDirectory parsedMetadataDirectory = new ERParsedMetadataDirectory(directory.getName());
        DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(parsedMetadataDirectory, directory);
        directorySet.addMetadata(parsedMetadataDirectory);
      }
      return directorySet;
    }
    catch (JpegProcessingException e) {
      throw new ERMetadataParserException("Failed to parse metadata.", e);
    } catch (IOException e) {
      throw new ERMetadataParserException("Failed to read metadata from file " + importFile.getName() + ".", e);
	}
  }

  public static void fillInParsedMetadataDirectoryFromDrewMetadata(ERParsedMetadataDirectory parsedMetadataDirectory, Directory directory) {
    String directoryName = directory.getName();
    boolean isEXIF = IERMetadataDirectory.EXIF.equalsIgnoreCase(directoryName);
    for (Tag tag : directory.getTags()) {
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
