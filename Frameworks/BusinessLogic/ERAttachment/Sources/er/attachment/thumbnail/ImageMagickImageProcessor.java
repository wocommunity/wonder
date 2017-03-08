package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import er.attachment.utils.ERMimeType;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXRuntimeUtilities.Result;

/**
 * ImageMagickImageProcessor is an implementation of the IImageProcessor interface on
 * top of the ImageMagick commandline tool. You must provide your own installation 
 * of ImageMagick, specifically the "convert" and "composite" binaries (along with
 * any dependencies they require).  
 * 
 * @property er.attachment.ImageProcessor.imageMagickBinFolder
 *
 * @author mschrag
 */
public class ImageMagickImageProcessor extends ERImageProcessor {
  private File _imageMagickConvertBinary;
  private File _imageMagickCompositeBinary;
  private long _maxMemory;

  /**
   * Constructs a new ImageMagickImageProcessor using a max memory setting of 128M.
   * 
   * @param imageMagickConvertBinary the "convert" binary path
   * @param imageMagickCompositeBinary the "composite" binary path
   */
  public ImageMagickImageProcessor(File imageMagickConvertBinary, File imageMagickCompositeBinary) {
    this(imageMagickConvertBinary, imageMagickCompositeBinary, 128000000L);
  }

  /**
   * Constructs a new ImageMagickImageProcessor.
   * 
   * @param imageMagickConvertBinary the "convert" binary path
   * @param imageMagickCompositeBinary the "composite" binary path
   * @param maxMemory the memory limit in bytes
   */
  public ImageMagickImageProcessor(File imageMagickConvertBinary, File imageMagickCompositeBinary, long maxMemory) {
    _imageMagickConvertBinary = imageMagickConvertBinary;
    _imageMagickCompositeBinary = imageMagickCompositeBinary;
    _maxMemory = maxMemory;
  }

  public void processImage(int resizeWidth, int resizeHeight, Quality resizeQuality, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, File watermarkFile, boolean tileWatermark, float compressionQuality, File colorProfileFile, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException {
    if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
      throw new IOException("Failed to create folder '" + outputFile.getParentFile() + "'.");
    }

    List<String> imageMagickCommandList = new LinkedList<>();
    imageMagickCommandList.add(_imageMagickConvertBinary.getAbsolutePath());
    imageMagickCommandList.add("-limit");
    imageMagickCommandList.add("Memory");
    imageMagickCommandList.add(String.valueOf(_maxMemory));

    String resizeString = null;
    if (resizeWidth != -1 && resizeHeight != -1) {
      resizeString = resizeWidth + "x" + resizeHeight;
    }
    else if (resizeWidth != -1) {
      resizeString = resizeWidth + "x";
    }
    else if (resizeHeight != -1) {
      resizeString = "x" + resizeHeight;
    }
    if (resizeString != null) {
      imageMagickCommandList.add("-size");
      imageMagickCommandList.add(resizeString);
      if (resizeQuality == Quality.Low) {
        imageMagickCommandList.add("-sample");
      }
      else if (resizeQuality == Quality.Medium) {
        imageMagickCommandList.add("-scale");
      }
      else {
        imageMagickCommandList.add("-resize");
      }
      imageMagickCommandList.add(resizeString);
    }

    if (dpi > 0.0) {
      imageMagickCommandList.add("-density");
      imageMagickCommandList.add(dpi + "x" + dpi);
    }

    if (cropWidth > 0 || cropHeight > 0) {
      imageMagickCommandList.add("-crop");
      imageMagickCommandList.add(cropWidth + "x" + cropHeight + "+" + cropX + "+" + cropY);
    }

    if (colorProfileFile != null) {
      imageMagickCommandList.add("-profile");
      imageMagickCommandList.add(colorProfileFile.getAbsolutePath());
    }

    if (sharpenRadius > 0.0) {
      imageMagickCommandList.add("-unsharp");
      // BUG: This should be more configurable
      imageMagickCommandList.add(sharpenRadius + "x1+" + sharpenIntensity + "+6");
    }

    if (gamma > 0.0) {
      imageMagickCommandList.add("-gamma");
      imageMagickCommandList.add(String.valueOf(gamma));
    }

    if (compressionQuality > 0.0) {
      imageMagickCommandList.add("-quality");
      imageMagickCommandList.add(String.valueOf((int) (compressionQuality * 100)));
    }

    imageMagickCommandList.add(inputFile.getAbsolutePath() + "[0]");
    imageMagickCommandList.add(outputFile.getAbsolutePath());

    String[] imageMagickCommands = imageMagickCommandList.toArray(new String[imageMagickCommandList.size()]);

    try {
      System.out.println("ImageMagickProcessor.processImage: " + imageMagickCommandList);
      Result result = ERXRuntimeUtilities.execute(imageMagickCommands, null, null, 0);
      int exitValue = result.getExitValue();
      if (exitValue != 0) {
        log.warn("Warning: ImageMagick convert returned with a value of " + exitValue + ", error = " + result.getErrorAsString());
      }
    }
    catch (Exception e) {
      IOException ioex = new IOException("ImageMagick failed.");
      ioex.initCause(e);
      throw ioex;
    }

    if (watermarkFile != null) {
      List<String> watermarkCommandList = new LinkedList<>();
      watermarkCommandList.add(_imageMagickCompositeBinary.getAbsolutePath());

      watermarkCommandList.add("-watermark");
      watermarkCommandList.add("100%");

      if (tileWatermark) {
        watermarkCommandList.add("-tile");
      }

      watermarkCommandList.add(watermarkFile.getAbsolutePath());
      watermarkCommandList.add(outputFile.getAbsolutePath());
      watermarkCommandList.add(outputFile.getAbsolutePath());

      String[] watermarkCommands = watermarkCommandList.toArray(new String[watermarkCommandList.size()]);

      try {
        Result result = ERXRuntimeUtilities.execute(watermarkCommands, null, null, 0);
        int exitValue = result.getExitValue();
        if (exitValue != 0) {
          log.warn("Warning: ImageMagick composite returned with a value of " + exitValue + ", error = " + result.getErrorAsString());
        }
      }
      catch (Exception e) {
        IOException ioex = new IOException("ImageMagick was interrupted.");
        ioex.initCause(e);
        throw ioex;
      }

    }

    if (!outputFile.exists()) {
      throw new IOException("Failed to process image '" + inputFile + "' into '" + outputFile + "'.");
    }
  }

  /**
   * Returns an ImageMagick image processor.
   * 
   * @return an ImageMagick image processor
   * @throws IOException if an ImageMagick cannot be created
   */
  public static IERImageProcessor imageMagickImageProcessor() throws IOException {
    IERImageProcessor imageProcessor = null;
    String imageMagickBinFolder = ERXProperties.stringForKey("er.attachment.ImageProcessor.imageMagickBinFolder");
    if (imageMagickBinFolder != null) {
      File imageMagickConvertFile = new File(imageMagickBinFolder, "convert");
      File imageMagickCompositeFile = new File(imageMagickBinFolder, "composite");
      if (imageMagickConvertFile.exists() && imageMagickCompositeFile.exists()) {
        imageProcessor = new ImageMagickImageProcessor(imageMagickConvertFile, imageMagickCompositeFile);
      }
      else {
        throw new IllegalArgumentException("Cannot use ImageMagick because either " + imageMagickConvertFile + " or " + imageMagickCompositeFile + " does not exist.");
      }
    }
    else {
      throw new IllegalArgumentException("Cannot use ImageMagick because you have not set 'er.attachment.ImageProcessor.imageMagickBinFolder'.");
    }
    return imageProcessor;
  }
}
