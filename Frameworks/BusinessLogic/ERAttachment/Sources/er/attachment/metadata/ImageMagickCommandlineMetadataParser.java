package er.attachment.metadata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.lf5.util.StreamUtils;

import er.extensions.foundation.ERXProperties;

/**
 *
 * @property er.attachment.ImageProcessor.imageMagickBinFolder
 */
public class ImageMagickCommandlineMetadataParser implements IERMetadataParser {
  public static final int BUFFER_SIZE = 8192;

  private File _imageMagickBinary;

  public ImageMagickCommandlineMetadataParser(File imageMagickBinary) {
    _imageMagickBinary = imageMagickBinary;
  }

  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException {
    try {
      ERMetadataDirectorySet metadataDirectorySet = new ERMetadataDirectorySet();
      byte[] exifBytes = extractMetadata(importFile, "exif");
      if (exifBytes != null && exifBytes.length > 0) {
        metadataDirectorySet.addMetadata(new ERUnparsedMetadataDirectory(importFile, IERMetadataDirectory.EXIF, exifBytes));
      }
      byte[] iptcBytes = extractMetadata(importFile, "iptc");
      if (iptcBytes != null && iptcBytes.length > 0) {
        metadataDirectorySet.addMetadata(new ERUnparsedMetadataDirectory(importFile, IERMetadataDirectory.IPTC, iptcBytes));
      }
      return metadataDirectorySet;
    }
    catch (IOException e) {
      throw new ERMetadataParserException("Failed to parse metadata.", e);
    }
  }

  private byte[] extractMetadata(File importFile, String type) throws IOException, ERMetadataParserException {
    List<String> imageMagickCommandList = new LinkedList<>();
    imageMagickCommandList.add(_imageMagickBinary.getAbsolutePath());

    imageMagickCommandList.add(importFile.getAbsolutePath());
    File metadataFile = File.createTempFile("Metadata", type);
    imageMagickCommandList.add(metadataFile.getAbsolutePath());

    String[] imageMagickCommands = imageMagickCommandList.toArray(new String[imageMagickCommandList.size()]);
    Process process = Runtime.getRuntime().exec(imageMagickCommands);
    try {
      int returnValue = process.waitFor();
      if (returnValue == 1) {
        // 1 = No metadata
      }
      else if (returnValue != 0) {
        throw new ERMetadataParserException("ImageMagick failed with return value " + returnValue);
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
      throw new ERMetadataParserException("ImageMagick was interrupted.");
    }

    byte[] metadataBytes;
    if (metadataFile.exists() && metadataFile.length() > 0) {
      try (FileInputStream fis = new FileInputStream(metadataFile); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        StreamUtils.copy(fis, baos, ImageMagickCommandlineMetadataParser.BUFFER_SIZE);
        metadataBytes = baos.toByteArray();
      }
      metadataFile.delete();
    }
    else {
      metadataBytes = null;
    }

    return metadataBytes;
  }

  /**
   * Returns an ImageMagick metadata parser.
   * 
   * @return an ImageMagick image processor
   * @throws IOException if an ImageMagick cannot be created
   */
  public static IERMetadataParser imageMagickMetadataParser() throws IOException {
    IERMetadataParser metadataParser = null;
    String imageMagickBinFolder = ERXProperties.stringForKey("er.attachment.ImageProcessor.imageMagickBinFolder");
    if (imageMagickBinFolder != null) {
      File imageMagickConvertFile = new File(imageMagickBinFolder, "convert");
      if (imageMagickConvertFile.exists()) {
        metadataParser = new ImageMagickCommandlineMetadataParser(imageMagickConvertFile);
      }
      else {
        throw new IllegalArgumentException("Cannot use ImageMagick because either " + imageMagickConvertFile + " does not exist.");
      }
    }
    else {
      throw new IllegalArgumentException("Cannot use ImageMagick because you have not set 'er.attachment.ImageProcessor.imageMagickBinFolder'.");
    }
    return metadataParser;
  }
}
