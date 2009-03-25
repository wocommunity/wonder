package er.attachment.metadata;

import java.io.File;
import java.io.IOException;

public class ERMetadataParserTest {

  public static void test(ERMetadataParser parser, String inputFileName) throws IOException, ERMetadataParserException {
    File inputFile = new File("Tests/" + inputFileName);
    if (!inputFile.exists()) {
      throw new IOException("No " + inputFile.getAbsolutePath() + ".");
    }
    
    ERMetadataDirectorySet metadata = parser.parseMetadata(inputFile);
    System.out.println("ERMetadataParserTest.test: " + metadata);
  }

  public static void main(String[] args) throws IOException, ERMetadataParserException {
    ERMetadataParser parser = ERMetadataParser.metadataParser();
    test(parser, "a.png");
    test(parser, "a.jpg");
  }

}
