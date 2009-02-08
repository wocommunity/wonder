package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import com.webobjects.foundation.NSArray;

import er.attachment.utils.ERMimeType;

public class ERImageProcessorTest {

  public static void main(String[] args) throws IOException {
    IERImageProcessor processor = ERImageProcessor.imageProcessor();

    File inputFile = new File("Tests/a.png");
    if (!inputFile.exists()) {
      throw new IOException("No " + inputFile.getAbsolutePath() + ".");
    }
    File outputFile = new File("/tmp/out.png");
    outputFile.delete();

    File watermarkFile = new File("Tests/w.jpg");
    //File watermarkFile = null;
    boolean tileWatermark = false;
    File colorProfileFile = new File("Resources/sRGB.icc");
    processor.processImage(150, 150, null, -1, 2.5f, 0.35f, 0.0f, -1, -1, -1, -1, watermarkFile, tileWatermark, 0.9f, colorProfileFile, inputFile, outputFile, new ERMimeType("jpeg", "image/jpg", "public.jpeg", new NSArray<String>(new String[] { "jpg" })));
    Runtime.getRuntime().exec(new String[] { "open", outputFile.getAbsolutePath() });
  }

}
