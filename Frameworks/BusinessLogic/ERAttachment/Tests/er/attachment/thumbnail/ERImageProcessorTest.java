package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import com.webobjects.foundation.NSArray;

import er.attachment.utils.ERMimeType;

public class ERImageProcessorTest {

  public static void test(IERImageProcessor processor, String inputFileName) throws IOException {
    File inputFile = new File("Tests/" + inputFileName);
    if (!inputFile.exists()) {
      throw new IOException("No " + inputFile.getAbsolutePath() + ".");
    }
    File outputFile = new File("/tmp/out.png");
    outputFile.delete();

    File watermarkFile = new File("Tests/w.jpg");
    //File watermarkFile = null;
    boolean tileWatermark = false;
    File colorProfileFile = new File("Resources/sRGB.icc");
    //processor.processImage(150, 150, null, -1, 2.5f, 0.35f, 0.0f, -1, -1, -1, -1, watermarkFile, tileWatermark, 0.9f, colorProfileFile, inputFile, outputFile, new ERMimeType("jpeg", "image/jpg", "public.jpeg", new NSArray<String>(new String[] { "jpg" })));
    processor.processImage(150, 150, null, -1, 0.0f, 0.0f, 0.0f, -1, -1, -1, -1, null, false, 0.9f, colorProfileFile, inputFile, outputFile, new ERMimeType("jpeg", "image/jpg", "public.jpeg", new NSArray<String>(new String[] { "jpg" })));
    //Runtime.getRuntime().exec(new String[] { "open", outputFile.getAbsolutePath() });
  }

  public static void main(String[] args) throws IOException {
    long a = System.currentTimeMillis();
    for (int i = 0; i < 300; i++) {
      IERImageProcessor processor = new SipsImageProcessor();
      ERImageProcessorTest.test(processor, "a.png");
      if ( i  % 100 == 0) {
        System.out.println("ERImageProcessorTest.main: " + i);
      }
    }
    System.out.println("ERImageProcessorTest.main: " + (System.currentTimeMillis() - a));
//    ERImageProcessorTest.test(new Java2DImageProcessor(), "a.png");
//    ERImageProcessorTest.test(processor, "a.jpg");
//    ERImageProcessorTest.test(new Java2DImageProcessor(), "a.jpg");
  }

}
