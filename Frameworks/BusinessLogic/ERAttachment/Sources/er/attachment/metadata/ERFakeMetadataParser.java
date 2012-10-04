package er.attachment.metadata;

import java.io.File;

public class ERFakeMetadataParser implements IERMetadataParser {
  public ERMetadataDirectorySet parseMetadata(File importFile) throws ERMetadataParserException {
    ERMetadataDirectorySet metadataDirectorySet = new ERMetadataDirectorySet();
    metadataDirectorySet.setWidth(500);
    metadataDirectorySet.setHeight(500);
    metadataDirectorySet.setCaption("This is a test of the caption This is a test of the caption This is a test of the caption This is a test of the caption This is a test of the caption This is a test of the caption ");
    ERParsedMetadataDirectory metadata = new ERParsedMetadataDirectory(IERMetadataDirectory.EXIF);
    for (int i = 0; i < 30; i++) {
      int length = 100;
      StringBuilder sb = new StringBuilder(length);
      for (int j = 0; j < length; j++) {
        sb.append('a');
      }
      metadata.addMetadataEntry(new ERMetadataEntry(i, "faketagname" + i, sb.toString(), String.class));
    }
    metadataDirectorySet.addMetadata(metadata);
    return metadataDirectorySet;
  }
}
