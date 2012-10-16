package er.attachment.metadata;

import com.drew.lang.ByteArrayReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;

public class DrewMetadataDirectoryParser implements IERMetadataDirectoryParser {
  public ERParsedMetadataDirectory parseMetadata(ERUnparsedMetadataDirectory unparsedMetadata) {
    ERParsedMetadataDirectory parsedMetadataDirectory = null;
    
    Metadata metadata = new Metadata();
    ByteArrayReader reader = new ByteArrayReader(unparsedMetadata.getMetadata());
    String directoryName = unparsedMetadata.getDirectoryName();
    if (directoryName.equalsIgnoreCase(IERMetadataDirectory.EXIF)) {
      new ExifReader().extract(reader, metadata);
      parsedMetadataDirectory = new ERParsedMetadataDirectory(directoryName);
    }
    else if (directoryName.equalsIgnoreCase(IERMetadataDirectory.IPTC)) {
      new IptcReader().extract(reader, metadata);
      parsedMetadataDirectory = new ERParsedMetadataDirectory(directoryName);
    }
    
    if (parsedMetadataDirectory != null) {
      for (Directory directory : metadata.getDirectories()) {
        DrewMetadataParser.fillInParsedMetadataDirectoryFromDrewMetadata(parsedMetadataDirectory, directory);
      }
    }
    
    return parsedMetadataDirectory;
  }
}
