package er.attachment.metadata;

import java.util.Iterator;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;

public class DrewMetadataDirectoryParser implements IERMetadataDirectoryParser {
  public ERParsedMetadataDirectory parseMetadata(ERUnparsedMetadataDirectory unparsedMetadata) {
    ERParsedMetadataDirectory parsedMetadataDirectory = null;
    
    Metadata metadata = new Metadata();
    byte[] data = unparsedMetadata.getMetadata();
    String directoryName = unparsedMetadata.getDirectoryName();
    if (directoryName.equalsIgnoreCase(IERMetadataDirectory.EXIF)) {
      new ExifReader(data).extract(metadata);
      parsedMetadataDirectory = new ERParsedMetadataDirectory(directoryName);
    }
    else if (directoryName.equalsIgnoreCase(IERMetadataDirectory.IPTC)) {
      new IptcReader(data).extract(metadata);
      parsedMetadataDirectory = new ERParsedMetadataDirectory(directoryName);
    }
    
    if (parsedMetadataDirectory != null) {
      Iterator directories = metadata.getDirectoryIterator();
      while (directories.hasNext()) {
        Directory directory = (Directory) directories.next();
        DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(parsedMetadataDirectory, directory);
      }
    }
    
    return parsedMetadataDirectory;
  }
}
