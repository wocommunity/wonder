package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import er.attachment.utils.ERMimeType;

/**
 * ImageIOImageProcessor is an implementation of the IImageProcessor interface on top of Mac OS X's 
 * ImageIO and CoreImage libraries.  This is, by far, the fastest implementation of image processing provided.
 * To use this implementation, you must build the native ImageIOImageProcessor.jnilib provided in the 
 * Native/MacOSX/ImageIOImageProcessor xcode project and place the library in your JNI library path (for 
 * example, /Library/Java/Extensions).
 * 
 * @author mschrag
 */
public class ImageIOImageProcessor extends ERImageProcessor {
  static {
    System.loadLibrary("ImageIOImageProcessor");
  }

  public void processImage(int resizeWidth, int resizeHeight, Quality resizeQuality, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, File watermarkFile, boolean tileWatermark, float compressionQuality, File colorProfileFile, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException {
    String watermarkPath = null;
    if (watermarkFile != null) {
      watermarkPath = watermarkFile.getAbsolutePath();
    }
    String colorProfilePath = null;
    if (colorProfileFile != null) {
      colorProfilePath = colorProfileFile.getAbsolutePath();
    }
    if (!processImage2(resizeWidth, resizeHeight, dpi, sharpenRadius, sharpenIntensity, gamma, cropX, cropY, cropWidth, cropHeight, watermarkPath, tileWatermark, compressionQuality, colorProfilePath, inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), outputMimeType == null ? null : outputMimeType.uti())) {
      throw new IOException("Failed to process image '" + inputFile + "' into '" + outputFile + "'.");
    }
  }

  private native boolean processImage(int resizeWidth, int resizeHeight, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, float compressionQuality, String profilePath, String inputFile, String outputFile, String uti);

  private native boolean processImage2(int resizeWidth, int resizeHeight, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, String watermarkFile, boolean tileWatermark, float compressionQuality, String profilePath, String inputFile, String outputFile, String uti);

  @Override
  public String toString() {
    return "[ImageIOJNIProcessor]";
  }

  public static IERImageProcessor imageIOImageProcessor() {
    IERImageProcessor imageProcessor = null;
    if ("Mac OS X".equals(System.getProperty("os.name"))) {
      imageProcessor = new ImageIOImageProcessor();
    }
    else {
      throw new UnsupportedOperationException("Cannot use ImageIOProcessor because you're not on OS X.");
    }
    return imageProcessor;
  }
}
