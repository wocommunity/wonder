package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import er.attachment.utils.ERMimeType;

/**
 * Provides an interface for processing an image, performing various operations on it (most commonly, thumbnailing).
 *  
 * @author mschrag
 */
public interface IERImageProcessor {
  /**
   * Processes the given image with all of the given configuration settings.
   * 
   * Order: resize/dpi, colorspace, crop, sharpen, gamma, watermark
   * 
   * @param resizeWidth the maximum width to resize to (or -1 to not specify)
   * @param resizeHeight the maximum height to resize to (or -1 to not specify)
   * @param resizeQuality influences the resize algorithm to use (or null to not specify) 
   * @param dpi the dpi to resize to (or -1 to skip)
   * @param sharpenRadius the radius of the unsharp mask (or <= 0.0 to skip)
   * @param sharpenIntensity the intensity of the unsharp mask (or <= 0.0 to skip)
   * @param gamma the gamma to apply (or 0.0 to skip)
   * @param cropX the x position of the crop (or <= 0 to skip)
   * @param cropY the y position of the crop (or <= 0 to skip)
   * @param cropWidth the width of the crop (or <= 0 to skip) 
   * @param cropHeight the height of the crop (or <= 0 to skip)
   * @param watermarkFile the watermark File to apply to this image (or null to skip)
   * @param tileWatermark whether or not to tile the watermark
   * @param compressionQuality the compression quality of the resize to perform (0.0 to ignore) -- range is 0.0 to 1.0 (1.0 = best)
   * @param colorProfileFile the ICC profile to use (or null to skip)
   * @param inputFile the File to perform the given operations on
   * @param outputFile the File to write the resulting output image to
   * @param outputMimeType the desired mime type of the output file
   * @throws IOException if the image processing fails
   */
  public void processImage(int resizeWidth, int resizeHeight, Quality resizeQuality, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, File watermarkFile, boolean tileWatermark, float compressionQuality, File colorProfileFile, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException;

  /**
   * Provides a shortcut to thumbnailing an image using some default values that produce
   * decent quality thumbnail outputs.
   * 
   * @param resizeWidth the maximum resize width
   * @param resizeHeight the maximum resize height
   * @param inputFile the input file to thumbnail
   * @param outputFile the output file to write the thumbnail into
   * @throws IOException if the thumbnailing fails
   */
  public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile) throws IOException;

  /**
   * Quality is an enumerated type used to specify the resize quality.
   * 
   * @author mschrag
   */
  public enum Quality {
    Low, Medium, High
  }

}